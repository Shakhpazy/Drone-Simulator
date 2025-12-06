package tests;

import org.junit.jupiter.api.Test;
import model.BatterySystem;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BatterySystem class
 * @author Yusuf
 */
public class BatterySystemTest {

    @Test
    void constructorInitializesBatteryLevel() {
        BatterySystem battery = new BatterySystem(100f);
        assertEquals(100f, battery.getBatteryLevel(), 0.0001);
    }

    @Test
    void calculateDrainReturnsPositiveValue() {
        BatterySystem battery = new BatterySystem(100f);
        float drain = battery.calculateDrain(5f, 1.0f);
        assertTrue(drain > 0);
    }

    @Test
    void calculateDrainIncreasesWithVelocity() {
        BatterySystem battery = new BatterySystem(100f);
        float drainSlow = battery.calculateDrain(1f, 1.0f);
        float drainFast = battery.calculateDrain(10f, 1.0f);
        assertTrue(drainFast > drainSlow);
    }

    @Test
    void calculateDrainIncreasesWithTime() {
        BatterySystem battery = new BatterySystem(100f);
        float drainShort = battery.calculateDrain(5f, 0.5f);
        float drainLong = battery.calculateDrain(5f, 1.0f);
        assertTrue(drainLong > drainShort);
    }

    @Test
    void drainBatteryReducesBatteryLevel() {
        BatterySystem battery = new BatterySystem(100f);
        float initialBattery = battery.getBatteryLevel();
        battery.drainBattery(10f);
        assertTrue(battery.getBatteryLevel() < initialBattery);
        assertEquals(90f, battery.getBatteryLevel(), 0.0001);
    }

    @Test
    void drainBatteryReturnsTrueWhenDead() {
        BatterySystem battery = new BatterySystem(10f);
        boolean isDead = battery.drainBattery(10f);
        assertTrue(isDead);
        assertEquals(0f, battery.getBatteryLevel(), 0.0001);
    }

    @Test
    void drainBatteryReturnsFalseWhenAlive() {
        BatterySystem battery = new BatterySystem(100f);
        boolean isDead = battery.drainBattery(10f);
        assertFalse(isDead);
    }

    @Test
    void drainBatteryCannotGoBelowZero() {
        BatterySystem battery = new BatterySystem(10f);
        battery.drainBattery(100f);
        assertEquals(0f, battery.getBatteryLevel(), 0.0001);
    }

    @Test
    void setBatteryLevelUpdatesBattery() {
        BatterySystem battery = new BatterySystem(100f);
        battery.setBatteryLevel(50f);
        assertEquals(50f, battery.getBatteryLevel(), 0.0001);
    }

    @Test
    void setBatteryLevelThrowsOnNegative() {
        BatterySystem battery = new BatterySystem(100f);
        assertThrows(IllegalArgumentException.class, () -> battery.setBatteryLevel(-1f));
    }

    @Test
    void setBatteryLevelAcceptsZero() {
        BatterySystem battery = new BatterySystem(100f);
        battery.setBatteryLevel(0f);
        assertEquals(0f, battery.getBatteryLevel(), 0.0001);
    }
}
