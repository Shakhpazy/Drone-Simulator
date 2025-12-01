package model;

import java.util.ArrayList;
import java.util.Random;

/**
 * RouteGenerator is responsible for producing various types of waypoint-based
 * routes for drones to follow. Routes may be rectangular, circular, or fully
 * randomized depending on simulation needs.
 * <p>
 * All generated route points respect global geographic and altitude boundaries.
 *
 * @author Yusuf Shakhpaz
 */
public class RouteGenerator {

    // Geographic boundaries
    /** Minimum allowable latitude. */
    public static final float MIN_LAT = -90.0f;
    /** Maximum allowable latitude. */
    public static final float MAX_LAT = 90.0f;
    /** Minimum allowable longitude. */
    public static final float MIN_LON = -180.0f;
    /** Maximum allowable longitude. */
    public static final float MAX_LON = 180.0f;
    /** Minimum altitude */
    public static final float MIN_ALT = 1.0f;
    /** Maximum altitude */
    public static final float MAX_ALT = 1000.0f;

    /**
     * Minimum required lat/lon difference between two corners when generating
     * predictable test rectangles. Used only in validation routines.
     */
    private static final float MIN_SIDE_LENGTH = 80.0f;

    /** Random generator used for producing route point coordinates. */
    private static final Random random = new Random();

    /**
     * Generates a route for a drone. Each call randomly chooses between a
     * rectangle-shaped route and a fully random multipoint route.
     *
     * @return a route consisting of multiple {@link RoutePoint} objects
     */
    public ArrayList<RoutePoint> generateRoute() {
        boolean rectangle = random.nextBoolean();  // true or false 50/50

        if (rectangle) {
            return generateRectangle();
        } else {
            return generateRandomRoute();
        }
    }

    /**
     * Generates a simple rectangle-shaped route defined by two randomly chosen
     * latitude/longitude pairs. The route consists of four corners representing
     * a quadrilateral around the chosen coordinates.
     *
     * @return a rectangular route with four points
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
     * Generates a fully random route composed of 10 points. Each point is
     * independently chosen within the valid geographic and altitude ranges.
     *
     * @return a list of 10 randomly distributed route points
     */
    private ArrayList<RoutePoint> generateRandomRoute() {
        final int NUM_POINTS = 10;

        ArrayList<RoutePoint> route = new ArrayList<>();

        for (int i = 0; i < NUM_POINTS; i++) {
            route.add(getRandomPoint());
        }

        return route;
    }



    /**
     * Generates a circular route consisting of 8 equally spaced points centered
     * around a random location. The circle radius is chosen such that all points
     * stay within global geographic bounds.
     *
     * @return a circular route containing 8 points
     */
    private ArrayList<RoutePoint> generateCircle() {

        // Pick a safe center anywhere
        float centerLat = getRandom(MIN_LAT + 20, MAX_LAT - 20);
        float centerLon = getRandom(MIN_LON + 20, MAX_LON - 20);

        // Compute max radius allowed by boundaries
        float maxLatRadius = Math.min(centerLat - MIN_LAT, MAX_LAT - centerLat);
        float maxLonRadius = Math.min(centerLon - MIN_LON, MAX_LON - centerLon);

        // Overall max radius (stay in bounds on both axes)
        float maxRadius = Math.min(maxLatRadius, maxLonRadius);

        // Make the circle big: 30%–90% of the max possible radius
        float radius = maxRadius * (0.30f + random.nextFloat() * 0.60f);

        ArrayList<RoutePoint> route = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);

            float lat = centerLat + radius * (float) Math.cos(angle);
            float lon = centerLon + radius * (float) Math.sin(angle);
            float alt = getRandom(MIN_ALT, MAX_ALT);

            route.add(new RoutePoint(lon, lat, alt));
        }

        return route;
    }

    /**
     * Generates a random {@link RoutePoint} with latitude, longitude, and altitude
     * values within the allowed global boundaries.
     *
     * <p>The point is uniformly sampled within:</p>
     * <ul>
     *     <li>Longitude: [MIN_LON, MAX_LON]</li>
     *     <li>Latitude:  [MIN_LAT, MAX_LAT]</li>
     *     <li>Altitude:  [MIN_ALT, MAX_ALT]</li>
     * </ul>
     *
     * @return a newly generated random {@link RoutePoint}
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
     * Generates a predictable rectangular route used primarily for unit testing.
     * The method ensures that the rectangle has a minimum required side length
     * to prevent degenerate or trivial shapes.
     *
     * @return a valid rectangular test route
     */
    private ArrayList<RoutePoint> generatePredictableRect() {
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

    /**
     * Determines whether a rectangular route is valid based on differences in
     * latitude and longitude. Ensures the rectangle is not too small.
     *
     * @param lon1 first longitude
     * @param lat1 first latitude
     * @param lon2 second longitude
     * @param lat2 second latitude
     * @return true if the rectangle meets minimum side-length requirements
     */    private boolean validRectangle(final float lon1, final float lat1, final float lon2, final float lat2) {
        return Math.abs(lon1 - lon2) >= MIN_SIDE_LENGTH &&
                Math.abs(lat1 - lat2) >= MIN_SIDE_LENGTH;
    }

}
