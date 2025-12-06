package tests;

import model.*;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test TelemetryGenerator
 * @author Yusuf
 * @version 12-05
 */
public class TelemetryGeneratorTest {

    // Reset singleton before every test
    @BeforeEach
    void resetSingleton() throws Exception {
        Field f = TelemetryGenerator.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    private ArrayList<RoutePoint> buildRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 0f));
        route.add(new RoutePoint(10f, 0f, 0f));
        return route;
    }

    @Test
    void singletonReturnsSameInstance() {
        TelemetryGenerator a = TelemetryGenerator.getInstance(0);
        TelemetryGenerator b = TelemetryGenerator.getInstance(50);
        assertSame(a, b);
    }

    @Test
    void addAndRemoveDroneWorks() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);
        Drone d = new Drone(5f, 100, buildRoute());

        gen.addDrone(d);
        assertEquals(1, gen.getMyDrones().size());

        gen.removeDrone(d);
        assertEquals(0, gen.getMyDrones().size());
    }

    @Test
    void processSkipsDeadDrones() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);
        Drone d = new Drone(5f, 100, buildRoute());

        d.collided(); // mark drone dead

        gen.addDrone(d);

        Map<DroneInterface, TelemetryRecord[]> map = gen.processAllDrones(1f);

        assertTrue(map.isEmpty());
    }

    @Test
    void processCallsNormalMoveWhenNoAnomaly() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0); // 0% anomaly
        Drone d = new Drone(5f, 100, buildRoute());

        float beforeLon = d.getLongitude();

        gen.addDrone(d);
        gen.processAllDrones(1f);

        assertNotEquals(beforeLon, d.getLongitude()); // drone moved normally
    }

    @Test
    void processCallsRandomMoveWhenAlwaysAnomaly() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(100); // 100% anomaly
        Drone d = new Drone(5f, 100, buildRoute());

        gen.addDrone(d);
        gen.processAllDrones(1f);

        assertNotNull(d.getMyLastAnomaly());
    }

    @Test
    void prevTelemetryRecordGetsUpdated() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);
        Drone d = new Drone(5f, 100, buildRoute());

        gen.addDrone(d);
        Map<DroneInterface, TelemetryRecord[]> map = gen.processAllDrones(1f);

        TelemetryRecord prev = d.getPreviousTelemetryRecord();
        assertNotNull(prev);
        assertEquals(prev.id(), d.getId());
    }

    @Test
    void collisionMarksBothDronesDead() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);

        // Two drones within collision threshold
        Drone d1 = new Drone(5f, 100, buildRoute());
        Drone d2 = new Drone(5f, 100, buildRoute());

        // Place them at nearly identical coordinates
        d1.updateDrone(0, 0, 0, 0, 0, 0);
        d2.updateDrone(1, 1, 1, 0, 0, 0); // close enough to collide

        gen.addDrone(d1);
        gen.addDrone(d2);

        gen.processAllDrones(1f);

        assertFalse(d1.isAlive());
        assertFalse(d2.isAlive());
    }

    @Test
    void collisionSendsDeadStateTelemetry() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);

        // Two drones that will collide
        Drone d1 = new Drone(5f, 100, buildRoute());
        Drone d2 = new Drone(5f, 100, buildRoute());

        // Place them very close (within collision threshold of 4 squared distance)
        d1.updateDrone(1f, 1f, 1f, 0f, 5f, 0f);
        d2.updateDrone(1f, 1f, 1f, 0f, 5f, 0f);

        gen.addDrone(d1);
        gen.addDrone(d2);

        // Store initial states
        float d1InitialBattery = d1.getBatteryLevel();
        float d1InitialAltitude = d1.getAltitude();

        // Process - collisions should happen and dead state telemetry should be sent
        Map<DroneInterface, TelemetryRecord[]> map = gen.processAllDrones(1f);

        // Both drones should be dead
        assertFalse(d1.isAlive());
        assertFalse(d2.isAlive());

        // Both drones should have dead state (0 altitude, 0 velocity)
        assertEquals(0f, d1.getAltitude(), 0.0001);
        assertEquals(0f, d1.getVelocity(), 0.0001);
        assertEquals(0f, d2.getAltitude(), 0.0001);
        assertEquals(0f, d2.getVelocity(), 0.0001);

        // Most importantly: The telemetry map should contain the dead state telemetry
        // Since collisions happen before telemetry generation, the telemetry should show dead state
        assertTrue(map.containsKey(d1));
        assertTrue(map.containsKey(d2));

        TelemetryRecord[] d1Records = map.get(d1);
        TelemetryRecord[] d2Records = map.get(d2);

        assertNotNull(d1Records);
        assertNotNull(d2Records);
        assertEquals(2, d1Records.length); // [prev, curr]
        assertEquals(2, d2Records.length);

        // Current telemetry should show dead state (0 altitude, 0 battery)
        TelemetryRecord d1Current = d1Records[1];
        TelemetryRecord d2Current = d2Records[1];

        assertEquals(0f, d1Current.altitude(), 0.0001, "Collided drone should send 0 altitude in telemetry");
        assertEquals(0f, d1Current.velocity(), 0.0001, "Collided drone should send 0 velocity in telemetry");
        // Battery might not be exactly 0 if it was drained, but altitude and velocity should be 0
        assertEquals(0f, d2Current.altitude(), 0.0001, "Collided drone should send 0 altitude in telemetry");
        assertEquals(0f, d2Current.velocity(), 0.0001, "Collided drone should send 0 velocity in telemetry");
    }

    @Test
    void collisionHappensBeforeTelemetryGeneration() {
        TelemetryGenerator gen = TelemetryGenerator.getInstance(0);

        Drone d1 = new Drone(5f, 100, buildRoute());
        Drone d2 = new Drone(5f, 100, buildRoute());

        // Place them to collide
        d1.updateDrone(0f, 0f, 0f, 0f, 5f, 0f);
        d2.updateDrone(1f, 1f, 1f, 0f, 5f, 0f);

        gen.addDrone(d1);
        gen.addDrone(d2);

        // Process all drones
        Map<DroneInterface, TelemetryRecord[]> map = gen.processAllDrones(1f);

        // Verify that the telemetry sent shows the dead state, not the pre-collision state
        TelemetryRecord[] d1Records = map.get(d1);
        TelemetryRecord[] d2Records = map.get(d2);

        if (d1Records != null && d1Records.length > 1) {
            TelemetryRecord d1Current = d1Records[1];
            // The current telemetry should reflect the dead state after collision
            assertEquals(0f, d1Current.altitude(), 0.0001, 
                "Telemetry should show dead state (0 altitude) after collision, not pre-collision state");
        }

        if (d2Records != null && d2Records.length > 1) {
            TelemetryRecord d2Current = d2Records[1];
            assertEquals(0f, d2Current.altitude(), 0.0001,
                "Telemetry should show dead state (0 altitude) after collision, not pre-collision state");
        }
    }
}
