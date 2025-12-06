package model;

import java.util.ArrayList;

/**
 * Represents a fully autonomous drone within the simulation.
 * <p>
 * This class implements {@link DroneInterface} and coordinates between
 * three composed systems: BatterySystem, NavigationSystem, and AnomalyHandler.
 *
 * @author Yusuf
 */
public class Drone implements DroneInterface {
    /** Minimum allowed altitude in normal moves. */
    private static final float MIN_ALTITUDE = 0;
    
    /** Max allowed altitude in normal moves. */
    private static final float MAX_ALTITUDE = 1000;
    
    /** Maximum allowed velocity in normal moves. */
    private static final float MAX_VELOCITY = 10;
    
    /** Minimum allowed velocity in normal moves. */
    private static final float MIN_VELOCITY = 0;
    
    /** Step size for increasing or decreasing velocity during movement. */
    private static final float ACCELERATION_STEP = 0.3f;
    
    /** Current altitude. */
    private float myAltitude;
    
    /** Current longitude. */
    private float myLongitude;
    
    /** Current latitude. */
    private float myLatitude;
    
    /** Current velocity. */
    private float myVelocity;
    
    /** Current orientation object (heading in degrees). */
    private final Orientation myOrientation;
    
    /** The drones health */
    private boolean myDroneIsAlive;
    
    private TelemetryRecord prevTelemetryRecord;
    
    /** Unique drone identifier. */
    private final int myID;
    
    /** Total number of drones created so far (global counter). */
    private static int totalDrones = 0;
    
    /** Composed systems */
    private final BatterySystem myBatterySystem;
    private final NavigationSystem myNavigationSystem;
    private final AnomalyHandler myAnomalyHandler;
    
    /**
     * Constructor for the Drone
     *
     * @param theVelocity The velocity of the Drone
     * @param theBatteryLevel The Battery of the Drone
     * @param theRoute The Route of the Drone.
     */
    public Drone(final float theVelocity, final int theBatteryLevel, final ArrayList<RoutePoint> theRoute) {
        if (theRoute.isEmpty()) {
            throw new IllegalArgumentException("Route cannot be empty");
        }
        if (theRoute.getFirst().getAltitude() < MIN_ALTITUDE || theRoute.getFirst().getAltitude() > MAX_ALTITUDE ||
                theVelocity > MAX_VELOCITY || theVelocity < MIN_VELOCITY) {
            throw new IllegalArgumentException("Arguments passed are not valid theAltitude or the Velocity is not in bound");
        }
        
        myLongitude = theRoute.getFirst().getLongitude();
        myLatitude = theRoute.getFirst().getLatitude();
        myAltitude = theRoute.getFirst().getAltitude();
        myVelocity = theVelocity;
        myOrientation = new Orientation(0);
        myDroneIsAlive = true;
        totalDrones++;
        myID = totalDrones;
        
        // Initialize composed systems
        myBatterySystem = new BatterySystem(theBatteryLevel);
        myNavigationSystem = new NavigationSystem(theRoute);
        myAnomalyHandler = new AnomalyHandler();
        
        prevTelemetryRecord = generateTelemetryRecord();
    }
    
    public int getId() {
        return myID;
    }
    
    public float getLongitude() {
        return myLongitude;
    }
    
    public float getLatitude() {
        return myLatitude;
    }
    
    public float getAltitude() {
        return myAltitude;
    }
    
    public float getVelocity() {
        return myVelocity;
    }
    
    public Orientation getOrientation() {
        return myOrientation;
    }
    
    public float getBatteryLevel() {
        return myBatterySystem.getBatteryLevel();
    }
    
    public float getAccelerationStep() {
        return ACCELERATION_STEP;
    }
    
    public AnomalyEnum getMyLastAnomaly() {
        return myAnomalyHandler.getLastAnomaly();
    }
    
    public static int getTotalDrones() {
        return totalDrones;
    }
    
    public boolean isAlive() {
        return myDroneIsAlive;
    }
    
    @Override
    public RoutePoint getNextPoint() {
        return myNavigationSystem.getNextPoint();
    }
    
    public void setAltitude(final float theAltitude) {
        myAltitude = Math.max(0, theAltitude);
    }
    
    public void setLongitude(final float theLongitude) {
        myLongitude = theLongitude;
    }
    
    public void setLatitude(final float theLatitude) {
        myLatitude = theLatitude;
    }
    
    public void setBatteryLevel(final float theBatteryLevel) {
        myBatterySystem.setBatteryLevel(theBatteryLevel);
        if (myBatterySystem.getBatteryLevel() == 0) {
            setIsAlive(false);
        }
    }
    
    public void setNextRoute() {
        myNavigationSystem.advanceToNextPoint();
    }
    
    public void setVelocity(final float theVelocity) {
        if (theVelocity < MIN_VELOCITY || theVelocity > MAX_VELOCITY) {
            throw new IllegalArgumentException("The velocity must stay in bound");
        }
        myVelocity = theVelocity;
    }
    
    public void setOrientation(final float theDegree) {
        myOrientation.setDegrees(theDegree);
    }
    
    public void setIsAlive(final boolean theHealth) {
        myDroneIsAlive = theHealth;
    }
    
    public void setPrevTelemetryRecord(TelemetryRecord theTelemetryRecord) {
        prevTelemetryRecord = theTelemetryRecord;
    }
    
    /**
     * Updates the state of the entire Drone when the drone moves to its next position
     */
    public void updateDrone(final float theLongitude, final float theLatitude, final float theAltitude,
                           final float theBatteryDrained, final float theVelocity, final float theDegree) {
        setLongitude(theLongitude);
        setLatitude(theLatitude);
        setAltitude(theAltitude);
        setVelocity(theVelocity);
        setOrientation(theDegree);
        boolean isDead = myBatterySystem.drainBattery(theBatteryDrained);
        if (isDead) {
            setIsAlive(false);
        }
    }
    
    public TelemetryRecord getPreviousTelemetryRecord() {
        return prevTelemetryRecord;
    }
    
    public TelemetryRecord generateTelemetryRecord() {
        return new TelemetryRecord(
                myID,
                myLongitude,
                myLatitude,
                myAltitude,
                myVelocity,
                myBatterySystem.getBatteryLevel(),
                myOrientation.getDegree(),
                System.currentTimeMillis()
        );
    }
    
    public void collided() {
        setAltitude(0);
        setVelocity(0);
        setIsAlive(false);
    }
    
    @Override
    public void getNextRandomMove(final float theDeltaTime) {
        float latitude = this.getLatitude();
        float longitude = this.getLongitude();
        float altitude = this.getAltitude();
        float velocity = this.getVelocity();
        
        // Apply anomaly
        AnomalyHandler.AnomalyResult anomalyResult = myAnomalyHandler.applyRandomAnomaly(
                altitude, velocity, theDeltaTime);
        
        if (anomalyResult.batteryFail) {
            setVelocity(0);
            setAltitude(0);
            setBatteryLevel(0);
            return;
        }
        
        altitude = anomalyResult.altitude;
        velocity = anomalyResult.velocity;
        float extraDrain = anomalyResult.extraDrain;
        
        // Calculate normal battery drain
        float normalDrain = myBatterySystem.calculateDrain(velocity, theDeltaTime);
        float totalDrain = normalDrain + extraDrain;
        
        // Calculate orientation
        float degree = getOrientation().findNextOrientation(
                this.getLongitude(),
                this.getLatitude(),
                longitude,
                latitude
        );
        
        updateDrone(longitude, latitude, altitude, totalDrain, velocity, degree);
    }
    
    @Override
    public void getNextMove(final float theDeltaTime) {
        // Use navigation system to calculate next move
        NavigationSystem.MovementResult result = myNavigationSystem.calculateNextMove(
                myLongitude, myLatitude, myAltitude, myVelocity, theDeltaTime);
        
        // Calculate battery drain
        float drained = myBatterySystem.calculateDrain(result.velocity, theDeltaTime);
        
        // Calculate orientation
        float degree = getOrientation().findNextOrientation(
                myLongitude, myLatitude, result.longitude, result.latitude);
        
        updateDrone(result.longitude, result.latitude, result.altitude, drained, result.velocity, degree);
    }
    
    @Override
    public String toString() {
        return String.format(
                "Drone{id=%d, alive=%s, lon=%.2f, lat=%.2f, alt=%.2f, vel=%.2f, battery=%.2f, orientation=%.2f°}",
                myID,
                myDroneIsAlive,
                myLongitude,
                myLatitude,
                myAltitude,
                myVelocity,
                myBatterySystem.getBatteryLevel(),
                myOrientation.getDegree()
        );
    }
}
