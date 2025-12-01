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
    private final Random myRandom = new Random();

    /** Chance (0–100%) of generating a random anomaly instead of a normal move. */
    private final float RANDOM_PERCENT; //Should be set from 0-100

    private final float SPOOFING_CHANGE = 50;


    private TelemetryGenerator(float theRandomPercent) {
        myDrones = new ArrayList<>();
        RANDOM_PERCENT = theRandomPercent;
    }

    public static synchronized TelemetryGenerator getInstance(float theRandomPercent) {
        if (instance == null) {
            instance = new TelemetryGenerator(theRandomPercent);
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
    public void addDrone(final DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    public void removeDrone(final DroneInterface theDrone) {
        myDrones.remove((theDrone));
    }

    /**
     * Iterates through all drones in the simulation. For each drone:
     * - Skips processing if the drone is not alive.
     * - Otherwise decides (based on RANDOM_PERCENT) whether to generate
     *   a random anomaly move or a normal route-following move.
     */
    public Map<DroneInterface, TelemetryRecord[]> processAllDrones(final float deltaTime) {

        Map<DroneInterface, TelemetryRecord[]> map = new HashMap<>();
        for (DroneInterface drone : myDrones) {

            if (!drone.isAlive()) {
                continue;
            }

            TelemetryRecord prev = drone.getPreviousTelemetryRecord();
            boolean spoofed = false;

            if (myRandom.nextInt(100) < RANDOM_PERCENT) {
                getRandomMove(drone, deltaTime);
                if (drone.getMyLastAnomaly() == AnomalyEnum.SPOOFING) {
                    spoofed = true;
                }
            } else {
                getMove(drone, deltaTime);
            }

            TelemetryRecord curr;
            if (spoofed) {
                float offsetLon = (myRandom.nextFloat() * 2 - 1) * SPOOFING_CHANGE;
                float offsetLat = (myRandom.nextFloat() * 2 - 1) * SPOOFING_CHANGE;
                float offsetAlt = (myRandom.nextFloat() * 2 - 1) * SPOOFING_CHANGE;

                curr = new TelemetryRecord(
                        drone.getId(),
                        drone.getLongitude() + offsetLon,
                        drone.getLatitude() + offsetLat,
                        drone.getAltitude() + offsetAlt,
                        drone.getVelocity(),
                        drone.getBatteryLevel(),
                        drone.getOrientation().getDegree(),
                        System.currentTimeMillis()
                );
            } else {
                curr = drone.generateTelemetryRecord();
            }

            map.put(drone, new TelemetryRecord[]{prev, curr});
            drone.setPrevTelemetryRecord(curr);
        }

        checkCollisions();
        return map;
    }

    /**
     * Generates an anomalous move for the given drone. Types of anomalies include:
     * - Sudden altitude drop or climb
     * - Sudden velocity increase or decrease
     * - Sudden latitude/longitude drift
     *
     * @param theDrone the drone to update with an anomaly
     */
    public void getRandomMove(DroneInterface theDrone, final float deltaTime) {
        theDrone.getNextRandomMove(deltaTime);
    }

    /**
     * Generates a normal move for the given drone.
     * The drone moves toward its next waypoint, adjusting position and altitude
     * proportionally to velocity. Velocity is increased or decreased depending
     * on distance to the waypoint.
     *
     * @param theDrone the drone to update with a normal move
     */
    public void getMove(final DroneInterface theDrone, final float deltaTime) {
        theDrone.getNextMove(deltaTime);
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
        for (int i = 0; i < myDrones.size(); i++) {
            DroneInterface a = myDrones.get(i);
            if (!a.isAlive()) continue;

            for (int j = i + 1; j < myDrones.size(); j++) {
                DroneInterface b = myDrones.get(j);
                if (!b.isAlive()) continue;

                float dx = a.getLongitude() - b.getLongitude();
                float dy = a.getLatitude() - b.getLatitude();
                float dz = a.getAltitude() - b.getAltitude();

                float distanceSq = dx*dx + dy*dy + dz*dz;

                // collision threshold (adjust if needed)
                if (distanceSq < 4.0f) {  // radius of 2 units
                    a.collided();
                    b.collided();
                }
            }
        }
    }


}
