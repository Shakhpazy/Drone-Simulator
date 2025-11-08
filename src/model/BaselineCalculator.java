package model;

import java.io.*;
import java.util.*;

/**
 * A class to parse and calculate drone data from a CSV log of activity.
 * @author nlevin11
 * @version 11-6
 */
public class BaselineCalculator {
    /**
     * A list to hold all velocity data.
     */
    private final List<Double> velocityReadings;

    /**
     * A list to hold all battery drain data.
     */
    private final List<Double> batteryDrainReadings;

    /**
     * A list to hold all battery drain data.
     */
    private final List<Double> orientationDeltaReadings;

    /**
     * A map to hold all previous battery values;
     */
    private final Map<Integer, Float> prevBatteryLife;

    /**
     * A map to hold all previous orientation values;
     */
    private final Map<Integer, Float> prevOrientationReadings;


    public BaselineCalculator() {
        this.velocityReadings = new ArrayList<>();
        this.batteryDrainReadings = new ArrayList<>();
        this.orientationDeltaReadings = new ArrayList<>();
        this.prevBatteryLife = new HashMap<>();
        this.prevOrientationReadings = new HashMap<>();
    }

    /**
     * A method to calculate and save drone data.
     * @param inputLog              The string representation of the log filepath.
     * @param outputProperties      The string representation of the output filepath.
     */
    public void calculateAndSaveStats(String inputLog, String outputProperties) {
        try {
            processLogFile(inputLog);

            if (velocityReadings.isEmpty()) {
                System.err.println("No data read from log file. Cannot calculate stats.");
                return;
            }

            double velocityMean = calculateMean(velocityReadings);
            double velocityStandardDev = calculateStandardDev(velocityReadings, velocityMean);

            double batteryDrainMean = calculateMean(batteryDrainReadings);
            double batteryDrainStandardDev = calculateStandardDev(batteryDrainReadings, velocityMean);

            double orientationDeltaMean = calculateMean(orientationDeltaReadings);
            double orientationDeltaStandardDev = calculateStandardDev(orientationDeltaReadings, velocityMean);

            saveStatsToProperties(outputProperties, velocityMean, velocityStandardDev, batteryDrainMean,
                    batteryDrainStandardDev, orientationDeltaMean, orientationDeltaStandardDev);

        } catch (IOException e) {
            System.err.println("Error during baseline calculation: " + e.getMessage());
        }
    }

    /**
     * A method to process and parse the data from a log file.
     * @param filepath          The string representation of the log filepath.
     * @throws IOException      Throws an exception when incoming files are improperly formatted.
     */
    public void processLogFile(String filepath) throws IOException {
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
                    !headerMap.containsKey("batteryLevel") || !headerMap.containsKey("orientation")) {
                throw new IOException("Log file is missing required headers.");
            }

            int idIndex = headerMap.get("id");
            int velocityIndex = headerMap.get("velocity");
            int batteryIndex = headerMap.get("batteryLevel");
            int orientationIndex = headerMap.get("orientation");

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(values.length <= Math.max(idIndex, Math.max(velocityIndex, batteryIndex))) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    int droneID = Integer.parseInt(values[idIndex].trim());
                    double currVelocity = Double.parseDouble(values[velocityIndex].trim());
                    float currBattery = Float.parseFloat(values[batteryIndex].trim());
                    float currOrientation = Float.parseFloat(values[orientationIndex].trim());

                    velocityReadings.add(currVelocity);

                    if (prevBatteryLife.containsKey(droneID)) {
                        float prevBattery = prevBatteryLife.get(droneID);
                        float drain = prevBattery - currBattery;
                        batteryDrainReadings.add((double) drain);
                    }

                    if (prevOrientationReadings.containsKey(droneID)) {
                        double prevOrientation = prevOrientationReadings.get(droneID);

                        double diff = Math.abs(currOrientation - prevOrientation);
                        if (diff > 180) {
                            diff = 360 - diff;
                        }
                        orientationDeltaReadings.add(diff);
                    }

                    prevBatteryLife.put(droneID, currBattery);
                    prevOrientationReadings.put(droneID, currOrientation);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with unparseable number: " + line);
                }
            }
        }
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
    private void saveStatsToProperties(String filepath, double vMean, double vStandardDev, double bMean,
                                       double bStandardDev, double oMean, double oStandardDev) throws IOException {
        Properties props = new Properties();

        props.setProperty("velocity.mean", String.valueOf(vMean));
        props.setProperty("velocity.standardDev", String.valueOf(vStandardDev));
        props.setProperty("batteryDrain.mean", String.valueOf(bMean));
        props.setProperty("batteryDrain.standardDev", String.valueOf(bStandardDev));

        try (FileWriter writer = new FileWriter(filepath)) {
            props.store(writer, "Drone Anomaly Baseline Statistics\nGenerated from log data.");
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
