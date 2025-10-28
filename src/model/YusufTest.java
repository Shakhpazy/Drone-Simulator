package model;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.HashMap;

public class YusufTest {

    public static void main(String[] args) {
        // Route 1 within 900 x 500 world
        ArrayList<RoutePoint> route1 = new ArrayList<>();
        route1.add(new RoutePoint(0,   0,   100));   // bottom-left
        route1.add(new RoutePoint(900, 0,   120));   // bottom-right
        route1.add(new RoutePoint(900, 500, 140));   // top-right
        route1.add(new RoutePoint(0,   500, 160));   // top-left

        // Route 2 (diagonal across same space)
        ArrayList<RoutePoint> route2 = new ArrayList<>();
        route2.add(new RoutePoint(0,   0,   200));
        route2.add(new RoutePoint(900, 500, 220));
        route2.add(new RoutePoint(0,   500, 240));
        route2.add(new RoutePoint(900, 0,   260));

        // Create drones
        Drone drone1 = new Drone(5, 100, Orientation.NORTH, route1);
        Drone drone2 = new Drone(7, 100, Orientation.EAST, route2);

        // Add them into the telemetry generator
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone1);
        drones.add(drone2);

        TelemetryGenerator generator = new TelemetryGenerator(drones);
        //Start
        System.out.println("---- Start ----");
        for (DroneInterface d : drones) {
            System.out.printf("Drone %d | Lon=%.2f Lat=%.2f Alt=%.2f Vel=%.2f Battery=%d%n",
                    d.getId(),
                    d.getLongitude(),
                    d.getLatitude(),
                    d.getAltitude(),
                    d.getVelocity(),
                    d.getBatteryLevel());
        }
        System.out.print("\n");

        // Run simulation for 20 ticks
        for (int tick = 0; tick < 20; tick++) {
            System.out.println("---- TICK " + tick + " ----");
            generator.processAllDrones();

            // Print out state of each drone
            for (DroneInterface d : drones) {
                System.out.printf("Drone %d | Lon=%.2f Lat=%.2f Alt=%.2f Vel=%.2f Battery=%d%n",
                        d.getId(),
                        d.getLongitude(),
                        d.getLatitude(),
                        d.getAltitude(),
                        d.getVelocity(),
                        d.getBatteryLevel());
            }
            System.out.println();
        }

        Testmap(generator);
    }

    /**
     * When data is getting passed on the Hashmap from String to Object
     * You must cast the object to its intended type when you want to use
     * it.
     *
     * @param generator
     */
    private static void Testmap(TelemetryGenerator generator) {
        HashMap<String, Object> dictionary = generator.getMybefore();
        float altitude = (float) dictionary.get("altitude");
        System.out.println(dictionary.get("altitude") + " " + altitude);
    }
}
