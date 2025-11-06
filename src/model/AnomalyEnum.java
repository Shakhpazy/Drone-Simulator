package model;

/**
 * A class to encompass drone anomaly enumerations and their string representations.
 */
public enum AnomalyEnum {
    BATTERY_DRAIN("Abnormal Battery Drain Rate"),
    BATTERY_FAIL("Battery Failure"),
    ALTITUDE("Dangerous Change in Altitude"),
    SPOOFING("GPS Spoofing"),
    OUT_OF_BOUNDS("Out of Bounds");

    private final String myDisplayString;

    AnomalyEnum(String theDisplayString){
        this.myDisplayString = theDisplayString;
    }

    @Override
    public String toString() {
        return this.myDisplayString;
    }
}
