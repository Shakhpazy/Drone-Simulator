package model;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to detect anomalies with drone behavior.
 * @author nlevin11
 * @version 11-6
 */
public class AnomalyDetector {

    private static final double MAX_Z_SCORE = 3.0;
    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the most recent telemetry data.
     */
    private HashMap<String, Object> myCurrTelemetry;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the telemetry data of a drone prior to the current cycle.
     */
    private HashMap<String, Object> myPrevTelemetry;

    /**
     * A float to represent the x-axis size of the drone flight area.
     */
    private static final float LATITUDE_MAX = 90;

    /**
     * A float to represent the y-axis size of the drone flight area.
     */
    private static final float LONGITUDE_MAX = 180;

    /**
     * A float to represent the z-axis size of the drone flight area.
     */
    private static final float ALTITUDE_MAX = 1000;

    /**
     * A float to represent the maximum orthogonal velocity of a drone.
     */
    private static final float ORTHOGONAL_VELOCITY_MAX = 10F;

    private static double VELOCITY_MEAN_BASELINE;

    private static double VELOCITY_STANDARD_DEV_BASELINE;

    private static double BATTERY_DRAIN_MEAN_BASELINE;

    private static double BATTERY_DRAIN_STANDARD_DEV_BASELINE;

    private static double ORIENTATION_DELTA_MEAN_BASELINE;

    private static double ORIENTATION_DELTA_STANDARD_DEV_BASELINE;

    private static final String MY_Z_SCORE_LOG_PATH = "dataLogs/BaselineLog.properties";


    public AnomalyDetector() {
        loadBaseline();
    }

    private void loadBaseline() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(MY_Z_SCORE_LOG_PATH)) {
            props.load(fis);

            VELOCITY_MEAN_BASELINE = Double.parseDouble(props.getProperty("velocity.mean"));
            VELOCITY_STANDARD_DEV_BASELINE = Double.parseDouble(props.getProperty("velocity.standardDev"));

            BATTERY_DRAIN_MEAN_BASELINE = Double.parseDouble(props.getProperty("batteryDrain.mean"));
            BATTERY_DRAIN_STANDARD_DEV_BASELINE = Double.parseDouble(props.getProperty("batteryDrain.standardDev"));

            ORIENTATION_DELTA_MEAN_BASELINE = Double.parseDouble(props.getProperty("orientationDelta.mean"));
            ORIENTATION_DELTA_STANDARD_DEV_BASELINE = Double.parseDouble(props.
                    getProperty("orientationDelta.standardDev"));

            System.out.println("Baseline Data Gathered");

        } catch (IOException e) {
            System.err.println("Error loading the baseline properties file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing baseline value: " + e.getMessage());
        }

    }

    /**
     * A method to detect anomalies between two telemetry objects.
     *
     * @param theCurrTelemetry A telemetry representing the current drone state.
     * @param thePrevTelemetry A telemetry representing the previous drone state.
     * @return Returns the AnomalyReport object when created, null if not created.
     */
    public AnomalyReport detect(HashMap<String, Object> theCurrTelemetry, HashMap<String, Object> thePrevTelemetry) {
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;
        StringBuilder sb = new StringBuilder();
        /* Check for bounds anomaly */
        String errorMessage = positionAnomaly();
        if (!errorMessage.equals("N/A")) {
            sb.append(errorMessage);
        }

        /* Check for dead battery anomaly */
        errorMessage = powerAnomaly();
        if (!errorMessage.equals("N/A")) {
            sb.append(errorMessage);
        }

        AnomalyEnum statResult = statisticalDetect(theCurrTelemetry, thePrevTelemetry);
        if (statResult != null) {
            sb.append(statResult);
        }

        if (!sb.isEmpty()) {
            return createAnomalyReport(sb.toString());
        }
        return null;
    }

    public AnomalyEnum statisticalDetect(HashMap<String, Object> theCurrTelemetry, HashMap<String,
            Object> thePrevTelemetry) {
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;
        long currTime = ((Number) myCurrTelemetry.get("timeStamp")).longValue();
        long prevTime = ((Number) myPrevTelemetry.get("timeStamp")).longValue();
        double deltaTime = (double) (currTime - prevTime);

        double currVelocity = ((Number) myCurrTelemetry.get("velocity")).doubleValue();
        double velocityZScore = (currVelocity - VELOCITY_MEAN_BASELINE)
                / VELOCITY_STANDARD_DEV_BASELINE;
        if (velocityZScore >= MAX_Z_SCORE) {
            return AnomalyEnum.SPOOFING;
        }

        double currBattery = ((Number) myCurrTelemetry.get("batteryLevel")).doubleValue();
        double prevBattery = ((Number) myPrevTelemetry.get("batteryLevel")).doubleValue();

        double batteryNormDelta = ((currBattery - prevBattery) / deltaTime);
        double batteryZScore = (batteryNormDelta - BATTERY_DRAIN_MEAN_BASELINE) / BATTERY_DRAIN_STANDARD_DEV_BASELINE;
        if (batteryZScore >= MAX_Z_SCORE) {
            return AnomalyEnum.BATTERY_DRAIN;
        }

        double currOrientation = ((Number) myCurrTelemetry.get("orientation")).doubleValue();
        double prevOrientation = ((Number) myPrevTelemetry.get("orientation")).doubleValue();

        double orientationNormDelta = (Math.abs(currOrientation - prevOrientation) / deltaTime);
        double orientationZScore = (orientationNormDelta - ORIENTATION_DELTA_MEAN_BASELINE)
                / ORIENTATION_DELTA_STANDARD_DEV_BASELINE;
        if (orientationZScore >= 3) {
            return AnomalyEnum.SPOOFING;
        }
        return null;
    }


    /**
     * A private method to hold positional anomaly detection logic.
     *
     * @return Returns a String anomaly description.
     */
    private String positionAnomaly() {
        StringBuilder ret = new StringBuilder();

        float currLatitude = (float) myCurrTelemetry.get("latitude");
        float currLongitude = (float) myCurrTelemetry.get("longitude");
        float currAltitude = (float) myCurrTelemetry.get("altitude");

        // Check in bounds allows for velocity check to diagnose anomaly cause
        if (currLatitude < LATITUDE_MAX * -1 || currLatitude > LATITUDE_MAX ||
                currLongitude < LONGITUDE_MAX * -1 || currLongitude > LONGITUDE_MAX ||
                currAltitude < 0 || currAltitude > ALTITUDE_MAX) {
            ret.append(AnomalyEnum.OUT_OF_BOUNDS);
        }

        float prevAltitude = (float) myPrevTelemetry.get("altitude");

        // Check intended velocity
        if (Math.abs(currAltitude - prevAltitude) > ORTHOGONAL_VELOCITY_MAX) {
            if (!ret.isEmpty()) ret.append(" Due to ");
            ret.append(AnomalyEnum.ALTITUDE);
            return ret.toString();
        }

        return ret.toString();
    }

    /**
     * A private method to hold the power anomaly detection logic.
     *
     * @return Returns a boolean representing whether the battery level is 0.
     */
    private String powerAnomaly() {
        float currBatteryLevel = (float) myCurrTelemetry.get("batteryLevel");
        if (currBatteryLevel <= 0) {
            return AnomalyEnum.BATTERY_FAIL.toString();
        }
        return "N/A";
    }

    /**
     * A private method to create an AnomalyReport object
     *
     * @param theAnomalyType A string classification of the anomaly to be reported on.
     * @return Returns an anomaly report with the relevant information.
     */
    private AnomalyReport createAnomalyReport(String theAnomalyType) {
        String simpleReport = ReportFormatter.createDescSimple(theAnomalyType, myCurrTelemetry);
        String detailedReport = ReportFormatter.createDescDetailed(theAnomalyType, myCurrTelemetry, myPrevTelemetry);

        UUID myAnomalyID = UUID.randomUUID();

        return new AnomalyReport(
                myAnomalyID,
                (Long) myCurrTelemetry.get("timeStamp"),
                theAnomalyType,
                (Integer) myCurrTelemetry.get("id"),
                simpleReport,
                detailedReport);
    }
}