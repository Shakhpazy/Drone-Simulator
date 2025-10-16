package model;

public class Drone implements  DroneInterface {

    private float myAltitude;

    private float myLongitude;

    private float myLatitude;

    private float myVelocity;

    private int myBatterLevel;

    private final int myID;

    static int totalDrones = 0;

    /**
     * This is the Constructor for the Drone Class, that
     * sets the current DroneID
     */
    public Drone() {
        totalDrones += 1;
        myID = totalDrones;
    }

    //These are all the Getters
    public int getId() {
        return myID;
    }

    public float getAltitude() {
        return myAltitude;
    }

    public float getLongitude() {
        return myLongitude;
    }

    public float getLatitude() {
        return myLatitude;
    }

    public int getBatteryLevel() {
        return myBatterLevel;
    }

    public float getVelocity() {
        return myVelocity;
    }

    /**
     * Gets the total number of drones
     * @return Int representing the total number of drones.
     */
    public int getTotalDrones() {
        return totalDrones;
    }

    // These are all the Setters
    public void setAltitude(float theAltitude) {
        myAltitude = theAltitude;
    }

    public void setLongitude(float theLongitude) {
        myLongitude = theLongitude;
    }

    public void setLatitude(float theLatitude) {
        myLatitude = theLatitude;
    }

    public void setBatteryLevel(int theBatteryLevel) {
        myBatterLevel = theBatteryLevel;
    }

    public void setVelocity(float theVelocity) {
        myVelocity = theVelocity;
    }

}
