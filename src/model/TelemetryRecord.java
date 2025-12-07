package model;

/**
 * A TelemetryRecord represents a single snapshot of a drone's state at a
 * specific moment in time. It is produced during each simulation update and
 * is used for logging, monitoring, and visualization of drone behavior.
 * <p>
 * Each record includes positional data, motion attributes, battery level,
 * orientation, and a timestamp for chronological ordering.
 *
 * @param id           unique identifier of the drone producing this record
 * @param longitude    drone's longitude at the time of recording
 * @param latitude     drone's latitude at the time of recording
 * @param altitude     drone's altitude at the time of recording
 * @param velocity     drone's current velocity
 * @param batteryLevel  drone's remaining battery level (0–100)
 * @param orientation  drone's orientation (e.g., yaw/heading angle in degrees)
 * @param timeStamp    system time (milliseconds since epoch) when the record was generated
 *
 * @author Team
 */
public record TelemetryRecord(
        int id, float longitude, float latitude, float altitude, float velocity, float batteryLevel, float orientation, long timeStamp
) {}




