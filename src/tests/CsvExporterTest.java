package tests;

import model.AnomalyEnum;
import model.AnomalyReport;
import model.CsvExporter;
import model.ReportExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvExporterTest {
    @TempDir
    Path tempDir;

    @Test
    void testExport() throws IOException {
        ReportExporter exporter = new CsvExporter();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        long ts1 = System.currentTimeMillis();
        long ts2 = ts1 - 5000;
        String aEnum1 = AnomalyEnum.ALTITUDE.toString();
        String aEnum2 = AnomalyEnum.BATTERY_DRAIN.toString();

        AnomalyReport report1 = new AnomalyReport(id1, ts1, aEnum1, 1, "Simple Report",
                "Detailed Report");
        AnomalyReport report2 = new AnomalyReport(id2, ts2, aEnum2, 2, "Simple Report",
                "Detailed Report");

        List<AnomalyReport> reports = List.of(report1, report2);
        Path outputFile = tempDir.resolve("test-export.csv");

        exporter.export(reports, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        assertEquals(3, lines.size());
        assertEquals("id, timestamp, anomalyType, droneID, simpleReport, detailedReport", lines.getFirst());

        String expectedLine1 = String.join(",",
                id1.toString(), String.valueOf(ts1), aEnum1, String.valueOf(1),
                "Simple Report", "Detailed Report");
        String expectedLine2 = String.join(",",
                id2.toString(), String.valueOf(ts2), aEnum2, String.valueOf(2),
                "Simple Report", "Detailed Report");

        assertEquals(expectedLine1, lines.get(1));
        assertEquals(expectedLine2, lines.get(2));
    }

    @Test
    void testExportEmpty() throws IOException {
        ReportExporter exporter = new CsvExporter();
        List<AnomalyReport> emptyReports = List.of();
        Path outputFile = tempDir.resolve("empty-export.csv");

        exporter.export(emptyReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        assertEquals(1, lines.size());
        assertEquals("id, timestamp, anomalyType, droneID, simpleReport, detailedReport", lines.getFirst());
    }
}
