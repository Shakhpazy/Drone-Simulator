package tests;

import model.AnomalyEnum;
import model.ReportFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class to test the ReportFormatter class methods.
 * @author nlevin11
 * @version 11-12
 */
public class ReportFormatterTests {
    private HashMap<String, Object> myCurrTelemetry;
    private HashMap<String, Object> myPrevTelemetry;
    private String myFormattedTimestamp;
    private String myAnomalyEnum;

    @BeforeEach
    void setUp() {
        long myTimestamp = 1678886400000L;
        myFormattedTimestamp = "2023-03-15 06:20:00.000";
        myAnomalyEnum = AnomalyEnum.ALTITUDE.toString();

        myCurrTelemetry = new HashMap<>();
        myCurrTelemetry.put("id",5);
        myCurrTelemetry.put("timeStamp", myTimestamp);
        myCurrTelemetry.put("latitude",100);
        myCurrTelemetry.put("longitude",200);
        myCurrTelemetry.put("altitude",300);
        myCurrTelemetry.put("velocity",10);
        myCurrTelemetry.put("batteryLevel",90);
        myCurrTelemetry.put("orientation",90);

        myPrevTelemetry = new HashMap<>();
        myPrevTelemetry.put("id",5);
        myPrevTelemetry.put("timeStamp", myTimestamp);
        myPrevTelemetry.put("latitude",101);
        myPrevTelemetry.put("longitude",201);
        myPrevTelemetry.put("altitude",301);
        myPrevTelemetry.put("velocity",10);
        myPrevTelemetry.put("batteryLevel",80);
        myPrevTelemetry.put("orientation",90);
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
