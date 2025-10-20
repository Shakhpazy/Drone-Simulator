package model;

import java.util.Date;

public class Telemetry {

    private final float myAltitude;

    private final float myLongitude;

    private final float myLatitude;

    private final float myVelocity;

    private final int myBatterLevel;

    private final long myTimestamp;

    private static final Date date = new Date();

    /**
     * Constructor for the Telemetry class that holds drone information, such as
     * Altitude, longitude, Latitude, Velocity, battery level, and current time stamp
     * @param theAltitude
     * @param theLongitude
     * @param theLatitude
     * @param theVelocity
     * @param theBatterLevel
     */
    public Telemetry(float theAltitude, float theLongitude, float theLatitude, float theVelocity, int theBatterLevel) {
        //need to implement error handling later
        myAltitude = theAltitude;
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myVelocity = theVelocity;
        myBatterLevel = theBatterLevel;
        myTimestamp = date.getTime();
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

    public long getMyTimestamp() {
        return myTimestamp;
    }

}
