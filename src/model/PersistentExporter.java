package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A persistent log of drone data for use in statistical analysis and machine learning.
 * @author nlevin11
 * @version 11-6
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
    public void logTelemetryData(HashMap<String, Object> theTelemetryData, List<String> theHeader) {
        if (persistentTelemetryWriter == null) {
            System.err.println("Error: Telemetry log is not open.");
        }

        try {
            List<String> droneData = new ArrayList<>();
            for (String header : theHeader) {
                droneData.add(String.valueOf(theTelemetryData.get(header)));
            }

            String line = String.join(",", droneData);
            persistentTelemetryWriter.write(line);
            persistentTelemetryWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error writing telemetry data: " + e.getMessage());
        }
    }

}
