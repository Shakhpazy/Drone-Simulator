package model;

import java.util.ArrayList;
import java.util.Random;

public class Drone extends AbstractDrone {
    //This might change if  I want each Drone to have its own
    //max based on a range of those numbers.

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    public static final float MIN_LATITUDE = -90.0f;

    public static final float MAX_LATITUDE = 90.0f;

    public static final float MIN_LONGITUDE = -180.0f;

    public static final float MAX_LONGITUDE = 180.0f;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = .07f;

    private static final float ANOMALY_EXTRA_DRAIN_RATE = 0.1f;

    private static final float ANOMALY_ALTITUDE_CHANGE = 50f;

    private static final float ANOMALY_VELOCITY_CHANGE = 7;

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 1;

    /** Maximum allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 700;

    private static final AnomalyEnum[] movementAnomalies = {
            AnomalyEnum.BATTERY_DRAIN,
            AnomalyEnum.BATTERY_FAIL,
            AnomalyEnum.ALTITUDE,
            AnomalyEnum.SPOOFING,
            AnomalyEnum.SPEED
            // OUT_OF_BOUNDS NOT included on purpose
    };

    /** Random (instance level race condition?)*/
    private final Random myRandom = new Random();

    /** Arraylist of RoutePoints that the drone goes to (Circular) */
    ArrayList<RoutePoint> myRoute;

    /* Current RoutePoint the Drone is moving towards */
    private int nextPoint = 0;

    /**
     * Constructor for the Drone
     *
     * @param theVelocity The velocity of the Drone
     * @param theBatteryLevel The Battery of the Drone
     * @param theRoute The Route of the Drone.
     */
    public Drone(final float theVelocity, final int theBatteryLevel, final ArrayList<RoutePoint> theRoute) {
        super(
                !theRoute.isEmpty() ? theRoute.getFirst().getLongitude() : 0f,
                !theRoute.isEmpty() ? theRoute.getFirst().getLatitude()  : 0f,
                !theRoute.isEmpty() ? theRoute.getFirst().getAltitude()  : 0f,
                theVelocity,
                theBatteryLevel,
                MAX_VELOCITY,
                MIN_VELOCITY,
                MAX_ALTITUDE,
                MIN_ALTITUDE,
                ACCELERATION_STEP
        );

        if (theRoute.isEmpty()) {
            throw new IllegalArgumentException("Route cannot be empty");
        }

        myRoute = theRoute;
        nextPoint = 1;
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
     * Increments NextPoint by 1, circular so that it stays in range of
     * the size of MyRoute
     */
    public void setNextRoute() {
        nextPoint = (nextPoint + 1) % myRoute.size();
    }

    @Override
    public void setVelocity(final float theVelocity) {
        if (theVelocity < MIN_VELOCITY || theVelocity > MAX_VELOCITY) {
            throw new IllegalArgumentException("The velocity must stay in bound");
        }
        myVelocity = theVelocity;
    }

    @Override
    public void getNextRandomMove(final float theDeltaTime) {
        float latitude = this.getLatitude();
        float longitude = this.getLongitude();
        float altitude = this.getAltitude();
        float velocity = this.getVelocity();

        float drained = 0f;

        AnomalyEnum anomaly = movementAnomalies[myRandom.nextInt(movementAnomalies.length)];
        System.out.println("Detected anomaly: " + anomaly);

        switch (anomaly) {
            case BATTERY_DRAIN:
                drained += ANOMALY_EXTRA_DRAIN_RATE;
                break;

            case BATTERY_FAIL:
                setAltitude(0);
                setBatteryLevel(0);
                break;

            case ALTITUDE:
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1)
                        * ANOMALY_ALTITUDE_CHANGE * theDeltaTime;

                // Prevent going below 0, but allow going ABOVE max altitude for out of bound anomaly
                altitude = Math.max(this.getMinAltitude(), altitude + changeAlt);

                break;

            case SPOOFING: //Spoofing moves the drone to a completely random position that is in range
                longitude = myRandom.nextFloat(MIN_LONGITUDE, MAX_LONGITUDE);
                latitude  = myRandom.nextFloat(MIN_LATITUDE, MAX_LATITUDE);
                altitude  = myRandom.nextFloat(MIN_ALTITUDE, MAX_ALTITUDE);  // optional: include altitude spoof
                break;


            case SPEED:
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + ANOMALY_VELOCITY_CHANGE, this.getMaxVelocity());
                } else {
                    velocity = Math.max(velocity - ANOMALY_VELOCITY_CHANGE, this.getMinVelocity());
                }
                break;
        }

        // distance moved due to anomaly
        float anomalyDistance = (float) Math.sqrt(
                Math.pow(longitude - this.getLongitude(), 2) +
                        Math.pow(latitude  - this.getLatitude(), 2) +
                        Math.pow(altitude  - this.getAltitude(), 2)
        );

        if (anomaly != AnomalyEnum.BATTERY_FAIL && anomaly != AnomalyEnum.SPOOFING) {
            drained += batteryDrained(anomalyDistance, theDeltaTime);
        }

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
        
        // Adjust velocity slightly (acceleration/deceleration) BEFORE calculating movement
        if (distance < 30.0f) {
            velocity = Math.max(this.getVelocity() - this.getAccelerationStep(), this.getMinVelocity());
        } else {
            velocity = Math.min(this.getVelocity() + this.getAccelerationStep(), this.getMaxVelocity());
        }
        
        // Use the updated velocity for movement calculation
        float moveDist = velocity * theDeltaTime; // movement this frame

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

        float drained = batteryDrained(moveDist, theDeltaTime);
        float degree = getOrientation().findNextOrientation(this.getLongitude(), this.getLatitude(), longitude, latitude);
        updateDrone(longitude, latitude, altitude, drained, velocity, degree);
    }

}
