package icm.greencities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

import icm.entities.GPSTracker;

public class StartActivity extends AppCompatActivity {

    // GPSTracker class
    GPSTracker gps;

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final int CONFIDENCE = 70;

    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    private TextView txtActivity, txtLocation, txtDistance;
    private ImageView imgActivity;
    private Button btnStartTracking, btnStopTracking;

    private double startLat, startLong, endLat, endLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        txtActivity = findViewById(R.id.txt_activity);
        imgActivity = findViewById(R.id.img_activity);
        txtLocation = findViewById(R.id.txt_location);
        txtDistance = findViewById(R.id.txt_distance);
        btnStartTracking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);

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
                    /*double[] coords = getLocation();
                    startLat = coords[0];
                    startLong = coords[1];
                    */
                    handleUserActivity(type, confidence);
                }
            }
        };


        //startTracking();
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
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                icon = R.drawable.ic_still;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
        }


        /*double[] coords = getLocation();
        endLat = coords[0];
        endLong = coords[1];
        float[] results = new float[0];
        Location.distanceBetween(startLat, endLat, startLong, endLong, results);
        */

        txtLocation.setText("Lat:" + getLocation()[0] +  "\nLong: " + getLocation()[1]);
        //txtDistance.setText(""+results[0]+"");

        if (confidence > CONFIDENCE) {
            txtActivity.setText(label);
            imgActivity.setImageResource(icon);
        }
    }

    private double[] getLocation() {
        // Check if GPS enabled
        double latitude;
        double longitude;
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // Show Settings when GPS or network is not enable
            gps.showSettingsAlert();
            latitude=0;
            longitude=0;
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
        Intent intent = new Intent(StartActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
        // Get Start Position to calculate distance
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    private void stopTracking() {
        Intent intent = new Intent(StartActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

}
