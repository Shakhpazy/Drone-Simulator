package tests;

import model.AnomalyEnum;
import model.ReportFormatter;
import model.TelemetryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class to test the ReportFormatter class methods.
 * @author nlevin11
 * @version 11-12
 */
public class ReportFormatterTests {
    private TelemetryRecord myCurrTelemetry;
    private TelemetryRecord myPrevTelemetry;
    private String myFormattedTimestamp;
    private String myAnomalyEnum;

    @BeforeEach
    void setUp() {
        long myTimestamp = 1678886400000L;
        myFormattedTimestamp = "2023-03-15 06:20:00.000";
        myAnomalyEnum = AnomalyEnum.ALTITUDE.toString();

        myCurrTelemetry = new TelemetryRecord(5, 200, 100, 300, 10, 79,
                90, myTimestamp);

        myPrevTelemetry = new TelemetryRecord(5, 201, 101, 301, 10, 80,
                80, myTimestamp);
    }

    @Test
    void testGetFormattedTime() {
        String result = ReportFormatter.getFormattedTime(myCurrTelemetry);
        assertEquals(myFormattedTimestamp, result);
    }

    @Test
    void testCreateDescSimple() {
        String expected = "Anomaly Detected! \nDrone ID: 5\nAnomaly Type: " + myAnomalyEnum + "\nTime Stamp: " +
                myFormattedTimestamp + "\n";
        String result = ReportFormatter.createDescSimple(myAnomalyEnum, myCurrTelemetry);
        assertEquals(expected, result);

    }

    @Test
    void testCreateDescDetailed() {
        String result = ReportFormatter.createDescDetailed(myAnomalyEnum, myCurrTelemetry, myPrevTelemetry);
        assertTrue(result.contains("\nDrone Number: 5"));
        assertTrue(result.contains(" Has experienced an anomaly at time: " + myFormattedTimestamp));
        assertTrue(result.contains(myAnomalyEnum + " anomaly detected\n"));
        assertTrue(result.contains("x: 100 y: 200 z: 300\n"));
        assertTrue(result.contains("Velocity: 10 units/cycle"));
        assertTrue(result.contains("Orientation: 90"));
        assertTrue(result.contains("Battery (%): 90"));

        assertTrue(result.contains("x: 101 y: 201 z: 301\n"));
        assertTrue(result.contains("Velocity: 10 units/cycle"));
        assertTrue(result.contains("Orientation: 90"));
        assertTrue(result.contains("Battery (%): 80"));
    }
}
