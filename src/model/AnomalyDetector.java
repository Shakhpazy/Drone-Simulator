package model;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to detect anomalies with drone behavior.
 * @author nlevin11
 * @version 11-6
 */
public class AnomalyDetector {

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the most recent telemetry data.
     */
    private ConcurrentHashMap<String, Object> myCurrTelemetry;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the telemetry data of a drone prior to the current cycle.
     */
    private ConcurrentHashMap<String, Object> myPrevTelemetry;

    /**
     * A float to represent the x-axis size of the drone flight area.
     */
    private static final float LATITUDE_MAX = 930;

    /**
     * A float to represent the y-axis size of the drone flight area.
     */
    private static final float LONGITUDE_MAX = 530;

    /**
     * A float to represent the z-axis size of the drone flight area.
     */
    private static final float ALTITUDE_MAX = 700;

    /**
     * A float to represent the maximum orthogonal velocity of a drone.
     */
    private static final float ORTHOGONAL_VELOCITY_MAX = 10;

    /**
     * A float to represent the maximum battery drain of a drone in a cycle.
     */
    private static final int BATTERY_DRAIN_RATE_MAX = 100;

    /**
     * A method to detect anomalies between two telemetry objects.
     *
     * @param theCurrTelemetry A telemetry representing the current drone state.
     * @param thePrevTelemetry A telemetry representing the previous drone state.
     * @return Returns the AnomalyReport object when created, null if not created.
     */
    public AnomalyReport detect(ConcurrentHashMap<String, Object> theCurrTelemetry, ConcurrentHashMap<String, Object> thePrevTelemetry) {
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;

        /* Check for spoofing or positional anomaly */
        String errorMessage = positionAnomaly();
        if (!errorMessage.equals("N/A")) {
            return createAnomalyReport(errorMessage);
        }

        /* Check for battery anomaly */
        errorMessage = powerAnomaly();
        if (errorMessage != null) {
            return createAnomalyReport(errorMessage);
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
        if (currLatitude < 0 || currLatitude > LATITUDE_MAX ||
                currLongitude < 0 || currLongitude > LONGITUDE_MAX ||
                currAltitude < 0 || currAltitude > ALTITUDE_MAX) {
            ret.append(AnomalyEnum.OUT_OF_BOUNDS);
        }

        // Possible Future Implementations both require "normal" flight data collection

        // Change over time and Z-score for pattern recognition
        // telemetryValueChange = curr - prev
        // timeElapsed = curr - prev THIS SHOULD BE CONSTANT
        // rateOfChange = telemetryValueChange / timeElapsed
        // if (Math.abs(rateOfChange) > MAX

        // Statistical Baselines mean and standard deviation
        // BatteryDrainRate VelocityFluctuation
        // zScore = (dataPoint - mean) / standard deviation
        // if (Math.abs(z_score) > 3)

        //----------------Or----------------

        // Future implementations could also use machine learning
        // Outlier detection with Weka DBSCAN or Isolation Forest

        float prevLatitude = (float) myPrevTelemetry.get("latitude");
        float prevLongitude = (float) myPrevTelemetry.get("longitude");
        float prevAltitude = (float) myPrevTelemetry.get("altitude");

        // Check intended velocity
        if (Math.abs(currAltitude - prevAltitude) > ORTHOGONAL_VELOCITY_MAX) {
            if (!ret.isEmpty()) ret.append(" Due to ");
            ret.append(AnomalyEnum.ALTITUDE);
            return ret.toString();
        } else if (Math.abs(currLongitude - prevLongitude) >
                ORTHOGONAL_VELOCITY_MAX ||
                Math.abs(currLatitude - prevLatitude) > ORTHOGONAL_VELOCITY_MAX) {
            if (!ret.isEmpty()) ret.append(" Due to ");
            ret.append(AnomalyEnum.SPOOFING);
            return ret.toString();
        }

        if (ret.isEmpty()) ret.append("N/A");

        return ret.toString();
    }

    /**
     * A private method to hold the power anomaly detection logic.
     *
     * @return Returns a boolean representing whether the battery level is 0.
     */
    private String powerAnomaly() {
        int currBatteryLevel = (int) myCurrTelemetry.get("batteryLevel");
        if ((int) myPrevTelemetry.get("batteryLevel") - currBatteryLevel > BATTERY_DRAIN_RATE_MAX) {
            return AnomalyEnum.BATTERY_DRAIN.toString();
        } else if (currBatteryLevel <= 0) {
            return AnomalyEnum.BATTERY_FAIL.toString();
        }
        return null;
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