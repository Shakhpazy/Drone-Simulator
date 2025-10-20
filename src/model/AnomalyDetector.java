package model;
import java.time.LocalDateTime;

public class AnomalyDetector {

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     */
    private Telemetry myTelem;

    public boolean Detect(Telemetry telem){
        myTelem = telem;

        /* Check for spoofing or positional anomaly */
        String positionalErr = PositionAnomaly();
        if (positionalErr != null){
            CreateAnomalyReport(positionalErr);
            return true;
        }

        /* Check for battery anomaly */
        if (PowerAnomaly()){
            CreateAnomalyReport("Power");
            return false;
        }

        return false;
    }

    private String PositionAnomaly(){
        String ret = null;

        return ret;
    }

    private boolean PowerAnomaly(){
        boolean ret = false;

        return ret;
    }

    private String CreateDescSimple(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Anomaly Detected! \n Drone ID: ");
        sb.append(myTelem.droneID);
        sb.append("\nAnomaly Type: ");
        sb.append(theAnomalyType);
        sb.append("\nTimestamp: ");
        sb.append(myTelem.timestamp);
        return sb.toString();
    }

    private String CreateDescDetailed(String theAnomalyType){
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private AnomalyReport CreateAnomalyReport(String theAnomalyType){
        String simpleReport = CreateDescSimple(theAnomalyType);
        String detailedReport = CreateDescDetailed(theAnomalyType);

        AnomalyReport ar = new AnomalyReport(
                myTelem.timestamp,
                theAnomalyType,
                myTelem.droneID,
                simpleReport,
                detailedReport);

        return ar;
    }

    /**
     * A simple, immutable data carrier for a drone anomaly report.
     */
    public record AnomalyReport(
            LocalDateTime timestamp,
            String anomalyType,
            int droneId,
            String simpleReport,
            String detailedReport
    ){}
}
