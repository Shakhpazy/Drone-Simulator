package controller;

import model.*;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import view.MonitorDashboard;

import javax.swing.*;

/**
 * Entry point for the Drone Monitoring Simulator.
 * <p>
 * This class initializes the user interface, creates drones, and continuously
 * updates telemetry through a scheduled loop. It also runs anomaly detection
 * logic and logs events to both the on-screen dashboard and the anomaly
 * database.
 * </p>
 *
 * <p>The simulation operates by:</p>
 * <ul>
 * <li>Generating drones with automatically produced flight routes.</li>
 * <li>Processing telemetry updates at a fixed interval.</li>
 * <li>Detecting anomalies based on changes in telemetry.</li>
 * <li>Triggering alert sounds and logging anomaly reports.</li>
 * <li>Updating the UI in real time to reflect drone states.</li>
 * </ul>
 *
 * <p>
 * Developer mode enables console telemetry printing and clears the database
 * automatically on exit.
 * </p>
 *
 * @author Natan Artemiev
 * @version 11/30/2025
 */
public class DroneMonitorApp {

    /**
     * Flag to enable or disable developer mode, which prints telemetry to the console
     * and clears the database on exit.
     */
    private static final boolean MY_DEV_MODE = false;

    /** The route generator used to create flight paths for the drones. */
    private static final RouteGenerator myRouteGenerator = new RouteGenerator();

    /** The drone generator used to instantiate drone objects. */
    private static final DroneGenerator myDroneGenerator = new DroneGenerator();

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
     * Maximum amount of supported drones.
     */
   private static final int MAX_DRONE_COUNT = 200;

    /**
     * The main entry point for the program. Initializes the UI and creates drones. Initializes the TelemetryGenerator
     * in order to update each drone. Listens for a change of state in the model and updates the view.
     *
     * @param theArgs - The command line arguments passed into the program.
     */
    public static void main(String[] theArgs) {
        String input = JOptionPane.showInputDialog("Enter the number of drones for the simulation.");

        int myDroneCount = validateInput(input);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        MonitorDashboard view = MonitorDashboard.getInstance(); //Initialize the UI.

        /*
         * The anomaly percentage that Telemetry Generator should use
         * to determine frequency of anomalous movement.
         *
         *  % = 100 (percentage steps) / [15 (seconds) * 2 (updates/sec) * MY_DRONE_COUNT]
         *  % = 10 / (3.0 * MY_DRONE_COUNT)
         */
        float MY_ANOMALY_PERCENT = 10.0f / (3.0f * myDroneCount);

        //Initialize telemetry generator
        TelemetryGenerator gen = TelemetryGenerator.getInstance(MY_ANOMALY_PERCENT);

        //Generate Drones
        for (int i = 0; i < myDroneCount; i++) {
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

        /**
         * Runnable task executed periodically to update all drones:
         * <ul>
         * <li>Processes telemetry changes.</li>
         * <li>Detects anomalies.</li>
         * <li>Logs reports and triggers UI updates.</li>
         * <li>Removes drones in the event of critical anomalies.</li>
         * </ul>
         */
        Runnable simulateNextStep = () -> {
            try {
                //Get Previous and Current telemetry of all drones.
                Map<DroneInterface, TelemetryRecord[]> droneTelemetry = gen.processAllDrones((float) MY_DELTA_TIME);

                //For each drone
                for (Map.Entry<DroneInterface, TelemetryRecord[]> entry : droneTelemetry.entrySet()) {
                    boolean removeDrone = false;
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
                        if (anomalyString.contains("Failure") || anomalyString.contains("Ground")) { //BATTERY_FAIL OR HIT_GROUND (2/9)
                            AlertPlayer.INSTANCE.addSoundToQueue("crash");
                            view.markDroneDead(drone.getId());
                            gen.removeDrone(drone);
                            removeDrone = true;
                        } else if (anomalyString.contains("Out of Bounds")) { //OUT_OF_BOUNDS (1/9)
                            AlertPlayer.INSTANCE.addSoundToQueue("out-of-bounds");
                        } else if (anomalyString.contains("Battery")) { //BATTERY_DRAIN  and BATTERY_WARNING anomalies (2/9)
                            AlertPlayer.INSTANCE.addSoundToQueue("battery");
                        } else if (anomalyString.contains("Acceleration")
                                || anomalyString.contains("Speed")
                                || anomalyString.contains("Altitude")) { //ACCELERATION and SPEED anomalies (3/9)
                            AlertPlayer.INSTANCE.addSoundToQueue("acceleration");
                        } else  { //SPOOFING and OFF_COURSE anomalies (1/9)
                            AlertPlayer.INSTANCE.addSoundToQueue("spoof");
                        }

                        //Add anomaly to database.
                        anomalyDTBS.insertReport(anomaly);

                        //Add a log entry to view.
                        javax.swing.SwingUtilities.invokeLater(() -> {
                                view.addLogEntry(anomaly.simpleReport(), anomaly.detailedReport());
                        });
                    }

                    // Check if drone died naturally (battery = 0)
                    if (!removeDrone && !drone.isAlive()) {
                        view.markDroneDead(drone.getId());
                    }

                    //If the drone hasn't been removed...
                    if (!removeDrone) {
                        //Get drone location to pass to view
                        float[] location = {myCurrentTelemetryRecord.longitude(),
                                myCurrentTelemetryRecord.latitude()};

                        //Get telemetry as a String to pass to view
                        String theTelemetry = telemetryToString(myCurrentTelemetryRecord);

                        //Draw the drone on the view
                        view.drawDrone(drone.getId(), location, theTelemetry);
                    }
                }
            }
            catch (Exception e) {
                System.err.println("Fatal error in simulation loop (main loop will continue)");
                e.printStackTrace();
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

            System.out.println("Closing database connection...");
            anomalyDTBS.close();
        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownScheduler));

        //Clear database after each use if developer mode is enabled.
        if (MY_DEV_MODE) {
            Runnable clearDatabase = anomalyDTBS::clear;
            Runtime.getRuntime().addShutdownHook(new Thread(clearDatabase));
        }
    }

    private static int validateInput(final String theInput) {
        if (theInput == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }
        int res = 1;
        try  {
            res = Integer.parseInt(theInput);
        } catch (NumberFormatException e) {
            System.out.println("Trouble parsing integer from input: " + e);
        }
        if (res < 1) {
            throw new IllegalArgumentException("Input too small (<1)");
        }
        res = Math.min(res, MAX_DRONE_COUNT);
        return res;
    }

    /**
     * Converts a drone's telemetry record into a formatted, multi-line string
     * for display on the monitoring dashboard.
     *
     * @param theTelemetryRecord the drone's telemetry record.
     * @return formatted text representing all telemetry fields.
     */
    private static String telemetryToString(TelemetryRecord theTelemetryRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(theTelemetryRecord.id()).append("\n");
        sb.append("Altitude: ").append(theTelemetryRecord.altitude()).append("\n");
        sb.append("Longitude: ").append(theTelemetryRecord.longitude()).append("\n");
        sb.append("Latitude: ").append(theTelemetryRecord.latitude()).append("\n");
        sb.append("Velocity: ").append(theTelemetryRecord.velocity()).append("\n");
        sb.append("Battery Level: ").append(theTelemetryRecord.batteryLevel()).append("\n");
        sb.append("Orientation: ").append(theTelemetryRecord.orientation()).append("\n");
        return sb.toString();
    }
}