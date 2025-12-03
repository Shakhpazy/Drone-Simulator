package tests;

import model.Orientation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void findNextOrientationNoMovementReturnsCurrent() {
        Orientation o = new Orientation(123f);
        float next = o.findNextOrientation(1f, 2f, 1f, 2f);
        assertEquals(123f, next, 0.0001);
    }

    @Test
    void findNextOrientationNorthIsZeroDegrees() {
        Orientation o = new Orientation(0f);
        // prev (0,0) -> next (0,10): moving north
        float next = o.findNextOrientation(0f, 0f, 0f, 10f);
        assertEquals(0f, next, 0.0001);
    }

    @Test
    void findNextOrientationEastIsNinetyDegrees() {
        Orientation o = new Orientation(0f);
        // dx>0, dy=0 → 90°
        float next = o.findNextOrientation(0f, 0f, 10f, 0f);
        assertEquals(90f, next, 0.0001);
    }

    @Test
    void toStringFormatsDegree() {
        Orientation o = new Orientation(123.456f);
        assertEquals("123.46°", o.toString());
    }
}

