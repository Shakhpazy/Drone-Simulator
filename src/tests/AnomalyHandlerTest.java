package tests;

import org.junit.jupiter.api.Test;
import model.AnomalyHandler;
import model.AnomalyEnum;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AnomalyHandler class
 * @author Yusuf
 */
public class AnomalyHandlerTest {

    @Test
    void applyRandomAnomalyReturnsResult() {
        AnomalyHandler handler = new AnomalyHandler();
        AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
        assertNotNull(result);
    }

    @Test
    void applyRandomAnomalyUpdatesLastAnomaly() {
        AnomalyHandler handler = new AnomalyHandler();
        handler.applyRandomAnomaly(100f, 5f, 1.0f);
        assertNotNull(handler.getLastAnomaly());
    }

    @Test
    void getLastAnomalyReturnsNullInitially() {
        AnomalyHandler handler = new AnomalyHandler();
        assertNull(handler.getLastAnomaly());
    }

    @Test
    void batteryFailAnomalySetsBatteryFailFlag() {
        AnomalyHandler handler = new AnomalyHandler();
        // Run multiple times to increase chance of getting BATTERY_FAIL
        boolean foundBatteryFail = false;
        for (int i = 0; i < 50; i++) {
            AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
            if (result.batteryFail) {
                foundBatteryFail = true;
                assertEquals(0f, result.altitude, 0.0001);
                assertEquals(0f, result.velocity, 0.0001);
                break;
            }
        }
        assertTrue(foundBatteryFail, "Should eventually get BATTERY_FAIL anomaly");
    }

    @Test
    void batteryDrainAnomalyAddsExtraDrain() {
        AnomalyHandler handler = new AnomalyHandler();
        // Run multiple times to increase chance of getting BATTERY_DRAIN
        boolean foundBatteryDrain = false;
        for (int i = 0; i < 50; i++) {
            AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
            if (handler.getLastAnomaly() == AnomalyEnum.BATTERY_DRAIN) {
                foundBatteryDrain = true;
                assertTrue(result.extraDrain > 0);
                break;
            }
        }
        assertTrue(foundBatteryDrain, "Should eventually get BATTERY_DRAIN anomaly");
    }

    @Test
    void altitudeAnomalyChangesAltitude() {
        AnomalyHandler handler = new AnomalyHandler();
        // Run multiple times to increase chance of getting ALTITUDE
        boolean foundAltitude = false;
        for (int i = 0; i < 50; i++) {
            AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
            if (handler.getLastAnomaly() == AnomalyEnum.ALTITUDE) {
                foundAltitude = true;
                assertNotEquals(100f, result.altitude, 0.1f);
                assertTrue(result.altitude >= 0f);
                break;
            }
        }
        assertTrue(foundAltitude, "Should eventually get ALTITUDE anomaly");
    }

    @Test
    void speedAnomalyChangesVelocity() {
        AnomalyHandler handler = new AnomalyHandler();
        // Run multiple times to increase chance of getting SPEED
        boolean foundSpeed = false;
        for (int i = 0; i < 50; i++) {
            AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
            if (handler.getLastAnomaly() == AnomalyEnum.SPEED) {
                foundSpeed = true;
                assertNotEquals(5f, result.velocity, 0.1f);
                assertTrue(result.velocity >= 0f);
                assertTrue(result.velocity <= 10f);
                break;
            }
        }
        assertTrue(foundSpeed, "Should eventually get SPEED anomaly");
    }

    @Test
    void spoofingAnomalyDoesNotChangeMovement() {
        AnomalyHandler handler = new AnomalyHandler();
        // Run multiple times to increase chance of getting SPOOFING
        boolean foundSpoofing = false;
        for (int i = 0; i < 50; i++) {
            AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
            if (handler.getLastAnomaly() == AnomalyEnum.SPOOFING) {
                foundSpoofing = true;
                assertEquals(100f, result.altitude, 0.0001);
                assertEquals(5f, result.velocity, 0.0001);
                assertEquals(0f, result.extraDrain, 0.0001);
                assertFalse(result.batteryFail);
                break;
            }
        }
        assertTrue(foundSpoofing, "Should eventually get SPOOFING anomaly");
    }

    @Test
    void anomalyResultFieldsAreAccessible() {
        AnomalyHandler handler = new AnomalyHandler();
        AnomalyHandler.AnomalyResult result = handler.applyRandomAnomaly(100f, 5f, 1.0f);
        
        // Just verify fields exist and are accessible
        float alt = result.altitude;
        float vel = result.velocity;
        float drain = result.extraDrain;
        boolean fail = result.batteryFail;
        
        assertTrue(alt >= 0);
        assertTrue(vel >= 0 && vel <= 10);
        assertTrue(drain >= 0);
    }
}
