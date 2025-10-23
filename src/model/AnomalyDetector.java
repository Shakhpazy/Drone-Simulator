package model;
import java.util.UUID;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class AnomalyDetector {

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the most recent telemetry data.
     */
    private Telemetry myCurrTelemetry;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the telemetry data of a drone prior to the current cycle.
     */
    private Telemetry myPrevTelemetry;

    /**
     * A float to represent the x-axis size of the drone flight area.
     */
    private final float LATITUDE_MAX;

    /**
     * A float to represent the y-axis size of the drone flight area.
     */
    private final float LONGITUDE_MAX;

    /**
     * A float to represent the z-axis size of the drone flight area.
     */
    private final float ALTITUDE_MAX;

    /**
     * A float to represent the maximum velocity of a drone.
     */
    private final float VELOCITY_MAX;

    /**
     * A float to represent the maximum orthogonal velocity of a drone.
     */
    private final float ORTHOGONAL_VELOCITY_MAX;

    /**
     * A float to represent the maximum battery drain of a drone in a cycle.
     */
    private final int BATTERY_DRAIN_RATE_MAX;

    /**
     * A PropertyChangeSupport object used for notifying GUI of an anomaly found
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * A method to detect anomalies between two telemetry objects.
     * @param theCurrTelemetry         A telemetry representing the current drone state.
     * @param thePrevTelemetry         A telemetry representing the previous drone state.
     * @return                         Returns the AnomalyReport object when created, null if not created.
     */
    public AnomalyReport Detect(Telemetry theCurrTelemetry, Telemetry thePrevTelemetry){
        myCurrTelemetry = theCurrTelemetry;
        myPrevTelemetry = thePrevTelemetry;

        /* Check for spoofing or positional anomaly */
        String errorMessage = PositionAnomaly();
        if (!errorMessage.equals("N/A")){
            AnomalyReport ar = CreateAnomalyReport(errorMessage);
            pcs.firePropertyChange("Anomaly Detected", null, ar);
            return ar;
        }

        /* Check for battery anomaly */
        errorMessage = PowerAnomaly();
        if (errorMessage != null){
            AnomalyReport ar = CreateAnomalyReport(errorMessage);
            pcs.firePropertyChange("Anomaly Detected", null, ar);
            return ar;
        }

        return null;
    }

    /**
     * A private method to hold positional anomaly detection logic.
     * @return  Returns a String anomaly description.
     */
    private String PositionAnomaly(){
        StringBuilder ret = new StringBuilder();

        // Check in bounds allows for velocity check to diagnose anomaly cause
        if (myCurrTelemetry.getLatitude() < 0 || myCurrTelemetry.getLatitude() > LONGITUDE_MAX ||
        myCurrTelemetry.getLongitude() < 0 || myCurrTelemetry.getLongitude() > LATITUDE_MAX ||
        myCurrTelemetry.getAltitude() < 0 || myCurrTelemetry.getAltitude() > ALTITUDE_MAX){
            ret.append("Out of Bounds");
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

        // Check intended velocity
        if (Math.abs(myCurrTelemetry.getAltitude() - myPrevTelemetry.getAltitude()) > ORTHOGONAL_VELOCITY_MAX){
            if (!ret.isEmpty()) ret.append(" Due to ");
            ret.append("Dangerous Change in Altitude");
            return ret.toString();
        } else if (Math.abs(myCurrTelemetry.getLongitude() - myPrevTelemetry.getLongitude()) >
                ORTHOGONAL_VELOCITY_MAX ||
                Math.abs(myCurrTelemetry.getLatitude() - myPrevTelemetry.getLatitude()) > ORTHOGONAL_VELOCITY_MAX){
            if (!ret.isEmpty()) ret.append(" Due to ");
            ret.append("GPS Spoofing");
            return ret.toString();
        }

        if (ret.isEmpty()) ret.append("N/A");

        return ret.toString();
    }

    /**
     * A private method to hold the power anomaly detection logic.
     * @return  Returns a boolean representing whether the battery level is 0.
     */
    private String PowerAnomaly(){
        if (myPrevTelemetry.getBatteryLevel() - myCurrTelemetry.getBatteryLevel() < BATTERY_DRAIN_RATE_MAX) {
            return "Battery Drain Failure";
        } else if (myCurrTelemetry.getBatteryLevel() <= 0){
            return "Battery Failure";
        }
        return null;
    }

    /**
     * A private method to create a simplified anomaly report string.
     * @param theAnomalyType    A string representing the type of anomaly being reported.
     * @return                  Returns a simplified string anomaly report.
     */
    private String CreateDescSimple(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Anomaly Detected! \n Drone ID: ");
        sb.append(myCurrTelemetry.getMyDroneId());
        sb.append("\nAnomaly Type: ");
        sb.append(theAnomalyType);
        sb.append("\nTimestamp: ");
        sb.append(myCurrTelemetry.getMyTimestamp());
        return sb.toString();
    }

    /**
     * A private method to create a detailed anomaly report string.
     * @param theAnomalyType    A string representing the type of anomaly being reported.
     * @return                  Returns a detailed string anomaly report.
     */
    private String CreateDescDetailed(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Drone Number: ").append(myCurrTelemetry.getMyDroneId());
        sb.append("has experienced an anomaly at time: ").append(myCurrTelemetry.getMyTimestamp());
        sb.append("\nDetails:\n");
        sb.append(theAnomalyType).append(" anomaly detected\n");

        //Current State
        sb.append("Current State: \n");
        sb.append("x: ").append(myCurrTelemetry.getLatitude());
        sb.append(" y: ").append(myCurrTelemetry.getLongitude());
        sb.append(" z: ").append(myCurrTelemetry.getAltitude()).append("\n");
        sb.append("Velocity: ").append(myCurrTelemetry.getVelocity()).append(" units/second(cycle)\n");
        sb.append("Battery (%): ").append(myCurrTelemetry.getBatteryLevel());

        //Previous state
        sb.append("\nPrevious State: \n");
        sb.append("x: ").append(myPrevTelemetry.getLatitude());
        sb.append(" y: ").append(myPrevTelemetry.getLongitude());
        sb.append(" z: ").append(myPrevTelemetry.getAltitude()).append("\n");
        sb.append("Velocity: ").append(myPrevTelemetry.getVelocity()).append(" units/second(cycle)\n");
        sb.append("Battery (%): ").append(myPrevTelemetry.getBatteryLevel());

        //Attempted Telemetry instruction
        sb.append("\nAttempted Instructions: \n");
        sb.append("x: ").append(String.format("%+f", myCurrTelemetry.getLatitude() - myPrevTelemetry.getLatitude()));
        sb.append(" y: ").append(String.format("%+f", myCurrTelemetry.getLongitude() - myPrevTelemetry.getLongitude()));
        sb.append(" z: ").append(String.format("%+f", myCurrTelemetry.getAltitude() - myPrevTelemetry.getAltitude()));
        sb.append("\n");

        return sb.toString();
    }

    /**
     * A private method to create an AnomalyReport object
     * @param theAnomalyType    A string classification of the anomaly to be reported on.
     * @return                  Returns an anomaly report with the relevant information.
     */
    private AnomalyReport CreateAnomalyReport(String theAnomalyType){
        String simpleReport = CreateDescSimple(theAnomalyType);
        String detailedReport = CreateDescDetailed(theAnomalyType);

        UUID myAnomalyID = UUID.randomUUID();

        return new AnomalyReport(
                myAnomalyID,
                myCurrTelemetry.getMyTimestamp(),
                theAnomalyType,
                myCurrTelemetry.getMyDroneId(),
                simpleReport,
                detailedReport);
    }

    /**
     * A method to acquire listeners for anomaly notification.
     * @param listener  A PropertyChangeListener to be added to the PropertyChangeSupport.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * A method to remove listeners for anomaly notification.
     * @param listener  A PropertyChangeListener to be removed from the PropertyChangeSupport.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
     * A simple, immutable data carrier for a drone anomaly report.
     */
    public record AnomalyReport(
            UUID id,
            Long timestamp,
            String anomalyType,
            int droneId,
            String simpleReport,
            String detailedReport
    ){}
}
