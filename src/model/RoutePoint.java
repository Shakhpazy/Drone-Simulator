package model;

public class RoutePoint {

    private final float myAltitude;

    private final float myLongitude;

    private final float myLatitude;

    public RoutePoint(float theAltitude, float theLongitude, float theLatitude) {
        myAltitude = theAltitude;
        myLongitude = theLongitude;
        myLatitude = theLatitude;
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
        return "(" + myLatitude + ", " + myLongitude + ", " + myAltitude + ")";
    }

}
