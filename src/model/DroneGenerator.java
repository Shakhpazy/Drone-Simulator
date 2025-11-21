package model;

import java.util.ArrayList;

/**
 * A factory-style class responsible for creating {@link DroneInterface} objects.
 * <p>
 * This class abstracts the creation logic for drones. Given a route consisting
 * of {@link RoutePoint} objects, it instantiates and returns a new {@link Drone}
 * configured with default parameters (e.g., drone ID and starting battery).
 * </p>
 */
public class DroneGenerator {

    /**
     * Creates a new Drone instance using the provided route.
     *
     * @param theRoute the list of {@link RoutePoint} objects that define the drone's route;
     *                 must not be {@code null}
     *
     * @return a newly created {@link DroneInterface}
     */
    public DroneInterface createDrone(ArrayList<RoutePoint> theRoute) {
        if (theRoute == null) {
            throw new IllegalArgumentException("the route must be defined");
        }
        DroneInterface drone = new Drone(1, 100, theRoute);
        return drone;
    }

    /**
     * Create a new Drone instance with no Routes
     * (maybe I can use this for SuperSonic Drone or different types of drones with not route given)
     *
     * @return a newly created {@link DroneInterface}
     */
    public DroneInterface createDrone() {
        return null;
    }

}
