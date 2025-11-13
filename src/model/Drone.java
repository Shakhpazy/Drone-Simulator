package model;

import java.util.ArrayList;

public class Drone extends AbstractDrone {

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 1;

    /** Maximum allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 700;

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = 1;

    ArrayList<RoutePoint> myRoute;
    private int nextPoint = 0;

    public Drone(float theVelocity, int theBatteryLevel, ArrayList<RoutePoint> theRoute) {
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

    public RoutePoint getNextPoint() {
        return myRoute.get(nextPoint % myRoute.size());
    }

    public void setNextRoute() {
        nextPoint = (nextPoint + 1) % myRoute.size();
    }



}
