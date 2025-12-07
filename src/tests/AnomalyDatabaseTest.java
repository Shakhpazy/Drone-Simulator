package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.AnomalyDatabase;
import model.AnomalyEnum;
import model.AnomalyReport;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyDatabaseTest {

    private AnomalyDatabase db;
    private AnomalyReport testReport1;
    private AnomalyReport testReport2;

    @BeforeEach
    void setUp() {
        db = new AnomalyDatabase();
        db.initialize();
        db.clear(); // Ensure clean slate before every test

        // Create a report using the Enum's toString() representation
        testReport1 = new AnomalyReport(
                UUID.randomUUID(),
                System.currentTimeMillis(),
                AnomalyEnum.BATTERY_FAIL.toString(), // "Battery Failure"
                101,
                "Battery critical",
                "Voltage dropped below 5% instantly."
        );

        // Create a second report with a different Enum and time
        testReport2 = new AnomalyReport(
                UUID.randomUUID(),
                System.currentTimeMillis() - 100000,
                AnomalyEnum.SPOOFING.toString(), // "GPS Spoofing"
                102,
                "Location mismatch",
                "GPS coordinates jumping erratically."
        );
    }

    @AfterEach
    void tearDown() {
        db.clear();
        db.close();
    }

    @Test
    void testInsertAndRetrieve() {
        db.insertReport(testReport1);
        List<AnomalyReport> results = db.findAllReports();

        assertEquals(1, results.size(), "Should return 1 report");
        // Verify the String matches the Enum's display string
        assertEquals(AnomalyEnum.BATTERY_FAIL.toString(), results.getFirst().anomalyType());
    }

    @Test
    void testFindReportsByDroneID() {
        db.insertReport(testReport1); // ID 101
        db.insertReport(testReport2); // ID 102

        List<AnomalyReport> results = db.findReportsByDroneID(101);

        assertEquals(1, results.size());
        assertEquals(testReport1.id(), results.getFirst().id());
    }

    @Test
    void testFindReportsByAnomalyType() {
        db.insertReport(testReport1);
        db.insertReport(testReport2);

        // Search using the Enum string
        List<AnomalyReport> results = db.findReportsByAnomalyType(AnomalyEnum.SPOOFING.toString());

        assertEquals(1, results.size());
        assertEquals(102, results.getFirst().droneId());
    }

    @Test
    void testFindReportsByPartialType() {
        db.insertReport(testReport1); // "Battery Failure"

        // Test that your LIKE %query% logic works with partial strings
        List<AnomalyReport> results = db.findReportsByAnomalyType("Battery");

        assertEquals(1, results.size());
    }

    @Test
    void testFindReportsByTimeRange() {
        long now = System.currentTimeMillis();

        db.insertReport(testReport1); // Happens at 'now'
        db.insertReport(testReport2); // Happens at 'now - 100000'

        // Search for a range that only captures the most recent report
        // Range: (now - 1000ms) to (now)
        List<AnomalyReport> results = db.findReportsByTimeRange(now - 1000, now);

        assertEquals(1, results.size());
        assertEquals(testReport1.id(), results.getFirst().id());
    }
}