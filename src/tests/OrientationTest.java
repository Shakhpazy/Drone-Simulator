package tests;

import model.Orientation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test Orientation.
 * @author Yusuf
 * @version 12-05
 */
class OrientationTest {

    @Test
    void constructorAndGetDegree() {
        Orientation o = new Orientation(45f);
        assertEquals(45f, o.getDegree(), 0.0001);
    }

    @Test
    void setDegreesNormalizesAbove360() {
        Orientation o = new Orientation(0f);
        o.setDegrees(370f); // 370 -> 10
        assertEquals(10f, o.getDegree(), 0.0001);
    }

    @Test
    void setDegreesNormalizesNegative() {
        Orientation o = new Orientation(0f);
        o.setDegrees(-10f); // -10 -> 350
        assertEquals(350f, o.getDegree(), 0.0001);
    }

    @Test
    void findNextOrientationSouthIs180Degrees() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, 0f, -10f);
        assertEquals(180f, next, 0.0001);
    }

    @Test
    void findNextOrientationWestIs270Degrees() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, -10f, 0f);
        assertEquals(270f, next, 0.0001);
    }

    @Test
    void findNextOrientationNorthEastQuadrant() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, 10f, 10f);
        assertTrue(next > 0 && next < 90);
    }

    @Test
    void findNextOrientationNorthWestQuadrant() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, -10f, 10f);
        assertTrue(next > 270 || next == 270);
    }

    @Test
    void findNextOrientationSouthEastQuadrant() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, 10f, -10f);
        assertTrue(next > 90 && next < 180);
    }

    @Test
    void findNextOrientationSouthWestQuadrant() {
        Orientation o = new Orientation(0f);
        float next = o.findNextOrientation(0f, 0f, -10f, -10f);
        assertTrue(next > 180 && next < 270);
    }

    @Test
    void findNextOrientationDoesNotMutateCurrentDegree() {
        Orientation o = new Orientation(50f);
        float next = o.findNextOrientation(0f, 0f, 10f, 0f); // east (90)
        assertEquals(50f, o.getDegree(), 0.0001);            // unchanged
        assertEquals(90f, next, 0.0001);
    }

    @Test
    void findNextOrientationNoMovementReturnsCurrent() {
        Orientation o = new Orientation(123f);
        float next = o.findNextOrientation(1f, 2f, 1f, 2f);
        assertEquals(123f, next, 0.0001);
    }

    @Test
    void toStringFormatsDegree() {
        Orientation o = new Orientation(123.456f);
        assertEquals("123.46°", o.toString());
    }


}

