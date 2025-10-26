package view;

import model.AnomalyReport;

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
    private final MapPanel MAP_PANEL;

    /** This constant contains a reference to the map panel. */
    private final LogPanel LOG_PANEL;

    /** This constant contains a reference to the map panel. */
    private final DetailsPanel DETAILS_PANEL;

    /** This constant contains a reference to the map panel. */
    private final TelemetryPanel TELEMETRY_PANEL;

    /** Constructor to initialize the window. */
    public MonitorDashboard() {
        super();
        MAP_PANEL = new MapPanel();
        LOG_PANEL = new LogPanel();
        DETAILS_PANEL = new DetailsPanel();
        TELEMETRY_PANEL = new TelemetryPanel();
        initWindow();
    }

    /**
     * Adds the anomaly report to the log panel.
     *
     * @param theReport the report to log.
     */
    public void addLogEntry(final AnomalyReport theReport) {
        LOG_PANEL.addLogEntry(theReport);
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
        setLocationRelativeTo(null);
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

    private void saveAsCSV() {
        System.out.println("eventually, this will save as .csv");
    }

    private void saveAsPDF() {
        System.out.println("eventually, this will save as .pdf");
    }

    private void openInstructions() {
        System.out.println("eventually, this will open instructions");
    }

    private void openDatabase() {
        System.out.println("eventually, this will open database");
    }

    private void addPanels() {
        add(MAP_PANEL, BorderLayout.CENTER);
        add(LOG_PANEL, BorderLayout.EAST);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(TELEMETRY_PANEL, BorderLayout.CENTER);
        bottomPanel.add(DETAILS_PANEL, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
