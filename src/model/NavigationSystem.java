package model;

import java.util.ArrayList;

/**
 * Manages route navigation and normal movement logic for a drone.
 * Handles waypoint tracking, circular routing, and movement calculations.
 *
 * @author Yusuf
 */
public class NavigationSystem {
    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;
    
    /** Max allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 1000;
    
    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = 0.3f;
    
    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;
    
    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 0;
    
    /** Arraylist of RoutePoints that the drone goes to (Circular) */
    private final ArrayList<RoutePoint> myRoute;
    
    /** Current RoutePoint the Drone is moving towards */
    private int myNextPoint;
    
    /**
     * Creates a NavigationSystem with the given route.
     *
     * @param theRoute the route to follow
     */
    public NavigationSystem(final ArrayList<RoutePoint> theRoute) {
        if (theRoute == null || theRoute.isEmpty()) {
            throw new IllegalArgumentException("Route cannot be null or empty");
        }
        myRoute = theRoute;
        myNextPoint = 1;
    }
    
    /**
     * Gets the next waypoint in the route (circular).
     *
     * @return the next RoutePoint
     */
    public RoutePoint getNextPoint() {
        return myRoute.get(myNextPoint % myRoute.size());
    }
    
    /**
     * Advances to the next waypoint in the route (circular).
     */
    public void advanceToNextPoint() {
        myNextPoint = (myNextPoint + 1) % myRoute.size();
    }
    
    /**
     * Calculates the next movement position toward the current waypoint.
     *
     * @param theCurrentLongitude current longitude
     * @param theCurrentLatitude current latitude
     * @param theCurrentAltitude current altitude
     * @param theCurrentVelocity current velocity
     * @param theDeltaTime time step
     * @return a MovementResult containing new position, velocity, and whether waypoint was reached
     */
    public MovementResult calculateNextMove(
            final float theCurrentLongitude,
            final float theCurrentLatitude,
            final float theCurrentAltitude,
            final float theCurrentVelocity,
            final float theDeltaTime) {
        
        RoutePoint next = getNextPoint();
        float dx = next.getLongitude() - theCurrentLongitude;
        float dy = next.getLatitude() - theCurrentLatitude;
        float dz = next.getAltitude() - theCurrentAltitude;
        
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Prevent freeze when drone is exactly on the waypoint
        if (distance < 0.0001f) {
            advanceToNextPoint();
            return new MovementResult(
                next.getLongitude(),
                next.getLatitude(),
                next.getAltitude(),
                theCurrentVelocity,
                true
            );
        }
        
        // Adjust velocity (slow near waypoint / speed up otherwise)
        float minVelocityToMove = 0.5f;
        float newVelocity;
        if (distance < 30.0f) {
            newVelocity = Math.max(theCurrentVelocity - ACCELERATION_STEP, MIN_VELOCITY);
            if (distance > 0.001f && newVelocity < minVelocityToMove) {
                newVelocity = minVelocityToMove;
            }
        } else {
            newVelocity = Math.min(theCurrentVelocity + ACCELERATION_STEP, MAX_VELOCITY);
        }
        
        float moveDist = newVelocity * theDeltaTime;
        float newLongitude, newLatitude, newAltitude;
        boolean waypointReached = false;
        
        if (distance <= moveDist) {
            newLongitude = next.getLongitude();
            newLatitude = next.getLatitude();
            newAltitude = next.getAltitude();
            advanceToNextPoint();
            waypointReached = true;
        } else {
            float ratio = moveDist / distance;
            newLongitude = theCurrentLongitude + dx * ratio;
            newLatitude = theCurrentLatitude + dy * ratio;
            newAltitude = theCurrentAltitude + dz * ratio;
        }
        
        return new MovementResult(newLongitude, newLatitude, newAltitude, newVelocity, waypointReached);
    }
    
    /**
     * Gets the acceleration step constant.
     *
     * @return the acceleration step
     */
    public float getAccelerationStep() {
        return ACCELERATION_STEP;
    }
    
    /**
     * Result of a movement calculation.
     */
    public static class MovementResult {
        public final float longitude;
        public final float latitude;
        public final float altitude;
        public final float velocity;
        public final boolean waypointReached;
        
        public MovementResult(final float theLongitude, final float theLatitude,
                            final float theAltitude, final float theVelocity,
                            final boolean theWaypointReached) {
            longitude = theLongitude;
            latitude = theLatitude;
            altitude = theAltitude;
            velocity = theVelocity;
            waypointReached = theWaypointReached;
        }
    }
}
