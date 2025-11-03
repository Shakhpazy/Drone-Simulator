package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the drone's locations on a map.
 */
public class MapPanel extends JPanel {

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

    private static final GridPanel GRID = new GridPanel();

    /**
     * Constructor to initialize panel.
     */
    public MapPanel() {
        super();
        initPanel();
        add(GRID, BorderLayout.CENTER);

    }

    /**
     * Sets up the size and border for the panel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BorderLayout());
    }

    /**
     * Stores the given location of the provided drone id number. If the
     * given drone ID is not contained in the mapping, it is added. If it
     * is already added, then the location is updated.
     *
     * @param theID the id number of the drone.
     * @param theLoc the longitude and latitude of the drone, in that order.
     */
    public void setDroneMapping(final int theID, final float[] theLoc) {
        ID_LOC_MAP.put(theID, formatLocation(theLoc));
    }

    /**
     * Converts the float array from the drone to a drawable int array.
     *
     * @param theLoc the float array to convert.
     * @return the given float array as an int array, floored and centered.
     */
    private int[] formatLocation(final float[] theLoc) {
        return new int[] {
                (int) Math.floor(theLoc[LON]),
                (int) Math.floor(-theLoc[LAT])};
    }

    /**
     * This inner class represents the window in which the grid and drones will be drawn.
     */
    private static final class GridPanel extends JPanel {

        /**
         * This constant represents the size of the grid cells in pixels.
         */
        private static final int CELL_SIZE = 10;

        /**
         * This constant is the diameter of the circles used to represent
         * drones on the map (in pixels).
         */
        private static final int DIAMETER = 5;

        /**
         * This constant represents the buffer zone or margin between the graph lines
         * and the edge of the panel. This zone allows for the axis labels.
         */
        private static final int BUFFER = 40;

        /**
         * This constant represents the size of the grid panel.
         */
        private static final Dimension SIZE = new Dimension(900, 456);

        /**
         * This constant represents the minimum value for the scaling factor.
         */
        private static final int MIN_SCALE = 3;

        /**
         * This is the maximum value for the scaling factory.
         */
        private static final int MAX_SCALE = 8;

        /**
         * Zoom scale factor of the grid.
         */
        private int myScale;

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
        public GridPanel() {
            super();
            myDelta = new Point(0, 0);
            myDragPoint = new Point(0, 0);
            myScale = 6;
            initPanel();
        }

        /**
         * Initializes the JPanel.
         */
        private void initPanel() {
            setPreferredSize(SIZE);

            // Mouse listener to detect the start of a mouse drag
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent theE) {
                    myDragPoint = theE.getPoint();
                }
            });

            // Mouse wheel listener for zoom in / out.
            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent theE) {
                    myScale = Math.clamp(myScale + theE.getWheelRotation(), MIN_SCALE, MAX_SCALE);
                    clampPan();
                    repaint();
                }
            });

            // Mouse motion listener to allow grid to be dragged / panned.
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent theE) {
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
            g2D.setColor(Color.GRAY);

            drawGrid(g2D);
            drawDrones(g2D);
        }

        /**
         * Draws the grid for the map.
         *
         * @param theG2D the graphics object to draw with.
         */
        private void drawGrid(final Graphics2D theG2D) {
            FontMetrics fm = theG2D.getFontMetrics();

            // Horizontal lines
            int yOffset = (myDelta.y % (CELL_SIZE * myScale) + (CELL_SIZE * myScale)) % (CELL_SIZE * myScale) + BUFFER;
            for (int i = 0; i * (CELL_SIZE * myScale) + yOffset <= getHeight() - BUFFER; i++) {
                int y = i * (CELL_SIZE * myScale) + yOffset;
                theG2D.drawLine(BUFFER, y, getWidth() - BUFFER, y);

                // label centered vertically on the line, in right buffer
                int worldY = (-(myDelta.y) + y - BUFFER) / (CELL_SIZE * myScale);
                String label = String.format("%d", -worldY * CELL_SIZE);
                int labelWidth = fm.stringWidth(label);
                int labelHeight = fm.getAscent();
                theG2D.drawString(
                        label,
                        getWidth() - BUFFER + (BUFFER - labelWidth) / 2,
                        y + labelHeight / 2 - 2
                );
            }

            // Vertical lines
            int xOffset = (myDelta.x % (CELL_SIZE * myScale) + (CELL_SIZE * myScale)) % (CELL_SIZE * myScale) + BUFFER;
            for (int i = 0; i * (CELL_SIZE * myScale) + xOffset <= getWidth() - BUFFER; i++) {
                int x = i * (CELL_SIZE * myScale) + xOffset;
                theG2D.drawLine(x, BUFFER, x, getHeight() - BUFFER);

                // label centered horizontally on the line, in bottom buffer
                int worldX = (-(myDelta.x) + x - BUFFER) / (CELL_SIZE * myScale);
                String label = String.format("%d", worldX * CELL_SIZE);
                int labelWidth = fm.stringWidth(label);
                theG2D.drawString(
                        label,
                        x - labelWidth / 2,
                        getHeight() - BUFFER / 2 + fm.getAscent() / 2
                );
            }
        }

        /**
         * Draws the drones on the map.
         *
         * @param theG2D the graphics object to draw with.
         */
        private void drawDrones(final Graphics2D theG2D) {
            for (int[] loc : ID_LOC_MAP.values()) {
                int d = DIAMETER * myScale;
                int x = loc[LON] * myScale + myDelta.x - d / 2 + BUFFER;
                int y = loc[LAT] * myScale + myDelta.y - d / 2 + BUFFER;

                // Only draw drones if fully visible (avoid buffer zone)
                if (x >= -LON_MAX * myScale && x <= getWidth() - BUFFER &&
                        y >= -LAT_MAX * myScale && y <= getHeight() - BUFFER) {
                    theG2D.setColor(Color.RED);
                    theG2D.fillOval(x, y, d, d);
                    theG2D.setColor(Color.BLACK);
                    theG2D.drawOval(x, y, d, d);
                }
            }
        }

        /**
         * Clamps the panning function so you can't go past the maximum and
         * minimum values.
         */
        private void clampPan() {
            int viewWidth  = getWidth() - 2 * BUFFER;
            int viewHeight = getHeight() - 2 * BUFFER;

            int minDeltaX = -LON_MAX * myScale + viewWidth;
            int maxDeltaX = LON_MAX * myScale;

            int minDeltaY = -LAT_MAX * myScale + viewHeight;
            int maxDeltaY = LAT_MAX * myScale;

            myDelta.x = Math.max(minDeltaX, Math.min(myDelta.x, maxDeltaX));
            myDelta.y = Math.max(minDeltaY, Math.min(myDelta.y, maxDeltaY));
        }
    }

}
