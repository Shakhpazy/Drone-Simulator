package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * This class represents the right-hand-side of the GUI which
 * displays quick descriptions of recent anomaly logs.
 *
 * @author Evin Roen
 * @version 11/19/2025
 */
class LogPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(170, 530);

    /**
     * This constant is the viewport for the scroll pane, i.e.
     * the viewable portion of the entire scrollable area.
     */
    private static final JPanel SCROLL_VIEW = new JPanel();

    /**
     * Adjust scroll panel increment (default too slow)
     */
    private static final int SCROLL_INC = 10;

    /**
     * Constructor to initialize the panel.
     */
    LogPanel() {
        super();
        initPanel();
        initScroll();
    }

    /**
     * This method allows for adding entries to the anomaly log.
     *
     * @param theSimpleReport the simple report to display.
     * @param theDetailedReport the detailed report to display after clicking.
     */
    public void addLogEntry(final String theSimpleReport, final String theDetailedReport) {
        if (theSimpleReport == null || theDetailedReport == null) {
            throw new IllegalArgumentException("Report strings must not be null.");
        }
        SCROLL_VIEW.add(new LogEntry(theSimpleReport, theDetailedReport), 0);
        revalidate();
    }

    /**
     * This method initializes this class's main content panel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(new Color(0x4A4A4A))); // Dark border
        setBackground(new Color(0x2D2D2D)); // Dark background
        setLayout(new GridLayout());
    }

    /**
     * This method initializes the scroll pane.
     */
    private void initScroll() {
        SCROLL_VIEW.setLayout(new BoxLayout(SCROLL_VIEW, BoxLayout.Y_AXIS));
        SCROLL_VIEW.setBackground(new Color(0x2D2D2D)); // Dark background
        JScrollPane scroll = new JScrollPane(SCROLL_VIEW);
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_INC);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(new Color(0x2D2D2D));
        scroll.getViewport().setBackground(new Color(0x2D2D2D));
        add(scroll);
    }

    /**
     * This inner class represents one entry in the anomaly log.
     */
    private static class LogEntry extends JTextArea {

        /**
         * This constant defines the size of the entry within the
         * viewed portion of the scrollable area.
         */
        private static final Dimension SIZE = new Dimension(150, 100);

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
         * @param theSimpleReport the simple report to add to the log.
         * @param theDetailedReport the detailed report to display when clicked.
         */
        private LogEntry(final String theSimpleReport, final String theDetailedReport) {
            super();
            if (theSimpleReport == null || theDetailedReport == null) {
                throw new IllegalArgumentException("Report strings must not be null.");
            }
            init();
            ENTRIES.add(this);
            setText(theSimpleReport);

            // Adds mouse listener for selecting a log
            // in order to view the detailed description.
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent theEvent) {
                    MonitorDashboard.setDetailReport(theDetailedReport);
                    selected = (LogEntry) theEvent.getSource();
                    ENTRIES.forEach(theEntry -> {
                        theEntry.setBackground(new Color(0x2D2D2D)); // Dark background
                        theEntry.setForeground(new Color(0xE0E0E0)); // Light text
                    });
                    selected.setBackground(new Color(0x4CAF50)); // Muted green
                    selected.setForeground(Color.WHITE); // White text when selected
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
            setBackground(new Color(0x2D2D2D)); // Dark background
            setForeground(new Color(0xE0E0E0)); // Light text
            setBorder(BorderFactory.createLineBorder(new Color(0x4A4A4A))); // Dark border
            setAlignmentX(LEFT_ALIGNMENT);
            setCaretColor(new Color(0, 0, 0, 0)); // invisible
        }
    }
}
