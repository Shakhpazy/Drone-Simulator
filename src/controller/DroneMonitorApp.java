package controller;

import model.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import view.MonitorDashboard;

/**
 * The DroneMonitorApp provides the entry point for the simulator.
 * <p>
 * It will initialize and update the view, as well as interact with
 * the model to drive telemetry generation and anomaly checks.
 *
 * @author Natan Artemiev
 * @version 11/1/2025
 */
public class DroneMonitorApp implements PropertyChangeListener {
    /**
     * The main entry point for the program. Initializes the UI and creates drones. Initializes the TelemetryGenerator
     * in order to update each drone. Listens for a change of state in the model and updates the view.
     *
     * @param theArgs - The command line arguments passed into the program.
     */
    public static void main(String[] theArgs) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        MonitorDashboard view = new MonitorDashboard(); //Initialize the UI.

        //Get a simple route, a rectangle around the map.
        ArrayList<RoutePoint> route = createRoute();

        //Create 3 Drones to pass to Telemetry Generator.
        Drone drone1 = new Drone(3.0f, 100, Orientation.NORTH, route);
        Drone drone2 = new Drone(2.0f, 85, Orientation.EAST, route);
        Drone drone3 = new Drone(1.5f, 75, Orientation.WEST, route);

        //Add drones to pass to telemetry generator.
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone1);
        drones.add(drone2);
        drones.add(drone3);

        //Initialize telemetry generator and add drones
        TelemetryGenerator gen = new TelemetryGenerator(drones);

        //Create a runnable task that will execute at every time interval
        Runnable updateDrones = () -> {
            gen.processAllDrones();

            //FIX ME: Figure out a way to get Telemetry hashmap to call Anomaly Detector
            for(DroneInterface drone : drones) {
                gen.createTelemetryMap(drone);

                printDrone(drone);
                System.out.println();
            }
        };

        //Create a runnable task that will shut down the scheduler on program exit
        Runnable shutdownScheduler = () -> {
            scheduler.shutdown();
            try {
                if(!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }
            catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        };

        System.out.println("---- START ----");
        for(DroneInterface drone : drones) {
            printDrone(drone);
            System.out.println();
        }

        //Have the scheduler fire a thread to run updateDrones every 5 seconds
        scheduler.scheduleAtFixedRate(updateDrones, 0, 5, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownScheduler));


    }

    private static ArrayList<RoutePoint> createRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(100, 100, 110)); // bottom-left
        route.add(new RoutePoint(130, 100, 115)); // bottom-right (30 units)
        route.add(new RoutePoint(130, 120, 120)); // top-right    (20 units)
        route.add(new RoutePoint(100, 120, 125)); // top-left     (30 units)
        route.add(new RoutePoint(100, 100, 130)); // back to start(20 units)
        return route;
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
