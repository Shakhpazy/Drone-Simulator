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
    public static final float MIN_ALT = 0.0f;      // ground level
    public static final float MAX_ALT = 1000.0f;   // max altitude (e.g., 1 km)

    private static final Random random = new Random();

    /**
     * For now we are just doing circle and rectangular routes
     *
     * @return A Route
     */
    public ArrayList<RoutePoint> generateRoute() {
        if (random.nextBoolean()) {
            return generateRectangle();
        } else {
            return generateCircle();
        }
    }

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
        route.add(new RoutePoint(lat1, lon1, altitude1)); // A
        route.add(new RoutePoint(lat1, lon2, altitude2)); // B
        route.add(new RoutePoint(lat2, lon2, altitude3)); // C
        route.add(new RoutePoint(lat2, lon1, altitude4)); // D

        return route;
    }

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
            route.add(new RoutePoint(lat, lon, altitude));
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
        return new RoutePoint(lat, lon, alt);
    }

    private float getRandom(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
