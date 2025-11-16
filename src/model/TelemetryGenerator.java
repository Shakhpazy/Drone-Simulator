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

    public ArrayList<DroneInterface> getMyDrones() {
        return myDrones;
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
            if (!drone.isAlive()) {
                continue;
            }
            HashMap<String, Object> myBeforeTelemetryMap = createTelemetryMap(drone);

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
        theDrone.getNextRandomMove((float) deltaTime);
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
        theDrone.getNextMove((float) deltaTime);
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
                drone.collided();
                seen.get(position).collided();
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

}
