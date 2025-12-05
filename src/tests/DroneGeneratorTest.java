package tests;

import model.DroneInterface;
import model.RoutePoint;
import model.DroneGenerator;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test drone generator
 * @author Yusuf
 * @version 12-05
 */
class DroneGeneratorTest {

    @Test
    void createDroneThrowsOnNullRoute() {
        DroneGenerator gen = new DroneGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.createDrone(null));
    }

    @Test
    void createDroneThrowsOnEmptyRoute() {
        DroneGenerator gen = new DroneGenerator();
        ArrayList<RoutePoint> emptyRoute = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> gen.createDrone(emptyRoute));
    }

    @Test
    void createDroneInitializesDroneFromRoute() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));

        DroneInterface d = gen.createDrone(route);

        assertNotNull(d);
        assertEquals(10f, d.getLongitude(), 0.0001);
        assertEquals(20f, d.getLatitude(), 0.0001);
        assertEquals(30f, d.getAltitude(), 0.0001);
        assertEquals(100f, d.getBatteryLevel(), 0.0001);
    }

    @Test
    void createDroneUsesDefaultVelocityAndBattery() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));

        DroneInterface d = gen.createDrone(route);

        assertEquals(1f, d.getVelocity(), 0.0001, "Default velocity should be 1");
        assertEquals(100f, d.getBatteryLevel(), 0.0001, "Default battery should be 100");
    }

    @Test
    void createDroneStartsAtFirstRoutePoint() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));
        route.add(new RoutePoint(50f, 60f, 70f));
        route.add(new RoutePoint(100f, 110f, 120f));

        DroneInterface d = gen.createDrone(route);

        assertEquals(10f, d.getLongitude(), 0.0001);
        assertEquals(20f, d.getLatitude(), 0.0001);
        assertEquals(30f, d.getAltitude(), 0.0001);
    }

    @Test
    void createDroneSetsNextPointToSecondRoutePoint() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));
        route.add(new RoutePoint(50f, 60f, 70f));

        DroneInterface d = gen.createDrone(route);
        RoutePoint next = d.getNextPoint();

        assertEquals(50f, next.getLongitude(), 0.0001);
        assertEquals(60f, next.getLatitude(), 0.0001);
        assertEquals(70f, next.getAltitude(), 0.0001);
    }

    @Test
    void createDroneWithSingleRoutePointWrapsToFirst() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));

        DroneInterface d = gen.createDrone(route);
        RoutePoint next = d.getNextPoint();

        // With only one point, nextPoint should wrap back to the first point
        assertEquals(10f, next.getLongitude(), 0.0001);
        assertEquals(20f, next.getLatitude(), 0.0001);
        assertEquals(30f, next.getAltitude(), 0.0001);
    }

    @Test
    void createDroneWorksWithMultipleRoutePoints() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 10f));
        route.add(new RoutePoint(10f, 10f, 20f));
        route.add(new RoutePoint(20f, 20f, 30f));
        route.add(new RoutePoint(30f, 30f, 40f));

        DroneInterface d = gen.createDrone(route);

        assertNotNull(d);
        assertEquals(0f, d.getLongitude(), 0.0001);
        assertEquals(0f, d.getLatitude(), 0.0001);
        assertEquals(10f, d.getAltitude(), 0.0001);
        
        RoutePoint next = d.getNextPoint();
        assertEquals(10f, next.getLongitude(), 0.0001);
        assertEquals(10f, next.getLatitude(), 0.0001);
        assertEquals(20f, next.getAltitude(), 0.0001);
    }

    @Test
    void createDroneCreatesAliveDrone() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));

        DroneInterface d = gen.createDrone(route);

        assertTrue(d.isAlive(), "Newly created drone should be alive");
    }

    @Test
    void createDroneInitializesTelemetryRecord() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(10f, 20f, 30f));

        DroneInterface d = gen.createDrone(route);

        assertNotNull(d.getPreviousTelemetryRecord());
        assertEquals(d.getId(), d.getPreviousTelemetryRecord().id());
        assertEquals(10f, d.getPreviousTelemetryRecord().longitude(), 0.0001);
        assertEquals(20f, d.getPreviousTelemetryRecord().latitude(), 0.0001);
        assertEquals(30f, d.getPreviousTelemetryRecord().altitude(), 0.0001);
    }

    @Test
    void createDroneAssignsUniqueIds() {
        DroneGenerator gen = new DroneGenerator();

        ArrayList<RoutePoint> route1 = new ArrayList<>();
        route1.add(new RoutePoint(10f, 20f, 30f));

        ArrayList<RoutePoint> route2 = new ArrayList<>();
        route2.add(new RoutePoint(40f, 50f, 60f));

        ArrayList<RoutePoint> route3 = new ArrayList<>();
        route3.add(new RoutePoint(70f, 80f, 90f));

        DroneInterface d1 = gen.createDrone(route1);
        DroneInterface d2 = gen.createDrone(route2);
        DroneInterface d3 = gen.createDrone(route3);

        assertNotEquals(d1.getId(), d2.getId());
        assertNotEquals(d2.getId(), d3.getId());
        assertNotEquals(d1.getId(), d3.getId());
    }
}
