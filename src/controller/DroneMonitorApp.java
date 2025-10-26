package controller;

import model.Drone;
import model.Orientation;
import model.RoutePoint;
import view.MonitorDashboard;

import java.util.ArrayList;

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
     * The main entry point for the program. Initializes
     * @param args
     */
    public static void main(String[] args) {
        MonitorDashboard view = new MonitorDashboard(); //Initialize new view

        //Create Route (basic test)

        RoutePoint point1 = new RoutePoint(274.0f, 75.5f, 87.2f);
        //Increase altitude in place
        RoutePoint point2 = new RoutePoint(300.0f, 75.5f, 87.2f);
        RoutePoint point3 = new RoutePoint(300.0f, 75.8f, 87.3f);

        //Create 3 Drones
//        Drone drone1 = new Drone(3.0f, 100, Orientation.NORTH);
//        Drone drone2 = new Drone();
//        Drone drone3 = new Drone();

        ArrayList<Drone> drones = new ArrayList<Drone>();
//        drones.add(drone1);
//        drones.add(drone2);
//        drones.add(drone3);
    }
}
