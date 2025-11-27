package controller;

import model.*;
import view.DatabaseWindow;
import view.MonitorDashboard;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the database operations and updates the GUI via
 * the DatabaseWindow class.
 *
 * @author Evin Roen
 * @version 11/19/2025
 */
public class DatabaseController implements PropertyChangeListener {

    private final String FILENAME = "anomaly_report";

    /**
     * This field holds the reference to the database object from the model package.
     */
    private final AnomalyDatabase myDTBS;

    /**
     * This field holds the reference to the GUI window from the view package.
     */
    private final DatabaseWindow myWindow;

    private List<AnomalyReport> myFilteredReports;

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
        myFilteredReports = new ArrayList<>();

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
                myFilteredReports = reps;

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
                    List<AnomalyReport> intersection = new ArrayList<>(myDTBS.findAllReports());

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

                    // update field
                    myFilteredReports = intersection;
                }
                break;

            // Exporting options for entire anomaly database.
            case MonitorDashboard.PROPERTY_SAVE_AS:
                if (theEvent.getSource() instanceof MonitorDashboard) {
                    exportData(myDTBS.findAllReports());
                } else if (theEvent.getSource() instanceof DatabaseWindow) {
                    exportData(List.copyOf(myFilteredReports));
                }
                break;
        }
    }

    /**
     * Exports the database to an output file.
     */
    private void exportData(final List<AnomalyReport> theReports) {

        // Create file chooser
        JFileChooser choose = initChooser();

        // Listener for when user changes file extension filter
        choose.addPropertyChangeListener(fileTypeChanged());

        if (choose.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            File selected = choose.getSelectedFile();
            FileNameExtensionFilter filter =
                    (FileNameExtensionFilter) choose.getFileFilter();

            String ext = filter.getExtensions()[0];  // pdf, csv, or json
            String path = selected.getAbsolutePath();

            // Add extension if missing
            if (!path.toLowerCase().endsWith("." + ext)) {
                path += "." + ext;
            }

            ReportExporter e = switch (ext) {
                case "pdf" -> new PdfExporter();
                case "csv" -> new CsvExporter();
                case "json" -> new JsonExporter();
                default -> throw new IllegalStateException("Unexpected filetype: " + ext);
            };
            e.export(theReports, path);
        }
    }

    private JFileChooser initChooser() {

        // Create file chooser
        JFileChooser choose = new JFileChooser();
        choose.setDialogTitle("Save Anomaly Report");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Add file name extension filters
        String[] desc = {"PDF Files (*.pdf)", "CSV Files (*.csv)", "JSON Files (*.json)"};
        String[] exts = {"pdf", "csv", "json"};
        for (int i = 0; i < desc.length; i++) {
            choose.addChoosableFileFilter(new FileNameExtensionFilter(desc[i], exts[i]));
        }
        // turn off "accept all files" aka "no filters"
        choose.setAcceptAllFileFilterUsed(false);

        // default option (save as pdf)
        choose.setSelectedFile(new File(FILENAME + ".pdf"));
        return choose;
    }

    private PropertyChangeListener fileTypeChanged() {
        return evt -> {
            if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                // get the chooser
                JFileChooser c = (JFileChooser) evt.getSource();

                // get the new filter
                FileNameExtensionFilter f = (FileNameExtensionFilter) evt.getNewValue();

                // update text in the chooser file name area
                String newFilename = FILENAME + "." + f.getExtensions()[0];
                c.setSelectedFile(new File(newFilename));
            }
        };
    }
}
