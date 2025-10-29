package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter implements ReportExporter {
    // May rename to fileExporter for future applications for various extra credit opportunities.
    // Anomaly export to pdf etc.

    // Perhaps a second method for later extra credit options requiring data gathering
    // such as pattern recognition and machine learning applications.

    @Override
    public void export(List<AnomalyReport> reports, String filePath) {

        String header = "id, timestamp, anomalyType, droneID, simpleReport, detailedReport";

        try (FileWriter fw = new FileWriter(filePath);
        BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(header);
            bw.newLine();

            for (AnomalyReport report : reports){
                String line = String.join(",",
                        report.id().toString(),
                        report.timestamp().toString(),
                        report.anomalyType(),
                        String.valueOf(report.droneId()),
                        report.simpleReport(),
                        report.detailedReport());

                bw.write(line);
                bw.newLine();
            }

            System.out.println("Successfully exported " + reports.size() + " reports to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
}
