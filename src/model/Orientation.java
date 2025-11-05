package model;


//Currently the Degree 0 should be east with this current implementation
public class Orientation {

    float myDegree;

    public Orientation(float theDegree) {
        myDegree = theDegree;
    }

    public float getDegree() {
        return myDegree;
    }

    public void setDegrees(float theDegrees) {
        // Normalize using modulo arithmetic
        myDegree = ((theDegrees % 360) + 360) % 360;
    }

    /** Returns the difference (in degrees) between this orientation and another. */
    public float findNextOrientation(float thePrevLong, float thePrevLat, float theNextLong, float theNextLat) {
        float dx = theNextLong - thePrevLong;
        float dy = theNextLat - thePrevLat;

        float angleDegrees = (float) Math.toDegrees(Math.atan2(dy, dx));
        return ((angleDegrees % 360) + 360) % 360;
    }

    @Override
    public String toString() {
        return String.format("%.2f°", myDegree);
    }




}