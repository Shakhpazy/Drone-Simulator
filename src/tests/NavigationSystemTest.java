package tests;

import org.junit.jupiter.api.Test;
import model.NavigationSystem;
import model.RoutePoint;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NavigationSystem class
 * @author Yusuf
 */
public class NavigationSystemTest {

    private ArrayList<RoutePoint> buildSimpleRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 0f));
        route.add(new RoutePoint(100f, 100f, 100f));
        return route;
    }

    @Test
    void constructorThrowsOnNullRoute() {
        assertThrows(IllegalArgumentException.class, () -> new NavigationSystem(null));
    }

    @Test
    void constructorThrowsOnEmptyRoute() {
        ArrayList<RoutePoint> empty = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new NavigationSystem(empty));
    }

    @Test
    void getNextPointReturnsSecondPointInitially() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        RoutePoint next = nav.getNextPoint();
        assertEquals(route.get(1), next);
    }

    @Test
    void advanceToNextPointCyclesThroughRoute() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        RoutePoint p1 = nav.getNextPoint();
        assertEquals(route.get(1), p1);
        
        nav.advanceToNextPoint();
        RoutePoint p2 = nav.getNextPoint();
        assertEquals(route.get(0), p2);
        
        nav.advanceToNextPoint();
        RoutePoint p3 = nav.getNextPoint();
        assertEquals(route.get(1), p3);
    }

    @Test
    void calculateNextMoveMovesTowardWaypoint() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        NavigationSystem.MovementResult result = nav.calculateNextMove(0f, 0f, 0f, 5f, 1.0f);
        
        assertTrue(result.longitude > 0f);
        assertTrue(result.latitude > 0f);
        assertTrue(result.altitude > 0f);
    }

    @Test
    void calculateNextMoveReachesWaypointWhenClose() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        // Start very close to waypoint
        NavigationSystem.MovementResult result = nav.calculateNextMove(99f, 99f, 99f, 10f, 1.0f);
        
        assertTrue(result.waypointReached);
        assertEquals(100f, result.longitude, 0.1f);
        assertEquals(100f, result.latitude, 0.1f);
        assertEquals(100f, result.altitude, 0.1f);
    }

    @Test
    void calculateNextMoveHandlesExactWaypoint() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        // Start exactly on waypoint
        NavigationSystem.MovementResult result = nav.calculateNextMove(100f, 100f, 100f, 5f, 1.0f);
        
        assertTrue(result.waypointReached);
    }

    @Test
    void calculateNextMoveIncreasesVelocityWhenFar() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        NavigationSystem.MovementResult result = nav.calculateNextMove(0f, 0f, 0f, 1f, 1.0f);
        
        assertTrue(result.velocity > 1f);
    }

    @Test
    void calculateNextMoveDecreasesVelocityWhenNear() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        
        NavigationSystem.MovementResult result = nav.calculateNextMove(95f, 95f, 95f, 10f, 1.0f);
        
        assertTrue(result.velocity < 10f);
    }

    @Test
    void calculateNextMoveMaintainsMinimumVelocity() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(0f, 0f, 0f));
        route.add(new RoutePoint(0.01f, 0.01f, 0.01f)); // very close
        
        NavigationSystem nav = new NavigationSystem(route);
        NavigationSystem.MovementResult result = nav.calculateNextMove(0f, 0f, 0f, 1f, 1.0f);
        
        assertTrue(result.velocity >= 0.5f); // minVelocityToMove
    }

    @Test
    void getAccelerationStepReturnsCorrectValue() {
        ArrayList<RoutePoint> route = buildSimpleRoute();
        NavigationSystem nav = new NavigationSystem(route);
        assertEquals(0.3f, nav.getAccelerationStep(), 0.0001);
    }
}
