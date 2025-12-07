package model;

import java.util.Random;

/**
 * Handles random anomaly application for a drone.
 * Manages anomaly selection and effect application.
 *
 * @author Yusuf
 */
public class AnomalyHandler {
    private static final float ANOMALY_EXTRA_DRAIN_RATE = 0.1f;
    private static final float ANOMALY_ALTITUDE_CHANGE = 50f;
    private static final float ANOMALY_VELOCITY_CHANGE = 7;
    private static final float MIN_ALTITUDE = 0;
    private static final float MAX_VELOCITY = 10;
    private static final float MIN_VELOCITY = 0;
    
    private static final AnomalyEnum[] MOVEMENT_ANOMALIES = {
        AnomalyEnum.BATTERY_DRAIN,
        AnomalyEnum.BATTERY_FAIL,
        AnomalyEnum.ALTITUDE,
        AnomalyEnum.SPOOFING,
        AnomalyEnum.SPEED
    };
    
    private final Random myRandom = new Random();
    private AnomalyEnum myLastAnomaly;
    
    /**
     * Applies a random anomaly and returns the effects.
     *
     * @param theCurrentAltitude current altitude
     * @param theCurrentVelocity current velocity
     * @param theDeltaTime time step
     * @return AnomalyResult containing the effects
     */
    public AnomalyResult applyRandomAnomaly(
            final float theCurrentAltitude,
            final float theCurrentVelocity,
            final float theDeltaTime) {
        
        AnomalyEnum anomaly = MOVEMENT_ANOMALIES[myRandom.nextInt(MOVEMENT_ANOMALIES.length)];
        myLastAnomaly = anomaly;
        
        float newAltitude = theCurrentAltitude;
        float newVelocity = theCurrentVelocity;
        float extraDrain = 0f;
        boolean batteryFail = false;
        
        switch (anomaly) {
            case BATTERY_DRAIN:
                extraDrain = ANOMALY_EXTRA_DRAIN_RATE;
                break;
                
            case BATTERY_FAIL:
                newAltitude = 0;
                newVelocity = 0;
                batteryFail = true;
                break;
                
            case ALTITUDE:
                float changeAlt = (myRandom.nextBoolean() ? 1 : -1)
                        * ANOMALY_ALTITUDE_CHANGE * theDeltaTime;
                newAltitude = Math.max(MIN_ALTITUDE, theCurrentAltitude + changeAlt);
                break;
                
            case SPOOFING:
                // Spoofing handled by TelemetryGenerator, no movement change
                break;
                
            case SPEED:
                if (myRandom.nextBoolean()) {
                    newVelocity = Math.min(theCurrentVelocity + ANOMALY_VELOCITY_CHANGE, MAX_VELOCITY);
                } else {
                    newVelocity = Math.max(theCurrentVelocity - ANOMALY_VELOCITY_CHANGE, MIN_VELOCITY);
                }
                break;
        }
        
        return new AnomalyResult(newAltitude, newVelocity, extraDrain, batteryFail);
    }
    
    /**
     * Gets the last anomaly that was applied.
     *
     * @return the last anomaly
     */
    public AnomalyEnum getLastAnomaly() {
        return myLastAnomaly;
    }
    
    /**
     * Result of an anomaly application.
     */
    public static class AnomalyResult {
        public final float altitude;
        public final float velocity;
        public final float extraDrain;
        public final boolean batteryFail;
        
        public AnomalyResult(final float theAltitude, final float theVelocity,
                           final float theExtraDrain, final boolean theBatteryFail) {
            altitude = theAltitude;
            velocity = theVelocity;
            extraDrain = theExtraDrain;
            batteryFail = theBatteryFail;
        }
    }
}
