package controller;

import model.*;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import view.MonitorDashboard;

public class ZScoreMonitor {

    /** The route generator used to create flight paths for the drones. */
    private static final RouteGenerator myRouteGenerator = new RouteGenerator();

    /** The drone generator used to instantiate drone objects. */
    private static final DroneGenerator myDroneGenerator = new DroneGenerator();

    private static PersistentExporter exporter = new PersistentExporter();

    /*
     * How long the program waits between updates (in milliseconds)
     */
    private static final long MY_UPDATE_TIME = 500;

    /**
     * Time delta for smooth updates in TelemetryGenerator
     * MY_DELTA_TIME = MY_UPDATE_TIME in seconds due to implementation.
     */
    private static final double MY_DELTA_TIME = MY_UPDATE_TIME / 1000.0;

    /**
     * Define the anomaly percentage
     */
    private static final double MY_ANOMALY_PERCENT = 0.0;

    private static final String MY_TELEMETRY_LOG_PATH = "dataLogs/TelemetryLog.txt";

    private static final String MY_Z_SCORE_LOG_PATH = "dataLogs/BaselineLog.properties";

    /**
     * The main entry point for the program. Initializes the UI and creates drones. Initializes the TelemetryGenerator
     * in order to update each drone. Listens for a change of state in the model and updates the view.
     *
     * @param theArgs - The command line arguments passed into the program.
     */
    public static void main(String[] theArgs) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        MonitorDashboard view = MonitorDashboard.getInstance(); //Initialize the UI.

        //Initialize telemetry generator
        TelemetryGenerator gen = TelemetryGenerator.getInstance();

        //Generate Drones
        for (int i = 0; i < 4; i++) {
            ArrayList<RoutePoint> theRoute = myRouteGenerator.generateRoute();
            DroneInterface drone = myDroneGenerator.createDrone(theRoute);
            gen.addDrone(drone);
        }
        ArrayList<DroneInterface> drones = gen.getMyDrones();

        ArrayList<String> headers = new ArrayList<>();
        headers.add("id");
        headers.add("velocity");
        headers.add("batteryLevel");
        headers.add("orientation");
        headers.add("timeStamp");

        //Get Persistent Exporter
        exporter = new PersistentExporter();
        exporter.startTelemetryLog(MY_TELEMETRY_LOG_PATH, headers);

        /*
          A runnable task that simulates the next step of the drone monitoring system.
          It processes telemetry for all drones, updates the view, checks for anomalies,
          and logs reports to the database and view.
         */
        Runnable simulateNextStep = () -> {
            //Get Previous and Current telemetry of all drones.
            ArrayList<HashMap<String, Object>[]> droneTelemetry = gen.processAllDrones((float) MY_DELTA_TIME);

            //For each drone
            for (int i = 0; i < drones.size(); i++) {
                DroneInterface drone = drones.get(i);

                //Get Current Telemetry
                HashMap<String, Object> myCurrentTelemetryMap = droneTelemetry.get(i)[1];

                //Log telemetry data
                exporter.logTelemetryData(myCurrentTelemetryMap, headers);

                //Get drone location to pass to view
                float[] location = {(float) myCurrentTelemetryMap.get("longitude"),
                        (float) myCurrentTelemetryMap.get("latitude")};

                //Get telemetry as a String to pass to view
                String theTelemetry = telemetryToString(myCurrentTelemetryMap);

                //Draw the drone on the view.
                view.drawDrone(drone.getId(), location, theTelemetry);
            }
        };

        scheduler.scheduleWithFixedDelay(simulateNextStep, 0, MY_UPDATE_TIME, TimeUnit.MILLISECONDS);

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

        Runnable shutDownLoggingAndCalculate = () -> {
            exporter.closeTelemetryLog();
            System.out.println("Telemetry log closed. Baseline calculation started.");

            try {
                BaselineCalculator calc = new BaselineCalculator();
                calc.calculateAndSaveStats(MY_TELEMETRY_LOG_PATH, MY_Z_SCORE_LOG_PATH);
            } catch (Exception e) {
                System.err.println("Error in running baseline calculation: " + e.getMessage());
            }

        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutDownLoggingAndCalculate));
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