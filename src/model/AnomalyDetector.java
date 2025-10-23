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
        String positionalErr = PositionAnomaly();
        if (positionalErr != null){
            AnomalyReport ar = CreateAnomalyReport(positionalErr);
            pcs.firePropertyChange("Anomaly Detected", null, ar);
            return ar;
        }

        /* Check for battery anomaly */
        if (PowerAnomaly()){
            AnomalyReport ar = CreateAnomalyReport("Battery Failure");
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
        String ret = null;

        // Check intended velocity
        // int velIntended = math.pow((math.pow(myTelem.insX, 2) +
        // math.pow(myTelem.insY, 2) + math.pow(myTelem.insZ, 2)), .5)

        // Check actual velocity
        // velIntended == myCurrTelem.vel
        // verify against prev and curr telem
        // If not equal than allowed go forward with testing to narrow the possible error

        // Checks return early once error found
        // Check altitude, most common case
        // Check x
        // Check y

        return ret;
    }

    /**
     * A private method to hold the power anomaly detection logic.
     * @return  Returns a boolean representing whether the battery level is 0.
     */
    private boolean PowerAnomaly(){
        return myCurrTelemetry.getBatteryLevel() <= 0;
    }

    /**
     * A private method to create a simplified anomaly report string.
     * @param theAnomalyType    A string representing the type of anomaly being reported.
     * @return                  Returns a simplified string anomaly report.
     */
    private String CreateDescSimple(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Anomaly Detected! \n Drone ID: ");
        sb.append(myCurrTelemetry.getDroneID);
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
        sb.append("Drone Number: ").append(myCurrTelemetry.droneID);
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
        sb.append("x: ").append(myCurrTelemetry.insX);
        sb.append(" y: ").append(myCurrTelemetry.insY);
        sb.append(" z: ").append(myCurrTelemetry.insZ).append("\n");

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
                myCurrTelemetry.droneID,
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
