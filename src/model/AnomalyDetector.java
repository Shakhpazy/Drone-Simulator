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
    private static double ORIENTATION_STEADY_MAX_DELTA;

    /**
     * A double to represent the standard deviation of the expected orientation change of a drone.
     */
    private static double ORIENTATION_TURN_MIN_DELTA;

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

            ORIENTATION_STEADY_MAX_DELTA = Double.parseDouble(props.getProperty("orientationSteady.max"));
            ORIENTATION_TURN_MIN_DELTA = Double.parseDouble(props.getProperty("orientationTurn.min"));
            ORIENTATION_STEADY_MAX_DELTA *= 1.1;

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
        StringBuilder sb = new StringBuilder();

        // Position + Velocity check
        AnomalyEnum error = positionAnomaly(theCurrTelemetry, thePrevTelemetry);
        boolean altitudeErr = false;
        if (error != null) {
            sb.append(error);
            if (error.equals(AnomalyEnum.ALTITUDE))  altitudeErr = true;
        }

        // Battery check
        error = powerAnomaly(theCurrTelemetry);
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
            return createAnomalyReport(sb.toString(), theCurrTelemetry, thePrevTelemetry);
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
        long currTime = theCurrTelemetry.timeStamp();
        long prevTime = thePrevTelemetry.timeStamp();
        double deltaTime = (double) (currTime - prevTime) / 1000;

        if (firstTimestamp == -1) firstTimestamp = currTime;

        if (firstTimestamp == currTime || deltaTime == 0.0) return null;

        // Velocity + Acceleration check
        // Gather Data
        double currVelocity = theCurrTelemetry.velocity();
        double prevVelocity = thePrevTelemetry.velocity();
        double currAcceleration = Math.abs(prevVelocity - currVelocity) / deltaTime;

        // Calc Z-Scores
        double effectiveStandardDev = Math.max(ACCELERATION_STANDARD_DEV_BASELINE, 0.05);
        double accelerationZScore = (currAcceleration - ACCELERATION_MEAN_BASELINE)
                / effectiveStandardDev;

        boolean isAccel = currAcceleration > ACCELERATION_THRESHOLD;

        double velocityZScore = (currVelocity - VELOCITY_MEAN_BASELINE) / VELOCITY_STANDARD_DEV_BASELINE;
        boolean velFlag = Math.abs(velocityZScore) > MAX_Z_SCORE;

        if (currTime > firstTimestamp + 1000) {
            if (velFlag && !isAccel) {
                return AnomalyEnum.OFF_COURSE;
            } else if (accelerationZScore > MAX_Z_SCORE){
                return AnomalyEnum.ACCELERATION;
            }
        }

        // Battery check
        double currBattery = theCurrTelemetry.batteryLevel();
        double prevBattery = thePrevTelemetry.batteryLevel();

        double batteryNormDelta = ((prevBattery - currBattery) / deltaTime);
        double batteryZScore = (batteryNormDelta - BATTERY_DRAIN_MEAN_BASELINE) / BATTERY_DRAIN_STANDARD_DEV_BASELINE;
        if (batteryZScore >= MAX_Z_SCORE) {
            return AnomalyEnum.BATTERY_DRAIN;
        }

        // Orientation check
        double currOrientation = theCurrTelemetry.orientation();
        double prevOrientation = thePrevTelemetry.orientation();

        double orientationDelta = Math.abs(currOrientation - prevOrientation);
        if (orientationDelta > 180) orientationDelta = 360 - orientationDelta;
        if (orientationDelta < ORIENTATION_TURN_MIN_DELTA && orientationDelta > ORIENTATION_STEADY_MAX_DELTA) {
            return AnomalyEnum.OFF_COURSE;
        }
        return null;
    }

    /**
     * A private method to hold positional anomaly detection logic.
     *
     * @return Returns an AnomalyEnum representing the anomaly found.
     */
    private AnomalyEnum positionAnomaly(TelemetryRecord theCurrentTelemetry, TelemetryRecord thePrevTelemetry) {
        float currLatitude = theCurrentTelemetry.latitude();
        float currLongitude = theCurrentTelemetry.longitude();
        float currAltitude = theCurrentTelemetry.altitude();

        // Check in bounds
        if (currLatitude < LATITUDE_MAX * -1 || currLatitude > LATITUDE_MAX ||
                currLongitude < LONGITUDE_MAX * -1 || currLongitude > LONGITUDE_MAX || currAltitude > ALTITUDE_MAX) {
            return AnomalyEnum.OUT_OF_BOUNDS;
        }
        if (currAltitude < 0 ) {
            return AnomalyEnum.HIT_GROUND;
        }

        // Check z-axis velocity
        float prevAltitude = thePrevTelemetry.altitude();
        double displacement = Math.sqrt(Math.pow(thePrevTelemetry.longitude() - theCurrentTelemetry.longitude(), 2)
                + Math.pow(thePrevTelemetry.latitude() - theCurrentTelemetry.latitude(), 2)
                + Math.pow(thePrevTelemetry.altitude() - theCurrentTelemetry.altitude(), 2));

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
    private AnomalyEnum powerAnomaly(TelemetryRecord theCurrentTelemetry) {
        float currBatteryLevel = theCurrentTelemetry.batteryLevel();
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
    private AnomalyReport createAnomalyReport(String theAnomalyType, TelemetryRecord theCurrTelemetry, TelemetryRecord thePrevTelemetry) {

        String simpleReport = ReportFormatter.createDescSimple(theAnomalyType, theCurrTelemetry);
        String detailedReport = ReportFormatter.createDescDetailed(theAnomalyType, theCurrTelemetry, thePrevTelemetry);

        UUID myAnomalyID = UUID.randomUUID();

        return new AnomalyReport(
                myAnomalyID,
                theCurrTelemetry.timeStamp(),
                theAnomalyType,
                theCurrTelemetry.id(),
                simpleReport,
                detailedReport);
    }
}