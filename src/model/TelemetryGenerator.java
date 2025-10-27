package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TelemetryGenerator {

    ArrayList<DroneInterface> myDrones;

    HashMap<String, Object> myBeforeTelemetryMap;

    private static final Random myRandom = new Random();

    private final AnomalyDetector myAnomalyDetector = new AnomalyDetector();

    private static final float MAX_VELOCITY = 10;

    private static final float ACCELERATION_STEP = 2;

    private static final int RANDOM_PERCENT = 15; //Should be set from 0-100

    public TelemetryGenerator(ArrayList<DroneInterface> theDrones) {
        myDrones = theDrones;
    }

    public void addDrone(DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    /**
     * This will process all drones and decide if the specific
     * drone will get a random move or a normal move.
     */
    public void processAllDrones() {
        for (DroneInterface drone : myDrones) {
            myBeforeTelemetryMap = createTelemetryMap(drone);

            if (!drone.isAlive()) {
                continue;
            }

            if (myRandom.nextInt(100) < RANDOM_PERCENT) {
                getRandomMove(drone);
            } else {
                getMove(drone);
            }

        }
    }

    //cause an anomaly.
    public void getRandomMove(DroneInterface theDrone) {

    }

    public void getMove(DroneInterface theDrone) {
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity;

        RoutePoint nextPoint = theDrone.getNextPoint();

        float dx = nextPoint.getLatitude() - latitude;
        float dy = nextPoint.getLongitude() - longitude;
        float dz = nextPoint.getAltitude() - altitude;

        // distance left until we get to the next point
        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);


        if (distance <= theDrone.getVelocity()) {
            // reached waypoint
            latitude = nextPoint.getLatitude();
            longitude = nextPoint.getLongitude();
            altitude = nextPoint.getAltitude();
            theDrone.setNextRoute(); // advance to next waypoint
        } else {
            float ratio = theDrone.getVelocity() / distance;
            latitude += dx * ratio;
            longitude += dy * ratio;
            altitude += dz * ratio;
        }

        // After movement
        if (distance < 10.0f) {
            velocity = Math.max(theDrone.getVelocity() - 2, 1);
        } else {
            velocity = Math.min(theDrone.getVelocity() + 2, 10);
        }
        //now we need to update the drone state
        theDrone.updateDrone(longitude, latitude, altitude, 1, velocity);

        HashMap<String, Object> afterTelemetryMap = createTelemetryMap(theDrone);

        //after updating we can check if there is an anomaly
    }

    private HashMap<String, Object> createTelemetryMap(DroneInterface theDrone) {
        HashMap<String, Object> telemetryMap = new HashMap<>();
        telemetryMap.put("id", theDrone.getId());
        telemetryMap.put("altitude", theDrone.getAltitude());
        telemetryMap.put("longitude", theDrone.getLongitude());
        telemetryMap.put("latitude", theDrone.getLatitude());
        telemetryMap.put("velocity", theDrone.getVelocity());
        telemetryMap.put("batteryLevel", theDrone.getBatteryLevel());
        telemetryMap.put("orientation", theDrone.getOrientation());
        return telemetryMap;
    }

}
