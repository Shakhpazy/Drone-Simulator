package model;
import java.time.LocalDateTime;
import java.util.UUID;

public class AnomalyDetector {

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the most recent telemetry data.
     */
    private Telemetry myCurrTelem;

    /**
     * A private telemetry field for use in the Anomaly Detector class.
     * Represents the telemetry data of a drone prior to the current cycle.
     */
    private Telemetry myPrevTelem;

    public boolean Detect(Telemetry telem){
        myCurrTelem = telem;

        /* Check for spoofing or positional anomaly */
        String positionalErr = PositionAnomaly();
        if (positionalErr != null){
            CreateAnomalyReport(positionalErr);
            return true;
        }

        /* Check for battery anomaly */
        if (PowerAnomaly()){
            CreateAnomalyReport("Power");
            return true;
        }

        return false;
    }

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
        // Check altitude
        // Check x
        // Check y

        return ret;
    }

    private boolean PowerAnomaly(){
        boolean ret = false;
        if(myCurrTelem.batt <= 0){
            ret = true;
        }
        return ret;
    }

    private String CreateDescSimple(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Anomaly Detected! \n Drone ID: ");
        sb.append(myCurrTelem.droneID);
        sb.append("\nAnomaly Type: ");
        sb.append(theAnomalyType);
        sb.append("\nTimestamp: ");
        sb.append(myCurrTelem.timestamp);
        return sb.toString();
    }

    private String CreateDescDetailed(String theAnomalyType){
        StringBuilder sb = new StringBuilder();
        sb.append("Drone Number: ").append(myCurrTelem.droneID);
        sb.append("has experienced an anomaly at time: ").append(myCurrTelem.timestamp);
        sb.append("\nDetails:\n");
        sb.append(theAnomalyType).append(" anomaly detected\n");

        //Current State
        sb.append("Current State: \n");
        sb.append("x: ").append(myCurrTelem.x);
        sb.append(" y: ").append(myCurrTelem.y);
        sb.append(" z: ").append(myCurrTelem.z).append("\n");
        sb.append("Velocity: ").append(myCurrTelem.vel).append(" units/second(cycle)");
        sb.append("Battery (%): ").append(myCurrTelem.batt);

        //Previous state
        sb.append("Previous State: \n");
        sb.append("x: ").append(myPrevTelem.x);
        sb.append(" y: ").append(myPrevTelem.y);
        sb.append(" z: ").append(myPrevTelem.z).append("\n");
        sb.append("Velocity: ").append(myPrevTelem.vel).append(" units/second(cycle)");
        sb.append("Battery (%): ").append(myPrevTelem.batt);

        //Attempted telem instruction
        sb.append("Attempted Instructions: \n");
        sb.append("x: ").append(myCurrTelem.insX);
        sb.append(" y: ").append(myCurrTelem.insY);
        sb.append(" z: ").append(myCurrTelem.insZ).append("\n");

        return sb.toString();
    }

    private AnomalyReport CreateAnomalyReport(String theAnomalyType){
        String simpleReport = CreateDescSimple(theAnomalyType);
        String detailedReport = CreateDescDetailed(theAnomalyType);

        /*
         * A private ID for identification of each anomaly
         */
        UUID myAnomalyID = UUID.randomUUID();

        AnomalyReport ar = new AnomalyReport(
                myAnomalyID,
                myCurrTelem.timestamp,
                theAnomalyType,
                myCurrTelem.droneID,
                simpleReport,
                detailedReport);

        return ar;
    }

    /**
     * A simple, immutable data carrier for a drone anomaly report.
     */
    public record AnomalyReport(
            UUID id,
            LocalDateTime timestamp,
            String anomalyType,
            int droneId,
            String simpleReport,
            String detailedReport
    ){}
}
