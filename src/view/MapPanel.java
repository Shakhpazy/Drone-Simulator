package view;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MapPanel extends JPanel {

    /**
     * This constant is an alias for 0 to reference the first
     * element in an array of {latitude, longitude}.
     */
    private static final int LAT = 0;

    /**
     * This constant is an aliad for 1 to reference the second
     * element in an array of {latitude, longitude}.
     */
    private static final int LON = 1;

    /**
     * This constant is the diameter of the circles used to represent
     * drones on the map (in pixels).
     */
    private static final int DIAMETER = 10;

    /**
     * This constant determines the size of the panel.
     */
    private static final Dimension SIZE = new Dimension(930, 530);

    /**
     * This constant is a mapping between Drones' IDs and their locations.
     */
    private static final Map<Integer, int[]> ID_LOC_MAP = new HashMap<>();

    /**
     * Constructor to initialize panel.
     */
    public MapPanel() {
        super();
        initPanel();
    }

    /**
     * Sets up the size and border for the panel.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    /**
     * Stores the given location of the provided drone id number. If the
     * given drone ID is not contained in the mapping, it is added. If it
     * is already added, then the location is updated.
     *
     * @param theID the id number of the drone.
     * @param theLoc the latitude and longitude of the drone, in that order.
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
        return new int[] {(int) Math.floor(theLoc[LAT]) - DIAMETER / 2, (int) Math.floor(theLoc[LON]) - DIAMETER / 2};
    }

    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        final Graphics2D g2D = (Graphics2D) theGraphics;

        // Turn antialiasing on (looks better)
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw drones
        for (int[] loc : ID_LOC_MAP.values()) {
            g2D.setColor(Color.RED); // filled circle
            g2D.fillOval(loc[LAT], loc[LON], DIAMETER, DIAMETER);
            g2D.setColor(Color.BLACK); // outline
            g2D.drawOval(loc[LAT], loc[LON], DIAMETER, DIAMETER);
        }
    }

}
