package model;

/**
 * Defines the contract for a Drone within the simulation.
 * <p>
 * A Drone exposes access to its physical state (position, altitude,
 * velocity, orientation, battery level) and core movement behaviors
 * (normal movement and anomalous movement). It also supports collision
 * handling, telemetry generation, and basic physics parameters such
 * as max/min velocity and altitude.
 * <p>
 * This interface allows interchangeable implementations (e.g.,
 * simulation drones, hardware-controlled drones) while maintaining
 * a consistent API.
 *
 * @author Yusuf Shakhpaz
 */
public interface DroneInterface {

    /** @return the unique ID of the drone */
    int getId();

    /** @return the drone's current longitude */
    float getLongitude();

    /** @return the drone's current latitude */
    float getLatitude();

    /** @return the drone's current altitude */
    float getAltitude();

    /** @return the drone's current velocity */
    float getVelocity();

    /** @return the drone's current battery level (0–100) */
    float getBatteryLevel();

    /** @return the drone's current orientation object */
    Orientation getOrientation();

    //Core Behaviors

    /**
     * Performs a normal movement update based on the drone's next waypoint.
     * <p>
     * Movement is scaled by {@code theDeltaTime}.
     * A larger delta time results in larger positional changes.
     *
     * @param theDeltaTime simulation time step used to scale movement
     */
    void getNextMove(float theDeltaTime);

    /**
     * Performs an anomalous movement update, modifying state according
     * to a randomly selected anomaly type (e.g., sudden altitude change,
     * spoofing, battery failure).
     * <p>
     * Movement scales with {@code theDeltaTime}.
     *
     * @param theDeltaTime simulation time step used to scale movement
     */
    void getNextRandomMove(float theDeltaTime);

    /**
     * Called when the drone collides with another drone.
     * <p>
     * Default behavior: set altitude to zero and mark the drone as dead.
     */
    void collided();


    /** @return the maximum allowed velocity for this drone */
    float getMaxVelocity();

    /** @return the minimum allowed velocity for this drone */
    float getMinVelocity();

    /** @return the acceleration step used when adjusting velocity */
    float getAccelerationStep();

    /** @return the minimum altitude this drone can maintain (> 0) */
    float getMinAltitude();

    /**
     * Returns whether the drone is alive.
     * <p>
     * A drone is considered alive if:
     * <ul>
     *   <li>its altitude is above 0</li>
     *   <li>its battery level is above 0</li>
     * </ul>
     *
     * @return {@code true} if alive; {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Generates a new telemetry snapshot representing the drone's current state.
     *
     * @return a populated {@link TelemetryRecord}
     */
    TelemetryRecord generateTelemetryRecord();

    /**
     * Returns the previously recorded telemetry snapshot.
     *
     * @return the previous telemetry record
     */
    TelemetryRecord getPreviousTelemetryRecord();

    /**
     * Returns the last anomaly triggered during the drone's movement.
     *
     * @return an {@link AnomalyEnum} describing the last anomaly
     */
    AnomalyEnum getMyLastAnomaly();

    /**
     * Updates the drone's stored previous telemetry record.
     *
     * @param theTelemetryRecord the new previous telemetry record
     */
    void setPrevTelemetryRecord(TelemetryRecord theTelemetryRecord);

    /**
     * Returns the next waypoint the drone is moving toward.
     * <p>
     * NOTE: This is primarily for debugging and simulation testing.
     *       It should be removed or restricted in production usage.
     *
     * @return the next {@link RoutePoint} the drone is headed toward
     */
    RoutePoint getNextPoint();

    /**
     * @return string representation of the drone’s current state
     */
    String toString();

}
