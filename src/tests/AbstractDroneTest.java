package tests;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the abstract drone
 * @author Yusuf
 * @version 12-05
 */
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
    void constructorThrowsOnInvalidAltitude() {
        ArrayList<RoutePoint> testRoute = new ArrayList<>();
        testRoute.add(new RoutePoint(0f, 0f, -10f)); // Altitude below min (0)
        testRoute.add(new RoutePoint(5f, 5f, 20f));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Drone(5f, 100, testRoute));
    }

    @Test
    void constructorThrowsOnInvalidVelocity() {
        ArrayList<RoutePoint> testRoute = new ArrayList<>();
        testRoute.add(new RoutePoint(0f, 0f, 10f));
        
        // Velocity too high (max is 10)
        assertThrows(IllegalArgumentException.class, 
            () -> new Drone(15f, 100, testRoute));
        
        // Velocity too low (min is 0)
        assertThrows(IllegalArgumentException.class, 
            () -> new Drone(-1f, 100, testRoute));
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
    void setVelocityThrowsOnOutOfBounds() {
        // Velocity too high
        assertThrows(IllegalArgumentException.class, () -> drone.setVelocity(15f));
        
        // Velocity too low
        assertThrows(IllegalArgumentException.class, () -> drone.setVelocity(-1f));
    }

    @Test
    void setVelocityAcceptsValidBounds() {
        drone.setVelocity(0f);
        assertEquals(0f, drone.getVelocity(), 0.0001);
        
        drone.setVelocity(10f);
        assertEquals(10f, drone.getVelocity(), 0.0001);
        
        drone.setVelocity(5f);
        assertEquals(5f, drone.getVelocity(), 0.0001);
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
    void batteryDrainedIncreasesWithHigherVelocity() throws Exception {
        var method = AbstractDrone.class.getDeclaredMethod("batteryDrained", float.class);
        method.setAccessible(true);

        drone.setVelocity(0f);
        float drainAtZero = (float) method.invoke(drone, 1f);

        drone.setVelocity(10f);
        float drainAtMax = (float) method.invoke(drone, 1f);

        assertTrue(drainAtMax > drainAtZero, 
            "Battery drain should be higher at max velocity than at zero velocity");
    }

    @Test
    void batteryDrainedScalesWithDeltaTime() throws Exception {
        var method = AbstractDrone.class.getDeclaredMethod("batteryDrained", float.class);
        method.setAccessible(true);

        float drain1s = (float) method.invoke(drone, 1f);
        float drain2s = (float) method.invoke(drone, 2f);
        float drain05s = (float) method.invoke(drone, 0.5f);

        assertEquals(drain1s * 2f, drain2s, 0.0001f, 
            "Battery drain should scale linearly with deltaTime");
        assertEquals(drain1s * 0.5f, drain05s, 0.0001f, 
            "Battery drain should scale linearly with deltaTime");
    }

    @Test
    void setPrevTelemetryRecordUpdatesPreviousRecord() {
        TelemetryRecord newRecord = new TelemetryRecord(
            drone.getId(), 50f, 60f, 70f, 8f, 90f, 180f, System.currentTimeMillis()
        );
        
        drone.setPrevTelemetryRecord(newRecord);
        TelemetryRecord retrieved = drone.getPreviousTelemetryRecord();
        
        assertEquals(newRecord.id(), retrieved.id());
        assertEquals(newRecord.longitude(), retrieved.longitude(), 0.0001);
        assertEquals(newRecord.latitude(), retrieved.latitude(), 0.0001);
        assertEquals(newRecord.altitude(), retrieved.altitude(), 0.0001);
        assertEquals(newRecord.velocity(), retrieved.velocity(), 0.0001);
        assertEquals(newRecord.batteryLevel(), retrieved.batteryLevel(), 0.0001);
    }

    @Test
    void getTotalDronesIncrementsWithEachDrone() {
        int initialCount = AbstractDrone.getTotalDrones();
        
        ArrayList<RoutePoint> newRoute = new ArrayList<>();
        newRoute.add(new RoutePoint(0f, 0f, 10f));
        Drone newDrone = new Drone(5f, 100, newRoute);
        
        int afterCount = AbstractDrone.getTotalDrones();
        
        assertEquals(initialCount + 1, afterCount, 
            "Total drone count should increment when a new drone is created");
    }

    @Test
    void setOrientationUpdatesOrientation() {
        drone.setOrientation(90f);
        assertEquals(90f, drone.getOrientation().getDegree(), 0.0001);
        
        drone.setOrientation(180f);
        assertEquals(180f, drone.getOrientation().getDegree(), 0.0001);
        
        drone.setOrientation(270f);
        assertEquals(270f, drone.getOrientation().getDegree(), 0.0001);
        
        drone.setOrientation(360f); //should be 0
        assertEquals(0f, drone.getOrientation().getDegree(), 0.0001);
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
    void updateDroneBatteryDrainedExceedsCurrentBattery() {
        drone.setBatteryLevel(5f);
        
        // Try to drain more battery than available
        drone.updateDrone(10f, 20f, 30f, 10f, 8f, 180f);
        
        assertEquals(0f, drone.getBatteryLevel(), 0.0001, 
            "Battery should not go below 0");
        assertFalse(drone.isAlive(), 
            "Drone should be marked dead when battery reaches 0");
    }

    @Test
    void updateDroneClampsAltitudeToZero() {
        drone.updateDrone(10f, 20f, -50f, 2.5f, 8f, 180f);
        
        assertEquals(0f, drone.getAltitude(), 0.0001, 
            "Altitude should be clamped to 0 if negative");
    }

    @Test
    void collidedSetsAltitudeZeroAndDead() {
        drone.collided();
        assertEquals(0f, drone.getAltitude(), 0.0001);
        assertFalse(drone.isAlive());
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

    @Test
    void setIsAliveUpdatesAliveStatus() {
        assertTrue(drone.isAlive());
        
        drone.setIsAlive(false);
        assertFalse(drone.isAlive());
        
        drone.setIsAlive(true);
        assertTrue(drone.isAlive());
    }

    @Test
    void getMyLastAnomalyReturnsNullInitially() {
        assertNull(drone.getMyLastAnomaly());
    }
}
