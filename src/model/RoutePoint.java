package model;

public class RoutePoint {

    private final float myAltitude;

    private final float myLongitude;

    private final float myLatitude;

    public RoutePoint(float theLongitude, float theLatitude, float theAltitude) {
        myLongitude = theLongitude;
        myLatitude = theLatitude;
        myAltitude = theAltitude;
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

    @Override
    public String toString() {
        return "(" + myLongitude + ", " + myLatitude + ", " + myAltitude + ")";
    }

}
