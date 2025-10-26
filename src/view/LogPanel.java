package view;

import model.AnomalyReport;

import javax.swing.*;
import java.awt.*;
import java.util.UUID; // temporary

/**
 * This class represents the right-hand-side of the GUI which
 * displays quick descriptions of recent anomaly logs.
 */
public class LogPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(170, 530);

    /**
     * This field is the viewport for the scroll pane, i.e.
     * the viewable portion of the entire scrollable area.
     */
    private final JPanel myScrollView;

    /**
     * Constructor to initialize the panel.
     */
    public LogPanel() {
        super();
        myScrollView = new JPanel();
        initPanel();
        initScroll();

        // Meaningless sample data to show display behaviour
        AnomalyReport rep = new AnomalyReport(
                new UUID(10, 10),
                10L,
                "Test",
                1,
                "Sample",
                "Extra Sample");
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
        addLogEntry(rep);
    }

    /**
     * This method initializes this class's main content panel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridLayout());
    }

    /**
     * This method initializes the scroll pane.
     */
    private void initScroll() {
        myScrollView.setLayout(new BoxLayout(myScrollView, BoxLayout.PAGE_AXIS));
        add(new JScrollPane(myScrollView));
    }

    /**
     * This method allows for adding entries to the anomaly log.
     *
     * @param theReport the anomaly report record class to log.
     */
    public void addLogEntry(final AnomalyReport theReport) {
        myScrollView.add(new LogEntry(theReport));
    }

    /**
     * This inner class represents one entry in the anomaly log.
     */
    private static class LogEntry extends JTextArea {

        /**
         * This constant defines the size of the entry within the
         * viewed portion of the scrollable area.
         */
        private static final Dimension SIZE = new Dimension(165, 100);

        /**
         * Constructor to initialize the text area.
         *
         * @param theReport the report to add to the log.
         */
        public LogEntry(final AnomalyReport theReport) {
            super();
            init();
            addLine(String.format("Drone ID:  %d", theReport.droneId()));
            addLine(String.format("Timestamp: %d", theReport.timestamp()));
            addLine(String.format("Type:      %s", theReport.anomalyType()));
        }

        /**
         * This method initializes the text area.
         */
        private void init() {
            setMaximumSize(SIZE);
            setEditable(false);
            setBackground(Color.LIGHT_GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        /**
         * This method takes a string and adds it as a new line
         * to the log entry text area.
         *
         * @param theLine the new line to add.
         */
        private void addLine(final String theLine) {
            setText(String.format("%s\n%s", getText(), theLine));
        }
    }
}
