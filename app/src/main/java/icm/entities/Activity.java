package icm.entities;

import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class Activity {

    private String activity;
    private double distance;
    private int time;
    private List<GeoPoint> coordinates = new ArrayList<>();

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<GeoPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<GeoPoint> coordinates) {
        this.coordinates = coordinates;
    }
}
