package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnomalyDatabase {

    private final String connectionString = "jdbc:sqlite:drone_anomalies.db";

    /**
     * Initializes the database.
     * Creates table and indexes if not already present.
     */
    public void initialize() {

    }

    /**
     * A method to allow new AnomalyReports into the database.
     * @param report    The AnomalyReport to insert.
     */
    public void insertReport(AnomalyReport report){

    }

    /**
     * A method to find reports based on their Anomaly ID.
     * @param theAnomalyID  The ID number needed to find the AnomalyReport.
     * @return              Returns the necessary AnomalyReport.
     */
    public AnomalyReport findReportsByAnomalyID(int theAnomalyID){
        // Search for the report
        // Initialize a copy of the report to be output.
        AnomalyReport report = new AnomalyReport();

        return report;
    }

    /**
     * A method to find reports based on a given time range.
     * @param theBeginTime      The beginning value of the time range being searched.
     * @param theEndTime        The end value of the time range being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByTimeRange(float theBeginTime, float theEndTime){
        List<AnomalyReport> reports = new ArrayList<>();

        return reports;
    }

    /**
     * A method to find reports based on a given anomaly type.
     * @param theAnomalyType    The String representation of the anomaly type being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByAnomalyType(String theAnomalyType){
        List<AnomalyReport> reports = new ArrayList<>();

        return reports;
    }

    /**
     * A method to find reports based on a given Drone ID.
     * @param theDroneID        The int representation of the Drone ID being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByDroneID(int theDroneID){
        List<AnomalyReport> reports = new ArrayList<>();

        return reports;
    }

    /**
     * A method to list all reports currently in the database for use in CSVExporter
     * @return                  Returns a list of all AnomalyReports from the database.
     */
    public List<AnomalyReport> findAllReports(){
        List<AnomalyReport> reports = new ArrayList<>();

        return reports;
    }
}
