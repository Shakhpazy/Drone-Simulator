package model;

import java.util.*;

/**
 * TelemetryGenerator is responsible for simulating drone telemetry data.
 * It processes a list of drones, generating either normal movements
 * (towards route waypoints) or anomaly movements (sudden altitude, velocity,
 * or positional deviations).
 *
 * Normal moves respect waypoint routes and physical constraints such as
 * min/max velocity and altitude. Random moves simulate anomalies which
 * can deviate from expected behavior.
 */
public class TelemetryGenerator {

    /** List of drones being simulated. */
    ArrayList<DroneInterface> myDrones;

    /** Telemetry snapshot of the drone state before movement. */
    HashMap<String, Object> myBeforeTelemetryMap;

    /** Random generator used for movement and anomaly decisions. */
    private static final Random myRandom = new Random();

    /** Timestamp reference used for telemetry data. */
    private static final Date myDate = new Date();

    //private final AnomalyDetector myAnomalyDetector = new AnomalyDetector();

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 1;

    /** Maximum allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 700;

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = 1;

    /** Chance (0–100%) of generating a random anomaly instead of a normal move. */
    private static final int RANDOM_PERCENT = 15; //Should be set from 0-100


    /**
     * Constructs a TelemetryGenerator with a list of drones to simulate.
     *
     * @param theDrones list of drones implementing DroneInterface
     */
    public TelemetryGenerator(ArrayList<DroneInterface> theDrones) {
        myDrones = theDrones;
    }

    /**
     * Adds a new drone to the simulation.
     *
     * @param theDrone drone to be added
     */
    public void addDrone(DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    /**
     * Iterates through all drones in the simulation. For each drone:
     * - Skips processing if the drone is not alive.
     * - Otherwise decides (based on RANDOM_PERCENT) whether to generate
     *   a random anomaly move or a normal route-following move.
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

        //After all drones move to the next step we can check for collisions.
        checkCollisions();

    }

    /**
     * Generates an anomalous move for the given drone. Types of anomalies include:
     * - Sudden altitude drop or climb
     * - Sudden velocity increase or decrease
     * - Sudden latitude/longitude drift
     *
     * @param theDrone the drone to update with an anomaly
     */
    public void getRandomMove(DroneInterface theDrone) {
        System.out.println("Random move.");
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity = theDrone.getVelocity();

        int anomalyType = myRandom.nextInt(3); // 0 = drop/climb, 1 = speed anomaly, 2 = drift

        switch (anomalyType) {
            case 0: // Sudden drop or climb of 10-20 units
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1) * (10 + myRandom.nextFloat() * 10);
                altitude = Math.max(MIN_ALTITUDE, altitude + changeAlt);
                break;

            case 1: // Sudden speed anomaly by 7 units
                int change = 7;
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + change, MAX_VELOCITY);
                } else {
                    velocity = Math.max(velocity - change, MIN_VELOCITY);
                }
                break;

            case 2: // Random drift 15-25 units in long and latitude
                float driftX = (myRandom.nextBoolean() ? 1 : -1) * (15 + myRandom.nextFloat() * 10);
                float driftY = (myRandom.nextBoolean() ? 1 : -1) * (15 + myRandom.nextFloat() * 10);
                longitude += driftX;
                latitude += driftY;
                break;
        }

        // get the distance change.
        float anomalyDistance = (float) Math.sqrt(
                Math.pow(longitude - theDrone.getLongitude(), 2) +
                        Math.pow(latitude - theDrone.getLatitude(), 2) +
                        Math.pow(altitude - theDrone.getAltitude(), 2)
        );

        applyDroneUpdate(theDrone, longitude, latitude, altitude, velocity, anomalyDistance);
    }

    /**
     * Generates a normal move for the given drone.
     * The drone moves toward its next waypoint, adjusting position and altitude
     * proportionally to velocity. Velocity is increased or decreased depending
     * on distance to the waypoint.
     *
     * Status code is set to 1 for normal updates.
     *
     * @param theDrone the drone to update with a normal move
     */
    public void getMove(DroneInterface theDrone) {
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity;

        RoutePoint nextPoint = theDrone.getNextPoint();

        float dx = nextPoint.getLongitude() - longitude;
        float dy = nextPoint.getLatitude() - latitude;
        float dz = nextPoint.getAltitude() - altitude;

        // distance left until we get to the next point
        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);


        if (distance <= theDrone.getVelocity()) {
            // reached waypoint
            longitude = nextPoint.getLongitude();
            latitude = nextPoint.getLatitude();
            altitude = nextPoint.getAltitude();
            theDrone.setNextRoute(); // advance to next waypoint
        } else {
            float ratio = theDrone.getVelocity() / distance;
            longitude += dx * ratio;
            latitude += dy * ratio;
            altitude += dz * ratio;
        }

        // After movement
        if (distance < 10.0f) {
            velocity = Math.max(theDrone.getVelocity() - ACCELERATION_STEP, MIN_VELOCITY);
        } else {
            velocity = Math.min(theDrone.getVelocity() + ACCELERATION_STEP, MAX_VELOCITY);
        }

        applyDroneUpdate(theDrone, longitude, latitude, altitude, velocity, distance);
    }

    /**
     * Checks for collisions between drones in O(n) time using a HashMap.
     * Each drone's size is accounted by casting its longitude, latitude,
     * and altitude to Int, effectively giving the drone a "size" of 1 unit
     * in each dimension. If two drones fall into the same integer cell, they are
     * considered to have collided.
     * On collision, both drones are marked as crashed by setting their altitude
     * to 0. Dead drones (already not alive) are skipped.
     */
    private void checkCollisions() {
        HashMap<String, DroneInterface> seen = new HashMap<>();

        for (DroneInterface drone: myDrones) {
            if (!drone.isAlive()) {
                continue;
            }
            //cast to an Int to account for the drone having some kind of size associated to it.
            String position = (int)drone.getLongitude() + "," + (int)drone.getLatitude() + "," + (int)drone.getAltitude();
            if (seen.containsKey(position)) {
                drone.setAltitude(0);
                seen.get(position).setAltitude(0);
            }
            else {
                seen.put(position, drone);
            }
        }
    }

    /**
     * Creates a snapshot of telemetry data from the given drone.
     *
     * @param theDrone the drone to read telemetry from
     * @return a map containing drone id, altitude, longitude, latitude,
     *         velocity, battery level, orientation, and timestamp
     */
    public HashMap<String, Object> createTelemetryMap(DroneInterface theDrone) {
        HashMap<String, Object> telemetryMap = new HashMap<>();
        telemetryMap.put("id", theDrone.getId());
        telemetryMap.put("altitude", theDrone.getAltitude());
        telemetryMap.put("longitude", theDrone.getLongitude());
        telemetryMap.put("latitude", theDrone.getLatitude());
        telemetryMap.put("velocity", theDrone.getVelocity());
        telemetryMap.put("batteryLevel", theDrone.getBatteryLevel());
        telemetryMap.put("orientation", theDrone.getOrientation());
        telemetryMap.put("timeStamp", myDate.getTime());
        return telemetryMap;
    }

    private void applyDroneUpdate(DroneInterface theDrone, float theLongitude, float theLatitude, float theAltitude, float theVelocity, float theDistance) {
        //now we need to update the drone state
        int batteryDrain = batteryDrained(theDrone, theDistance);
        theDrone.updateDrone(theLongitude, theLatitude, theAltitude, batteryDrain, theVelocity);

        HashMap<String, Object> afterTelemetryMap = createTelemetryMap(theDrone);

        // Pass snapshots to anomaly detector
        //myAnomalyDetector.Detect();
    }

    /**
     * Calculates how much battery is drained during the last move.
     * This can be adjusted to depend on velocity, altitude, or distance.
     *
     * @return the amount of battery drained (integer percent or units)
     */
    private int batteryDrained(DroneInterface theDrone, float distanceTraveled) {
        int drain = 1;
        if (theDrone.getVelocity() > 7) {
            drain += 1; // penalty for high speed
        }

        return drain;
    }

    public HashMap<String, Object> getMyBeforeTelemetryMap() {
        return myBeforeTelemetryMap;
    }
}
