package tests;

import model.Drone;
import model.Orientation;
import model.RoutePoint;
import view.MonitorDashboard;

import java.util.ArrayList;

public class TestView {

    public static void main(String[] args) {
        MonitorDashboard view  =  new MonitorDashboard();

        ArrayList<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(100, 100, 110)); // bottom-left
        route.add(new RoutePoint(130, 100, 115)); // bottom-right (30 units)

        Drone drone1 = new Drone(3.0f, 100, Orientation.NORTH, route);

        view.drawDrone(drone1.getId(), new float[]{12, 12}, "");
    }
}
