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
     * This is the constructor for the Drone Class that
     * sets custom values for the drones attributes.
     *
     * @param theAltitude float of the altitude
     * @param theLongitude float of the longitude
     * @param theLatitude float of the latitude
     * @param theVelocity float of the velocity
     * @param theBatterLevel int of the battery level 0-100
     */
    public Drone(float theAltitude, float theLongitude, float theLatitude, float theVelocity, int theBatterLevel) {
        //implement error handling for  these values passed later
        myAltitude = theAltitude;
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myVelocity = theVelocity;
        myBatterLevel = theBatterLevel;

        totalDrones += 1;
        myID = totalDrones;
    }


    /**
     * This is the basic constructor for the Drone Class,
     * that sets default values.
     */
    public Drone() {
        //Thi
        this(0,0,0,0,100);
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
