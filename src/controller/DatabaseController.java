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

public class DatabaseController implements PropertyChangeListener {

    private final AnomalyDatabase myDTBS;

    private final DatabaseWindow myWindow;

    DatabaseController(final AnomalyDatabase theDTBS) {
        myDTBS = theDTBS;
        myDTBS.initialize();
        myWindow = new DatabaseWindow();
        myWindow.addPropertyChangeListener(this);
        MonitorDashboard.getInstance().addPropertyChangeListener(this);
    }

    private long formatDate(String theDate) {
        LocalDate date;
        try {
            date = LocalDate.parse(theDate, DateTimeFormatter.ofPattern("MM/dd/yyy"));
        } catch (DateTimeParseException theE) {
            throw new IllegalArgumentException("Date is not formatted correctly: " + theE);
        }
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    @Override
    public void propertyChange(PropertyChangeEvent theEvent) {
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

            // Exporting anomaly database
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

    private void exportData(ReportExporter theExporter) {
        String filename = switch (theExporter) {
            case PdfExporter _ -> "/anomaly_data.pdf";
            case CsvExporter _ -> "/anomaly_data.csv";
            case JsonExporter _ -> "/anomaly_data.json";
            case null, default ->
                    throw new IllegalArgumentException("Input ReportExporter is not an instance of PDF, CSV, or JSON exporters.");
        };

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int selectionApproved = chooser.showOpenDialog(null);
        if (selectionApproved == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            theExporter.export(myDTBS.findAllReports(), filePath + filename);
        }
    }
}
