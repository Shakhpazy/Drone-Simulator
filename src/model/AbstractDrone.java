package model;

public abstract class AbstractDrone implements DroneInterface{

    protected float myAltitude;

    protected float myLongitude;

    protected float myLatitude;

    protected float myVelocity;

    protected float myBatteryLevel;

    protected Orientation myOrientation;

    protected final int myID;

    protected static int totalDrones = 0;

    protected final float myMaxVelocity;
    protected final float myMinVelocity;
    protected final float myMaxAltitude;
    protected final float myMinAltitude;
    protected final float myAccelerationStep;


    public AbstractDrone(float theLong, float theLat, float theAlt, float theVel, float theBat, float theMaxVel,
                         float theMinVel, float theMaxAlt, float theMinAlt, float theAccStep) {
        myLongitude = theLong;
        myLatitude = theLat;
        myAltitude = theAlt;
        myVelocity = theVel;
        myBatteryLevel = theBat;
        myOrientation = new Orientation(0);
        myMaxVelocity = theMaxVel;
        myMinVelocity = theMinVel;
        myMaxAltitude = theMaxAlt;
        myMinAltitude = theMinAlt;
        myAccelerationStep = theAccStep;
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

    public float getBatteryLevel() {
        return myBatteryLevel;
    }

    public float getVelocity() {
        return myVelocity;
    }

    public Orientation getOrientation() {
        return myOrientation;
    }

    public float getMaxVelocity() {
        return myMaxVelocity;
    }

    public float getMinVelocity() {
        return myMinVelocity;
    }

    public float getMaxAltitude() {
        return myMaxAltitude;
    }

    public float getMinAltitude() {
        return myMinAltitude;
    }

    public float getAccelerationStep() {
        return myAccelerationStep;
    }


    public static int getTotalDrones() {
        return totalDrones;
    }

    // These are all the Setters
    public void setAltitude(float theAltitude) {
        myAltitude = Math.max(0, theAltitude);
    }

    public void setLongitude(float theLongitude) {
        myLongitude = theLongitude;
    }

    public void setLatitude(float theLatitude) {
        myLatitude = theLatitude;
    }

    public void setBatteryLevel(float theBatteryLevel) {
        myBatteryLevel = theBatteryLevel;
    }

    public void setVelocity(float theVelocity) {
        myVelocity = theVelocity;
    }

    public void setOrientation(float theDegree) {
        myOrientation.setDegrees(theDegree);
    }


    public void updateDrone(float theLongitude, float theLatitude, float theAltitude, float theBatteryDrained, float theVelocity, float theDegree) {
        setLongitude(theLongitude);
        setLatitude(theLatitude);
        setAltitude(theAltitude);
        setVelocity(theVelocity);
        setOrientation(theDegree);
        setBatteryLevel(Math.max(myBatteryLevel - theBatteryDrained, 0));
    }

    public boolean isAlive() {
        return myBatteryLevel > 0  && myAltitude > 0;
    }

}
