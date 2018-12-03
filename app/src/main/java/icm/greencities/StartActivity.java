package icm.greencities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import icm.entities.Activity;
import icm.entities.GPSTracker;
import icm.entities.User;

public class StartActivity extends AppCompatActivity {

    // GPSTracker class
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
    int count = 0;

    private TextView txtActivity, txtLocation, txtDistance;
    private ImageView imgActivity, imgKm;
    private Button btnStartTracking, btnStopTracking;

    private double startLat, startLong, endLat, endLong, lastLat, lastLong;


    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private double distance = 0;
    private List<GeoPoint> coordenadas;

    private String doing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        txtActivity = findViewById(R.id.txt_activity);
        imgActivity = findViewById(R.id.img_activity);
        txtLocation = findViewById(R.id.txt_location);
        txtDistance = findViewById(R.id.txt_distance);
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
                startTracking();
            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTracking();
            }
        });
        gps = new GPSTracker(StartActivity.this);

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
        btnStopTracking.setVisibility(View.GONE);

    }

    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_still);
        int icon = R.drawable.ic_still;
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_bus;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                doing = "bicycling";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                doing = "running";
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                icon = R.drawable.ic_still;
                doing = "nothing";
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                doing = "walking";
                break;
            }
        }



        if (confidence > CONFIDENCE) {
            txtActivity.setText(label);
            imgActivity.setImageResource(icon);
            imgKm.setImageResource(icon);
        }
    }

    private double[] getLocation() {
        // Check if GPS enabled
        double latitude = 0;
        double longitude = 0;
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // Show Settings when GPS or network is not enable
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
        btnStopTracking.setVisibility(View.VISIBLE);
        btnStartTracking.setVisibility(View.GONE);

        Intent intent = new Intent(StartActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);

        myTextView = findViewById(R.id.countTime);
        myTextView2 = findViewById(R.id.countTime2);
        myTextView3 = findViewById(R.id.countTime3);
        myTextView4 = findViewById(R.id.countTime4);

        Location location = gps.getLocation();
        startLat = location.getLatitude();
        startLong = location.getLongitude();
        coordenadas.add(new GeoPoint(startLat, startLong));
        lastLat = startLat;
        lastLong = startLong;

        T=new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
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
                        Location newLocation = gps.getLocation();
                        double newLat = newLocation.getLatitude();
                        double newLong = newLocation.getLongitude();
                        if (newLat != lastLat || newLong != lastLong) {
                            txtLocation.setText("Lat:" + newLat + "\nLong: " + newLong);
                            distance += Math.sqrt((newLong - lastLong) * (newLong - lastLong) + (newLat - lastLat) * (newLat - lastLat));
                            GeoPoint ponto = new GeoPoint(newLat, newLong);
                            coordenadas.add(ponto);
                            lastLat = newLat;
                            lastLong = newLong;
                        };
                        //txtLocation.setText("Lat:" + newLocation.getLatitude() +  "\nLong: " + newLocation.getLongitude());
                        //Log.d("Tag8", "-> " + distance + "m");
                        txtDistance.setText(String.format("%.2f m",distance));


                    }
                });
            }
        }, 1000, 1000);

    }
    private void stopTracking() {
        Intent intent = new Intent(StartActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
        T.cancel();
        double newLat = getLocation()[0];
        double newLong = getLocation()[1];
        if (newLat != lastLat || newLong != lastLong) {
            distance += Math.sqrt((newLong - lastLong) * (newLong - lastLong) + (newLat - lastLat) * (newLat - lastLat));
            GeoPoint ponto = new GeoPoint(newLat, newLong);
            coordenadas.add(ponto);
        }
        submit();
        btnStartTracking.setVisibility(View.VISIBLE);
        btnStopTracking.setVisibility(View.GONE);

        //Log.d("Tag8", "Time " + count);
    }

    // Save data in DB
    private void submit () {
        Log.d("Tag8", "HERE!");
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user2 = documentSnapshot.toObject(User.class);
                Activity aux = new Activity();
                aux.setActivity(doing);
                aux.setCoordinates(coordenadas);
                aux.setDistance(distance);
                aux.setTime(count);
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
            }
        });
    }

}
