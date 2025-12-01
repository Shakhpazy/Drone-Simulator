package model;

import java.io.*;
import java.util.*;

/**
 * A class to parse and calculate drone data from a CSV log of activity.
 * @author nlevin11
 * @version 11-26
 */
public class BaselineCalculator {
    /**
     * A double to hold the amount of time for a drone to reach velocity.
     */
    private static final double WARMUP_TIME_MS = 30000;

    /**
     * A double to hold the threshold for drone acceleration.
     */
    private static final double ACCELERATION_THRESHOLD = 0.01;

    /**
     * A list to hold all velocity data.
     */
    private final List<Double> velocityReadings;

    /**
     * A list to hold all battery drain data.
     */
    private final List<Double> batteryDrainReadings;

    /**
     * A list to hold all orientation change data.
     */
    private final List<Double> orientationDeltaReadings;

    /**
     * A list to hold all acceleration data.
     */
    private final List<Double> accelerationReadings;

    /**
     * A map to hold all previous battery values;
     */
    private final Map<Integer, Float> prevBatteryReadings;

    /**
     * A map to hold all previous orientation values;
     */
    private final Map<Integer, Float> prevOrientationReadings;

    /**
     * A map to hold all previous orientation values;
     */
    private final Map<Integer, Double> prevVelocityReadings;

    /**
     * A map to hold all previous timestamp values;
     */
    private final Map<Integer, Double> prevTimestampReadings;

    /**
     * A map to hold the first timestamp readings for a drone.
     * Allows calculator to ignore "startup" conditions for steady state monitoring.
     */
    private final Map<Integer, Double> firstTimestampReadings;


    public BaselineCalculator() {
        this.velocityReadings = new ArrayList<>();
        this.batteryDrainReadings = new ArrayList<>();
        this.orientationDeltaReadings = new ArrayList<>();
        this.accelerationReadings = new ArrayList<>();
        this.prevBatteryReadings = new HashMap<>();
        this.prevOrientationReadings = new HashMap<>();
        this.prevVelocityReadings = new HashMap<>();
        this.prevTimestampReadings = new HashMap<>();
        this.firstTimestampReadings = new HashMap<>();
    }

    /**
     * A method to calculate and save drone data.
     * @param inputLog              The string representation of the log filepath.
     * @param outputProperties      The string representation of the output filepath.
     */
    public void calculateAndSaveStats(String inputLog, String outputProperties) {
        try {
            // Process the file
            int lineCount = processLogFile(inputLog);

            if (velocityReadings.isEmpty()) {
                System.err.println("No data read from log file. Cannot calculate stats.");
                return;
            }

            // Calculate values
            double velocityMean = calculateMean(velocityReadings);
            double velocityStandardDev = calculateStandardDev(velocityReadings, velocityMean);

            double batteryDrainMean = calculateMean(batteryDrainReadings);
            double batteryDrainStandardDev = calculateStandardDev(batteryDrainReadings, batteryDrainMean);

            double orientationDeltaMean = calculateMean(orientationDeltaReadings);
            double orientationDeltaStandardDev = calculateStandardDev(orientationDeltaReadings, orientationDeltaMean);

            double accelerationMean = calculateMean(accelerationReadings);
            double accelerationStandardDev = calculateStandardDev(accelerationReadings, accelerationMean);

            saveStatsToProperties(outputProperties, lineCount, velocityMean, velocityStandardDev, batteryDrainMean,
                    batteryDrainStandardDev, orientationDeltaMean, orientationDeltaStandardDev,
                    accelerationMean, accelerationStandardDev);
            System.out.println(lineCount + " data points calculated.");
        } catch (IOException e) {
            System.err.println("Error during baseline calculation: " + e.getMessage());
        }
    }

    /**
     * A method to process and parse the data from a log file.
     * @param filepath          The string representation of the log filepath.
     * @throws IOException      Throws an exception when incoming files are improperly formatted.
     */
    private int processLogFile(String filepath) throws IOException {
        int lineCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            String line;

            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("Log file is empty");
            }

            Map<String, Integer> headerMap = new HashMap<>();
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }

            if (!headerMap.containsKey("id") || !headerMap.containsKey("velocity") ||
                    !headerMap.containsKey("batteryLevel") || !headerMap.containsKey("orientation") ||
                    !headerMap.containsKey("timestamp")) {
                throw new IOException("Log file is missing required headers.");
            }

            int idIndex = headerMap.get("id");
            int velocityIndex = headerMap.get("velocity");
            int batteryIndex = headerMap.get("batteryLevel");
            int orientationIndex = headerMap.get("orientation");
            int timestampIndex = headerMap.get("timestamp");


            while ((line = br.readLine()) != null) {
                lineCount += 1;
                String[] values = line.split(",");
                if(values.length <= Math.max(idIndex, Math.max(velocityIndex, Math.max(batteryIndex,
                        Math.max(orientationIndex, timestampIndex))))) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    int droneID = Integer.parseInt(values[idIndex].trim());
                    double currVelocity = Double.parseDouble(values[velocityIndex].trim());
                    float currBattery = Float.parseFloat(values[batteryIndex].trim());
                    float currOrientation = Float.parseFloat(values[orientationIndex].trim());
                    double currTimestamp = Double.parseDouble(values[timestampIndex].trim());

                    if (!firstTimestampReadings.containsKey(droneID)) {
                        firstTimestampReadings.put(droneID, currTimestamp);
                    }

                    if (prevTimestampReadings.containsKey(droneID) && prevVelocityReadings.containsKey(droneID)) {
                        double prevTimestamp = prevTimestampReadings.get(droneID);
                        double deltaTimeSec = (currTimestamp - prevTimestamp) / 1000;
                        double prevVelocity = prevVelocityReadings.get(droneID);

                        double currAcceleration = Math.abs(prevVelocity - currVelocity) / deltaTimeSec;
                        if (currAcceleration > ACCELERATION_THRESHOLD) accelerationReadings.add(currAcceleration);
                    }

                    double timeSinceStart = currTimestamp - firstTimestampReadings.get(droneID);
                    if (timeSinceStart >= WARMUP_TIME_MS) {
                        velocityReadings.add(currVelocity);

                        if (prevTimestampReadings.containsKey(droneID)) {
                            double prevTimestamp = prevTimestampReadings.get(droneID);

                            double deltaTime = currTimestamp - prevTimestamp;

                            if (prevBatteryReadings.containsKey(droneID)) {
                                float prevBattery = prevBatteryReadings.get(droneID);
                                float drain = (prevBattery - currBattery);
                                double normalizedDrain = drain / deltaTime;
                                batteryDrainReadings.add(normalizedDrain);
                            }

                            if (prevOrientationReadings.containsKey(droneID)) {
                                float prevOrientation = prevOrientationReadings.get(droneID);

                                double diff = Math.abs(currOrientation - prevOrientation);
                                if (diff > 180) {
                                    diff = 360 - diff;
                                }
                                if (diff > 1.0) {
                                    orientationDeltaReadings.add(diff);
                                }
                            }
                        }
                    }
                    prevVelocityReadings.put(droneID, currVelocity);
                    prevTimestampReadings.put(droneID, currTimestamp);
                    prevBatteryReadings.put(droneID, currBattery);
                    prevOrientationReadings.put(droneID, currOrientation);

                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with unparseable number: " + line);
                }
            }

        }
        return lineCount;
    }

    /**
     * A method to save the calculated statistical data to a file.
     * @param filepath          The string representation for the output filepath.
     * @param vMean             The calculated mean velocity of drone behavior.
     * @param vStandardDev      The calculated standard deviation of drone velocity behavior.
     * @param bMean             The calculated mean battery drain rate.
     * @param bStandardDev      The calculated standard deviation of drone battery behavior.
     * @throws IOException      Throws an exception when data cannot be written to the file.
     */
    private void saveStatsToProperties(String filepath, int lineCount, double vMean, double vStandardDev, double bMean,
                                       double bStandardDev, double oMean, double oStandardDev, double aMean,
                                       double aStandardDev) throws IOException {
        Properties props = new Properties();

        props.setProperty("velocity.mean", String.valueOf(vMean));
        props.setProperty("velocity.standardDev", String.valueOf(vStandardDev));
        props.setProperty("batteryDrain.mean", String.valueOf(bMean));
        props.setProperty("batteryDrain.standardDev", String.valueOf(bStandardDev));
        props.setProperty("orientationDelta.mean", String.valueOf(oMean));
        props.setProperty("orientationDelta.standardDev", String.valueOf(oStandardDev));
        props.setProperty("acceleration.mean", String.valueOf(aMean));
        props.setProperty("acceleration.standardDev", String.valueOf(aStandardDev));

        try (FileWriter writer = new FileWriter(filepath)) {
            props.store(writer, "Drone Anomaly Baseline Statistics\nGenerated from " +
                    lineCount + " data points.");
            System.out.println("Successfully saved baseline stats to " + filepath);
        }
    }

    /**
     * A method to calculate the mean of the given data.
     * @param data      The list of data to be calculated on.
     * @return          Returns a double representing the mean of the given data.
     */
    private double calculateMean(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;

        double sum = 0.0;
        for (double d : data) {
            sum += d;
        }
        return sum/data.size();
    }

    /**
     * A method to calculate the standard deviation of the given data.
     * @param data      The list of data to be calculated on.
     * @param mean      The mean datapoint to be calculated against.
     * @return          Returns a double representing the standard deviation of the given data.
     */
    private double calculateStandardDev(List<Double> data, double mean) {
        if (data == null || data.size() < 2) return 0.0;

        double sumOfSquares = 0.0;
        for (double d : data) {
            sumOfSquares += (d - mean) * (d - mean);
        }

        return Math.sqrt(sumOfSquares / (data.size() - 1));
    }
}
