package model;

/**
 * The DroneInterface defines the contract for a Drone object.
 * <p>
 * It provides accessors (getters) and mutators (setters) for
 * core drone telemetry data such as altitude, position (longitude/latitude),
 * velocity, and battery level.
 * <p>
 * This interface can be implemented by different drone models
 * (e.g., simulated drones, real hardware-integrated drones) for future use cases.
 *
 * @author Yusuf Shakhpaz
 */
public interface DroneInterface {

    /** Get the ID of the Drone */
    int getId();
    /** Get the Longitude of the Drone */
    float getLongitude();
    /** Get the Latitude of the Drone */
    float getLatitude();
    /** Get the Altitude of the Drone */
    float getAltitude();
    /** Get the Velocity of the Drone */
    float getVelocity();
    /** Get the Battery Level of the Drone */
    float getBatteryLevel();
    /** Get the Orientation of a Drone */
    Orientation getOrientation();

    //Core Behaviors

    /**
     * Moves the Drone to its next point via a DeltaTime.
     * Larger DeltaTime will make the drown move in larger
     * Increments, whereas a smaller DeltaTime will make the
     * Drone move in smaller increments.
     *
     * @param theDeltaTime {float} of the tick rate
     */
    void getNextMove(float theDeltaTime);

    /**
     * Moves the Drone to its next point via a DeltaTime.
     * Larger DeltaTime will make the drown move in larger
     * Increments, whereas a smaller DeltaTime will make the
     * Drone move in smaller increments.
     *
     * @param theDeltaTime {float} of the tick rate
     */
    void getNextRandomMove(float theDeltaTime);

    /**
     * If a Drone has Collided with another drone
     * This method will be called to set the altitude of the
     * Drone to 0, signalling that a Drone is dead
     */
    void collided();


    // Physics properties

    /**
     * @return the Max velocity a Drone can move at
     */
    float getMaxVelocity();

    /**
     * @return the Min velocity a Drone can move at
     */
    float getMinVelocity();

    /**
     * @return the Acceleration step of a Drone.
     */
    float getAccelerationStep();

    /**
     * @return the Max altitude a Drone could go to
     */
    float getMaxAltitude();

    /**
     * @return the Min altitude a Drone could go to must be > 0
     */
    float getMinAltitude();

    /**
     * @return {true} if altitude is > 0 and batter level is > 0 else {false}
     */
    boolean isAlive();

    /**
     *
     */
    TelemetryRecord generateTelemetryRecord();

    /**
     *
     */
    TelemetryRecord getPreviousTelemetryRecord();

    void setPrevTelemetryRecord(TelemetryRecord theTelemetryRecord);
    /**
     * @return string of currentDrone state.
     */
    String toString();


    //should be removed in prod
    RoutePoint getNextPoint();

}
