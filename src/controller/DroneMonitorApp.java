package controller;

import model.*;
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
 * @version 10/25/2025
 */
public class DroneMonitorApp {
    /**
     * The main entry point for the program. Initializes the UI and creates drones. Initializes the TelemetryGenerator
     * in order to update each drone. Listens for a change of state in the model and updates the view.
     *
     * @param theArgs - The command line arguments passed into the program.
     */
    public static void main(String[] theArgs) {
        MonitorDashboard view = new MonitorDashboard(); //Initialize the UI.

        //Create Route (basic test)
        ArrayList<RoutePoint> route =  new ArrayList<>();

        route.add(new RoutePoint(274.0f, 75.5f, 87.2f));
        //Increase altitude in place
        route.add(new RoutePoint(300.0f, 75.5f, 87.2f));
        //Change position without changing height
        route.add(new RoutePoint(300.0f, 75.8f, 87.3f));


        //Create 3 Drones to pass to Telemetry Generator.
        Drone drone1 = new Drone(3.0f, 100, Orientation.NORTH, route);
        Drone drone2 = new Drone(2.0f, 85, Orientation.EAST, route);
        Drone drone3 = new Drone(1.5f, 75, Orientation.WEST, route);

        //Add drones to pass to telemetry generator.
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone1);
        drones.add(drone2);
        drones.add(drone3);


        TelemetryGenerator gen = new TelemetryGenerator(drones);

        Runnable updateDrones = () -> {
            gen.processAllDrones();
        };


    }
}
