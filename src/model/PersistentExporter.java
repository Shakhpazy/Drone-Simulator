package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A persistent log of drone data for use in statistical analysis and machine learning.
 * @author nlevin11
 * @version 11-24
 */
public class PersistentExporter {
    /**
     * A Buffered writer to hold the data of the drone log.
     */
    private BufferedWriter persistentTelemetryWriter;

    /**
     * A method to initialize a persistent log of drone telemetry.
     * @param filepath      A string to represent the filepath of the log output.
     * @param theHeader     A list of strings to represent the headers of the data being written.
     */
    public void startTelemetryLog(String filepath, List<String> theHeader) {
        try {
            persistentTelemetryWriter = new BufferedWriter(new FileWriter(filepath));
            String headerLine = String.join(",", theHeader);
            persistentTelemetryWriter.write(headerLine);
            persistentTelemetryWriter.newLine();

        } catch (IOException e) {
            System.err.println("Error opening telemetry log file: " + e.getMessage());
        }
    }

    /**
     * A method to close the current log of drone telemetry.
     */
    public void closeTelemetryLog() {
        try {
            if (persistentTelemetryWriter != null) {
                persistentTelemetryWriter.flush();
                persistentTelemetryWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing telemetry log file: " + e.getMessage());
        }
    }

    /**
     * A method to log a snapshot of a drone's telemetry data.
     * @param theTelemetryData      A HashMap representation of the telemetry data.
     * @param theHeader             A list of strings to represent the headers of the data being written.
     */
    public void logTelemetryData(TelemetryRecord theTelemetryData, List<String> theHeader) {
        if (persistentTelemetryWriter == null) {
            System.err.println("Error: Telemetry log is not open.");
            return;
        }

        try {
            List<String> droneData = new ArrayList<>();
            for (String header : theHeader) {
                String value = switch (header) {
                    case "id" -> String.valueOf(theTelemetryData.id());
                    case "longitude" -> String.valueOf(theTelemetryData.longitude());
                    case "latitude" -> String.valueOf(theTelemetryData.latitude());
                    case "altitude" -> String.valueOf(theTelemetryData.altitude());
                    case "velocity" -> String.valueOf(theTelemetryData.velocity());
                    case "batteryLevel" -> String.valueOf(theTelemetryData.batteryLevel());
                    case "orientation" -> String.valueOf(theTelemetryData.orientation());
                    case "timestamp" -> String.valueOf(theTelemetryData.timeStamp());
                    default -> throw new IllegalStateException("Unexpected value: " + header);
                };
                droneData.add(value);
            }

            String line = String.join(",", droneData);
            persistentTelemetryWriter.write(line);
            persistentTelemetryWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error writing telemetry data: " + e.getMessage());
        }
    }

}
