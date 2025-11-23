package controller;

import model.*;
import view.DatabaseWindow;
import view.MonitorDashboard;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class handles the database operations and updates the GUI via
 * the DatabaseWindow class.
 *
 * @author Evin Roen
 * @version 11/19/2025
 */
public class DatabaseController implements PropertyChangeListener {

    /**
     * This field holds the reference to the database object from the model package.
     */
    private final AnomalyDatabase myDTBS;

    /**
     * This field holds the reference to the GUI window from the view package.
     */
    private final DatabaseWindow myWindow;

    /**
     * Package-private constructor to ensure only members of the
     * controller package can instantiate DatabaseController.
     * \n
     * Initializes the database model class and the database window
     * view class.
     *
     * @param theDTBS the shared database object used by the main controller.
     */
    DatabaseController(final AnomalyDatabase theDTBS) {
        // Assign and initialize database.
        myDTBS = theDTBS;
        myDTBS.initialize();

        // Assign GUI class and add self as listener to GUI.
        myWindow = new DatabaseWindow();
        myWindow.addPropertyChangeListener(this);
        MonitorDashboard.getInstance().addPropertyChangeListener(this);
    }

    /**
     * This method takes a date in the form of a string and
     * formats it for use throughout the class. Essentially
     * converts mm/dd/yyyy to Epoch Milliseconds.
     *
     * @param theDate string data of form mm/dd/yyyy.
     * @return the equivalent date in Epoch milliseconds.
     * @throws IllegalArgumentException if the date can not be formatted by LocalDate.parse().
     */
    private long formatDate(final String theDate) {
        // Attempt to format the date. If it fails, throw exception.
        LocalDate date;
        try {
            date = LocalDate.parse(theDate, DateTimeFormatter.ofPattern("MM/dd/yyy"));
        } catch (DateTimeParseException theE) {
            throw new IllegalArgumentException("Date is not formatted correctly: " + theE);
        }
        // Convert formatted time to Epoch Milli.
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        switch (theEvent.getPropertyName()) {

            // Open and initialize the database window.
            case MonitorDashboard.PROPERTY_DATABASE_OPENED:
                List<AnomalyReport> reps = myDTBS.findAllReports();

                // Clear reports from window and re-add them.
                myWindow.clearReports();
                for (AnomalyReport r : reps) {
                    myWindow.addReport(r.detailedReport());
                }
                myWindow.setVisible(true); // visible after done loading
                MonitorDashboard.getInstance().setCursor(Cursor.getDefaultCursor());
                break;

            // Retrieve all reports fulfilling the query conditions.
            case MonitorDashboard.PROPERTY_DATABASE_QUERY:
                if (theEvent.getNewValue() instanceof String[] arr) {

                    // Define set to store all reports, which we will filter using arr
                    Set<AnomalyReport> intersection = new HashSet<>(myDTBS.findAllReports());

                    // Filter by drone ID if not empty
                    if (!"".equals(arr[DatabaseWindow.IDX_DRONE_ID])) {
                        int id = Integer.parseInt(arr[DatabaseWindow.IDX_DRONE_ID]);
                        List<AnomalyReport> droneIDs = myDTBS.findReportsByDroneID(id);
                        intersection.retainAll(droneIDs);
                    }

                    // Filter by anomaly type if not empty
                    if (!"".equals(arr[DatabaseWindow.IDX_ANOMALY])) {
                        String type = arr[DatabaseWindow.IDX_ANOMALY];
                        List<AnomalyReport> anomTypes = myDTBS.findReportsByAnomalyType(type);
                        intersection.retainAll(anomTypes);
                    }

                    // Filter by date range when at least one is not empty
                    long from = 0;
                    if (!"".equals(arr[DatabaseWindow.IDX_FROM_DATE])) {
                        from = formatDate(arr[DatabaseWindow.IDX_FROM_DATE]);
                    }
                    long to = System.currentTimeMillis();
                    if (!"".equals(arr[DatabaseWindow.IDX_TO_DATE])) {
                        to = formatDate(arr[DatabaseWindow.IDX_TO_DATE]);
                    }
                    List<AnomalyReport> betweenDates = myDTBS.findReportsByTimeRange(from, to);
                    intersection.retainAll(betweenDates);

                    // Update GUI
                    myWindow.clearReports();
                    for (AnomalyReport rep : intersection) {
                        myWindow.addReport(rep.detailedReport());
                    }
                }
                break;

            // Exporting options for entire anomaly database.
            case MonitorDashboard.PROPERTY_SAVE_CSV:
                exportData(new CsvExporter());
                break;

            case MonitorDashboard.PROPERTY_SAVE_PDF:
                exportData(new PdfExporter());
                break;

            case MonitorDashboard.PROPERTY_SAVE_JSON:
                exportData(new JsonExporter());
                break;
        }
    }

    /**
     * Exports the database to an output file. The file type is determined by
     * the input ReportExporter implementation.
     *
     * @param theExporter the specific file type exporter (JSON, CSV, or PDF).
     * @throws IllegalArgumentException if the type exporter is not a PdfExporter, CsvExporter, or JsonExporter.
     */
    private void exportData(final ReportExporter theExporter) {
        // Determine the filename / type based on input exporter.
        // If not one of the accepted types, throw exception.
        String filename = switch (theExporter) {
            case PdfExporter _ -> "/anomaly_data.pdf";
            case CsvExporter _ -> "/anomaly_data.csv";
            case JsonExporter _ -> "/anomaly_data.json";
            case null, default ->
                    throw new IllegalArgumentException("Input ReportExporter is not an instance of PDF, CSV, or JSON exporters.");
        };

        // Open file chooser to allow user to select location for the new file.
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int selectionApproved = chooser.showOpenDialog(null);
        if (selectionApproved == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            theExporter.export(myDTBS.findAllReports(), filePath + filename);
        }
    }
}
