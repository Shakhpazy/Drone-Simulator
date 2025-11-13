package model;

import java.util.ArrayList;

public class Drone extends AbstractDrone {

    ArrayList<RoutePoint> myRoute;

    private int nextPoint = 0;

    public Drone(float theVelocity, int theBatteryLevel, ArrayList<RoutePoint> theRoute) {
        super(
                !theRoute.isEmpty() ? theRoute.getFirst().getLongitude() : 0f,
                !theRoute.isEmpty() ? theRoute.getFirst().getLatitude()  : 0f,
                !theRoute.isEmpty() ? theRoute.getFirst().getAltitude()  : 0f,
                theVelocity,
                theBatteryLevel
        );

        if (theRoute.isEmpty()) {
            throw new IllegalArgumentException("Route cannot be empty");
        }
        myRoute = theRoute;
        nextPoint = 1;
    }

    public RoutePoint getNextPoint() {
        return myRoute.get(nextPoint % myRoute.size());
    }

    public void setNextRoute() {
        nextPoint = (nextPoint + 1) % myRoute.size();
    }



}
