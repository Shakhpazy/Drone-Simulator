package model;

import java.util.ArrayList;
import java.util.Random;

public class Drone extends AbstractDrone {
    //This might change if  I want each Drone to have its own
    //max based on a range of those numbers.

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 1;

    /** Maximum allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 700;

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = .07f;

    private static final AnomalyEnum[] movementAnomalies = {
            AnomalyEnum.BATTERY_DRAIN,
            AnomalyEnum.BATTERY_FAIL,
            AnomalyEnum.ALTITUDE,
            AnomalyEnum.SPOOFING,
            AnomalyEnum.SPEED
            // OUT_OF_BOUNDS NOT included on purpose
    };

    /** Random */
    private static final Random myRandom = new Random();

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
                drained += 0.1f;
                break;

            case BATTERY_FAIL:
                // maybe set battery to zero?
                break;

            case ALTITUDE:
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1)
                        * (10 + myRandom.nextFloat() * 10) * theDeltaTime;
                altitude = Math.min(
                        this.getMaxAltitude(),
                        Math.max(this.getMinAltitude(), altitude + changeAlt)
                );
                break;

            case SPOOFING:
                float driftX = (myRandom.nextBoolean() ? 1 : -1)
                        * (15 + myRandom.nextFloat() * 10) * theDeltaTime;
                float driftY = (myRandom.nextBoolean() ? 1 : -1)
                        * (15 + myRandom.nextFloat() * 10) * theDeltaTime;
                longitude += driftX;
                latitude  += driftY;
                break;

            case SPEED:
                int change = 7;
                if (myRandom.nextBoolean()) {
                    velocity = Math.min(velocity + change, this.getMaxVelocity());
                } else {
                    velocity = Math.max(velocity - change, this.getMinVelocity());
                }
                break;
        }

        // distance moved due to anomaly
        float anomalyDistance = (float) Math.sqrt(
                Math.pow(longitude - this.getLongitude(), 2) +
                        Math.pow(latitude  - this.getLatitude(), 2) +
                        Math.pow(altitude  - this.getAltitude(), 2)
        );

        drained += batteryDrained(anomalyDistance, theDeltaTime);

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
        float moveDist = this.getVelocity() * theDeltaTime; // movement this frame

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

        // Adjust velocity slightly (acceleration/deceleration)
        if (distance < 10.0f) {
            velocity = Math.max(this.getVelocity() - this.getAccelerationStep(), this.getMinVelocity());
        } else {
            velocity = Math.min(this.getVelocity() + this.getAccelerationStep(), this.getMaxVelocity());
        }

        float drained = batteryDrained(moveDist, theDeltaTime);
        float degree = getOrientation().findNextOrientation(this.getLongitude(), this.getLatitude(), longitude, latitude);
        updateDrone(longitude, latitude, altitude, drained, velocity, degree);
    }
}
