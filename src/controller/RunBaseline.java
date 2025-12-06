package controller;

import model.BaselineCalculator;

/**
 * A class to run the baseline calculator.
 *
 * @author nlevin11
 * @version 11-24
 */
public class RunBaseline {
    /**
     * A string representing the telemetry log filepath.
     */
    private static final String MY_TELEMETRY_LOG_PATH = "dataLogs/TelemetryLog.txt";

    /**
     * A string representing the z-score data filepath.
     */
    private static final String MY_Z_SCORE_LOG_PATH = "dataLogs/BaselineLog.properties";

    /**
     * Main entry point for executing the baseline processing tool.
     * <p>
     * This method constructs a {@link BaselineCalculator} instance and instructs
     * it to load telemetry data from the input log and export computed baseline
     * metrics to the designated output file.
     * </p>
     *
     * @param arg command-line arguments (unused).
     */
    public static void main(String[] arg) {
        BaselineCalculator calc = new BaselineCalculator();

        calc.calculateAndSaveStats(MY_TELEMETRY_LOG_PATH, MY_Z_SCORE_LOG_PATH);
    }
}
