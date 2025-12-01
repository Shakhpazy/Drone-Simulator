package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class is the main window / dashboard for the autonomous drone monitoring
 * system.
 *
 * @author Evin Roen
 * @version 11/30/2025
 */
public class MonitorDashboard extends PropertyEnabledJFrame {

    /** This constant determines the size of the window. */
    private static final Dimension SIZE = new Dimension(1100, 700);

    /** This is the size of the instructions window. */
    private static final Dimension HELP_SIZE = new Dimension(500, 510);

    /** This constant contains a reference to the map panel. */
    private static final MapPanel MAP_PANEL = new MapPanel();

    /** This constant contains a reference to the map panel. */
    private static final LogPanel LOG_PANEL = new LogPanel();

    /** This constant contains a reference to the map panel. */
    private static final DetailsPanel DETAILS_PANEL = new DetailsPanel();

    /** This constant contains a reference to the map panel. */
    private static final TelemetryPanel TELEMETRY_PANEL = new TelemetryPanel();

    /** This constant holds the singleton instance of the dashboard. */
    private static final MonitorDashboard INSTANCE = new MonitorDashboard();

    /** Constructor to initialize the window. */
    private MonitorDashboard() {
        super();
        initWindow();
    }

    /**
     * Provides a point to access the singleton instance of this class.
     *
     * @return the singleton instance.
     */
    public static MonitorDashboard getInstance() {
        return INSTANCE;
    }

    /**
     * Adds the anomaly report to the log panel.
     *
     * @param theSimpleReport the report to log.
     * @param theDetailedReport the detailed report to display after clicked.
     * @throws IllegalArgumentException if any given string is null.
     */
    public void addLogEntry(final String theSimpleReport, final String theDetailedReport) {
        if (theSimpleReport == null || theDetailedReport == null) {
            throw new IllegalArgumentException("Report strings must not be null.");
        }
        SwingUtilities.invokeLater(() -> {
            LOG_PANEL.addLogEntry(theSimpleReport, theDetailedReport);
            revalidate();
            repaint();
        });
    }

    /**
     * Draws a representation of the drone's location on the map panel.
     *
     * @param theID the drone's unique id value.
     * @param theLoc the drone's location as a float array containing {longitude, latitude}.
     * @param theTelData the drone's telemetry data as a string.
     */
    public void drawDrone(final int theID, final float[] theLoc, final String theTelData) {
        MAP_PANEL.setDroneMapping(theID, theLoc);
        TELEMETRY_PANEL.addTelemetryEntry(theID, theTelData);
        revalidate();
        repaint();
    }

    /**
     * Removes all traces of drones with the given ID from the front end.
     *
     * @param theID the id of the drone to remove from the sim.
     */
    public void removeDrone(final int theID) {
        SwingUtilities.invokeLater(() -> {
            MAP_PANEL.removeDrone(theID);
            TELEMETRY_PANEL.removeTelemetryEntry(theID);
            revalidate();
            repaint();
        });
    }

    /**
     * This static method sets the anomaly report for the details panel.
     *
     * @param theDetailedReport the detailed report to display.
     */
    static void setDetailReport(final String theDetailedReport) {
        DETAILS_PANEL.setReport(theDetailedReport);
    }

    /**
     * Sets the selected drone in the MapPanel and TelemetryPanel.
     *
     * @param theID the ID number for the drone.
     * @return true if already selected, false otherwise.
     */
    static boolean setSelectedDrone(final int theID) {
        return MAP_PANEL.setSelectedID(theID);
    }

    /**
     * JFrame setup.
     */
    private void initWindow() {
        setPreferredSize(SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setVisible(true);
        initMenuBar();
        addPanels();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Autonomous Drone Monitor Dashboard");
    }

    /**
     * Sets up the menu bar with File, Help, and Data tabs.
     */
    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();

        // File
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveAs = new JMenuItem("Save all reports as...");
        saveAs.addActionListener(_ -> myPCS.firePropertyChange(PROPERTY_SAVE_AS, null, null));
        fileMenu.add(saveAs);
        bar.add(fileMenu);

        // Data
        JMenu dataMenu = new JMenu("Data");
        JMenuItem openDB = new JMenuItem("Open Database...");
        openDB.addActionListener(_ -> openDatabase());
        dataMenu.add(openDB);
        bar.add(dataMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        JMenuItem inst = new JMenuItem("Instructions...");
        inst.addActionListener(_ -> openInstructions());
        helpMenu.add(inst);
        bar.add(helpMenu);

        setJMenuBar(bar);
        bar.setVisible(true);
    }

    /**
     * Opens a window that instructs users on how to navigate this app.
     */
    private void openInstructions() {
        String[] instructions = {
                "Menu Bar",
                "---------------",
                "> File: Save the entire stored anomaly data to a separate file.",
                "        Supported file types: pdf, csv, json.",
                "> Help: Here you can open the instructions for the dashboard.",
                "> Data: Here you can open and query the anomaly database.",
                "        For database window instructions, open the window and go",
                "        to \"Help -> Instructions...\"",
                "",
                "Map Panel",
                "---------------",
                "> This panel shows the locations of the drones on a latitude / longitude grid.",
                "",
                "Telemetry Data",
                "---------------",
                "> Each drone's telemetry data (location, velocity, etc.) is displayed below the",
                "  map panel. To see a specific drone's location on the map, click on the data entry",
                "  for the desired drone.",
                "",
                "Anomaly Log",
                "---------------",
                "> This panel displays the most recent anomalies since starting the simulation.",
                "  To view the entire database, go to \"Data -> Open database...\"",
                "> To view the details of a recent anomaly, click on the desired entry and the",
                "  details will appear on the panel below the log.",
                "",
                "Anomaly Details Panel",
                "---------------",
                "> This panel displays the details for the last clicked anomaly log entry."
        };
        new InstructionWindow("Monitor Dashboard Instructions", instructions, HELP_SIZE);
    }

    /**
     * Opens a window to view and query the anomaly report database.
     */
    private void openDatabase() {
        // Lots of data may cause the window to open after a delay.
        // Setting cursor to waiting lets user know the window is opening.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        myPCS.firePropertyChange(PROPERTY_DATABASE_OPENED, null, null);
    }

    /**
     * Adds the organizational panels to the window.
     */
    private void addPanels() {
        add(MAP_PANEL, BorderLayout.CENTER);
        add(LOG_PANEL, BorderLayout.EAST);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(TELEMETRY_PANEL, BorderLayout.CENTER);
        bottomPanel.add(DETAILS_PANEL, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
