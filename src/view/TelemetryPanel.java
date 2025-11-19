package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * This class displays the telemetry data for the mapped drones under the map.
 */
class TelemetryPanel extends JPanel {

    /**
     * This constant determines the size of the panel.
     */
    private static final Dimension SIZE = new Dimension(930, 170);

    /**
     * This constant is the viewport of the scroll pane.
     */
    private static final JPanel SCROLL_VIEW = new JPanel();

    /**
     * This constant is a mapping between drone IDs and their associated entries.
     */
    private static final Map<Integer, TelemetryEntry> ID_ENTRY_MAP = new HashMap<>();

    /**
     * Constructor to initialize the panel and its components.
     */
    TelemetryPanel() {
        super();
        initPanel();
        initScroll();
    }

    /**
     * If the given ID already has an entry, that entry is removed
     * and a new one with updated data is added. Otherwise, a new
     * entry is added.
     *
     * @param theID the drone's id number.
     * @param theData the drone's telemetry data.
     * @throws IllegalArgumentException if drone ID is negative or data is null.
     */
    public void addTelemetryEntry(final int theID, final String theData) {
        if (theID < 0) {
            throw new IllegalArgumentException("Drone ID cannot be negative.");
        }
        if (theData == null) {
            throw new IllegalArgumentException("Data string must not be null.");
        }

        if (ID_ENTRY_MAP.get(theID) == null) {
            // add new entry
            TelemetryEntry e = new TelemetryEntry(theID, theData);
            ID_ENTRY_MAP.put(theID, e);
            SCROLL_VIEW.add(e);
        } else {
            // update existing entry
            ID_ENTRY_MAP.get(theID).setText(theData);
        }
    }

    /**
     * Initializes the JPanel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridLayout());
        setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * Initializes the scroll pane.
     */
    private void initScroll() {
        SCROLL_VIEW.setLayout(new BoxLayout(SCROLL_VIEW, BoxLayout.X_AXIS));
        SCROLL_VIEW.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane scroll = new JScrollPane(SCROLL_VIEW);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        add(scroll);
    }

    /**
     * This inner class represents a telemetry data entry for tracking a single drone.
     *
     * @author Evin Roen
     * @version 11/19/2025
     */
    private static class TelemetryEntry extends JTextArea {

        /**
         * This constant determines the size of the panel.
         */
        private static final Dimension SIZE = new Dimension(150, 150);

        /**
         * This field will hold the ID value of the drone that this
         * entry is about.
         */
        private final int myID;

        /**
         * Constructor to initialize entry text area.
         *
         * @param theID the drone's id number.
         * @param theData the telemetry data to display.
         * @throws IllegalArgumentException if drone ID is negative or data is null.
         */
        private TelemetryEntry(final int theID, final String theData) {
            super();
            if (theID < 0) {
                throw new IllegalArgumentException("Drone ID cannot be negative.");
            }
            if (theData == null) {
                throw new IllegalArgumentException("Data string must not be null.");
            }
            myID = theID;
            init();
            setText(theData);

            // Mouse listener to select specific drones in the GUI.
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent theE) {
                    boolean isSelected = MonitorDashboard.setSelectedDrone(myID);
                    ID_ENTRY_MAP.values().forEach(e -> e.setBackground(Color.LIGHT_GRAY));
                    if (!isSelected) {
                        setBackground(Color.GREEN);
                    }
                }
            });
        }

        /**
         * Initializes the text area.
         */
        private void init() {
            setPreferredSize(SIZE);
            setMaximumSize(SIZE);
            setEditable(false);
            setBackground(Color.LIGHT_GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setAlignmentX(LEFT_ALIGNMENT);
            setCaretColor(new Color(0, 0, 0, 0)); // invisible
        }

    }


}
