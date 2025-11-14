package controller;

import model.*;
import java.util.HashMap;
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

    /**
     * Flag to enable or disable developer mode, which prints telemetry to the console
     * and clears the database on exit.
     */
    private static final boolean MY_DEV_MODE = true;

    //Define delta time in seconds
    private static final double MY_DELTA_TIME = 1.0;

    //Battery alert file path
    private static final String MY_BATTERY_ALERT = "../src/SFX/BatteryAlert.wav";

    //Crash Alert file path
    private static final String MY_CRASH_ALERT = "..src/SFX/CrashAlert.wav";

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
        Drone drone1 = new Drone(3.0f, 100, route);
//        Drone drone2 = new Drone(2.0f, 85, Orientation.EAST, route);
//        Drone drone3 = new Drone(1.5f, 75, Orientation.WEST, route);

        //Add drones to pass to telemetry generator.
        ArrayList<DroneInterface> drones = new ArrayList<>();
        drones.add(drone1);
//        drones.add(drone2);
//        drones.add(drone3);

        //Initialize telemetry generator and add drones
        TelemetryGenerator gen = TelemetryGenerator.getInstance();

        //Initialize AnomalyDetector
        AnomalyDetector detector = new AnomalyDetector();

        //Initialize AnomalyDatabase
        AnomalyDatabase anomalyDTBS = new AnomalyDatabase();
        anomalyDTBS.initialize();

        //Output to console if developer mode is enabled
        if(MY_DEV_MODE) {
            System.out.println("---- START ----");
            for (DroneInterface drone : drones) {
                printDrone(drone);
                System.out.println();
            }
        }

        /**
         * A runnable task that simulates the next step of the drone monitoring system.
         * It processes telemetry for all drones, updates the view, checks for anomalies,
         * and logs reports to the database and view.
         */
        Runnable simulateNextStep = () -> {
            //Get Previous and Current telemetry of all drones.
            ArrayList<HashMap<String, Object>[]> droneTelemetry = gen.processAllDrones(MY_DELTA_TIME);

            //For each drone
            for (DroneInterface drone : drones) {
                //Get previous Telemetry
                HashMap<String, Object> myBeforeTelemetryMap = droneTelemetry.get(0)[0];

                //Get Current Telemetry
                HashMap<String, Object> myCurrentTelemetryMap = droneTelemetry.get(0)[1];

                //Send previous and current telemetry to anomaly detector for analysis
                AnomalyReport anomaly = detector.detect(myBeforeTelemetryMap, myCurrentTelemetryMap);

                //If anomaly is not null.
                if (anomaly != null) {
                    //playBatteryAlert();
                    //Add anomaly to database.
                    anomalyDTBS.insertReport(anomaly);
                    //Add a log entry to view.
                    view.addLogEntry(anomaly.simpleReport(), anomaly.detailedReport());
                }

                //Get drone location to pass to view
                float[] location = {(float) myCurrentTelemetryMap.get("longitude"),
                        (float) myCurrentTelemetryMap.get("latitude")};

                //Get telemetry as a String to pass to view
                String theTelemetry = telemetryToString(myCurrentTelemetryMap);

                //Draw the drone on the view.
                view.drawDrone(drone.getId(), location, theTelemetry);

                //Print to console if developer mode is enabled
                if(MY_DEV_MODE) {
                    printDrone(drone);
                    System.out.println();
                }
            }
        };

        //Have the scheduler fire a thread to run simulateNextStep every 1 seconds
        scheduler.scheduleAtFixedRate(simulateNextStep, 0, 1000, TimeUnit.MILLISECONDS);

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
        if (MY_DEV_MODE) {
            Runnable clearDatabase = () -> {
                anomalyDTBS.clear();
            };
            Runtime.getRuntime().addShutdownHook(new Thread(clearDatabase));
        }
    }

    /**
     * Creates a predefined simple rectangular route with specific waypoints.
     *
     * @return An {@link ArrayList} of {@link RoutePoint} objects defining the route.
     */
    private static ArrayList<RoutePoint> createRoute() {
        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(60, 60, 110)); // bottom-left
        route.add(new RoutePoint(90, 60, 115)); // bottom-right (30 units)
        route.add(new RoutePoint(90, 80, 120)); // top-right    (20 units)
        route.add(new RoutePoint(60, 80, 125)); // top-left     (30 units)
        route.add(new RoutePoint(60, 60, 130)); // back to start(20 units)
        return route;
    }

    /**
     * Prints the current telemetry data of a specific drone to the console for debugging purposes.
     * Enabled only in developer mode.
     *
     * @param theDrone The {@link DroneInterface} object representing the drone.
     */
    private static void printDrone(DroneInterface theDrone) {
        RoutePoint target = theDrone.getNextPoint(); // the waypoint it’s heading to
        System.out.printf(
                "Drone %d | Lon=%.2f Lat=%.2f Alt=%.2f Vel=%.2f Battery=%f | Heading to (%.0f, %.0f, %.0f)%n",
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
     * Converts a telemetry data map into a formatted string representation
     * suitable for display in the view.
     *
     * @param myTelemetryMap A {@link HashMap} containing the drone's telemetry data.
     * @return The String representation of the Telemetry data.
     */
    private static String telemetryToString(HashMap<String, Object> myTelemetryMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(myTelemetryMap.get("id")).append("\n");
        sb.append("altitude: ").append(myTelemetryMap.get("altitude")).append("\n");
        sb.append("longitude: ").append(myTelemetryMap.get("longitude")).append("\n");
        sb.append("latitude: ").append(myTelemetryMap.get("latitude")).append("\n");
        sb.append("velocity: ").append(myTelemetryMap.get("velocity")).append("\n");
        sb.append("batteryLevel: ").append(myTelemetryMap.get("batteryLevel")).append("\n");
        sb.append("orientation: ").append(myTelemetryMap.get("orientation")).append("\n");
        return sb.toString();
    }
}
