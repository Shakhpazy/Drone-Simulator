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
}
