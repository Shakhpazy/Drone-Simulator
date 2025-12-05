package tests;

import model.RouteGenerator;
import model.RoutePoint;
import java.util.ArrayList;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test RouteGenerator
 * @author Yusuf
 * @version 12-05
 */
public class RouteGeneratorTest {

    @Test
    void generateRouteProducesNonEmptyRouteWithinBounds() {
        RouteGenerator gen = new RouteGenerator();
        ArrayList<RoutePoint> route = gen.generateRoute();

        assertNotNull(route);
        assertFalse(route.isEmpty());

        for (RoutePoint p : route) {
            assertTrue(p.getLatitude()  >= RouteGenerator.MIN_LAT && p.getLatitude()  <= RouteGenerator.MAX_LAT);
            assertTrue(p.getLongitude() >= RouteGenerator.MIN_LON && p.getLongitude() <= RouteGenerator.MAX_LON);
            assertTrue(p.getAltitude()  >= RouteGenerator.MIN_ALT && p.getAltitude()  <= RouteGenerator.MAX_ALT);
        }
    }

    // Because generateRoute randomly picks rectangle, and random 10 points
    // just check a few times that we sometimes get >=4 points and <=10.
    @RepeatedTest(5)
    void generateRouteSizeReasonable() {
        RouteGenerator gen = new RouteGenerator();
        ArrayList<RoutePoint> route = gen.generateRoute();
        assertTrue(route.size() >= 4);
        assertTrue(route.size() <= 10);
    }

    @Test
    void getRandomPointWithinBounds() throws Exception {
        RouteGenerator gen = new RouteGenerator();

        var method = RouteGenerator.class.getDeclaredMethod("getRandomPoint");
        method.setAccessible(true);

        RoutePoint p = (RoutePoint) method.invoke(gen);

        assertTrue(p.getLatitude()  >= RouteGenerator.MIN_LAT && p.getLatitude()  <= RouteGenerator.MAX_LAT);
        assertTrue(p.getLongitude() >= RouteGenerator.MIN_LON && p.getLongitude() <= RouteGenerator.MAX_LON);
        assertTrue(p.getAltitude()  >= RouteGenerator.MIN_ALT && p.getAltitude()  <= RouteGenerator.MAX_ALT);
    }

}
