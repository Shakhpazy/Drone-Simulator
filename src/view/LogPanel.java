package view;

import model.AnomalyReport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.UUID; // temporary for sample data

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
                """
                        Sample Simple Report
                        2nd Line of Sample Simple Report
                        Even more Lines for Sample Simple Report""",
                """
                        Sample Detailed Report
                        Very very detailed detaily report
                        tons of important details here that operators need""");
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
        myScrollView.setLayout(new BoxLayout(myScrollView, BoxLayout.Y_AXIS));
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
         * This constant keeps track of all instances of LogEntry.
         */
        private static final ArrayList<LogEntry> ENTRIES = new ArrayList<>();

        /**
         * This static field keeps track of which instance is currently
         * selected (last clicked on).
         */
        private static LogEntry selected = null;

        /**
         * Constructor to initialize the text area.
         *
         * @param theReport the report to add to the log.
         */
        public LogEntry(final AnomalyReport theReport) {
            super();
            init();
            ENTRIES.add(this);
            setText(theReport.simpleReport());

            // Adds mouse listener for selecting a log
            // in order to view the detailed description.
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent theEvent) {
                    MonitorDashboard.setDetailReport(theReport);
                    selected = (LogEntry) theEvent.getSource();
                    ENTRIES.forEach(theE -> theE.setBackground(Color.LIGHT_GRAY));
                    selected.setBackground(Color.GREEN);
                }
            });
        }

        /**
         * This method initializes the text area.
         */
        private void init() {
            setMaximumSize(SIZE);
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setBackground(Color.LIGHT_GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setAlignmentX(LEFT_ALIGNMENT);
            setCaretColor(new Color(0, 0, 0, 0)); // invisible
        }
    }
}
