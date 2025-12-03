package tests;

import model.DroneInterface;
import model.RoutePoint;
import model.DroneGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DroneGeneratorTest {

    @Test
    void createDroneThrowsOnNullRoute() {
        DroneGenerator gen = new DroneGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.createDrone(null));
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
        assertEquals(100f, d.getBatteryLevel(), 0.0001); // from your Drone ctor
    }
}
