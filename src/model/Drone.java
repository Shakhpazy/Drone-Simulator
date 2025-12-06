package model;

import java.util.ArrayList;
import java.util.Random;

public class Drone implements DroneInterface {

    /** Base energy consumption per second (hovering + electronics). */
    private static final float BASE_DRAIN_RATE = 0.01f;

    /** Additional energy consumption per unit of velocity per second. */
    private static final float SPEED_DRAIN_RATE = 0.003f;

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    /** Max allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 1000;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = .3f;

    private static final float ANOMALY_EXTRA_DRAIN_RATE = 0.1f;

    private static final float ANOMALY_ALTITUDE_CHANGE = 50f;

    private static final float ANOMALY_VELOCITY_CHANGE = 7;

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 0;

    private static final AnomalyEnum[] movementAnomalies = {
            AnomalyEnum.BATTERY_DRAIN,
            AnomalyEnum.BATTERY_FAIL,
            AnomalyEnum.ALTITUDE,
            AnomalyEnum.SPOOFING,
            AnomalyEnum.SPEED
            // OUT_OF_BOUNDS NOT included on purpose
    };

    /** Current altitude. */
    private float myAltitude;

    /** Current longitude. */
    private float myLongitude;

    /** Current latitude. */
    private float myLatitude;

    /** Current velocity. */
    private float myVelocity;

    /** Current remaining battery level. */
    private float myBatteryLevel;

    /** Current orientation object (heading in degrees). */
    private final Orientation myOrientation;

    /* The drones health */
    private boolean myDroneIsAlive;

    private TelemetryRecord prevTelemetryRecord;

    private AnomalyEnum myLastAnomaly;

    /** Unique drone identifier. */
    private final int myID;

    /** Total number of drones created so far (global counter). */
    private static int totalDrones = 0;

    /** Random (instance level race condition?)*/
    private final Random myRandom = new Random();

    /** Arraylist of RoutePoints that the drone goes to (Circular) */
    ArrayList<RoutePoint> myRoute;

    /* Current RoutePoint the Drone is moving towards */
    private int nextPoint;

    /**
     * Constructor for the Drone
     *
     * @param theVelocity The velocity of the Drone
     * @param theBatteryLevel The Battery of the Drone
     * @param theRoute The Route of the Drone.
     */
    public Drone(final float theVelocity, final int theBatteryLevel, final ArrayList<RoutePoint> theRoute) {
        if (theRoute.isEmpty()) {
            throw new IllegalArgumentException("Route cannot be empty");
        }
        if (theRoute.getFirst().getAltitude() < MIN_ALTITUDE || theRoute.getFirst().getAltitude() > MAX_ALTITUDE ||
                theVelocity > MAX_VELOCITY || theVelocity < MIN_VELOCITY) {
            throw new IllegalArgumentException("Arguments passed are not valid theAltitude or the Velocity is not in bound");
        }
        myLongitude = theRoute.getFirst().getLongitude();
        myLatitude = theRoute.getFirst().getLatitude();
        myAltitude = theRoute.getFirst().getAltitude();
        myVelocity = theVelocity;
        myBatteryLevel = theBatteryLevel;
        myOrientation = new Orientation(0);
        myRoute = theRoute;
        nextPoint = 1;
        myDroneIsAlive = true;
        totalDrones++;
        myID = totalDrones;
        prevTelemetryRecord = generateTelemetryRecord();
        myLastAnomaly = null;
    }

    public int getId() {
        return myID;
    }

    public float getLongitude() {
        return myLongitude;
    }

    public float getLatitude() {
        return myLatitude;
    }

    public float getAltitude() {
        return myAltitude;
    }

    public float getVelocity() {
        return myVelocity;
    }

    public Orientation getOrientation() {
        return myOrientation;
    }

    public float getBatteryLevel() {
        return myBatteryLevel;
    }

    public float getAccelerationStep() {
        return ACCELERATION_STEP;
    }

    public AnomalyEnum getMyLastAnomaly() {
        return myLastAnomaly;
    }

    public static int getTotalDrones() {
        return totalDrones;
    }

    public boolean isAlive() {
        return myDroneIsAlive;
    }

    /**
     * @return {RoutePoint} of the next Position the Drone is moving towards.
     *          Circular so that the Drone loops through infinite times.
     */
    @Override
    public RoutePoint getNextPoint() {
        return myRoute.get(nextPoint % myRoute.size());
    }

    /**
     * Sets the Altitude of the Drone protects if negative altitude is received
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
     * Increments NextPoint by 1, circular so that it stays in range of
     * the size of MyRoute
     */
    public void setNextRoute() {
        nextPoint = (nextPoint + 1) % myRoute.size();
    }

    public void setVelocity(final float theVelocity) {
        if (theVelocity < MIN_VELOCITY || theVelocity > MAX_VELOCITY) {
            throw new IllegalArgumentException("The velocity must stay in bound");
        }
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
     * Records which anomaly was last applied to this drone.
     *
     * @param theAnomaly anomaly type
     */
    private void setMyLastAnomaly(AnomalyEnum theAnomaly) {
        myLastAnomaly = theAnomaly;
    }

    public void setPrevTelemetryRecord(TelemetryRecord theTelemetryRecord) {
        prevTelemetryRecord = theTelemetryRecord;
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

    public void collided() {
        setAltitude(0);
        setVelocity(0);
        setIsAlive(false);
    }

    @Override
    public void getNextRandomMove(final float theDeltaTime) {
        float latitude = this.getLatitude();
        float longitude = this.getLongitude();
        float altitude = this.getAltitude();
        float velocity = this.getVelocity();
        float battery;
        float drained = 0f;

        AnomalyEnum anomaly = movementAnomalies[myRandom.nextInt(movementAnomalies.length)];
        setMyLastAnomaly(anomaly);

        switch (anomaly) {
            case BATTERY_DRAIN:
                drained += ANOMALY_EXTRA_DRAIN_RATE;
                break;

            case BATTERY_FAIL:
                altitude = 0;
                battery = 0;
                velocity = 0;
                setMyLastAnomaly(AnomalyEnum.BATTERY_FAIL);
                setVelocity(velocity);
                setAltitude(altitude);
                setBatteryLevel(battery);
                return;

            case ALTITUDE:
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1)
                        * ANOMALY_ALTITUDE_CHANGE * theDeltaTime;

                // Prevent going below 0, but allow going ABOVE max altitude for out of bound anomaly
                altitude = Math.max(MIN_ALTITUDE, altitude + changeAlt);

                break;

            case SPOOFING: //Spoofing moves the drone to a completely random position that is in range
                //Spoofing will make it so drone makes no movements however, the Telemetry Generator
                //Decides what spoofing will do. (in our case it will give fake data)
                break;


            case SPEED:
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + ANOMALY_VELOCITY_CHANGE, MAX_VELOCITY);
                } else {
                    velocity = Math.max(velocity - ANOMALY_VELOCITY_CHANGE, MIN_VELOCITY);
                }
                break;
        }

        drained += batteryDrained(theDeltaTime);
        float degree = getOrientation().findNextOrientation(
                this.getLongitude(),
                this.getLatitude(),
                longitude,
                latitude
        );

        updateDrone(longitude, latitude, altitude, drained, velocity, degree);
    }

    @Override
    public void getNextMove(final float theDeltaTime) {
        float latitude = this.getLatitude();
        float longitude = this.getLongitude();
        float altitude = this.getAltitude();
        float velocity;

        RoutePoint next = this.getNextPoint();
        float dx = next.getLongitude() - longitude;
        float dy = next.getLatitude() - latitude;
        float dz = next.getAltitude() - altitude;

        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        // Prevent freeze when drone is exactly on the waypoint
        if (distance < 0.0001f) {
            this.setNextRoute();
            return;
        }

        // Adjust velocity (slow near waypoint / speed up otherwise)
        // But ensure minimum velocity to prevent getting stuck (ran into the issue of 0 velocity)
        float minVelocityToMove = 0.5f; // Minimum velocity needed to make progress
        if (distance < 30.0f) {
            velocity = Math.max(this.getVelocity() - this.getAccelerationStep(), MIN_VELOCITY);
            // If we're still far enough that we need to move, ensure minimum velocity
            if (distance > 0.001f && velocity < minVelocityToMove) {
                velocity = minVelocityToMove;
            }
        } else {
            velocity = Math.min(this.getVelocity() + this.getAccelerationStep(), MAX_VELOCITY);
        }

        float moveDist = velocity * theDeltaTime;

        if (distance <= moveDist) {
            longitude = next.getLongitude();
            latitude  = next.getLatitude();
            altitude  = next.getAltitude();
            this.setNextRoute();
        } else {
            float ratio = moveDist / distance;
            longitude += dx * ratio;
            latitude  += dy * ratio;
            altitude  += dz * ratio;
        }

        float drained = batteryDrained(theDeltaTime);
        float degree = getOrientation().findNextOrientation(this.getLongitude(), this.getLatitude(), longitude, latitude);
        updateDrone(longitude, latitude, altitude, drained, velocity, degree);
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
