package tests;

import model.AnomalyDetector;
import model.AnomalyEnum;
import model.AnomalyReport;
import model.TelemetryRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AnomalyDetector.
 * Automatically handles the creation of the required baseline properties file.
 */
class AnomalyDetectorTest {

    private AnomalyDetector detector;
    private static final String LOG_DIR = "dataLogs";
    private static final String LOG_FILE = "dataLogs/BaselineLog.properties";

    /**
     * Sets up the environment by creating the necessary dataLogs directory
     * and the BaselineLog.properties file with sample data.
     */
    @BeforeAll
    static void setupBaselineFile() throws IOException {
        Path dirPath = Paths.get(LOG_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        File file = new File(LOG_FILE);
        // We write the same values provided in your uploaded file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("acceleration.mean=0.5985\n");
            writer.write("acceleration.standardDev=0.0028\n");
            writer.write("batteryDrain.mean=0.0393\n");
            writer.write("batteryDrain.standardDev=0.0014\n");
            writer.write("orientationSteady.max=1.635\n");
            writer.write("orientationTurn.min=14.092\n");
            writer.write("velocity.mean=9.813\n");
            writer.write("velocity.min=1.3\n");
            writer.write("velocity.standardDev=0.470\n");
        }
    }

    @BeforeEach
    void setUp() {
        // Initialize a fresh detector for each test
        detector = new AnomalyDetector();
    }

    // --- Helper Method ---

    /**
     * Helper to create a TelemetryRecord with specific values.
     * Arguments match the record: id, lon, lat, alt, vel, bat, orient, time
     */
    private TelemetryRecord createRecord(float lon, float lat, float alt, float vel, float bat, float orient, long time) {
        return new TelemetryRecord(1, lon, lat, alt, vel, bat, orient, time);
    }

    // --- Hard Rule Tests (Safety Checks) ---

    @Test
    void testNoAnomaly() {
        // Normal behavior: small movement, normal battery drain
        TelemetryRecord prev = createRecord(0, 0, 100, 10, 90.0f, 0, 100000);
        TelemetryRecord curr = createRecord(0.001f, 0.001f, 100, 10, 89.99f, 0, 101000); // 1 sec later

        AnomalyReport report = detector.detect(prev, curr);
        assertNull(report, "Normal behavior should not trigger an anomaly");
    }

    @Test
    void testGroundCollision() {
        TelemetryRecord prev = createRecord(0, 0, 50, 0, 50, 0, 1000);
        // Altitude hits 0
        TelemetryRecord curr = createRecord(0, 0, 0, 0, 50, 0, 2000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should report ground collision");
        assertTrue(report.simpleReport().contains(AnomalyEnum.HIT_GROUND.toString()));
    }

    @Test
    void testBatteryFailure() {
        TelemetryRecord prev = createRecord(0, 0, 100, 0, 50, 0, 1000);
        // Battery hits 0
        TelemetryRecord curr = createRecord(0, 0, 100, 0, 0, 0, 2000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should report battery failure");
        assertTrue(report.simpleReport().contains(AnomalyEnum.BATTERY_FAIL.toString()));
    }

    @Test
    void testBatteryWarning() {
        TelemetryRecord prev = createRecord(0, 0, 100, 0, 20, 0, 1000);
        // Battery drops to 14.5 (Between 14 and 15)
        TelemetryRecord curr = createRecord(0, 0, 100, 0, 14.5f, 0, 2000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should report battery warning");
        assertTrue(report.simpleReport().contains(AnomalyEnum.BATTERY_WARNING.toString()));
    }

    @Test
    void testOutOfBounds() {
        TelemetryRecord prev = createRecord(0, 0, 100, 0, 90, 0, 1000);
        // Latitude 91 (Max is 90)
        TelemetryRecord curr = createRecord(0, 91, 100, 0, 90, 0, 2000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should report out of bounds");
        assertTrue(report.simpleReport().contains(AnomalyEnum.OUT_OF_BOUNDS.toString()));
    }

    @Test
    void testSpoofing() {
        // "Spoofing" or teleportation: Moving impossibly fast between updates.
        // Logic: Distance > ORTHOGONAL_VELOCITY_MAX (10.0)

        TelemetryRecord prev = createRecord(0, 0, 100, 0, 90, 0, 1000);

        // Moved 20 units in Longitude in 1 second.
        // Displacement ~20 units. 20 > 10.
        TelemetryRecord curr = createRecord(20, 0, 100, 0, 90, 0, 2000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should report spoofing/teleportation");
        assertTrue(report.simpleReport().contains(AnomalyEnum.SPOOFING.toString()));
    }

    // --- Statistical Tests (Z-Score Logic) ---

    @Test
    void testAbnormalAcceleration() {
        // Logic constraint: currTime must be > firstTimestamp + 1000 for stats to run.
        long startTime = 100000;

        // 1. Prime the detector (sets firstTimestamp)
        TelemetryRecord init = createRecord(0, 0, 100, 0, 90, 0, startTime);
        detector.detect(init, init);

        // 2. Advance time by 2 seconds so we pass the "warmup" period
        TelemetryRecord prev = createRecord(0, 0, 100, 5, 89, 0, startTime + 2000);

        // 3. Trigger High Acceleration
        // Velocity jumps from 5 to 25 in 1 second. Accel = 20 units/s^2.
        // Baseline Mean ~0.6. This is a huge Z-score.
        TelemetryRecord curr = createRecord(0, 0, 100, 25, 88, 0, startTime + 3000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should detect acceleration anomaly");
        assertTrue(report.simpleReport().contains(AnomalyEnum.ACCELERATION.toString()));
    }

    @Test
    void testBatteryDrain() {
        // Logic constraint: Z-Score > 3.0
        // Baseline Drain Mean ~0.039%/sec
        // We will drain 1.0% in 1 second, which is ~25x higher than normal.

        long startTime = 200000;

        // 1. Prime detector
        TelemetryRecord init = createRecord(0, 0, 100, 0, 90, 0, startTime);
        detector.detect(init, init);

        // 2. Create scenario
        TelemetryRecord prev = createRecord(0, 0, 100, 10, 80.0f, 0, startTime + 2000);
        TelemetryRecord curr = createRecord(0, 0, 100, 10, 79.0f, 0, startTime + 3000); // Dropped 1%

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should detect abnormal battery drain");
        assertTrue(report.simpleReport().contains(AnomalyEnum.BATTERY_DRAIN.toString()));
    }

    @Test
    void testVelocityAnomaly_HighSpeed() {
        // CASE: Drone is flying steady, but way too fast.
        // Logic: velFlag is TRUE (Z-Score > 3), but isAccel is FALSE (Steady speed).
        // Expected Result: AnomalyEnum.OFF_COURSE

        long startTime = 300000;

        // 1. Prime the detector
        TelemetryRecord init = createRecord(0, 0, 100, 20.0f, 90, 0, startTime);
        detector.detect(init, init);

        // 2. Previous State: Already moving fast (20.0)
        // Velocity Mean is ~9.8, so 20.0 is a Z-Score of ~21.
        TelemetryRecord prev = createRecord(0, 0, 100, 20.0f, 85.0f, 0, startTime + 2000);

        // 3. Current State: Still moving fast (20.0) so Acceleration is 0.
        TelemetryRecord curr = createRecord(10, 0, 100, 20.0f, 84.9f, 0, startTime + 3000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should detect velocity anomaly");
        // Note: Your logic returns OFF_COURSE for velocity Z-score failures, not SPEED
        assertTrue(report.simpleReport().contains(AnomalyEnum.OFF_COURSE.toString()));
    }

    @Test
    void testVelocityAnomaly_TooSlow() {
        // CASE: Drone is flying too slow to be normal, but too fast to be "landing".
        // Logic: Velocity 2.0. Mean is 9.8. Z-Score is highly negative (magnitude > 3).
        // approachFlag is FALSE (because 2.0 > 1.3).

        long startTime = 400000;

        // 1. Prime
        TelemetryRecord init = createRecord(0, 0, 100, 2.0f, 90, 0, startTime);
        detector.detect(init, init);

        // 2. Steady low speed
        TelemetryRecord prev = createRecord(0, 0, 100, 2.0f, 80.0f, 0, startTime + 2000);
        TelemetryRecord curr = createRecord(1, 0, 100, 2.0f, 79.9f, 0, startTime + 3000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should detect low speed stall");
        assertTrue(report.simpleReport().contains(AnomalyEnum.OFF_COURSE.toString()));
    }

    @Test
    void testOrientationAnomaly() {
        // CASE: The "Uncanny Valley" of rotation.
        // Logic: Rotation Delta is 5 degrees.
        // Properties: orientationSteady.max (~1.8) < 5 < orientationTurn.min (~14.1)

        long startTime = 500000;

        TelemetryRecord init = createRecord(0, 0, 100, 9.8f, 90, 0, startTime);
        detector.detect(init, init);

        // Previous Orientation: 0
        TelemetryRecord prev = createRecord(0, 0, 100, 9.8f, 80.0f, 0.0f, startTime + 2000);

        // Current Orientation: 5 (Delta = 5)
        TelemetryRecord curr = createRecord(0, 0, 100, 9.8f, 79.96f, 5.0f, startTime + 3000);

        AnomalyReport report = detector.detect(prev, curr);

        assertNotNull(report, "Should detect suspicious orientation drift");
        assertTrue(report.simpleReport().contains(AnomalyEnum.OFF_COURSE.toString()));
    }
}