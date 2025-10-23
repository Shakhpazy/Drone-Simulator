package model;

import java.util.ArrayList;

public class Drone implements  DroneInterface {

    private float myAltitude;

    private float myLongitude;

    private float myLatitude;

    private float myVelocity;

    private int myBatterLevel;

    private Orientation myOrientation;

    private final int myID;

    static int totalDrones = 0;

    ArrayList<RoutePoint> myRoute;

    /**
     * This is the constructor for the Drone Class that
     * sets custom values for the drones attributes.
     *
     * @param theVelocity float of the velocity
     * @param theBatterLevel int of the battery level 0-100
     */
    public Drone(float theVelocity, int theBatterLevel, Orientation theOrientation, ArrayList<RoutePoint> theRoute) {
        //implement error handling for  these values passed later
        if (theRoute.isEmpty()) {
            throw new IllegalArgumentException("No route has been set for a drone");
        }
        RoutePoint start = theRoute.getFirst();
        myLongitude = start.getLongitude();
        myLatitude = start.getLatitude();
        myAltitude = start.getAltitude();
        myVelocity = theVelocity;
        myBatterLevel = theBatterLevel;
        myOrientation = theOrientation;
        myRoute = theRoute;

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

    public Orientation getOrientation() {
        return myOrientation;
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

    public void setOrientation(Orientation theOrientation) {
        myOrientation = theOrientation;
    }

}
