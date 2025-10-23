package model;

import java.util.ArrayList;
import java.util.Random;

public class TelemetryGenerator {

    ArrayList<DroneInterface> myDrones;

    Telemetry myPreviousTelemetry;

    private final Random myRandom = new Random();

    private final int RANDOM_PERCENT = 15; //Should be set from 0-100


    public TelemetryGenerator(ArrayList<DroneInterface> theDrones) {
        myDrones = theDrones;
    }

    public void addDrone(DroneInterface theDrone) {
        myDrones.add(theDrone);
    }

    /**
     * This will process all drones and decide if the specific
     * drone will get a random move or a normal move.
     */
    public void processAllDrones() {
        for (DroneInterface drone : myDrones) {
            if (myRandom.nextInt(100) < RANDOM_PERCENT) {
                getRandomMove(drone);
            } else {
                getMove(drone);
            }
        }
    }

    public void getRandomMove(DroneInterface theDrone) {

    }

    public void getMove(DroneInterface theDrone) {

    }

}
