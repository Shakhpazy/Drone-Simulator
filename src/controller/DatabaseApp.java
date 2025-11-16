package controller;

import model.AnomalyDatabase;
import model.AnomalyReport;
import view.DatabaseWindow;
import view.MonitorDashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class DatabaseApp implements PropertyChangeListener {

    private final AnomalyDatabase myDTBS;

    private DatabaseWindow myWindow;

    public DatabaseApp(final AnomalyDatabase theDTBS) {
        myDTBS = theDTBS;
        myDTBS.initialize();
        myWindow = new DatabaseWindow();
        myWindow.addPropertyChangeListener(this);
        MonitorDashboard.getInstance().addPropertyChangeListener(this);
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
        }
    }
}
