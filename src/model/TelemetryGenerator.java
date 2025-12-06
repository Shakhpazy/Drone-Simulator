package model;

import java.util.*;

/**
 * TelemetryGenerator manages the simulation of multiple drones by producing
 * normal and anomalous movement updates and converting those updates into
 * {@link TelemetryRecord} objects.
 * <p>
 * Each update cycle:
 * <ul>
 *     <li>Skips dead drones.</li>
 *     <li>For each alive drone, decides whether to execute a normal movement
 *         or an anomaly based on a configured probability.</li>
 *     <li>Generates spoofed telemetry if the anomaly type was SPOOFING.</li>
 *     <li>Produces a mapping of each drone to its previous and current
 *         telemetry records.</li>
 *     <li>Checks for collisions between drones.</li>
 * </ul>
 * <p>
 * This class follows the Singleton pattern—only one generator instance may exist.
 *
 * @author Yusuf Shakhpaz
 */
public class TelemetryGenerator {

    /** Singleton instance of the TelemetryGenerator. */
    public static TelemetryGenerator instance;

    /** List of all drones being simulated. */
    ArrayList<DroneInterface> myDrones;

    /** Random generator used for determining anomaly occurrence and spoof offsets. */
    private final Random myRandom = new Random();

    /** Maximum spoofing offset applied to lat/lon/alt when SPOOFING is triggered. */
    private final float SPOOFING_CHANGE = 50;

    /** Drone Collision Threshold **/
    private final float COLLISION_THRESHOLD = 4;

    /**
     * Percentage chance (0–100+) that a drone will generate an anomalous update
     * instead of a normal movement.
     * <p>
     * Example: If {@code RANDOM_PERCENT = 5}, then on each update there is a
     * ~0.5% chance (5/1000) of anomaly.
     */
    private final float RANDOM_PERCENT;


    /**
     * Private constructor for singleton access.
     *
     * @param theRandomPercent probability of generating an anomaly (0–100).
     */
    private TelemetryGenerator(float theRandomPercent) {
        myDrones = new ArrayList<>();
        RANDOM_PERCENT = theRandomPercent;
    }

    /**
     * Retrieves the singleton instance of the TelemetryGenerator.
     * Creates a new instance if one does not already exist.
     *
     * @param theRandomPercent anomaly probability for new instance creation.
     * @return the global TelemetryGenerator instance.
     */
    public static synchronized TelemetryGenerator getInstance(float theRandomPercent) {
        if (instance == null) {
            instance = new TelemetryGenerator(theRandomPercent);
        }
        return instance;
    }

    /**
     * Returns the list of drones currently registered to the generator.
     *
     * @return list of simulated drones.
     */
    public ArrayList<DroneInterface> getMyDrones() {
        return myDrones;
    }

    /**
     * Adds a drone to the simulation.
     *
     * @param theDrone drone to be added.
     */
    public void addDrone(final DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    /**
     * Removes a drone from the simulation.
     *
     * @param theDrone drone to remove.
     */
    public void removeDrone(final DroneInterface theDrone) {
        myDrones.remove((theDrone));
    }

    /**
     * Processes movement updates for all drones in the system.
     * <p>
     * For each drone:
     * <ul>
     *     <li>If the drone is dead, it is skipped.</li>
     *     <li>Randomly chooses normal or anomalous movement.</li>
     *     <li>Checks for collisions between drones after all movements.</li>
     *     <li>Generates spoofed telemetry if the anomaly was SPOOFING.</li>
     *     <li>Produces a mapping of the drone to its previous and current
     *         telemetry records.</li>
     * </ul>
     * Collision detection is performed after all drones move but before telemetry
     * generation, ensuring that collided drones send their dead state (0 altitude,
     * 0 battery) to the UI.
     *
     * @param deltaTime elapsed simulation time since the last update step.
     * @return a map associating each drone with its previous and current
     *         {@link TelemetryRecord}.
     */
    public Map<DroneInterface, TelemetryRecord[]> processAllDrones(final float deltaTime) {

        // Track which drones were spoofed during movement
        Map<DroneInterface, Boolean> spoofedMap = new HashMap<>();
        
        // First pass: Move all drones
        for (DroneInterface drone : myDrones) {
            if (!drone.isAlive()) {
                continue;
            }

            boolean spoofed = false;
            if (myRandom.nextFloat() < (RANDOM_PERCENT / 100.0f)) {
                getRandomMove(drone, deltaTime);
                if (drone.getMyLastAnomaly() == AnomalyEnum.SPOOFING) {
                    spoofed = true;
                }
            } else {
                getMove(drone, deltaTime);
            }
            
            spoofedMap.put(drone, spoofed);
        }

        // Check collisions after all drones have moved, but before generating telemetry
        // This ensures collided drones send their dead state (0 altitude, 0 battery) to the UI
        checkCollisions();

        // Second pass: Generate telemetry for all drones (including ones that just collided)
        Map<DroneInterface, TelemetryRecord[]> map = new HashMap<>();
        for (DroneInterface drone : myDrones) {
            if (!drone.isAlive()) {
                continue;
            }

            TelemetryRecord prev = drone.getPreviousTelemetryRecord();
            boolean spoofed = spoofedMap.getOrDefault(drone, false);

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

        return map;
    }


    /**
     * Applies a random anomaly movement to the specified drone.
     * <p>
     * Types of anomalies include:
     * <ul>
     *     <li>Battery drain increase</li>
     *     <li>Battery failure</li>
     *     <li>Sudden altitude changes</li>
     *     <li>Velocity spikes/drops</li>
     *     <li>Spoofing (fake telemetry coordinates)</li>
     * </ul>
     *
     * @param theDrone  drone to update.
     * @param deltaTime elapsed simulation time.
     */
    public void getRandomMove(DroneInterface theDrone, final float deltaTime) {
        theDrone.getNextRandomMove(deltaTime);
    }

    /**
     * Applies a normal route-following movement update to the specified drone.
     * The drone adjusts velocity based on proximity to its next waypoint and
     * then moves proportionally along the route.
     *
     * @param theDrone  drone to update.
     * @param deltaTime elapsed simulation time.
     */
    public void getMove(final DroneInterface theDrone, final float deltaTime) {
        theDrone.getNextMove(deltaTime);
    }

    /**
     * Detects collisions between drones by testing proximity in 3D space.
     * <p>
     * Two drones are considered collided if the squared distance between them
     * is less than a fixed threshold. A collision marks both drones as crashed.
     * <p>
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
                if (distanceSq < COLLISION_THRESHOLD) {
                    a.collided();
                    b.collided();
                }
            }
        }
    }

}
