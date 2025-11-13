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

    //Remove these after as each Drone will update its state in its own class
    void updateDrone(float theLongitude, float theLatitude, float theAltitude, float theBatteryDrained, float theVelocity, float theDegree);
    RoutePoint getNextPoint();
    void setNextRoute();
    void setAltitude(float theAltitude);

    boolean isAlive();

    // Core behaviors
    //void move(double deltaTime);             // movement behavior
    //void updateOrientation();                // turning behavior

    // Physics properties
    float getMaxVelocity();
    float getMinVelocity();
    float getAccelerationStep();
    float getMaxAltitude();
    float getMinAltitude();

    // Telemetry snapshot (optional)
    String toString();

}
