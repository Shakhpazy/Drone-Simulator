package controller;

import model.*;

import java.util.HashMap;
import java.util.Map;
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
    private static final boolean MY_DEV_MODE = true;

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

   /**
    * Time delta for smooth updates in TelemetryGenerator
    * MY_DELTA_TIME = MY_UPDATE_TIME in seconds due to implementation.
    */
   private static final double MY_DELTA_TIME = MY_UPDATE_TIME / 1000.0;
    /**
     * The anomaly percentage that Telemetry Generator should use
     * to determine frequency of anomalous movement.
     */
   private static final float MY_ANOMALY_PERCENT = 1.0F;

   private static final float MY_DRONE_COUNT = 10;

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
        for (int i = 0; i < MY_DRONE_COUNT; i++) {
            ArrayList<RoutePoint> theRoute = myRouteGenerator.generateRoute();
            DroneInterface drone = myDroneGenerator.createDrone(theRoute);
            gen.addDrone(drone);
        }

        //Initialize AnomalyDetector
        AnomalyDetector detector = new AnomalyDetector();

        //Initialize AnomalyDatabase
        AnomalyDatabase anomalyDTBS = new AnomalyDatabase();
        anomalyDTBS.initialize();
        new DatabaseController(anomalyDTBS); //Initialize Database controllers

        /*
         * A runnable task that simulates the next step of the drone monitoring system.
         * It processes telemetry for all drones, updates the view, checks for anomalies,
         * and logs reports to the database and view.
         */
        Runnable simulateNextStep = () -> {
            //Get Previous and Current telemetry of all drones.
            Map<DroneInterface, TelemetryRecord[]> droneTelemetry = gen.processAllDrones((float) MY_DELTA_TIME);

            //For each drone
            for (Map.Entry<DroneInterface, TelemetryRecord[]> entry : droneTelemetry.entrySet()) {
                DroneInterface drone = entry.getKey();
                TelemetryRecord[] recordPair = entry.getValue();

                //Get previous Telemetry
                TelemetryRecord myBeforeTelemetryRecord = recordPair[0];

                //Get Current Telemetry
                TelemetryRecord myCurrentTelemetryRecord = recordPair[1];

                //Send previous and current telemetry to anomaly detector for analysis
                AnomalyReport anomaly = detector.detect(myBeforeTelemetryRecord, myCurrentTelemetryRecord);

                //If anomaly is not null.
                if (anomaly != null) {
                    String anomalyString = anomaly.anomalyType();
                    if(anomalyString.contains("Out of Bounds")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("out-of-bounds");
                    }
                    else if(anomalyString.contains("Battery") &&
                            !anomalyString.contains("Failure")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("battery");
                    }
                    else if(anomalyString.contains("Acceleration") &&
                            !anomalyString.contains("Speed")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("acceleration");
                    }
                    else if(anomalyString.contains("Spoof")) {
                        AlertPlayer.INSTANCE.addSoundToQueue("spoof");
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
                float[] location = {myCurrentTelemetryRecord.longitude(),
                        myCurrentTelemetryRecord.latitude()};

                //Get telemetry as a String to pass to view
                String theTelemetry = telemetryToString(myCurrentTelemetryRecord);

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

        //Clear database after each use if developer mode is enabled.
        if (MY_DEV_MODE) {
            Runnable clearDatabase = anomalyDTBS::clear;
            Runtime.getRuntime().addShutdownHook(new Thread(clearDatabase));
        }
    }

    /**
     * Converts a map of telemetry data into a formatted string for display purposes.
     *
     * @param theTelemetryRecord A {@link HashMap} containing the drone's current telemetry data.
     * @return A formatted {@link String} representation of the telemetry.
     */
    private static String telemetryToString(TelemetryRecord theTelemetryRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(theTelemetryRecord.id()).append("\n");
        sb.append("Altitude: ").append(theTelemetryRecord.altitude()).append("\n");
        sb.append("Longitude: ").append(theTelemetryRecord.longitude()).append("\n");
        sb.append("Latitude: ").append(theTelemetryRecord.latitude()).append("\n");
        sb.append("Velocity: ").append(theTelemetryRecord.velocity()).append("\n");
        sb.append("Battery Level: ").append(theTelemetryRecord.batterLevel()).append("\n");
        sb.append("Orientation: ").append(theTelemetryRecord.orientation()).append("\n");
        return sb.toString();
    }
}