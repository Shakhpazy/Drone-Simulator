package model;

/**
 * An abstract base implementation of {@link DroneInterface} that provides
 * shared functionality for all drone models in the system.
 * <p>
 * This class manages the internal physics state of a drone (position, velocity,
 * altitude, battery, orientation) along with global drone metadata (unique ID,
 * creation count). Concrete subclasses are responsible for defining how the
 * drone moves under normal and anomalous conditions.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Maintains drone state and physical properties.</li>
 *     <li>Provides shared update logic (battery drain, setting fields safely,
 *         orientation normalization, etc.).</li>
 *     <li>Constructs telemetry records for logging and UI consumption.</li>
 *     <li>Tracks anomaly history and last recorded telemetry.</li>
 *     <li>Handles “crash” logic when a drone collides.</li>
 * </ul>
 *
 * @author Yusuf Shakhpaz
 */
public abstract class AbstractDrone implements DroneInterface{

    /** Base energy consumption per second (hovering + electronics). */
    private static final float BASE_DRAIN_RATE = 0.01f;

    /** Additional energy consumption per unit of velocity per second. */
    private static final float SPEED_DRAIN_RATE = 0.003f;

    /** Current altitude. */
    protected float myAltitude;

    /** Current longitude. */
    protected float myLongitude;

    /** Current latitude. */
    protected float myLatitude;

    /** Current velocity. */
    protected float myVelocity;

    /** Current remaining battery level. */
    protected float myBatteryLevel;

    /** Current orientation object (heading in degrees). */
    protected Orientation myOrientation;

    /** Unique drone identifier. */
    protected final int myID;

    /** Total number of drones created so far (global counter). */
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

    private TelemetryRecord prevTelemetryRecord;

    private AnomalyEnum myLastAnomaly;

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
        prevTelemetryRecord = new TelemetryRecord(myID, myLongitude, myLatitude, myAltitude, myVelocity, myBatteryLevel, myOrientation.getDegree(), System.currentTimeMillis());
        myLastAnomaly = null;
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

    public AnomalyEnum getMyLastAnomaly() {
        return myLastAnomaly;
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

    public void setPrevTelemetryRecord(TelemetryRecord theTelemetryRecord) {
        prevTelemetryRecord = theTelemetryRecord;
    }

    public TelemetryRecord getPreviousTelemetryRecord() {
        return prevTelemetryRecord;
    }

    public TelemetryRecord generateTelemetryRecord() {
        return new TelemetryRecord(
                myID,
                myLongitude,
                myLatitude,
                myAltitude,
                myVelocity,
                myBatteryLevel,
                myOrientation.getDegree(),
                System.currentTimeMillis()
        );
    }

    /**
     * Records which anomaly was last applied to this drone.
     *
     * @param theAnomaly anomaly type
     */
    protected void setMyLastAnomaly(AnomalyEnum theAnomaly) {
        myLastAnomaly = theAnomaly;
    }

    @Override
    public String toString() {
        return String.format(
                "Drone{id=%d, alive=%s, lon=%.2f, lat=%.2f, alt=%.2f, vel=%.2f, battery=%.2f, orientation=%.2f°}",
                myID,
                myDroneIsAlive,
                myLongitude,
                myLatitude,
                myAltitude,
                myVelocity,
                myBatteryLevel,
                myOrientation.getDegree()
        );
    }


}
