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

    public static TelemetryGenerator instance;

    /** List of drones being simulated. */
    ArrayList<DroneInterface> myDrones;

    /** Random generator used for movement and anomaly decisions. */
    private static final Random myRandom = new Random();

//    /** Timestamp reference used for telemetry data. */
//    private static final Date myDate = new Date();

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
    private static final int RANDOM_PERCENT = 1; //Should be set from 0-100


    private TelemetryGenerator() {
        myDrones = new ArrayList<>();
    }

    public static synchronized TelemetryGenerator getInstance() {
        if (instance == null) {
            instance = new TelemetryGenerator();
        }
        return instance;
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
     *
     *   gen.processAllDrones(1.0 / FPS);
     */
    public ArrayList<HashMap<String, Object>[]> processAllDrones(double deltaTime) {
        ArrayList<HashMap<String, Object>[]> telemetryList = new ArrayList<>();

        for (DroneInterface drone : myDrones) {
            HashMap<String, Object> myBeforeTelemetryMap = createTelemetryMap(drone);

            if (!drone.isAlive()) {
                continue;
            }

            if (myRandom.nextInt(100) < RANDOM_PERCENT) {
                getRandomMove(drone, deltaTime);
            } else {
                getMove(drone, deltaTime);
            }

            HashMap<String, Object> myAfterTelemetryMap = createTelemetryMap(drone);

            @SuppressWarnings("unchecked")
            HashMap<String, Object>[] pair = (HashMap<String, Object>[]) new HashMap[2];
            pair[0] = myBeforeTelemetryMap;
            pair[1] = myAfterTelemetryMap;

            telemetryList.add(pair);
        }

        //After all drones move to the next step we can check for collisions.
        checkCollisions();
        return telemetryList;
    }

    /**
     * Generates an anomalous move for the given drone. Types of anomalies include:
     * - Sudden altitude drop or climb
     * - Sudden velocity increase or decrease
     * - Sudden latitude/longitude drift
     *
     * @param theDrone the drone to update with an anomaly
     */
    public void getRandomMove(DroneInterface theDrone, double deltaTime) {
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity = theDrone.getVelocity();

        int anomalyType = myRandom.nextInt(3); // 0=altitude,1=speed,2=drift

        switch (anomalyType) {
            case 0: // Sudden drop/climb
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1)
                        * (10 + myRandom.nextFloat() * 10) * (float) deltaTime;
                altitude = Math.max(MIN_ALTITUDE, altitude + changeAlt);
                break;

            case 1: // Speed anomaly
                int change = 7;
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + change, MAX_VELOCITY);
                } else {
                    velocity = Math.max(velocity - change, MIN_VELOCITY);
                }
                break;

            case 2: // Random drift
                float driftX = (myRandom.nextBoolean() ? 1 : -1)
                        * (15 + myRandom.nextFloat() * 10) * (float) deltaTime;
                float driftY = (myRandom.nextBoolean() ? 1 : -1)
                        * (15 + myRandom.nextFloat() * 10) * (float) deltaTime;
                longitude += driftX;
                latitude += driftY;
                break;
        }

        // Calculate distance change
        float anomalyDistance = (float) Math.sqrt(
                Math.pow(longitude - theDrone.getLongitude(), 2) +
                        Math.pow(latitude - theDrone.getLatitude(), 2) +
                        Math.pow(altitude - theDrone.getAltitude(), 2)
        );

        applyDroneUpdate(theDrone, longitude, latitude, altitude, velocity, anomalyDistance, deltaTime);
    }

    /**
     * Generates a normal move for the given drone.
     * The drone moves toward its next waypoint, adjusting position and altitude
     * proportionally to velocity. Velocity is increased or decreased depending
     * on distance to the waypoint.
     *
     * @param theDrone the drone to update with a normal move
     */
    public void getMove(DroneInterface theDrone, double deltaTime) {
        float latitude = theDrone.getLatitude();
        float longitude = theDrone.getLongitude();
        float altitude = theDrone.getAltitude();
        float velocity;

        RoutePoint next = theDrone.getNextPoint();
        float dx = next.getLongitude() - longitude;
        float dy = next.getLatitude() - latitude;
        float dz = next.getAltitude() - altitude;

        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        float moveDist = theDrone.getVelocity() * (float) deltaTime; // movement this frame

        if (distance <= moveDist) {
            longitude = next.getLongitude();
            latitude  = next.getLatitude();
            altitude  = next.getAltitude();
            theDrone.setNextRoute();
        } else {
            float ratio = moveDist / distance;
            longitude += dx * ratio;
            latitude  += dy * ratio;
            altitude  += dz * ratio;
        }

        // Adjust velocity slightly (acceleration/deceleration)
        if (distance < 10.0f) {
            velocity = Math.max(theDrone.getVelocity() - ACCELERATION_STEP, MIN_VELOCITY);
        } else {
            velocity = Math.min(theDrone.getVelocity() + ACCELERATION_STEP, MAX_VELOCITY);
        }

        applyDroneUpdate(theDrone, longitude, latitude, altitude, velocity, distance, deltaTime);
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
        telemetryMap.put("orientation", theDrone.getOrientation().getDegree());
        telemetryMap.put("timeStamp", System.currentTimeMillis());
        return telemetryMap;
    }

    private void applyDroneUpdate(DroneInterface d, float lon, float lat, float alt, float vel, float dist, double deltaTime) {
        float drained = batteryDrained(d, dist, deltaTime);
        float degree = d.getOrientation().findNextOrientation(d.getLongitude(), d.getLatitude(), lon, lat);

        d.updateDrone(lon, lat, alt, drained, vel, degree);
    }

    /**
     * Calculates how much battery is drained during the last move.
     * This can be adjusted to depend on velocity, altitude, or distance.
     *
     * @return the amount of battery drained (integer percent or units)
     */
    private float batteryDrained(DroneInterface d, float dist, double deltaTime) {
        float drain = 0.07f * (float) deltaTime;
        if (d.getVelocity() > 7) drain += 0.05f * (float) deltaTime;
        drain += dist * 0.001f * (float) deltaTime;
        return drain;
    }

}
