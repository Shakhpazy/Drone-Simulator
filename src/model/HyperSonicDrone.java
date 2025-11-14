package model;

import java.util.ArrayList;

public class HyperSonicDrone extends AbstractDrone{

    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 50;

    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 15;

    /** Maximum allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 10000;

    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;

    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = 5;


    public HyperSonicDrone(float theVelocity, int theBatteryLevel, ArrayList<RoutePoint> theRoute) {
        super(
                0f,
                0f,
                0f,
                theVelocity,
                theBatteryLevel,
                MAX_VELOCITY,
                MIN_VELOCITY,
                MAX_ALTITUDE,
                MIN_ALTITUDE,
                ACCELERATION_STEP
        );
    }



    @Override
    public void getNextRandomMove(float theDeltaTime) {

    }

    @Override
    public void getNextMove(float theDeltaTime) {

    }


}
