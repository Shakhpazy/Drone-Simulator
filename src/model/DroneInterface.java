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

    /**
     * Gets the unique ID of the drone.
     *
     * @return the ID of the drone
     */
    int getId();

    /**
     * Gets the current altitude of the drone.
     *
     * @return the altitude in meters
     */
    float getAltitude();

    /**
     * Gets the current longitude of the drone.
     *
     * @return the longitude in decimal degrees
     */
    float getLongitude();

    /**
     * Gets the current latitude of the drone.
     *
     * @return the latitude in decimal degrees
     */
    float getLatitude();

    /**
     * Gets the current velocity of the drone.
     *
     * @return the velocity in meters per second
     */
    float getVelocity();

    /**
     * Gets the current battery level of the drone.
     *
     * @return the battery level as a percentage (0–100)
     */
    int getBatteryLevel();

    /**
     * Sets the altitude of the drone.
     *
     * @param theAltitude the altitude in meters
     */
    void setAltitude(float theAltitude);

    /**
     * Sets the longitude of the drone.
     *
     * @param theLongitude the longitude in decimal degrees
     */
    void setLongitude(float theLongitude);

    /**
     * Sets the latitude of the drone.
     *
     * @param theLatitude the latitude in decimal degrees
     */
    void setLatitude(float theLatitude);

    /**
     * Sets the velocity of the drone.
     *
     * @param theVelocity the velocity in meters per second
     */
    void setVelocity(float theVelocity);

    /**
     * Sets the battery level of the drone.
     *
     * @param theBatteryLevel the battery level as a percentage (0–100)
     */
    void setBatteryLevel(int theBatteryLevel);

    /**
     * Prints the current Drone in a neat fashion
     *
     * @return the string representation of a Drone
     */
    String toString();
}
