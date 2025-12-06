package model;

/**
 * Manages battery consumption and battery level for a drone.
 * Handles battery drain calculations based on velocity and time.
 *
 * @author Yusuf
 */
public class BatterySystem {
    /** Base energy consumption per second (hovering + electronics). */
    private static final float BASE_DRAIN_RATE = 0.01f;
    
    /** Additional energy consumption per unit of velocity per second. */
    private static final float SPEED_DRAIN_RATE = 0.003f;
    
    /** Current remaining battery level. */
    private float myBatteryLevel;
    
    /**
     * Creates a BatterySystem with the given initial battery level.
     *
     * @param theInitialBattery the starting battery level
     */
    public BatterySystem(final float theInitialBattery) {
        myBatteryLevel = theInitialBattery;
    }
    
    /**
     * Calculates the amount of battery drained based on velocity and time.
     *
     * @param theVelocity the current velocity
     * @param theDeltaTime the time step
     * @return the amount of battery drained
     */
    public float calculateDrain(final float theVelocity, final float theDeltaTime) {
        float drain = BASE_DRAIN_RATE * theDeltaTime;
        float speed = Math.abs(theVelocity);
        drain += speed * SPEED_DRAIN_RATE * theDeltaTime;
        return drain;
    }
    
    /**
     * Applies battery drain to the current battery level.
     *
     * @param theDrainAmount the amount to drain
     * @return true if battery is now dead (0 or below)
     */
    public boolean drainBattery(final float theDrainAmount) {
        myBatteryLevel = Math.max(0, myBatteryLevel - theDrainAmount);
        return myBatteryLevel == 0;
    }
    
    /**
     * Sets the battery level.
     *
     * @param theBatteryLevel the new battery level
     * @throws IllegalArgumentException if battery level is negative
     */
    public void setBatteryLevel(final float theBatteryLevel) {
        if (theBatteryLevel < 0) {
            throw new IllegalArgumentException("Battery level cannot be below 0");
        }
        myBatteryLevel = theBatteryLevel;
    }
    
    /**
     * Gets the current battery level.
     *
     * @return the current battery level
     */
    public float getBatteryLevel() {
        return myBatteryLevel;
    }
}
