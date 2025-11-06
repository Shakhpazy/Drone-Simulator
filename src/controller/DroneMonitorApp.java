package controller;

import model.*;
import java.util.concurrent.ConcurrentHashMap;
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
 * @version 11/2/2025
 */
public class DroneMonitorApp {

    private static boolean myDevMode = true;

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
//        Drone drone2 = new Drone(2.0f, 85, Orientation.EAST, route);
//        Drone drone3 = new Drone(1.5f, 75, Orientation.WEST, route);

        //Add drones to pass to telemetry generator.
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone1);
//        drones.add(drone2);
//        drones.add(drone3);

        //Initialize telemetry generator and add drones
        TelemetryGenerator gen = new TelemetryGenerator(drones);

        //Initialize AnomalyDetector
        AnomalyDetector detector = new AnomalyDetector();

        //Initialize AnomalyDatabase
        AnomalyDatabase anomalyDTBS = new AnomalyDatabase();
        anomalyDTBS.initialize();

        //Output to console if developer mode is enabled
        if(myDevMode) {
            System.out.println("---- START ----");
            for (DroneInterface drone : drones) {
                printDrone(drone);
                System.out.println();
            }
        }

        //Create a runnable task that will execute at every time interval
        Runnable simulateNextStep = () -> {
            gen.processAllDrones();

            //For each drone
            for (DroneInterface drone : drones) {
                //Get previous Telemetry
                ConcurrentHashMap<String, Object> myBeforeTelemetryMap = gen.getMyBeforeTelemetryMap();

                //Get Current Telemetry
                ConcurrentHashMap<String, Object> myCurrentTelemetryMap = gen.createTelemetryMap(drone);

                //Get drone location to pass to view
                float[] location = {(float) myCurrentTelemetryMap.get("longitude"),
                                    (float) myCurrentTelemetryMap.get("latitude")};
                //Get telemetry as a String to pass to view
                String theTelemetry = telemetryToString(myCurrentTelemetryMap);

                //Draw the drone on the view.
                view.drawDrone(drone.getId(), location, theTelemetry);

                //Send previous and current telemetry to anomaly detector for analysis
                AnomalyReport anomaly = detector.detect(myBeforeTelemetryMap, myCurrentTelemetryMap);

                //If anomaly is not null.
                if (anomaly != null) {
                    //Add anomaly to database.
                    anomalyDTBS.insertReport(anomaly);
                    //Add a log entry to view.
                    view.addLogEntry(anomaly.simpleReport(), anomaly.detailedReport());
                }
                //Print to console if developer mode is enabled
                if(myDevMode) {
                    printDrone(drone);
                    System.out.println();
                }
            }
        };

        //Have the scheduler fire a thread to run simulateNextStep every 5 seconds
        scheduler.scheduleAtFixedRate(simulateNextStep, 0, 7, TimeUnit.SECONDS);

        //Create a runnable task that will shut down the scheduler on program exit
        Runnable shutdownScheduler = () -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownScheduler));

        //Clear database after each use if developer mode is enabled.
        if (myDevMode) {
            Runnable clearDatabase = () -> {
                anomalyDTBS.clear();
            };
            Runtime.getRuntime().addShutdownHook(new Thread(clearDatabase));
        }
    }

    private static ArrayList<RoutePoint> createRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(60, 60, 110)); // bottom-left
        route.add(new RoutePoint(90, 60, 115)); // bottom-right (30 units)
        route.add(new RoutePoint(90, 80, 120)); // top-right    (20 units)
        route.add(new RoutePoint(60, 80, 125)); // top-left     (30 units)
        route.add(new RoutePoint(60, 60, 130)); // back to start(20 units)
        return route;
    }

    private static void printDrone(DroneInterface theDrone) {
        RoutePoint target = theDrone.getNextPoint(); // the waypoint it’s heading to
        System.out.printf(
                "Drone %d | Lon=%.2f Lat=%.2f Alt=%.2f Vel=%.2f Battery=%d | Heading to (%.0f, %.0f, %.0f)%n",
                theDrone.getId(),
                theDrone.getLongitude(),
                theDrone.getLatitude(),
                theDrone.getAltitude(),
                theDrone.getVelocity(),
                theDrone.getBatteryLevel(),
                target.getLongitude(),
                target.getLatitude(),
                target.getAltitude()
        );
    }

    /**
     * Return a string representation to pass to the view to draw drone.
     *
     * @param myTelemetryMap
     * @return the String representation of the Telemetry
     */
    private static String telemetryToString(ConcurrentHashMap<String, Object> myTelemetryMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(myTelemetryMap.get("id")).append("\n");
        sb.append("altitude: ").append(myTelemetryMap.get("altitude")).append("\n");
        sb.append("longitude: ").append(myTelemetryMap.get("longitude")).append("\n");
        sb.append("latitude: ").append(myTelemetryMap.get("latitude")).append("\n");
        sb.append("velocity: ").append(myTelemetryMap.get("velocity")).append("\n");
        sb.append("batteryLevel: ").append(myTelemetryMap.get("batteryLevel")).append("\n");
        sb.append("orientation: ").append(myTelemetryMap.get("orientation")).append("\n");
        sb.append("timeStamp: ").append(myTelemetryMap.get("timeStamp")).append("\n");
        return sb.toString();
    }
}
