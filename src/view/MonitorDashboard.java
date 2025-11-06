package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class is the main window / dashboard for the autonomous drone monitoring
 * system.
 */
public class MonitorDashboard extends JFrame {

    /** This constant determines the size of the window. */
    private static final Dimension SIZE = new Dimension(1100, 700);

    /** This constant contains a reference to the map panel. */
    private static final MapPanel MAP_PANEL = new MapPanel();

    /** This constant contains a reference to the map panel. */
    private static final LogPanel LOG_PANEL = new LogPanel();

    /** This constant contains a reference to the map panel. */
    private static final DetailsPanel DETAILS_PANEL = new DetailsPanel();

    /** This constant contains a reference to the map panel. */
    private static final TelemetryPanel TELEMETRY_PANEL = new TelemetryPanel();

    /** Constructor to initialize the window. */
    public MonitorDashboard() {
        super();
        initWindow();
    }

    /**
     * Adds the anomaly report to the log panel.
     *
     * @param theSimpleReport the report to log.
     * @param theDetailedReport the detailed report to display after clicked.
     */
    public void addLogEntry(final String theSimpleReport, final String theDetailedReport) {
        LOG_PANEL.addLogEntry(theSimpleReport, theDetailedReport);
        revalidate();
        repaint();
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
     * This static method sets the anomaly report for the details panel.
     *
     * @param theDetailedReport the detailed report to display.
     */
    protected static void setDetailReport(final String theDetailedReport) {
        DETAILS_PANEL.setReport(theDetailedReport);
    }

    /**
     * Sets the selected drone in the MapPanel and TelemetryPanel.
     *
     * @param theID the ID number for the drone.
     */
    protected static boolean setSelectedDrone(final int theID) {
        return MAP_PANEL.setSelectedID(theID);
    }

    /** Boilerplate JFrame setup. */
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
        JMenuItem saveCSV = new JMenuItem("Save as .csv");
        saveCSV.addActionListener(theEvent -> saveAsCSV());
        fileMenu.add(saveCSV);
        JMenuItem savePDF = new JMenuItem("Save as .pdf");
        savePDF.addActionListener(theEvent -> saveAsPDF());
        fileMenu.add(savePDF);
        bar.add(fileMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        JMenuItem inst = new JMenuItem("Instructions...");
        inst.addActionListener(theEvent -> openInstructions());
        helpMenu.add(inst);
        bar.add(helpMenu);

        // Data
        JMenu dataMenu = new JMenu("Data");
        JMenuItem openDB = new JMenuItem("Open Database...");
        openDB.addActionListener(theEvent -> openDatabase());
        dataMenu.add(openDB);
        bar.add(dataMenu);

        setJMenuBar(bar);
        bar.setVisible(true);
    }

    /**
     * Saves the anomaly reports from the database as a .csv file.
     */
    private void saveAsCSV() {
        System.out.println("eventually, this will save as .csv");
    }

    /**
     * Saves the anomaly reports from the database as a .pdf file.
     */
    private void saveAsPDF() {
        System.out.println("eventually, this will save as .pdf");
    }

    /**
     * Opens a window that instructs users on how to navigate this app.
     */
    private void openInstructions() {
        System.out.println("eventually, this will open instructions");
    }

    /**
     * Opens a window to view and query the anomaly report database.
     */
    private void openDatabase() {
        System.out.println("eventually, this will open database");
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
