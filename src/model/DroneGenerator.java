package model;

import java.util.ArrayList;

/**
 * A factory-style class responsible for creating {@link DroneInterface} objects.
 * <p>
 * This class abstracts away the instantiation details of {@link Drone} objects.
 * Given a route composed of {@link RoutePoint} instances, it constructs a new
 * drone initialized with default parameters (such as ID and starting battery level).
 * <p>
 * This design improves modularity and allows future extension, such as:
 * <ul>
 *     <li>assigning unique IDs,</li>
 *     <li>creating different types/models of drones,</li>
 *     <li>injecting configuration settings,</li>
 *     <li>or supporting dependency injection for testing.</li>
 * </ul>
 *
 * @author Yusuf Shakhpaz
 */
public class DroneGenerator {

    /**
     * Creates a new {@link DroneInterface} using the provided route.
     * <p>
     * The drone will:
     * <ul>
     *     <li>start at the first point of the route,</li>
     *     <li>have an initial velocity and battery level defined internally
     *         by the {@link Drone} constructor,</li>
     *     <li>follow the provided route in a continuous loop.</li>
     * </ul>
     *
     * @param theRoute the list of {@link RoutePoint} objects defining the drone's movement path;
     *                 must not be {@code null}. An {@link IllegalArgumentException} is thrown
     *                 if this precondition is violated.
     *
     * @return a newly created {@link DroneInterface} instance configured with the given route
     *
     * @throws IllegalArgumentException if {@code theRoute} is {@code null}
     */
    public DroneInterface createDrone(final ArrayList<RoutePoint> theRoute) {
        if (theRoute == null) {
            throw new IllegalArgumentException("the route must be defined");
        }
        return new Drone(1, 100, theRoute);
    }

}
