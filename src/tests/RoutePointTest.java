package tests;

import model.RoutePoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoutePointTest {

    @Test
    void constructorAndGettersWork() {
        RoutePoint p = new RoutePoint(10.5f, -20.25f, 1000f);
        assertEquals(10.5f, p.getLongitude(), 0.0001);
        assertEquals(-20.25f, p.getLatitude(), 0.0001);
        assertEquals(1000f, p.getAltitude(), 0.0001);
    }

    @Test
    void toStringFormatsCorrectly() {
        RoutePoint p = new RoutePoint(1.0f, 2.0f, 3.0f);
        assertEquals("(1.0, 2.0, 3.0)", p.toString());
    }
}
