package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A class to aid the AnomalyDetector class in AnomalyReport composition.
 * @author nlevin11
 * @version 12-6
 */
public class ReportFormatter {

    /**
     * A time format to format time output.
     */
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    /**
     * A method to return a user readable timestamp string.
     *
     * @param theCurrTelemetry         The telemetry snapshot to derive the time.
     * @return                         Returns a user readable timestamp string.
     */
    public static String getFormattedTime(TelemetryRecord theCurrTelemetry) {
        long timestamp = theCurrTelemetry.timeStamp();
        Instant instant = Instant.ofEpochMilli(timestamp);
        return FORMATTER.format(instant);
    }

    /**
     * A method to create a simplified anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @param theCurrTelemetry A telemetry record representing the data needed for a report string.
     * @return Returns a simplified string anomaly report.
     */
    public static String createDescSimple(String theAnomalyType, TelemetryRecord theCurrTelemetry) {

        return "Anomaly Detected! \nDrone ID: " +
                theCurrTelemetry.id() +
                "\nAnomaly Type: " +
                theAnomalyType +
                "\nTime Stamp: " +
                getFormattedTime(theCurrTelemetry) + "\n";
    }

    /**
     * A method to create a detailed anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @param theCurrTelemetry A telemetry record representing the data needed for a report string.
     * @return Returns a detailed string anomaly report.
     */
    public static String createDescDetailed(String theAnomalyType, TelemetryRecord theCurrTelemetry,
                                            TelemetryRecord thePrevTelemetry) {

        return "\nDrone number " + theCurrTelemetry.id() +
                " has experienced an anomaly at time: " + getFormattedTime(theCurrTelemetry) +
                "\nDetails:\n" +
                theAnomalyType + " anomaly detected\n" +

                //Current State
                "Current State: \n" +
                "x: " + theCurrTelemetry.latitude() +
                " y: " + theCurrTelemetry.longitude() +
                " z: " + theCurrTelemetry.altitude() + "\n" +
                "Velocity: " + theCurrTelemetry.velocity() + " units/second(cycle)\n" +
                "Orientation: " + theCurrTelemetry.orientation() + "\n" +
                "Battery (%): " + theCurrTelemetry.batteryLevel() +

                //Previous state
                "\nPrevious State: \n" +
                "x: " + thePrevTelemetry.latitude() +
                " y: " + thePrevTelemetry.longitude() +
                " z: " + thePrevTelemetry.altitude() + "\n" +
                "Velocity: " + thePrevTelemetry.velocity() + " units/cycle\n" +
                "Orientation (Deg from North): " + thePrevTelemetry.orientation() + "\n" +
                "Battery (%): " + thePrevTelemetry.batteryLevel() + "\n";
    }

}
