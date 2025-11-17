package tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test all database file output types.
 * @author nlevin11
 * @version 11-16
 */
public class ReportExporterTest {
    @TempDir
    Path tempDir;

    private List<AnomalyReport> testReports;
    private AnomalyReport report1;
    private AnomalyReport report2;
    private UUID id1;
    private UUID id2;
    private long ts1;
    private long ts2;
    private String aEnum1;
    private String aEnum2;

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        ts1 = System.currentTimeMillis();
        ts2 = ts1 - 5000;
        aEnum1 = AnomalyEnum.ALTITUDE.toString();
        aEnum2 = AnomalyEnum.BATTERY_DRAIN.toString();

        report1 = new AnomalyReport(id1, ts1, aEnum1, 1, "Simple Report 1",
                "Detailed Report 1");
        report2 = new AnomalyReport(id2, ts2, aEnum2, 2, "Simple Report 2",
                "Detailed Report 2");

        testReports = List.of(report1, report2);
    }

    @Test
    void testExportCsv() throws IOException {
        ReportExporter exporter = new CsvExporter();

        Path outputFile = tempDir.resolve("test-export.csv");

        exporter.export(testReports, outputFile.toString());

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
    void testExportCsvEmpty() throws IOException {
        ReportExporter exporter = new CsvExporter();
        List<AnomalyReport> emptyReports = List.of();
        Path outputFile = tempDir.resolve("empty-export.csv");

        exporter.export(emptyReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        assertEquals(1, lines.size());
        assertEquals("id, timestamp, anomalyType, droneID, simpleReport, detailedReport", lines.getFirst());
    }

    @Test
    void testExportPDF() throws IOException {
        ReportExporter exporter = new PdfExporter();
        Path outputFile = tempDir.resolve("test-export.pdf");

        exporter.export(testReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));

        String pdfText;
        try (PDDocument doc = Loader.loadPDF(outputFile.toFile())) {
            PDFTextStripper strip = new PDFTextStripper();
            pdfText = strip.getText(doc);
        }

        assertTrue(pdfText.contains("Anomaly Report Export"));

        assertTrue(pdfText.contains("Anomaly ID: " + id1.toString()));
        assertTrue(pdfText.contains("Timestamp: " + ts1));
        assertTrue(pdfText.contains("Type: " + aEnum1));
        assertTrue(pdfText.contains("Simple Report: Simple Report 1"));

        assertTrue(pdfText.contains("Anomaly ID: " + id2.toString()));
        assertTrue(pdfText.contains("Timestamp: " + ts2));
        assertTrue(pdfText.contains("Type: " + aEnum2));
        assertTrue(pdfText.contains("Detailed Report: Detailed Report 2"));
    }

    @Test
    void testExportPDFEmpty() throws IOException {
        ReportExporter exporter = new PdfExporter();
        List<AnomalyReport> emptyReports = List.of();
        Path outputFile = tempDir.resolve("empty-export.pdf");

        exporter.export(emptyReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));

        String pdfText;
        try (PDDocument doc = Loader.loadPDF(outputFile.toFile())) {
            PDFTextStripper strip = new PDFTextStripper();
            pdfText = strip.getText(doc);
        }

        assertTrue(pdfText.contains("Anomaly Report Export"));
        assertFalse(pdfText.contains("Anomaly ID:"));
        assertFalse(pdfText.contains("Timestamp:"));
    }

    @Test
    void testExportJson() throws IOException {
        ReportExporter exporter = new JsonExporter();
        Path outputFile = tempDir.resolve("empty-export.json");

        exporter.export(testReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        String jsonContent = Files.readString(outputFile);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<AnomalyReport>>() {}.getType();
        List<AnomalyReport> parsedReports = gson.fromJson(jsonContent, listType);

        assertEquals(2, parsedReports.size());
        assertEquals(testReports, parsedReports);
    }

    @Test
    void testExportJsonEmpty() throws IOException {
        ReportExporter exporter = new JsonExporter();
        List<AnomalyReport> emptyReports = List.of();
        Path outputFile = tempDir.resolve("empty-export.json");

        exporter.export(emptyReports, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        String jsonContent = Files.readString(outputFile);

        assertEquals("[]", jsonContent);
    }
}
