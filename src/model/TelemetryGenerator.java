package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TelemetryGenerator {

    ArrayList<DroneInterface> myDrones;

    HashMap<String, Object> myBeforeTelemetryMap;

    private static final Random myRandom = new Random();

    private final AnomalyDetector myAnomalyDetector = new AnomalyDetector();

    private static final float MAX_VELOCITY = 50;

    private static final float MIN_VELOCITY = 1;

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
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity = theDrone.getVelocity();

        int anomalyType = myRandom.nextInt(3); // 0 = drop/climb, 1 = speed anomaly, 2 = drift

        switch (anomalyType) {
            case 0: // Sudden drop or climb of 30-40 units
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1) * (30 + myRandom.nextFloat() * 10);
                altitude = Math.max(0, altitude + changeAlt);
                break;

            case 1: // Sudden speed anomaly by 30-40 units
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + (30 + myRandom.nextFloat() * 10), MAX_VELOCITY);
                } else {
                    velocity = Math.max(velocity - (30 + myRandom.nextFloat() * 10), MIN_VELOCITY);
                }
                break;

            case 2: // Random drift 30-40 units in long and latitude
                float driftX = (myRandom.nextBoolean() ? 1 : -1) * (30 + myRandom.nextFloat() * 10);
                float driftY = (myRandom.nextBoolean() ? 1 : -1) * (30 + myRandom.nextFloat() * 10);
                longitude += driftX;
                latitude += driftY;
                break;
        }

        // Update drone state with anomaly (status=2 means anomaly)
        theDrone.updateDrone(longitude, latitude, altitude, 2, velocity);

        HashMap<String, Object> afterTelemetryMap = createTelemetryMap(theDrone);

        // Compare against before state
        // myAnomalyDetector.Detect(afterTelemetryMap, myBeforeTelemetryMap);
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
            velocity = Math.max(theDrone.getVelocity() - ACCELERATION_STEP, MIN_VELOCITY);
        } else {
            velocity = Math.min(theDrone.getVelocity() + ACCELERATION_STEP, MAX_VELOCITY);
        }
        //now we need to update the drone state
        theDrone.updateDrone(longitude, latitude, altitude, 1, velocity);

        HashMap<String, Object> afterTelemetryMap = createTelemetryMap(theDrone);
        // Pass snapshots to anomaly detector
        //myAnomalyDetector.Detect();
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
