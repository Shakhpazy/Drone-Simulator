package tests;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractDroneTest {

    private Drone drone;
    private ArrayList<RoutePoint> route;

    @BeforeEach
    void setup() {
        route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 10f));
        route.add(new RoutePoint(5f, 5f, 20f));

        // velocity = 5, battery = 100
        drone = new Drone(5f, 100, route);
    }

    @Test
    void constructorInitializesTelemetryAndAlive() {
        assertTrue(drone.isAlive());
        assertNotNull(drone.getPreviousTelemetryRecord());
        assertEquals(drone.getId(), drone.getPreviousTelemetryRecord().id());
    }

    @Test
    void setAltitudeNeverBelowZero() {
        drone.setAltitude(-50f);
        assertEquals(0f, drone.getAltitude(), 0.0001);
    }

    @Test
    void setBatteryLevelThrowsOnNegative() {
        assertThrows(IllegalArgumentException.class, () -> drone.setBatteryLevel(-1f));
    }

    @Test
    void setBatteryLevelZeroMarksDroneDead() {
        drone.setBatteryLevel(0f);
        assertFalse(drone.isAlive());
    }

    @Test
    void updateDroneUpdatesStateAndBattery() {
        float oldBattery = drone.getBatteryLevel();

        drone.updateDrone(10f, 20f, 30f, 2.5f, 8f, 180f);

        assertEquals(10f, drone.getLongitude(), 0.0001);
        assertEquals(20f, drone.getLatitude(), 0.0001);
        assertEquals(30f, drone.getAltitude(), 0.0001);
        assertEquals(8f, drone.getVelocity(), 0.0001);
        assertEquals(180f, drone.getOrientation().getDegree(), 0.0001);
        assertEquals(oldBattery - 2.5f, drone.getBatteryLevel(), 0.0001);
    }

    @Test
    void collidedSetsAltitudeZeroAndDead() {
        drone.collided();
        assertEquals(0f, drone.getAltitude(), 0.0001);
        assertFalse(drone.isAlive());
    }

    @Test
    void batteryDrainedIncreasesWithVelocity() throws Exception {
        // Use reflection because batteryDrained is protected in AbstractDrone
        var method = AbstractDrone.class.getDeclaredMethod("batteryDrained", float.class);
        method.setAccessible(true);

        float drain = (float) method.invoke(drone, 1f);
        assertTrue(drain > 0f);
    }

    @Test
    void generateTelemetryRecordReflectsCurrentState() {
        drone.setLongitude(11f);
        drone.setLatitude(22f);
        drone.setAltitude(33f);
        drone.setVelocity(4f);
        drone.setBatteryLevel(77f);
        drone.setOrientation(135f);

        TelemetryRecord rec = drone.generateTelemetryRecord();

        assertEquals(drone.getId(), rec.id());
        assertEquals(11f, rec.longitude(), 0.0001);
        assertEquals(22f, rec.latitude(), 0.0001);
        assertEquals(33f, rec.altitude(), 0.0001);
        assertEquals(4f, rec.velocity(), 0.0001);
        assertEquals(77f, rec.batteryLevel(), 0.0001);
        assertEquals(135f, rec.orientation(), 0.0001);
    }

    @Test
    void toStringContainsKeyFields() {
        String out = drone.toString();
        assertTrue(out.contains("Drone{id="));
        assertTrue(out.contains("lon="));
        assertTrue(out.contains("lat="));
        assertTrue(out.contains("alt="));
        assertTrue(out.contains("vel="));
        assertTrue(out.contains("battery="));
    }
}
