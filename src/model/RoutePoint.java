package model;

/**
 * Represents a single waypoint in a drone's route.
 * <p>
 * A RoutePoint defines a fixed 3D coordinate consisting of longitude,
 * latitude, and altitude. Drones navigate between consecutive RoutePoints
 * to follow a predefined route path.
 * <p>
 * RoutePoints are immutable—once created, their values cannot be changed.
 *
 * @author Yusuf Shakhpaz
 */
public class RoutePoint {

    /** Altitude component of the route point. */
    private final float myAltitude;

    /** Longitude component of the route point. */
    private final float myLongitude;

    /** Latitude component of the route point. */
    private final float myLatitude;

    /**
     * Constructs a new RoutePoint with the given coordinates.
     *
     * @param theLongitude the longitude of the waypoint
     * @param theLatitude  the latitude of the waypoint
     * @param theAltitude  the altitude of the waypoint
     */
    public RoutePoint(final float theLongitude, final float theLatitude, final float theAltitude) {
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myAltitude = theAltitude;
    }

    /**
     * Returns the altitude of the route point.
     *
     * @return altitude value
     */
    public float getAltitude() {
        return myAltitude;
    }

    /**
     * Returns the longitude of the route point.
     *
     * @return longitude value
     */
    public float getLongitude() {
        return myLongitude;
    }

    /**
     * Returns the latitude of the route point.
     *
     * @return latitude value
     */
    public float getLatitude() {
        return myLatitude;
    }

    /**
     * Returns a formatted string representation of the route point.
     * Format: <code>(longitude, latitude, altitude)</code>
     *
     * @return string representation of the waypoint
     */
    @Override
    public String toString() {
        return STR."(\{myLongitude}, \{myLatitude}, \{myAltitude})";
    }

}
