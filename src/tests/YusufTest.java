package tests;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class YusufTest {

    public static void main(String[] args) {
        // Route for a single drone (rectangle around the map)
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(100, 100, 110)); // bottom-left
        route.add(new RoutePoint(130, 100, 115)); // bottom-right (30 units)
        route.add(new RoutePoint(130, 120, 120)); // top-right    (20 units)
        route.add(new RoutePoint(100, 120, 125)); // top-left     (30 units)
        route.add(new RoutePoint(100, 100, 130)); // back to start(20 units)

        // Create one drone with velocity=5, battery=100, facing NORTH
        Drone drone = new Drone(5, 100, Orientation.NORTH, route);

        // Add drone to simulation
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone);
        TelemetryGenerator generator = new TelemetryGenerator(drones);

        // Print starting state
        System.out.println("---- START ----");
        printDrone(drone);
        System.out.println();

        // Run simulation for 20 ticks
        for (int tick = 0; tick < 20; tick++) {
            System.out.println("---- TICK " + tick + " ----");
            generator.processAllDrones();

            // Print state of the single drone
            printDrone(drone);
            System.out.println();
        }

        // Example: pull latest telemetry from HashMap
        Testmap(generator);
    }

    private static void printDrone(DroneInterface d) {
        RoutePoint target = d.getNextPoint(); // the waypoint it’s heading to
        System.out.printf(
                "Drone %d | Lon=%.2f Lat=%.2f Alt=%.2f Vel=%.2f Battery=%d | Heading to (%.0f, %.0f, %.0f)%n",
                d.getId(),
                d.getLongitude(),
                d.getLatitude(),
                d.getAltitude(),
                d.getVelocity(),
                d.getBatteryLevel(),
                target.getLongitude(),
                target.getLatitude(),
                target.getAltitude()
        );
    }

    private static void Testmap(TelemetryGenerator generator) {
        HashMap<String, Object> dictionary = generator.getMyBeforeTelemetryMap();
        float altitude = (float) dictionary.get("altitude");
        System.out.println("Telemetry (cast example) -> altitude raw=" +
                dictionary.get("altitude") + " casted=" + altitude);
    }
}
