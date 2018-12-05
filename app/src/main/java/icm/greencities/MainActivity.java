package icm.greencities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import icm.entities.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NotificationManager mNotificationManager;

    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    FirebaseAuth auth;
    FirebaseUser user;
    Button btnSignOut;
    ProgressDialog PD;
    GoogleMap googleMap;
    Location location;


    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnSignOut = (Button) findViewById(R.id.sign_out_button);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                };
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            }
        });

        LinearLayout btnProfile = (LinearLayout) findViewById(R.id.buttonProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileAtivity.class));
            }
        });


        LinearLayout btnResults = (LinearLayout) findViewById(R.id.buttonResults);
        btnResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ResultsMain.class));
            }
        });

        LinearLayout menu_Goals = (LinearLayout) findViewById(R.id.buttonGoals);
        menu_Goals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Goals.class));
            }
        });

        LinearLayout menu_startActivity = (LinearLayout) findViewById(R.id.buttonStart);
        menu_startActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StartActivity.class));
            }
        });

        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        // Set up the Notification Broadcast Intent.
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);


        final PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (this, NOTIFICATION_ID, notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getSystemService
                (ALARM_SERVICE);

        long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime()
                + repeatInterval;

        // If the Toggle is turned on, set the repeating alarm with
        // a 15 minute interval.
        if (alarmManager != null) {
            alarmManager.setInexactRepeating
                    (AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime, repeatInterval,
                            notifyPendingIntent);
        }

    }



    @Override
    protected void onResume() {
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        super.onResume();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        askPermissions();
    }


    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {
            represent(true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    represent(true);
                } else {
                    represent(false);
                }
                return;
            }
        }
    }


    private void represent (boolean permission) {
        double latitude = 0;
        double longitude = 0;
        if (permission) {
            GPSTracker gps = new GPSTracker(this);
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "It's necessary to enable location permissions";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            latitude = 38.7166700;
            longitude =  -9.1333300;
            //Log.d("Tag8", "Hello!");
        }
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

        /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document("caroliinaalbuquerque29@gmail.com").collection("activities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Activity atividade = document.toObject(Activity.class);
                                Log.d("Tag10","YELLLO");
                                if (document.getId().equals("18")) {
                                    Log.d("Tag10","YELLLO2");
                                    for (GeoPoint i: atividade.getCoordinates()) {
                                        double latitude = i.getLatitude();
                                        double longitude = i.getLongitude();
                                        int height = 25;
                                        int width = 25;
                                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ponto);
                                        Bitmap b=bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                        googleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longitude))
                                                .title("Marker"))
                                                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
                                    }
                                }

                            }
                        } else {
                            Log.d("error", "Error getting documents: ", task.getException());
                        }
                    }
                });
        */

        /*
        latitude = 40.63401558;
        longitude = -8.65952882;
        int height = 25;
        int width = 25;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ponto);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Marker"))
                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);




        latitude = 40.63435829;
        longitude = -8.65959428;
        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ponto);
        b=bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Marker"))
                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

        latitude = 40.63482194;
        longitude = -8.65908073;
        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ponto);
        b=bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Marker"))
                .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
        */

    }



}
