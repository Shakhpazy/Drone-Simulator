package model;

public abstract class AbstractDrone implements DroneInterface{

    /** Base battery drain per second (hovering + electronics). */
    private static final float BASE_DRAIN_RATE = 0.01f;

    /** Additional battery drain per unit of speed per second. */
    private static final float SPEED_DRAIN_RATE = 0.003f;

    /** The altitude of the Drone */
    protected float myAltitude;

    /** The longitude of the Drone */
    protected float myLongitude;

    /** The Latitude of the Drone */
    protected float myLatitude;

    /** The velocity of the Drone */
    protected float myVelocity;

    /* The battery level of the Drone */
    protected float myBatteryLevel;

    /* The Orientation of the Drone */
    protected Orientation myOrientation;

    /* The unique ID of the Drone */
    protected final int myID;

    /* The number of drones created */
    protected static int totalDrones = 0;

    /* The Max Velocity of the Drone */
    protected final float myMaxVelocity;

    /* The Min Velocity of the Drone */
    protected final float myMinVelocity;

    /* The Max altitude of the Drone */
    protected final float myMaxAltitude;

    /* The Min altitude of the Drone */
    protected final float myMinAltitude;

    /* The acceleration step of the Drone */
    protected final float myAccelerationStep;

    /* The drones health */
    protected boolean myDroneIsAlive;

    /**
     * Creates an Abstract Drone.
     *
     * @param theLong the starting longitude
     * @param theLat the stating latitude
     * @param theAlt the starting altitude
     * @param theVel the starting velocity
     * @param theBat the starting battery life
     * @param theMaxVel the max velocity of a drone
     * @param theMinVel the min velocity of a drone
     * @param theMaxAlt the max altitude of a drone
     * @param theMinAlt the min altitude of a drone
     * @param theAccStep the acceleration step of a drone
     */
    public AbstractDrone(final float theLong, final float theLat, final float theAlt, final float theVel, final float theBat, final float theMaxVel,
                         final float theMinVel, final float theMaxAlt, final float theMinAlt, final float theAccStep) {
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
        myDroneIsAlive = true;
        totalDrones += 1;
        myID = totalDrones;
    }

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

    public boolean isAlive() {
        return myDroneIsAlive;
    }

    /**
     * @return Total number of Drones created.
     */
    public static int getTotalDrones() {
        return totalDrones;
    }

    /**
     * Sets the Altitude of the Drone
     *
     * @param theAltitude float of the altitude
     */
    public void setAltitude(final float theAltitude) {
        myAltitude = Math.max(0, theAltitude);
    }

    /**
     * Sets the Longitude of the Drone
     *
     * @param theLongitude float of the Longitude
     */
    public void setLongitude(final float theLongitude) {
        myLongitude = theLongitude;
    }

    /**
     * Sets the Latitude of the Drone
     *
     * @param theLatitude float of the Latitude
     */
    public void setLatitude(final float theLatitude) {
        myLatitude = theLatitude;
    }

    /**
     * Sets the battery level of the Drone
     *
     * @param theBatteryLevel float of the Battery
     */
    public void setBatteryLevel(final float theBatteryLevel) {
        if (theBatteryLevel < 0) {
            throw new IllegalArgumentException("Battery level cannot be below 0");
        }

        myBatteryLevel = theBatteryLevel;

        if (myBatteryLevel == 0) {
            setIsAlive(false);
        }
    }

    /**
     * Sets the velocity of the Drone
     *
     * @param theVelocity float of the velocity
     */
    public void setVelocity(final float theVelocity) {
        myVelocity = theVelocity;
    }

    /**
     * Sets the Orientation of the Drone
     *
     * @param theDegree float of the degree the drone is supposed to face
     *                  N: 0 E: 90 S:180 W: 2770
     */
    public void setOrientation(final float theDegree) {
        myOrientation.setDegrees(theDegree);
    }

    public void setIsAlive(final boolean theHealth) {
        myDroneIsAlive = theHealth;
    }

    /**
     * Updates the state of the entire Drone when the drone moves to its next position
     *
     * @param theLongitude float of the longitude
     * @param theLatitude float of the latitude
     * @param theAltitude float of the altitude
     * @param theBatteryDrained float of the battery that got drained
     * @param theVelocity float of the velocity
     * @param theDegree float of the degree (orientation)
     */
    public void updateDrone(final float theLongitude, final float theLatitude, final float theAltitude, final float theBatteryDrained, final float theVelocity, final float theDegree) {
        setLongitude(theLongitude);
        setLatitude(theLatitude);
        setAltitude(theAltitude);
        setVelocity(theVelocity);
        setOrientation(theDegree);
        setBatteryLevel(Math.max(myBatteryLevel - theBatteryDrained, 0));
    }

    /**
     * When a drone collides with another drone the Altitude will be set to
     * 0, meaning the Drone is now dead.
     */
    public void collided() {
        setAltitude(0);
        setIsAlive(false);
    }

    public abstract void getNextRandomMove(final float theDeltaTime);

    public abstract void getNextMove(final float theDeltaTime);

    public RoutePoint getNextPoint() {
        return new RoutePoint(0,0,0);
    }

    /**
     *
     * @param deltaTime the tick rate, of the simulation
     * @return the amount of battery that got drained from the Drone
     */
    protected float batteryDrained(final float deltaTime) {
        float drain = BASE_DRAIN_RATE * deltaTime; //Base drain rate
        float speed = Math.abs(this.getVelocity());
        drain += speed * SPEED_DRAIN_RATE * deltaTime; //drain based on speed
        return drain;
    }


}
