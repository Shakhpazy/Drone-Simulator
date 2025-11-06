package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class ReportFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    public static String getFormattedTime(ConcurrentHashMap<String, Object> telemetry) {
        long timestamp = (Long) telemetry.get("timestamp");
        Instant instant = Instant.ofEpochMilli(timestamp);
        return FORMATTER.format(instant);
    }
    /**
     * A private method to create a simplified anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @return Returns a simplified string anomaly report.
     */
    public static String createDescSimple(String theAnomalyType, ConcurrentHashMap<String, Object> theCurrTelemetry) {

        return "Anomaly Detected! \nDrone ID: " +
                theCurrTelemetry.get("id") +
                "\nAnomaly Type: " +
                theAnomalyType +
                "\nTime Stamp: " +
                getFormattedTime(theCurrTelemetry);
    }

    /**
     * A private method to create a detailed anomaly report string.
     *
     * @param theAnomalyType A string representing the type of anomaly being reported.
     * @return Returns a detailed string anomaly report.
     */
    public static String createDescDetailed(String theAnomalyType, ConcurrentHashMap<String, Object> theCurrTelemetry, ConcurrentHashMap<String, Object> thePrevTelemetry) {

        return "Drone Number: " + theCurrTelemetry.get("id") +
                "has experienced an anomaly at time: " + getFormattedTime(theCurrTelemetry) +
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
                "Battery (%): " + thePrevTelemetry.get("batteryLevel");
    }

}
