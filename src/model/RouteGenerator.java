package model;

import java.util.ArrayList;
import java.util.Random;

public class RouteGenerator {

    // Geographic boundaries (in decimal degrees)
    public static final float MIN_LAT = -90.0f;
    public static final float MAX_LAT = 90.0f;
    public static final float MIN_LON = -180.0f;
    public static final float MAX_LON = 180.0f;

    // Altitude boundaries (in meters)
    public static final float MIN_ALT = 1.0f;      // ground level
    public static final float MAX_ALT = 1000.0f;   // max altitude (e.g., 1 km)

    // Minimum required difference between lat/lon (side lengths) This is just used of testing
    private static final float MIN_SIDE_LENGTH = 80.0f;

    /** Random */
    private static final Random random = new Random();

    /**
     * Generates a drone route. By default, this returns a predictable rectangular
     * route intended for testing reproducible drone behavior.
     *
     * @return a Route
     */
    public ArrayList<RoutePoint> generateRoute() {
        return generatePredictableRect();
    }

    /**
     * Generates a random rectangle-shaped route using random latitude, longitude,
     * and altitude values. The resulting route contains four points representing
     * a simple quadrilateral (A → B → C → D).
     *
     * @return a randomly generated rectangular route
     */
    private ArrayList<RoutePoint> generateRectangle() {
        float lat1 = getRandom(MIN_LAT, MAX_LAT);
        float lon1 = getRandom(MIN_LON, MAX_LON);
        float lat2 = getRandom(MIN_LAT, MAX_LAT);
        float lon2 = getRandom(MIN_LON, MAX_LON);

        float altitude1 = getRandom(MIN_ALT, MAX_ALT);
        float altitude2 = getRandom(MIN_ALT, MAX_ALT);
        float altitude3 = getRandom(MIN_ALT, MAX_ALT);
        float altitude4 = getRandom(MIN_ALT, MAX_ALT);

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(lon1, lat1, altitude1)); // A
        route.add(new RoutePoint(lon2, lat1, altitude2)); // B
        route.add(new RoutePoint(lon2, lat2, altitude3)); // C
        route.add(new RoutePoint(lon1, lat2, altitude4)); // D

        return route;
    }

    /**
     * Generates a circular route with 8 equally spaced points around
     * a random center. Each point has a randomly assigned altitude.
     *
     * @return a circular route consisting of 8 points
     */
    private ArrayList<RoutePoint> generateCircle() {
        float centerLat = getRandom(MIN_LAT + 10, MAX_LAT - 10);
        float centerLon = getRandom(MIN_LON + 10, MAX_LON - 10);
        float radius = 5.0f + random.nextFloat() * 10.0f;  // radius between 5-15 degrees

        ArrayList<RoutePoint> route = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45); // 8 points spaced at 45-degree intervals
            float lat = centerLat + radius * (float) Math.cos(angle);
            float lon = centerLon + radius * (float) Math.sin(angle);
            float altitude = getRandom(MIN_ALT, MAX_ALT);
            route.add(new RoutePoint(lon, lat, altitude));
        }

        return route;
    }

    /**
     * Generates a completely random route consisting of a fixed number
     * of random objects. Each point with the allowed geographic and altitude ranges.
     *
     * @return a route with 10 points
     */
    public ArrayList<RoutePoint> generateRandomRoute() {
        final int NUM_POINTS = 10;

        ArrayList<RoutePoint> route = new ArrayList<>();

        for (int i = 0; i < NUM_POINTS; i++) {
            route.add(getRandomPoint());
        }

        return route;
    }

    /**
     * Use late for other Completely Random Routes
     *
     * @return RoutePoint.
     */
    private RoutePoint getRandomPoint() {
        float lat = getRandom(MIN_LAT, MAX_LAT);
        float lon = getRandom(MIN_LON, MAX_LON);
        float alt = getRandom(MIN_ALT, MAX_ALT);
        return new RoutePoint(lon, lat, alt);
    }

    /**
     * Returns a random float value between {@code min} and {@code max}.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random float in the range [min, max)
     */
    private float getRandom(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    /**
     * @return a predictable Route of a Drone used for testing
     */
    public ArrayList<RoutePoint> generatePredictableRect() {
        float lon1, lat1, lon2, lat2;

        do {
            lon1 = getRandom(MIN_LON, MAX_LON);
            lat1 = getRandom(MIN_LAT, MAX_LAT);
            lon2 = getRandom(MIN_LON, MAX_LON);
            lat2 = getRandom(MIN_LAT, MAX_LAT);
        } while (!validRectangle(lon1, lat1, lon2, lat2));

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(lon1, lat1, getRandom(MIN_ALT, MAX_ALT))); // A
        route.add(new RoutePoint(lon2, lat1, getRandom(MIN_ALT, MAX_ALT))); // B
        route.add(new RoutePoint(lon2, lat2, getRandom(MIN_ALT, MAX_ALT))); // C
        route.add(new RoutePoint(lon1, lat2, getRandom(MIN_ALT, MAX_ALT))); // D

        return route;
    }

    /** Checks if the rectangle is valid {testing only} */
    private boolean validRectangle(float lon1, float lat1, float lon2, float lat2) {
        return Math.abs(lon1 - lon2) >= MIN_SIDE_LENGTH &&
                Math.abs(lat1 - lat2) >= MIN_SIDE_LENGTH;
    }
}
