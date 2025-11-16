package model;

import java.util.ArrayList;

public class DroneGenerator {

    public DroneInterface createDrone(ArrayList<RoutePoint> theRoute) {
        DroneInterface drone = new Drone(1, 100, theRoute);
        return drone;
    }

}
