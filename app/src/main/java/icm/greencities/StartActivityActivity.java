package icm.greencities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import icm.entities.Activity;
import icm.others.GPSTracker;
import icm.others.StepDetector;
import icm.others.StepListener;
import icm.entities.User;
import icm.others.BackgroundDetectedActivitiesService;

public class StartActivityActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    GPSTracker gps;

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final int CONFIDENCE = 70;

    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;
    Timer T;
    TextView myTextView;
    TextView myTextView2;
    TextView myTextView3;
    TextView myTextView4;
    TextView myDistanceView;
    TextView myDistanceView2;
    TextView myDistanceView3;
    TextView myDistanceView4;
    TextView myDistanceView5;
    int count = 0;
    int countAux = 0;

    private TextView txtActivity;
    private ImageView imgActivity, imgKm;
    private Button btnStartTracking, btnStopTracking;

    private double startLat, startLong, lastLat, lastLong;
    private double distance = 0;
    private List<GeoPoint> coordenadas;
    private String doing;
    private int numSteps;
    private double newDistance;
    private boolean detected;
    private boolean started;
    private long time;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private TextView TvSteps;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        txtActivity = findViewById(R.id.txt_activity);
        imgActivity = findViewById(R.id.img_activity);
        imgKm = findViewById(R.id.imageKM);
        btnStartTracking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        coordenadas = new ArrayList<>();
        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartActivityActivity.this);
                    alertDialogBuilder
                            .setMessage("Your GPS seems to be disabled, do you want to enable it?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    startTracking();
                }
            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorManager.unregisterListener(StartActivityActivity.this);
                stopTracking();

            }
        });

        gps = new GPSTracker(StartActivityActivity.this);

        btnStopTracking.setVisibility(View.GONE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        doing = "NotDetected";
        detected = false;
        started = false;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };
    }


    private void handleUserActivity(int type, int confidence) {
        String label = "Detecting activity...";
        int icon = R.drawable.ic_still;
        switch (type) {
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                doing = "Cycling";
                detected = true;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                doing = "Running";
                detected = true;
                break;
            }
            case DetectedActivity.STILL: {
                label = "Detecting activity...";
                icon = R.drawable.ic_still;
                doing = "Nothing";
                detected = true;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                doing = "Walking";
                break;
            }
        }

        if (confidence > CONFIDENCE) {
            txtActivity.setText(label);
            imgActivity.setImageResource(icon);
            imgKm.setImageResource(icon);

            if (!label.equals("Detecting activity...") && started && count == 0) {
                countTime();
            }
        }
    }


    private double[] getLocation() {
        double latitude = 0;
        double longitude = 0;

        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            gps.showSettingsAlert();
        }

        return new double[]{latitude, longitude};
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BROADCAST_DETECTED_ACTIVITY));
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }


    private void startTracking() {
        started = true;
        btnStopTracking.setVisibility(View.VISIBLE);
        btnStartTracking.setVisibility(View.GONE);
        txtActivity.setText("Detecting activity...");

        Intent intent = new Intent(StartActivityActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);

        myTextView = findViewById(R.id.countTime);
        myTextView2 = findViewById(R.id.countTime2);
        myTextView3 = findViewById(R.id.countTime3);
        myTextView4 = findViewById(R.id.countTime4);
        myDistanceView = findViewById(R.id.count);
        myDistanceView2 = findViewById(R.id.count2);
        myDistanceView3 = findViewById(R.id.count3);
        myDistanceView4 = findViewById(R.id.count4);
        myDistanceView5 = findViewById(R.id.count5);

        Location location = gps.getLocation();
        startLat = location.getLatitude();
        startLong = location.getLongitude();
        coordenadas.add(new GeoPoint(startLat, startLong));
        lastLat = startLat;
        lastLong = startLong;

    }


    private void countTime() {
        numSteps = 0;
        sensorManager.registerListener(StartActivityActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        time = (new Timestamp(new Date())).getSeconds();
        T=new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (countAux < 2) {
                        countAux++;
                    }
                    count++;
                    int min = count/60;
                    int seg = 0;
                    if (count%60 != 0) {
                        seg = count - (60 * min);
                    }
                    //myTextView.setText(Integer.toString(seg));

                    String minutos = Integer.toString(min);
                    if (minutos.length() == 1) {
                        myTextView.setText("0");
                        myTextView2.setText(minutos);
                    } else {
                        myTextView.setText(String.valueOf(minutos.charAt(0)));
                        myTextView2.setText(String.valueOf(minutos.charAt(1)));
                    }
                    String segundos = Integer.toString(seg);
                    if (segundos.length() == 1) {
                        myTextView3.setText("0");
                        myTextView4.setText(segundos);
                    } else {
                        myTextView3.setText(String.valueOf(segundos.charAt(0)));
                        myTextView4.setText(String.valueOf(segundos.charAt(1)));
                    }
                    if (countAux == 2) {
                        countAux = 0;
                        Location newLocation = gps.getLocation();
                        double newLat = newLocation.getLatitude();
                        double newLong = newLocation.getLongitude();
                        if (newLat != lastLat || newLong != lastLong) {
                            //txtLocation.setText("Lat:" + newLat + "\nLong: " + newLong);
                            //distance += Math.sqrt((newLong - lastLong) * (newLong - lastLong) + (newLat - lastLat) * (newLat - lastLat));
                            GeoPoint ponto = new GeoPoint(newLat, newLong);
                            coordenadas.add(ponto);
                            lastLat = newLat;
                            lastLong = newLong;
                        }
                        //txtLocation.setText("Lat:" + newLocation.getLatitude() +  "\nLong: " + newLocation.getLongitude());
                        //Log.d("Tag8", "-> " + distance + "m");
                    }
                    newDistance = 0.76 * numSteps;
                    String aux = Double.toString(newDistance);
                    if (aux.length() > 0 && aux.contains(".")) {
                        //Log.d("Tag10", Arrays.toString(aux.split("\\.")));
                        aux = (aux.split("\\."))[0];
                    }
                    if (aux.length() == 1) {
                        myDistanceView5.setText(String.valueOf(aux.charAt(0)));
                    } else if (aux.length() == 2) {
                        myDistanceView5.setText(String.valueOf(aux.charAt(1)));
                        myDistanceView4.setText(String.valueOf(aux.charAt(0)));
                    } else if (aux.length() == 3) {
                        myDistanceView5.setText(String.valueOf(aux.charAt(2)));
                        myDistanceView4.setText(String.valueOf(aux.charAt(1)));
                        myDistanceView3.setText(String.valueOf(aux.charAt(0)));
                    } else if (aux.length() == 4) {
                        myDistanceView5.setText(String.valueOf(aux.charAt(3)));
                        myDistanceView4.setText(String.valueOf(aux.charAt(2)));
                        myDistanceView3.setText(String.valueOf(aux.charAt(1)));
                        myDistanceView2.setText(String.valueOf(aux.charAt(0)));
                    } else if (aux.length() == 5) {
                        myDistanceView5.setText(String.valueOf(aux.charAt(4)));
                        myDistanceView4.setText(String.valueOf(aux.charAt(3)));
                        myDistanceView3.setText(String.valueOf(aux.charAt(2)));
                        myDistanceView2.setText(String.valueOf(aux.charAt(1)));
                        myDistanceView.setText(String.valueOf(aux.charAt(0)));
                    }
                }
            });
            }
        }, 1000, 1000);
    }


    private void stopTracking() {
        Intent intent = new Intent(StartActivityActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
        if (count != 0) {
            T.cancel();
            double newLat = getLocation()[0];
            double newLong = getLocation()[1];
            if (newLat != lastLat || newLong != lastLong) {
                distance += Math.sqrt((newLong - lastLong) * (newLong - lastLong) + (newLat - lastLat) * (newLat - lastLat));
                GeoPoint ponto = new GeoPoint(newLat, newLong);
                coordenadas.add(ponto);
            }
            submit();
            Toast.makeText(
                    StartActivityActivity.this,
                    "Activity finished successfully! ",
                    Toast.LENGTH_LONG).show();
        } else if ( doing == null || doing.equals("nothing") || doing.equals("") ) {
            if (count != 0) {
                T.cancel();
            }
            Toast.makeText(
                    StartActivityActivity.this,
                    "This activity was not registered. Error detecting activity!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(
                    StartActivityActivity.this,
                    "This activity was not registered. No activity found!",
                    Toast.LENGTH_LONG).show();
        }
        btnStartTracking.setVisibility(View.VISIBLE);
        btnStopTracking.setVisibility(View.GONE);

        //Log.d("Tag8", "Time " + count);
    }


    private void submit () {
        //Log.d("Tag8", "HERE!");
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user2 = documentSnapshot.toObject(User.class);
                Activity aux = new Activity();
                aux.setActivity(doing);
                aux.setCoordinates(coordenadas);
                aux.setDistance(newDistance);
                aux.setTime(count);
                aux.setPoints(count/10);
                aux.setDate(time);
                db.collection("users").document(user.getEmail()).collection("activities").document(Integer.toString(user2.getnActivities() + 1)).set(aux);
                DocumentReference docRef = db.collection("users").document(user.getEmail());
                docRef
                        .update("nActivities", user2.getnActivities() + 1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
                docRef
                        .update("points", user2.getPoints() + (count/10))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }


    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(numSteps+"");
    }

}
