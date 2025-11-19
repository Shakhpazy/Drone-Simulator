package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class is the main window / dashboard for the autonomous drone monitoring
 * system.
 */
public class MonitorDashboard extends PropertyEnabledJFrame {

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

    private static final MonitorDashboard INSTANCE = new MonitorDashboard();

    /** Constructor to initialize the window. */
    private MonitorDashboard() {
        super();
        initWindow();
    }

    public static MonitorDashboard getInstance() {
        return INSTANCE;
    }

    /**
     * Adds the anomaly report to the log panel.
     *
     * @param theSimpleReport the report to log.
     * @param theDetailedReport the detailed report to display after clicked.
     */
    public void addLogEntry(final String theSimpleReport, final String theDetailedReport) {
        if (theSimpleReport == null || theDetailedReport == null) {
            throw new IllegalArgumentException("Report strings must not be null.");
        }
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
        JMenu fileMenu = initFileMenu();
        bar.add(fileMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        JMenuItem inst = new JMenuItem("Instructions...");
        inst.addActionListener(_ -> openInstructions());
        helpMenu.add(inst);
        bar.add(helpMenu);

        // Data
        JMenu dataMenu = new JMenu("Data");
        JMenuItem openDB = new JMenuItem("Open Database...");
        openDB.addActionListener(_ -> openDatabase());
        dataMenu.add(openDB);
        bar.add(dataMenu);

        // Debug
        JMenu debugMenu = new JMenu("Debug");
        JSlider tickSpdSlider = new JSlider(1, 5, 1);
        tickSpdSlider.addChangeListener(
                _ -> myPCS.firePropertyChange(
                        PROPERTY_TICK_SPEED,
                        null,
                        tickSpdSlider.getValue()));
        tickSpdSlider.setLabelTable(tickSpdSlider.createStandardLabels(1));
        tickSpdSlider.setPaintLabels(true);
        debugMenu.add(new JLabel("Tick Speed"));
        debugMenu.add(tickSpdSlider);
        bar.add(debugMenu);

        setJMenuBar(bar);
        bar.setVisible(true);
    }

    private JMenu initFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveCSV = new JMenuItem("Save as .csv");
        saveCSV.addActionListener(_ -> myPCS.firePropertyChange(PROPERTY_SAVE_CSV, null, null));
        fileMenu.add(saveCSV);
        JMenuItem savePDF = new JMenuItem("Save as .pdf");
        savePDF.addActionListener(_ -> myPCS.firePropertyChange(PROPERTY_SAVE_PDF, null, null));
        fileMenu.add(savePDF);
        JMenuItem saveJSON = new JMenuItem("Save as .json");
        saveJSON.addActionListener(_ -> myPCS.firePropertyChange(PROPERTY_SAVE_JSON, null, null));
        fileMenu.add(saveJSON);
        return fileMenu;
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
