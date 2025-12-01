package tests;

import org.junit.jupiter.api.Test;
import model.RoutePoint;
import model.Drone;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DroneTest {
    private ArrayList<RoutePoint> buildSimpleRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(1f, 1f, 1f)); // start
        route.add(new RoutePoint(100f, 100f, 100f)); // east
        return route;
    }

    @Test
    void constructorThrowsOnEmptyRoute() {
        ArrayList<RoutePoint> empty = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new Drone(5f, 100, empty));
    }

    @Test
    void getNextPointCyclesThroughRoute() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        RoutePoint p1 = d.getNextPoint();
        assertEquals(route.get(1), p1);

        d.setNextRoute();
        RoutePoint p2 = d.getNextPoint();
        assertEquals(route.get(0), p2);
    }

    @Test
    void setVelocityRejectsOutOfBounds() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        assertThrows(IllegalArgumentException.class, () -> d.setVelocity(-1f));
        assertThrows(IllegalArgumentException.class, () -> d.setVelocity(1000f));

        d.setVelocity(0f);
        assertEquals(0f, d.getVelocity(), 0.0001);
        d.setVelocity(10f);
        assertEquals(10f, d.getVelocity(), 0.0001);
    }

    @Test
    void getNextMoveMovesTowardNextWaypointAndDrainsBattery() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        float oldLon = d.getLongitude();
        float oldBattery = d.getBatteryLevel();

        d.getNextMove(1.0f);

        assertNotEquals(oldLon, d.getLongitude());
        assertTrue(d.getBatteryLevel() < oldBattery);
    }

    @Test
    void getNextRandomMoveKeepsStateWithinBounds() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        for (int i = 0; i < 20; i++) {
            d.getNextRandomMove(0.5f);
            assertTrue(d.getAltitude() >= 0f);
            assertTrue(d.getVelocity() >= 0f);
            assertTrue(d.getVelocity() <= 10f);
        }
    }
}
