package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * A class to aid the AnomalyDetector class in AnomalyReport composition.
 * @author nlevin11
 * @version 11-6
 */
public class ReportFormatter {

    /**
     * A time format to format time output.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    /**
     * A method to return a user readable timestamp string.
     *
     * @param telemetry         The telemetry snapshot to derive the time.
     * @return                  Returns a user readable timestamp string.
     */
    public static String getFormattedTime(HashMap<String, Object> telemetry) {
        long timestamp = (Long) telemetry.get("timeStamp");
        Instant instant = Instant.ofEpochMilli(timestamp);
        return FORMATTER.format(instant);
    }

    /**
     * A method to create a simplified anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @return Returns a simplified string anomaly report.
     */
    public static String createDescSimple(String theAnomalyType, HashMap<String, Object> theCurrTelemetry) {

        return "Anomaly Detected! \nDrone ID: " +
                theCurrTelemetry.get("id") +
                "\nAnomaly Type: " +
                theAnomalyType +
                "\nTime Stamp: " +
                getFormattedTime(theCurrTelemetry) + "\n";
    }

    /**
     * A method to create a detailed anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @return Returns a detailed string anomaly report.
     */
    public static String createDescDetailed(String theAnomalyType, HashMap<String, Object> theCurrTelemetry, HashMap<String, Object> thePrevTelemetry) {

        return "\nDrone Number: " + theCurrTelemetry.get("id") +
                " Has experienced an anomaly at time: " + getFormattedTime(theCurrTelemetry) +
                "\nDetails:\n" +
                theAnomalyType + " anomaly detected\n" +

                //Current State
                "Current State: \n" +
                "x: " + theCurrTelemetry.get("latitude") +
                " y: " + theCurrTelemetry.get("longitude") +
                " z: " + theCurrTelemetry.get("altitude") + "\n" +
                "Velocity: " + theCurrTelemetry.get("velocity") + " units/second(cycle)\n" +
                "Orientation: " + theCurrTelemetry.get("orientation") + "\n" +
                "Battery (%): " + theCurrTelemetry.get("batteryLevel") +

                //Previous state
                "\nPrevious State: \n" +
                "x: " + thePrevTelemetry.get("latitude") +
                " y: " + thePrevTelemetry.get("longitude") +
                " z: " + thePrevTelemetry.get("altitude") + "\n" +
                "Velocity: " + thePrevTelemetry.get("velocity") + " units/second(cycle)\n" +
                "Orientation: " + thePrevTelemetry.get("orientation") + "\n" +
                "Battery (%): " + thePrevTelemetry.get("batteryLevel") + "\n";
    }

}
