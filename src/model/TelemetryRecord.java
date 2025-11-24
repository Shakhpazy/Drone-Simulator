package model;

public record TelemetryRecord(
        int id, float longitude, float latitude, float altitude, float velocity, float batterLevel, float orientation, long timeStamp
) {}




