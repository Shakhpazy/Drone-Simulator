package model;

import java.util.ArrayList;
import java.util.Random;

public class TelemetryGenerator {

    ArrayList<DroneInterface> myDrones;

    Telemetry myPreviousTelemetry;

    private final Random random = new Random();


    public TelemetryGenerator(ArrayList<DroneInterface> theDrones) {
        myDrones = theDrones;
    }

    public void addDrone(DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    public void getRandomMove() {

    }

    public void getMove() {

    }

}
