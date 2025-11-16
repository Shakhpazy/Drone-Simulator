package controller;

import model.AnomalyDatabase;
import model.AnomalyReport;
import view.DatabaseWindow;
import view.MonitorDashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseController implements PropertyChangeListener {

    private final AnomalyDatabase myDTBS;

    private final DatabaseWindow myWindow;

    public DatabaseController(final AnomalyDatabase theDTBS) {
        myDTBS = theDTBS;
        myDTBS.initialize();
        myWindow = new DatabaseWindow();
        myWindow.addPropertyChangeListener(this);
        MonitorDashboard.getInstance().addPropertyChangeListener(this);
    }

    private long formatDate(String theDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyy");
        LocalDate date = LocalDate.parse(theDate, formatter);
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    @Override
    public void propertyChange(PropertyChangeEvent theEvent) {
        switch (theEvent.getPropertyName()) {
            case MonitorDashboard.PROPERTY_DATABASE_OPENED:
                myWindow.setVisible(true);
                List<AnomalyReport> reps = myDTBS.findAllReports();
                myWindow.clearReports();
                for (AnomalyReport r : reps) {
                    myWindow.addReport(r.detailedReport());
                }
                break;

            case MonitorDashboard.PROPERTY_DATABASE_QUERY:
                if (theEvent.getNewValue() instanceof String[] arr) {

                    Set<AnomalyReport> intersection = new HashSet<>(myDTBS.findAllReports());

                    if (!"".equals(arr[DatabaseWindow.IDX_DRONE_ID])) {
                        int id = Integer.parseInt(arr[DatabaseWindow.IDX_DRONE_ID]);
                        List<AnomalyReport> droneIDs = myDTBS.findReportsByDroneID(id);
                        intersection.retainAll(droneIDs);
                    }

                    if (!"".equals(arr[DatabaseWindow.IDX_ANOMALY])) {
                        String type = arr[DatabaseWindow.IDX_ANOMALY];
                        List<AnomalyReport> anomTypes = myDTBS.findReportsByAnomalyType(type);
                        intersection.retainAll(anomTypes);
                    }

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
        }
    }
}
