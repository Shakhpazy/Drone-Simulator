package model;


public class Orientation {

    /** The current degree of the Drone */
    float myDegree;

    /**
     * Creates an orientation object
     *
     * @param theDegree starting degree
     */
    public Orientation(final float theDegree) {
        myDegree = theDegree;
    }

    /**
     * @return {float} of the degree
     */
    public float getDegree() {
        return myDegree;
    }

    /**
     * Sets the degree of the drone, also is circular
     * so the degree range will always be between 0-359
     *
     * @param theDegrees {float} of the degree the drone is facing
     */
    public void setDegrees(final float theDegrees) {
        // Normalize using modulo arithmetic
        myDegree = ((theDegrees % 360) + 360) % 360;
    }

    /**
     *
     * @param thePrevLong the previous longitude of the Drone
     * @param thePrevLat the previous latitude of the Drone
     * @param theNextLong the next longitude of the Drone
     * @param theNextLat the next latitude of the Drone
     *
     * @return {float} degree of the next orientation
     */
    public float findNextOrientation(final float thePrevLong, final float thePrevLat, final float theNextLong, final float theNextLat) {
        float dx = theNextLong - thePrevLong;
        float dy = theNextLat - thePrevLat;

        float angleDegrees = (float) Math.toDegrees(Math.atan2(dx, dy));

        // Normalize to 0–360 range
        return ((angleDegrees % 360) + 360) % 360;
    }

    @Override
    public String toString() {
        return String.format("%.2f°", myDegree);
    }




}