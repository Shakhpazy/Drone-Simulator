package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

/**
 * This class represents and displays the drone's locations on a map.
 *
 * @author Evin Roen
 * @version 11/19/2025
 */
class MapPanel extends JPanel {

    /**
     * This constant is an alias for 0 to reference the second
     * element in an array of {longitude, latitude}.
     */
    private static final int LON = 0;

    /**
     * This constant is an alias for 1 to reference the first
     * element in an array of {longitude, latitude}.
     */
    private static final int LAT = 1;

    /**
     * Maximum value of longitude.
     */
    private static final int LON_MAX = 180;

    /**
     * Maximum value of latitude.
     */
    private static final int LAT_MAX = 90;

    /**
     * This constant determines the size of the panel.
     */
    private static final Dimension SIZE = new Dimension(930, 530);

    /**
     * This constant is a mapping between Drones' IDs and their locations.
     */
    private static final Map<Integer, int[]> ID_LOC_MAP = new HashMap<>();

    /**
     * Set of dead drone IDs.
     */
    private static final Set<Integer> DEAD_DRONES = new java.util.HashSet<>();

    /**
     * This constant is a reference to the GridPanel that draws the grid and drones.
     */
    private static final GridPanel GRID = new GridPanel();

    /**
     * Currently selected drone id, -1 if no drone selected;
     */
    private static int mySelectedID;

    /**
     * Constructor to initialize panel.
     */
    MapPanel() {
        super();
        mySelectedID = -1;
        initPanel();
    }

    /**
     * Stores the given location of the provided drone id number. If the
     * given drone ID is not contained in the mapping, it is added. If it
     * is already added, then the location is updated.
     *
     * @param theID the id number of the drone.
     * @param theLoc the longitude and latitude of the drone, in that order.
     * @throws IllegalArgumentException if the drone id is negative, or the location is not a 2D array or out of bounds.
     */
    void setDroneMapping(final int theID, final float[] theLoc) {
        validateLocation(theLoc);
        if (theID < 0) {
            throw new IllegalArgumentException("Drone ID must not be negative.");
        }
        ID_LOC_MAP.put(theID, formatLocation(theLoc));
        repaint();
    }

    /**
     * Marks a drone as dead, so it displays with a different color.
     *
     * @param theID the id of the dead drone.
     */
    void markDroneDead(final int theID) {
        DEAD_DRONES.add(theID);
        repaint();
    }

    void removeDrone(final int theID) {
        ID_LOC_MAP.remove(theID);
        DEAD_DRONES.remove(theID);
    }

    /**
     * Sets the selected drone id to reflect that in the gui.
     *
     * @param theID the drone to set as selected.
     * @return true if already selected, false otherwise.
     * @throws IllegalArgumentException if the map does not contain the given ID.
     */
    public boolean setSelectedID(final int theID) {
        if (!ID_LOC_MAP.containsKey(theID)) {
            throw new IllegalArgumentException("Location map does not contain given key (Drone ID).");
        }
        boolean isSelected = mySelectedID == theID;
        if (isSelected) {
            mySelectedID = -1;
        } else {
            mySelectedID = theID;
            GRID.focusOnSelected(theID);
        }
        repaint();
        return isSelected;
    }

    /**
     * Sets up the size and border for the panel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER));
        setBackground(ColorScheme.BACKGROUND_MAIN);
        setLayout(new BorderLayout());
        add(GRID, BorderLayout.CENTER);
    }

    /**
     * Converts the float array from the drone to a drawable int array.
     *
     * @param theLoc the float array to convert.
     * @return the given float array as an int array, floored and centered.
     * @throws IllegalArgumentException if theLoc is out of bounds or not a 2D array.
     */
    private int[] formatLocation(final float[] theLoc) {
        validateLocation(theLoc);
        return new int[] {
                (int) Math.floor(theLoc[LON]),
                (int) Math.floor(-theLoc[LAT])};
    }

    /**
     * This method validates the given position of a drone.
     *
     * @param theLoc the location of the drone to validate.
     * @throws IllegalArgumentException if the loc is out of bounds or not a 2D array.
     */
    private void validateLocation(final float[] theLoc) {
        if (theLoc.length != 2) {
            throw new IllegalArgumentException("Location array not the correct size of 2: {lon, lat}.");
        } else {
            if (theLoc[LON] > LON_MAX || theLoc[LON] < -LON_MAX) {
                throw new IllegalArgumentException("Longitude out of bounds: " + theLoc[LON]);
            }
            if (theLoc[LAT] > LAT_MAX || theLoc[LAT] < -LAT_MAX) {
                throw new IllegalArgumentException("Latitude out of bounds: " + theLoc[LAT]);
            }
        }
    }

    /**
     * This inner class represents the window in which the grid and drones will be drawn.
     *
     * @author Evin Roen
     * @version 11/19/2025
     */
    private static final class GridPanel extends JPanel {

        /**
         * This constant is the diameter of the circles used to represent
         * drones on the map (in pixels).
         */
        private static final int DIAMETER = 5;

        /**
         * This constant represents the buffer zone or margin between the graph lines
         * and the edge of the panel. This zone allows for the axis labels.
         */
        private static final Dimension BUFFER = new Dimension(24, 18);

        /**
         * This constant represents the size of the grid panel.
         */
        private static final Dimension SIZE = new Dimension(900, 456);

        /**
         * This is the maximum value for the scaling factory.
         */
        private static final int MAX_CELL_SIZE = 500;

        /**
         * This is the increment for the latitude and longitude labels.
         */
        private static final int LABEL_STEP = 10;

        /**
         * This field represents the size of the grid cells in pixels.
         */
        private int myCellSize;

        /**
         * This field represents the difference calculated to move the
         * grid when dragging the mouse.
         */
        private Point myDelta;

        /**
         * This field tracks the mouse x and y while the mouse is
         * being dragged.
         */
        private Point myDragPoint;

        /**
         * Constructor to initialize the object.
         */
        private GridPanel() {
            super();
            myDelta = new Point(0, 0);
            myDragPoint = new Point(0, 0);
            myCellSize = 50;
            initPanel();
        }

        /**
         * Initializes the JPanel.
         */
        private void initPanel() {
            setPreferredSize(SIZE);
            setBackground(ColorScheme.BACKGROUND_MAIN);

            // Center the grid initially
            myDelta = new Point(
                    SIZE.width / 2 - BUFFER.width,
                    SIZE.height / 2 - BUFFER.height
            );

            // Mouse listener to detect the start of a mouse drag
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent theE) {
                    myDragPoint = theE.getPoint();
                }
            });

            // Mouse wheel listener for zoom in / out.
            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(final MouseWheelEvent theE) {
                    myCellSize = Math.clamp(
                            myCellSize - theE.getWheelRotation() * 2L,
                            (getWidth() - 2 * BUFFER.width) / (LON_MAX * 2 / LABEL_STEP),
                            MAX_CELL_SIZE);
                    clampPan();
                    repaint();
                }
            });

            // Mouse motion listener to allow grid to be dragged / panned.
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(final MouseEvent theE) {
                    myDelta = new Point(
                            myDelta.x + theE.getX() - myDragPoint.x,
                            myDelta.y + theE.getY() - myDragPoint.y);
                    myDragPoint = theE.getPoint();
                    clampPan();
                    repaint();
                }
            });
        }

        @Override
        public void paintComponent(final Graphics theGraphics) {
            super.paintComponent(theGraphics);
            final Graphics2D g2D = (Graphics2D) theGraphics;

            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setColor(ColorScheme.GRID_LINE);

            drawGrid(g2D);
            drawDrones(g2D);
        }

        /**
         * Draws the grid for the map.
         *
         * @param theG2D the graphics object to draw with.
         * @throws IllegalArgumentException if graphics object is null.
         */
        private void drawGrid(final Graphics2D theG2D) {
            if (theG2D == null) {
                throw new IllegalArgumentException("Graphics object must not be null.");
            }

            FontMetrics fm = theG2D.getFontMetrics();
            theG2D.setColor(ColorScheme.TEXT_PRIMARY);

            // Horizontal lines
            int yOffset = (myDelta.y % myCellSize + myCellSize) % myCellSize + BUFFER.height;
            for (int i = 0; i * myCellSize + yOffset <= getHeight() - BUFFER.height; i++) {
                int y = i * myCellSize + yOffset;
                theG2D.setColor(ColorScheme.GRID_LINE);
                theG2D.drawLine(BUFFER.width, y, getWidth() - BUFFER.width, y);

                // label centered vertically on the line, in right buffer
                int worldY = (-(myDelta.y) + y - BUFFER.height) / myCellSize;
                String label = String.format("%d", -worldY * LABEL_STEP);
                int labelWidth = fm.stringWidth(label);
                int labelHeight = fm.getAscent();
                theG2D.setColor(ColorScheme.TEXT_PRIMARY);
                theG2D.drawString(
                        label,
                        getWidth() - BUFFER.width + (BUFFER.width - labelWidth) / 2,
                        y + labelHeight / 2 - 2
                );
            }

            // Vertical lines
            int xOffset = (myDelta.x % myCellSize + myCellSize) % myCellSize + BUFFER.width;
            for (int i = 0; i * myCellSize + xOffset <= getWidth() - BUFFER.width; i++) {
                int x = i * myCellSize + xOffset;
                theG2D.setColor(ColorScheme.GRID_LINE);
                theG2D.drawLine(x, BUFFER.height, x, getHeight() - BUFFER.height);

                // label centered horizontally on the line, in bottom buffer
                int worldX = (-(myDelta.x) + x - BUFFER.width) / myCellSize;
                String label = String.format("%d", worldX * LABEL_STEP);
                int labelWidth = fm.stringWidth(label);
                theG2D.setColor(ColorScheme.TEXT_PRIMARY);
                theG2D.drawString(
                        label,
                        x - labelWidth / 2,
                        getHeight() - BUFFER.height / 2 + fm.getAscent() / 2
                );
            }
        }

        /**
         * Draws the drones on the map.
         *
         * @param theG2D the graphics object to draw with.
         * @throws IllegalArgumentException if the graphics object is null.
         */
        private void drawDrones(final Graphics2D theG2D) {
            if (theG2D == null) {
                throw new IllegalArgumentException("Graphics object must not be null.");
            }
            double scale = 1.0 * myCellSize / LABEL_STEP;
            for (int id : ID_LOC_MAP.keySet()) {
                int[] loc = ID_LOC_MAP.get(id);
                int d = (int) Math.floor(DIAMETER * scale);
                int x = (int) Math.floor(loc[LON] * scale + myDelta.x - d / 2.0 + BUFFER.height);
                int y = (int) Math.floor(loc[LAT] * scale + myDelta.y - d / 2.0 + BUFFER.width);
                
                // Set color: dead > selected > normal
                if (DEAD_DRONES.contains(id)) {
                    theG2D.setColor(ColorScheme.DRONE_DEAD);
                } else if (id == mySelectedID) {
                    theG2D.setColor(ColorScheme.DRONE_SELECTED);
                } else {
                    theG2D.setColor(ColorScheme.DRONE);
                }
                
                theG2D.fillOval(x, y, d, d);
                theG2D.setColor(ColorScheme.BORDER_DARK);
                theG2D.drawOval(x, y, d, d);
            }
        }

        /**
         * Clamps the panning function so you can't go past the maximum and
         * minimum values.
         */
        private void clampPan() {
            // Size of the visible portion of the grid.
            int viewWidth = getWidth() - 2 * BUFFER.width;
            int viewHeight = getHeight() - 2 * BUFFER.height;

            // Bounds for delta, where delta is the
            // pixel coordinate of the top left corner
            // of the visible portion of the grid.
            int minDeltaX = -(LON_MAX / LABEL_STEP * myCellSize - viewWidth);
            int maxDeltaX = LON_MAX / LABEL_STEP * myCellSize;

            int minDeltaY = -(LAT_MAX / LABEL_STEP * myCellSize - viewHeight);
            int maxDeltaY = LAT_MAX / LABEL_STEP * myCellSize;

            myDelta.x = Math.max(minDeltaX, Math.min(myDelta.x, maxDeltaX));
            myDelta.y = Math.max(minDeltaY, Math.min(myDelta.y, maxDeltaY));
        }

        /**
         * Focuses (pans) the grid on the selected drone from the given id.
         *
         * @param theID the id of the drone to center on.
         * @throws IllegalArgumentException if the map does not contain the given drone ID.
         */
        public void focusOnSelected(final int theID) {
            if (!ID_LOC_MAP.containsKey(theID)) {
                throw new IllegalArgumentException("Given Drone ID is not in location mapping.");
            }
            int scale = myCellSize / LABEL_STEP;
            myDelta.x = -ID_LOC_MAP.get(theID)[LON] * scale + getWidth() / 2;
            myDelta.y = -ID_LOC_MAP.get(theID)[LAT] * scale + getHeight() / 2;
            clampPan();
        }
    }

}
