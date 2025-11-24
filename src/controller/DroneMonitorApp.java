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
 * @version 11/22/2025
 */
public class DroneMonitorApp {

    /**
     * Flag to enable or disable developer mode, which prints telemetry to the console
     * and clears the database on exit.
     */
    private static boolean MY_DEV_MODE = true;

    /** The route generator used to create flight paths for the drones. */
    private static final RouteGenerator myRouteGenerator = new RouteGenerator();

    /** The drone generator used to instantiate drone objects. */
    private static final DroneGenerator myDroneGenerator = new DroneGenerator();

    // ===============
    //  CONFIGURATION
    // ===============
//    private static final int FPS = 120; //REVISIT: Add to slider?

    /*
     * How long the program waits between updates (in milliseconds)
     */
    private static final long MY_UPDATE_TIME = 500;
//  OLD: private static final long MY_DELTA_TIME = FPS/1000;

   /**
    * Time delta for smooth updates in TelemetryGenerator
    * MY_DELTA_TIME = MY_UPDATE_TIME in seconds due to implementation.
    */
   private static final double MY_DELTA_TIME = MY_UPDATE_TIME / 1000.0;

   private static final float MY_ANOMALY_PERCENT = 1.0F;

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
        TelemetryGenerator gen = TelemetryGenerator.getInstance(MY_ANOMALY_PERCENT);

        //Generate Drones
        for (int i = 0; i < 1; i++) {
            ArrayList<RoutePoint> theRoute = myRouteGenerator.generateRoute();
            DroneInterface drone = myDroneGenerator.createDrone(theRoute);
            gen.addDrone(drone);
        }
        ArrayList<DroneInterface> drones = gen.getMyDrones();

        //Initialize AnomalyDetector
        AnomalyDetector detector = new AnomalyDetector();

        //Initialize AnomalyDatabase
        AnomalyDatabase anomalyDTBS = new AnomalyDatabase();
        anomalyDTBS.initialize();
        new DatabaseController(anomalyDTBS);

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
            ArrayList<TelemetryRecord[]> droneTelemetry = gen.processAllDrones((float) MY_DELTA_TIME);

            //For each drone
            for (int i = 0; i < drones.size(); i++) {
                DroneInterface drone = drones.get(i);

                //Get previous Telemetry
                TelemetryRecord myBeforeTelemetryRecord = droneTelemetry.get(i)[0];

                //Get Current Telemetry
                TelemetryRecord myCurrentTelemetryRecord = droneTelemetry.get(i)[1];

                //Send previous and current telemetry to anomaly detector for analysis
                AnomalyReport anomaly = detector.detect(myBeforeTelemetryRecord, myCurrentTelemetryRecord);

                //If anomaly is not null.
                if (anomaly != null) {
                    if(anomaly.anomalyType().contains("Out of Bounds")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("spoof");
                    }
                    else if(anomaly.anomalyType().contains("Battery")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("battery");
                    }
                    else {
                        AlertPlayer.INSTANCE.addSoundToQueue("crash");
                    }
                    //Add anomaly to database.
                    anomalyDTBS.insertReport(anomaly);
                    //Add a log entry to view.
                    view.addLogEntry(anomaly.simpleReport(), anomaly.detailedReport());
                }

                //Get drone location to pass to view
//                float[] location = {(float) myCurrentTelemetryRecord.get("longitude"),
//                        (float) myCurrentTelemetryRecord.get("latitude")};
                float[] location = {(float) myCurrentTelemetryRecord.longitude(),
                        (float) myCurrentTelemetryRecord.latitude()};

                //Get telemetry as a String to pass to view
                String theTelemetry = telemetryToString(myCurrentTelemetryRecord);

                //Draw the drone on the view.
                view.drawDrone(drone.getId(), location, theTelemetry);

                //Print to console if developer mode is enabled
                if(MY_DEV_MODE) {
                    printDrone(drone);
                    System.out.println();
                }
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

        //Clear database after each use if developer mode is enabled.
        if (MY_DEV_MODE) {
            Runnable clearDatabase = () -> {
                anomalyDTBS.clear();
            };
            Runtime.getRuntime().addShutdownHook(new Thread(clearDatabase));
        }
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
     * Converts a map of telemetry data into a formatted string for display purposes.
     *
     * @param theTelemetryRecord A {@link HashMap} containing the drone's current telemetry data.
     * @return A formatted {@link String} representation of the telemetry.
     */
    private static String telemetryToString(TelemetryRecord theTelemetryRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(theTelemetryRecord.id()).append("\n");
        sb.append("altitude: ").append(theTelemetryRecord.altitude()).append("\n");
        sb.append("longitude: ").append(theTelemetryRecord.longitude()).append("\n");
        sb.append("latitude: ").append(theTelemetryRecord.latitude()).append("\n");
        sb.append("velocity: ").append(theTelemetryRecord.velocity()).append("\n");
        sb.append("batteryLevel: ").append(theTelemetryRecord.batterLevel()).append("\n");
        sb.append("orientation: ").append(theTelemetryRecord.orientation()).append("\n");
        return sb.toString();
    }
}