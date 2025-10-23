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
            myPreviousTelemetry = new Telemetry(drone.getId(), drone.getAltitude(), drone.getLongitude(),
                    drone.getLatitude(), drone.getVelocity(), drone.getBatteryLevel(), drone.getOrientation());

            if (!drone.isAlive()) {
                continue;
            }

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
        float latitude = myPreviousTelemetry.getLatitude();
        float longitude = myPreviousTelemetry.getLongitude();
        float altitude = myPreviousTelemetry.getAltitude();

        RoutePoint nextPoint = theDrone.getNextPoint();

        float dx = nextPoint.getLatitude() - latitude;
        float dy = nextPoint.getLongitude() - longitude;
        float dz = nextPoint.getAltitude() - altitude;

        // distance left until we get to the next point
        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (distance <= theDrone.getVelocity()) {
            // reached waypoint
            latitude = nextPoint.getLatitude();
            longitude = nextPoint.getLongitude();
            altitude = nextPoint.getAltitude();
            theDrone.setNextRoute(); // advance to next waypoint
        } else {
            float ratio = theDrone.getVelocity() / distance;
            latitude += dx * ratio;
            longitude += dy * ratio;
            altitude += dz * ratio;
        }
        //now we need to update the drone state
        theDrone.updateDrone(longitude, latitude, altitude, 1);
        //after updating we can check if there is an anomaly
    }

}
