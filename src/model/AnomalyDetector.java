package model;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * A class to detect anomalies with drone behavior.
 * @author nlevin11
 * @version 12-2
 */
public class AnomalyDetector {

    /**
     * A double to hold the first timestamp given to the AnomalyDetector.
     */
    private double firstTimestamp = -1;

    /**
     * A double representing the maximum deviation from normal behavior a drone can express before detection.
     */
    private static final double MAX_Z_SCORE = 3.0;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the most recent telemetry data.
     */
    private TelemetryRecord myCurrTelemetry;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the telemetry data of a drone prior to the current cycle.
     */
    private TelemetryRecord myPrevTelemetry;

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

    /**
     * A double to hold the threshold for drone acceleration.
     */
    private static final double ACCELERATION_THRESHOLD = 0.01;

    /**
     * A double to represent the mean expected velocity of a drone.
     */
    private static double VELOCITY_MEAN_BASELINE;

    /**
     * A double to represent the standard deviation of expected velocity of a drone.
     */
    private static double VELOCITY_STANDARD_DEV_BASELINE;

    /**
     * A double to represent the mean expected battery drain of a drone.
     */
    private static double BATTERY_DRAIN_MEAN_BASELINE;

    /**
     * A double to represent the standard deviation of the expected battery drain of a drone.
     */
    private static double BATTERY_DRAIN_STANDARD_DEV_BASELINE;

    /**
     * A double to represent the mean expected orientation change of a drone.
     */
    private static double ORIENTATION_DELTA_MEAN_BASELINE;

    /**
     * A double to represent the standard deviation of the expected orientation change of a drone.
     */
    private static double ORIENTATION_DELTA_STANDARD_DEV_BASELINE;

    /**
     * A double to represent the mean expected acceleration of a drone.
     */
    private static double ACCELERATION_MEAN_BASELINE;

    /**
     * A double to represent the standard deviation of the expected acceleration of a drone.
     */
    private static double ACCELERATION_STANDARD_DEV_BASELINE;

    /**
     * A string representing the baseline data filepath.
     */
    private static final String MY_Z_SCORE_LOG_PATH = "dataLogs/BaselineLog.properties";

    public AnomalyDetector() {
        loadBaseline();
    }

    /**
     * A private helper method to parse stored drone telemetry behavior.
     */
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

            ACCELERATION_MEAN_BASELINE = Double.parseDouble(props.getProperty("acceleration.mean"));
            ACCELERATION_STANDARD_DEV_BASELINE = Double.parseDouble(props.
                    getProperty("acceleration.standardDev"));

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
    public AnomalyReport detect(TelemetryRecord thePrevTelemetry, TelemetryRecord theCurrTelemetry) {
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;
        StringBuilder sb = new StringBuilder();

        // Position + Velocity check
        AnomalyEnum error = positionAnomaly();
        boolean altitudeErr = false;
        if (error != null) {
            sb.append(error);
            if (error.equals(AnomalyEnum.ALTITUDE))  altitudeErr = true;
        }

        // Battery check
        error = powerAnomaly();
        if (error != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(error);
        }

        // Statistical Detection
        AnomalyEnum statResult = statisticalDetect(theCurrTelemetry, thePrevTelemetry);
        if (statResult != null && !altitudeErr) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(statResult);
        }

        if (!sb.isEmpty()) {
            return createAnomalyReport(sb.toString());
        }
        return null;
    }

    /**
     * A method to detect anomalous drone behavior via statistical analysis.
     * @param theCurrTelemetry      The current telemetry data.
     * @param thePrevTelemetry      The previous telemetry data.
     * @return                      Returns an AnomalyEnum representing the anomaly found.
     */
    private AnomalyEnum statisticalDetect(TelemetryRecord theCurrTelemetry, TelemetryRecord thePrevTelemetry) {
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;
        long currTime = myCurrTelemetry.timeStamp();
        long prevTime = myPrevTelemetry.timeStamp();
        double deltaTime = (double) (currTime - prevTime) / 1000;

        if (firstTimestamp == -1) firstTimestamp = currTime;

        // Velocity + Acceleration check
        // Gather Data
        double currVelocity = myCurrTelemetry.velocity();
        double prevVelocity = myPrevTelemetry.velocity();
        double currAcceleration = Math.abs(prevVelocity - currVelocity) / deltaTime;

        // Calc Z-Scores
        double effectiveStandardDev = Math.max(ACCELERATION_STANDARD_DEV_BASELINE, 0.05);
        double accelerationZScore = (currAcceleration - ACCELERATION_MEAN_BASELINE)
                / effectiveStandardDev;

        boolean accelFlag = Math.abs(accelerationZScore) > MAX_Z_SCORE
                && currAcceleration > ACCELERATION_THRESHOLD && currTime != firstTimestamp;

        double velocityZScore = (currVelocity - VELOCITY_MEAN_BASELINE) / VELOCITY_STANDARD_DEV_BASELINE;
        boolean velFlag = Math.abs(velocityZScore) > MAX_Z_SCORE;


        if (velFlag && accelFlag) {
            return AnomalyEnum.ACCELERATION;
        }

        // Battery check
        double currBattery = myCurrTelemetry.batteryLevel();
        double prevBattery = myPrevTelemetry.batteryLevel();

        double batteryNormDelta = ((prevBattery - currBattery) / deltaTime);
        double batteryZScore = (batteryNormDelta - BATTERY_DRAIN_MEAN_BASELINE) / BATTERY_DRAIN_STANDARD_DEV_BASELINE;
        if (batteryZScore >= MAX_Z_SCORE) {
            return AnomalyEnum.BATTERY_DRAIN;
        }

        // Orientation check
        double currOrientation = myCurrTelemetry.orientation();
        double prevOrientation = myPrevTelemetry.orientation();

        double orientationDelta = Math.abs(currOrientation - prevOrientation);
        if (orientationDelta > 180) orientationDelta = 360 - orientationDelta;
        double orientationZScore = (orientationDelta - ORIENTATION_DELTA_MEAN_BASELINE)
                / ORIENTATION_DELTA_STANDARD_DEV_BASELINE;
        if (orientationZScore >= 3) {
            return AnomalyEnum.OFF_COURSE;
        }
        return null;
    }

    /**
     * A private method to hold positional anomaly detection logic.
     *
     * @return Returns an AnomalyEnum representing the anomaly found.
     */
    private AnomalyEnum positionAnomaly() {
        float currLatitude = myCurrTelemetry.latitude();
        float currLongitude = myCurrTelemetry.longitude();
        float currAltitude = myCurrTelemetry.altitude();

        // Check in bounds
        if (currLatitude < LATITUDE_MAX * -1 || currLatitude > LATITUDE_MAX ||
                currLongitude < LONGITUDE_MAX * -1 || currLongitude > LONGITUDE_MAX || currAltitude > ALTITUDE_MAX) {
            return AnomalyEnum.OUT_OF_BOUNDS;
        }
        if (currAltitude < 0 ) {
            return AnomalyEnum.HIT_GROUND;
        }

        // Check z-axis velocity
        float prevAltitude = myPrevTelemetry.altitude();
        double displacement = Math.sqrt(Math.pow(myPrevTelemetry.longitude() - myCurrTelemetry.longitude(), 2)
                + Math.pow(myPrevTelemetry.latitude() - myCurrTelemetry.latitude(), 2)
                + Math.pow(myPrevTelemetry.altitude() - myCurrTelemetry.altitude(), 2));

        if (Math.abs(currAltitude - prevAltitude) > ORTHOGONAL_VELOCITY_MAX) {
            return AnomalyEnum.ALTITUDE;
        } else if (displacement > ORTHOGONAL_VELOCITY_MAX) {
            return AnomalyEnum.SPOOFING;
        }

        return null;
    }

    /**
     * A private method to hold the power anomaly detection logic.
     *
     * @return Returns an AnomalyEnum representing whether the battery level is 0.
     */
    private AnomalyEnum powerAnomaly() {
        float currBatteryLevel = myCurrTelemetry.batteryLevel();
        if (currBatteryLevel <= 0.0F) {
            return AnomalyEnum.BATTERY_FAIL;
        } else if (currBatteryLevel <= 15){
            return AnomalyEnum.BATTERY_WARNING;
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
                myCurrTelemetry.timeStamp(),
                theAnomalyType,
                myCurrTelemetry.id(),
                simpleReport,
                detailedReport);
    }
}