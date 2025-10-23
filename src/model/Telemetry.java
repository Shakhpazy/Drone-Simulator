package model;

import java.util.Date;

public class Telemetry {

    private final int myDroneId;

    private final float myAltitude;

    private final float myLongitude;

    private final float myLatitude;

    private final float myVelocity;

    private final int myBatterLevel;

    private final Orientation myOrientation;

    private final long myTimestamp;

    private static final Date date = new Date();

    /**
     * Constructor for the Telemetry class that holds drone information, such as
     * Altitude, longitude, Latitude, Velocity, battery level, and current time stamp
     *
     * @param theDroneId
     * @param theAltitude
     * @param theLongitude
     * @param theLatitude
     * @param theVelocity
     * @param theBatterLevel
     */
    public Telemetry(int theDroneId, float theAltitude, float theLongitude, float theLatitude, float theVelocity, int theBatterLevel, Orientation theOrientation) {
        //need to implement error handling later
        myDroneId = theDroneId;
        myAltitude = theAltitude;
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myVelocity = theVelocity;
        myBatterLevel = theBatterLevel;
        myOrientation = theOrientation;
        myTimestamp = date.getTime();
    }

    public int getMyDroneId() {
        return myDroneId;
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

    public long getMyTimestamp() {
        return myTimestamp;
    }

}
