package tests;

import org.junit.jupiter.api.Test;
import model.RoutePoint;
import model.Drone;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test Drone
 * @author Yusuf
 * @version 12-05
 */
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
    void constructorStartsAtFirstRoutePoint() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        RoutePoint start = route.get(0);

        assertEquals(start.getLongitude(), d.getLongitude(), 0.0001);
        assertEquals(start.getLatitude(),  d.getLatitude(),  0.0001);
        assertEquals(start.getAltitude(),  d.getAltitude(),  0.0001);
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
    void setNextRouteCyclesMultipleTimes() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        d.setNextRoute(); // now 0
        assertEquals(route.get(0), d.getNextPoint());

        d.setNextRoute(); // now 1
        assertEquals(route.get(1), d.getNextPoint());

        d.setNextRoute(); // now 0 again
        assertEquals(route.get(0), d.getNextPoint());
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

    @Test
    void getNextMoveHandlesBeingExactlyOnWaypoint() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        // Move drone exactly onto waypoint 1
        d.updateDrone(
                route.get(1).getLongitude(),
                route.get(1).getLatitude(),
                route.get(1).getAltitude(),
                0,
                5f,
                0
        );

        int before = d.getNextPoint().hashCode();
        d.getNextMove(1f); // should skip point instead of freezing
        int after = d.getNextPoint().hashCode();

        assertNotEquals(before, after);
    }

    @Test
    void getNextMoveIncreasesVelocityWhenFar() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(1f, 100, route); // deliberately slow

        d.getNextMove(1f);

        assertTrue(d.getVelocity() > 1f);
    }

    @Test
    void getNextMoveDecreasesVelocityNearWaypointButNotBelowMin() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 0f));
        route.add(new RoutePoint(0.01f, 0.01f, 0.01f)); // very close

        Drone d = new Drone(1f, 100, route);

        d.getNextMove(1f);

        assertTrue(d.getVelocity() >= 0.5f);  // minVelocityToMove
    }

    @Test
    void randomMoveUpdatesLastAnomaly() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        Drone d = new Drone(5f, 100, route);

        d.getNextRandomMove(1f);

        assertNotNull(d.getMyLastAnomaly());
    }



}
