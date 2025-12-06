package model;

/**
 * Represents the orientation (heading) of a drone in degrees.
 * <p>
 * Orientation values are always normalized to the range {@code 0–359} degrees.
 * The orientation can be updated directly or computed based on movement
 * between two geographic points.
 *
 * @author Yusuf Shakhpaz
 */
public class Orientation {

    /** Current heading of the drone, stored in degrees (0–359). */
    private float myDegree;

    /**
     * Constructs an Orientation object with the given initial degree.
     * The degree is automatically normalized into the 0–359 range.
     *
     * @param theDegree initial heading in degrees
     */
    public Orientation(final float theDegree) {
        myDegree = theDegree;
    }

    /**
     * Returns the drone's current orientation in degrees.
     *
     * @return heading angle in degrees (0–359)
     */
    public float getDegree() {
        return myDegree;
    }

    /**
     * Sets the drone’s orientation to the specified degree, normalizing it
     * such that the value always remains within the range {@code 0–359}.
     *
     * @param theDegrees new heading angle
     */
    public void setDegrees(final float theDegrees) {
        // Normalize using modulo arithmetic
        myDegree = ((theDegrees % 360) + 360) % 360;
    }

    /**
     * Computes the next orientation of the drone based on its motion from
     * a previous position to a new position. If there is no movement
     * (dx = dy = 0), the current orientation is returned unchanged.
     * <p>
     * Orientation is computed using {@link Math#atan2(double, double)},
     * converting the resulting radian angle into degrees and normalizing
     * it into the {@code 0–359} range.
     *
     * @param thePrevLong previous longitude of the drone
     * @param thePrevLat  previous latitude of the drone
     * @param theNextLong next longitude of the drone
     * @param theNextLat  next latitude of the drone
     *
     * @return computed orientation angle (0–359 degrees)
     */
    public float findNextOrientation(final float thePrevLong, final float thePrevLat, final float theNextLong, final float theNextLat) {
        float dx = theNextLong - thePrevLong;
        float dy = theNextLat - thePrevLat;

        //safety check for no movement
        if (dx == 0 && dy == 0) {
            return myDegree;
        }

        float angleDegrees = (float) Math.toDegrees(Math.atan2(dx, dy));

        // Normalize to 0–360 range
        return ((angleDegrees % 360) + 360) % 360;
    }

    /**
     * Returns a formatted string representation of this orientation,
     * displaying the degree with two decimal places.
     *
     * @return string like "123.45°"
     */
    @Override
    public String toString() {
        return String.format("%.2f°", myDegree);
    }




}