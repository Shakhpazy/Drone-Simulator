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
    private float getRandom(final float min, final float max) {
        return min + random.nextFloat() * (max - min);
    }

}
