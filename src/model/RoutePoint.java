package model;

public class RoutePoint {

    /** The altitude of the route point */
    private final float myAltitude;

    /** the longitude of the route point */
    private final float myLongitude;

    /** the latitude of the route point */
    private final float myLatitude;

    /**
     * Constructor for RoutePoint
     *
     * @param theLongitude the longitude
     * @param theLatitude the latitude
     * @param theAltitude the altitude
     */
    public RoutePoint(final float theLongitude, final float theLatitude, final float theAltitude) {
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myAltitude = theAltitude;
    }

    /**
     * @return the altitude of the route point
     */
    public float getAltitude() {
        return myAltitude;
    }

    /**
     * @return the longitude of the route point
     */
    public float getLongitude() {
        return myLongitude;
    }

    /**
     * @return the latitude of the route point
     */
    public float getLatitude() {
        return myLatitude;
    }

    @Override
    public String toString() {
        return "(" + myLongitude + ", " + myLatitude + ", " + myAltitude + ")";
    }

}
