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
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS anomaly_reports (
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                anomaly_type TEXT NOT NULL,
                drone_id INTEGER NOT NULL,
                simple_report TEXT,
                detailed_report TEXT
                );
                """;

        String indexTimestampSql = "CREATE INDEX IF NOT EXISTS idx_timestamp ON anomaly_reports (timestamp);";
        String indexTypeSql = "CREATE INDEX IF NOT EXISTS idx_anomaly_type ON anomaly_reports (anomaly_type);";
        String indexDroneIdSql = "CREATE INDEX IF NOT EXISTS idx_drone_id ON anomaly_reports (drone_id);";

        try (Connection conn = DriverManager.getConnection(connectionString);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSql);
            stmt.execute(indexTimestampSql);
            stmt.execute(indexTypeSql);
            stmt.execute(indexDroneIdSql);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * A method to allow new AnomalyReports into the database.
     * @param report    The AnomalyReport to insert.
     */
    public void insertReport(AnomalyReport report){
        String sql = "INSERT INTO anomaly_reports(id, timestamp, anomaly_type, drone_id, simple_report, detailed_report) " +
                "VALUES(?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(connectionString);
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, report.id().toString());
            preparedStatement.setLong(2, report.timestamp());
            preparedStatement.setString(3, report.anomalyType());
            preparedStatement.setInt(4, report.droneId());
            preparedStatement.setString(5, report.simpleReport());
            preparedStatement.setString(6, report.detailedReport());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * A method to find reports based on their Anomaly ID.
     * @param theAnomalyID  The ID number needed to find the AnomalyReport.
     * @return              Returns the necessary AnomalyReport.
     */
    public AnomalyReport findReportByAnomalyID(String theAnomalyID){
        String sql = "SELECT * FROM anomaly_reports WHERE id = ?";
        AnomalyReport report = null;

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, theAnomalyID);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                // Re-Build record from database
                report = new AnomalyReport(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("timestamp"),
                        rs.getString("anomaly_type"),
                        rs.getInt("drone_id"),
                        rs.getString("simple_report"),
                        rs.getString("detailed_report")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return report;
    }

    /**
     * A method to find reports based on a given time range.
     * @param theBeginTime      The beginning value of the time range being searched.
     * @param theEndTime        The end value of the time range being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByTimeRange(long theBeginTime, long theEndTime){
        List<AnomalyReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM anomaly_reports WHERE timestamp BETWEEN ? AND ? ";

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){

            preparedStatement.setLong(1, theBeginTime);
            preparedStatement.setLong(2, theEndTime);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                // Re-Build record from database
                AnomalyReport report = new AnomalyReport(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("timestamp"),
                        rs.getString("anomaly_type"),
                        rs.getInt("drone_id"),
                        rs.getString("simple_report"),
                        rs.getString("detailed_report")
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reports;
    }

    /**
     * A method to find reports based on a given anomaly type.
     * @param theAnomalyType    The String representation of the anomaly type being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByAnomalyType(String theAnomalyType){
        List<AnomalyReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM anomaly_reports WHERE anomaly_type = ?";

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){

            preparedStatement.setString(1, theAnomalyType);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                // Re-Build record from database
                AnomalyReport report = new AnomalyReport(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("timestamp"),
                        rs.getString("anomaly_type"),
                        rs.getInt("drone_id"),
                        rs.getString("simple_report"),
                        rs.getString("detailed_report")
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reports;
    }

    /**
     * A method to find reports based on a given Drone ID.
     * @param theDroneID        The int representation of the Drone ID being searched.
     * @return                  Returns a list of AnomalyReports from the given criteria.
     */
    public List<AnomalyReport> findReportsByDroneID(int theDroneID){
        List<AnomalyReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM anomaly_reports WHERE drone_id = ?";

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){

            preparedStatement.setInt(1, theDroneID);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                // Re-Build record from database
                AnomalyReport report = new AnomalyReport(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("timestamp"),
                        rs.getString("anomaly_type"),
                        rs.getInt("drone_id"),
                        rs.getString("simple_report"),
                        rs.getString("detailed_report")
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reports;
    }

    /**
     * A method to list all reports currently in the database for use in CSVExporter
     * @return                  Returns a list of all AnomalyReports from the database.
     */
    public List<AnomalyReport> findAllReports(){
        List<AnomalyReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM anomaly_reports";

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()){

            while(rs.next()) {
                // Re-Build record from database
                AnomalyReport report = new AnomalyReport(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("timestamp"),
                        rs.getString("anomaly_type"),
                        rs.getInt("drone_id"),
                        rs.getString("simple_report"),
                        rs.getString("detailed_report")
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reports;
    }

    public void clear() {
        String sql = "DELETE FROM anomaly_reports";

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Cleared anomaly reports table, " + rowsAffected + " rows deleted." );
        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
        }
    }
}
