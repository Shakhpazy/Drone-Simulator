package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * This class displays the telemetry data for the mapped drones under the map.
 */
class TelemetryPanel extends JPanel {

    /**
     * This constant determines the size of the panel.
     */
    private static final Dimension SIZE = new Dimension(150, 150);

    /**
     * This constant is the viewport of the scroll pane.
     */
    private static final JPanel SCROLL_VIEW = new JPanel();

    /**
     * This constant is a mapping between drone IDs and their associated entries.
     */
    private static final Map<Integer, TelemetryEntry> ID_ENTRY_MAP = new HashMap<>();

    /**
     * This constant is the scroll pane for the telemetry entries.
     */
    private static JScrollPane scrollPane;

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
    void addTelemetryEntry(final int theID, final String theData) {
        if (theID < 0) {
            throw new IllegalArgumentException("Drone ID cannot be negative.");
        }
        if (theData == null) {
            throw new IllegalArgumentException("Data string must not be null.");
        }

        // Preserve scroll position
        int scrollPosition;
        if (scrollPane != null) {
            scrollPosition = scrollPane.getHorizontalScrollBar().getValue();
        } else {
            scrollPosition = 0;
        }

        if (ID_ENTRY_MAP.containsKey(theID)) {
            // update existing entry
            ID_ENTRY_MAP.get(theID).setText(theData);
        } else {
            // add new entry in sorted order by ID
            TelemetryEntry e = new TelemetryEntry(theID, theData);
            ID_ENTRY_MAP.put(theID, e);
            
            // Find the correct insertion index to maintain sorted order
            int insertIndex = 0;
            Component[] components = SCROLL_VIEW.getComponents();
            for (Component comp : components) {
                if (comp instanceof TelemetryEntry) {
                    TelemetryEntry entry = (TelemetryEntry) comp;
                    if (entry.myID < theID) {
                        insertIndex++;
                    } else {
                        break;
                    }
                }
            }
            
            SCROLL_VIEW.add(e, insertIndex);
            SCROLL_VIEW.revalidate();
        }
        
        // Restore scroll position
        if (scrollPane != null) {
            SwingUtilities.invokeLater(() -> {
                scrollPane.getHorizontalScrollBar().setValue(scrollPosition);
            });
        }
    }

    void removeTelemetryEntry(final int theID) {
        remove(ID_ENTRY_MAP.get(theID));
        sortEntries();
    }

    private void sortEntries() {
        for (TelemetryEntry e : ID_ENTRY_MAP.values()) {
            SCROLL_VIEW.remove(e);
        }
        List<TelemetryEntry> entries = new ArrayList<>(List.copyOf(ID_ENTRY_MAP.values()));
        entries.sort((a, b) -> {return a.myID - b.myID;});
        for (TelemetryEntry e : entries) {
            SCROLL_VIEW.add(e);
        }
    }

    /**
     * Initializes the JPanel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER));
        setBackground(ColorScheme.BACKGROUND_PANEL);
        setLayout(new GridLayout());
        setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * Initializes the scroll pane.
     */
    private void initScroll() {
        SCROLL_VIEW.setLayout(new BoxLayout(SCROLL_VIEW, BoxLayout.X_AXIS));
        SCROLL_VIEW.setAlignmentX(LEFT_ALIGNMENT);
        SCROLL_VIEW.setBackground(ColorScheme.BACKGROUND_PANEL);
        scrollPane = new JScrollPane(SCROLL_VIEW);
        scrollPane.getHorizontalScrollBar().setUI(new ColorScheme.DarkScrollBarUI());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane);
    }

    /**
     * This inner class represents a telemetry data entry for tracking a single drone.
     *
     * @author Evin Roen
     * @version 11/19/2025
     */
    private static class TelemetryEntry extends JPanel {

        /**
         * This constant determines the size of the panel.
         */
        private static final Dimension SIZE = new Dimension(150, 150);

        /**
         * This field will hold the ID value of the drone that this
         * entry is about.
         */
        final int myID;

        /**
         * Label for the drone title.
         */
        private final JLabel titleLabel;

        /**
         * Text area for the telemetry data.
         */
        private final JTextArea dataArea;

        /**
         * Constructor to initialize entry text area.
         *
         * @param theID the drone's id number.
         * @param theData the drone's telemetry data to display.
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
            
            // Create title label and data area BEFORE init()
            titleLabel = new JLabel("Drone " + myID, JLabel.CENTER);
            dataArea = new JTextArea();
            
            init();
            setTelemetryData(theData);

            // Create a shared mouse listener for all components
            MouseAdapter clickListener = new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent theE) {
                    boolean isSelected = MonitorDashboard.setSelectedDrone(myID);
                    // Reset all entries to default colors
                    ID_ENTRY_MAP.values().forEach(e -> e.setSelected(false));
                    // Set this entry as selected if it wasn't already
                    if (!isSelected) {
                        setSelected(true);
                    }
                }
            };

            // Add mouse listener to the panel and all child components
            addMouseListener(clickListener);
            titleLabel.addMouseListener(clickListener);
            dataArea.addMouseListener(clickListener);
        }

        /**
         * Sets the selection state and updates all component colors accordingly.
         *
         * @param selected true if this entry should be selected, false otherwise.
         */
        private void setSelected(boolean selected) {
            if (selected) {
                setBackground(ColorScheme.ACCENT_SELECTED);
                titleLabel.setBackground(ColorScheme.ACCENT_SELECTED_LIGHT);
                titleLabel.setForeground(ColorScheme.WHITE);
                dataArea.setBackground(ColorScheme.ACCENT_SELECTED);
                dataArea.setForeground(ColorScheme.WHITE);
            } else {
                setBackground(ColorScheme.BACKGROUND_PANEL);
                titleLabel.setBackground(ColorScheme.BACKGROUND_SECONDARY);
                titleLabel.setForeground(ColorScheme.TEXT_PRIMARY);
                dataArea.setBackground(ColorScheme.BACKGROUND_PANEL);
                dataArea.setForeground(ColorScheme.TEXT_PRIMARY);
            }
            repaint();
        }

        /**
         * Initializes the panel with title and data area.
         */
        private void init() {
            setPreferredSize(SIZE);
            setMaximumSize(SIZE);
            setLayout(new BorderLayout());
            setBackground(ColorScheme.BACKGROUND_PANEL);
            setOpaque(true); // Make sure panel is opaque so background shows
            setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER)); // Dark border
            setAlignmentX(LEFT_ALIGNMENT);

            // Create title label with larger font
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
            titleLabel.setOpaque(true);
            titleLabel.setBackground(ColorScheme.BACKGROUND_SECONDARY); // Darker title background
            titleLabel.setForeground(ColorScheme.TEXT_PRIMARY); // Light text
            titleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER));
            
            // Create data text area
            dataArea.setEditable(false);
            dataArea.setOpaque(true); // Make sure text area is opaque
            dataArea.setBackground(ColorScheme.BACKGROUND_PANEL); // Dark background
            dataArea.setForeground(ColorScheme.TEXT_PRIMARY); // Light text
            dataArea.setFont(new Font(dataArea.getFont().getName(), Font.PLAIN, 11));
            dataArea.setCaretColor(ColorScheme.CARET_INVISIBLE); // invisible
            dataArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            dataArea.setLineWrap(true);
            dataArea.setWrapStyleWord(true);

            // Create scroll pane for data area
            JScrollPane dataScrollPane = new JScrollPane(dataArea);
            dataScrollPane.setBorder(null);
            dataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            dataScrollPane.setOpaque(false); // Make scroll pane transparent so panel background shows
            dataScrollPane.getViewport().setBackground(ColorScheme.BACKGROUND_PANEL);

            // Add components
            add(titleLabel, BorderLayout.NORTH);
            add(dataScrollPane, BorderLayout.CENTER);
        }

        /**
         * Sets the telemetry data, removing the ID line since it's in the title.
         *
         * @param theData the telemetry data string.
         */
        private void setTelemetryData(final String theData) {
            // Remove the ID line from the data since it's shown in the title
            String dataWithoutID = theData.replaceFirst("(?i)ID:\\s*\\d+\\s*\n", "")
                                          .replaceFirst("(?i)id:\\s*\\d+\\s*\n", "");
            dataArea.setText(dataWithoutID.trim());
        }

        /**
         * Updates the text content of the telemetry data.
         *
         * @param theData the new telemetry data.
         */
        void setText(final String theData) {
            setTelemetryData(theData);
        }
    }


}
