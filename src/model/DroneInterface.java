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

    int getId();
    float getLongitude();
    float getLatitude();
    float getAltitude();
    float getVelocity();
    float getBatteryLevel();
    Orientation getOrientation();

    //Core Behaviors
    void getNextMove(float theDeltaTime);
    void getNextRandomMove(float theDeltaTime);
    void collided();
    ////void updateOrientation();

    // Physics properties
    float getMaxVelocity();
    float getMinVelocity();
    float getAccelerationStep();
    float getMaxAltitude();
    float getMinAltitude();

    boolean isAlive();

    // Telemetry snapshot (optional)
    String toString();

    //should be removed in prod
    RoutePoint getNextPoint();

}
