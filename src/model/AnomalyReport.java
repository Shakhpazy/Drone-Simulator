package model;

import java.util.UUID;

/**
 * A simple, immutable data carrier for a drone anomaly report.
 *
 * @author nlevin11
 * @version 10/26
 */
public record AnomalyReport(
        UUID id,
        Long timestamp,
        String anomalyType,
        int droneId,
        String simpleReport,
        String detailedReport
) {
}
