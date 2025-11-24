package controller;

import model.BaselineCalculator;

public class RunBaseline {

    private static final String MY_TELEMETRY_LOG_PATH = "dataLogs/TelemetryLog.txt";

    private static final String MY_Z_SCORE_LOG_PATH = "dataLogs/BaselineLog.properties";

    public static void main(String[] arg) {
        BaselineCalculator calc = new BaselineCalculator();

        calc.calculateAndSaveStats(MY_TELEMETRY_LOG_PATH, MY_Z_SCORE_LOG_PATH);
    }
}
