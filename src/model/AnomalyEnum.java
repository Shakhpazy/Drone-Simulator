package model;

/**
 * A class to encompass drone anomaly enumerations and their string representations.
 */
public enum AnomalyEnum {
    BATTERY_DRAIN("Abnormal Battery Drain Rate"),
    BATTERY_FAIL("Battery Failure"),
    ALTITUDE("Dangerous Change in Altitude"),
    SPOOFING("GPS Spoofing"),
    SPEED("Dangerous change in Speed"),
    OUT_OF_BOUNDS("Out of Bounds");

    /**
     * A String to hold the String representation of an AnomalyEnum.
     */
    private final String myDisplayString;

    AnomalyEnum(String theDisplayString){
        this.myDisplayString = theDisplayString;
    }

    @Override
    public String toString() {
        return this.myDisplayString;
    }
}
