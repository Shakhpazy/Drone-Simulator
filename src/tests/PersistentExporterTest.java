package tests;

import model.PersistentExporter;
import model.TelemetryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistentExporterTest {

    @TempDir
    Path tempDir;

    private PersistentExporter exporter;
    private List<String> testHeader;
    private TelemetryRecord testData1;
    private TelemetryRecord testData2;

    @BeforeEach
    void setUp() {
        exporter = new PersistentExporter();

        testHeader = List.of("id", "timestamp", "velocity", "altitude");

        testData1 = new TelemetryRecord(1, 0, 0, 0, 10.5F, 100,
                270, 123456789L);

        testData2 = new TelemetryRecord(2, 0, 0, 0, 9.8F, 100,
                90, 123456999L);
    }

    @Test
    void testFull() throws IOException {
        Path outputFile = tempDir.resolve("test-log.csv");

        exporter.startTelemetryLog(outputFile.toString(), testHeader);
        exporter.logTelemetryData(testData1, testHeader);
        exporter.logTelemetryData(testData2, testHeader);
        exporter.closeTelemetryLog();

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        assertEquals(3, lines.size());

        assertEquals("id,timestamp,velocity,altitude", lines.getFirst());
        assertEquals("1,123456789,10.5,300", lines.get(1));
        assertEquals("2,123456999,9.8,310", lines.get(2));
    }

    @Test
    void testNoData() throws IOException {
        Path outputFile = tempDir.resolve("empty-log.csv");
        List<String> header = List.of("id", "timestamp");

        exporter.startTelemetryLog(outputFile.toString(), header);
        exporter.closeTelemetryLog();

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);

        assertEquals(1, lines.size());
        assertEquals("id,timestamp", lines.getFirst());
    }

    @Test
    void testNoStart() {
        assertDoesNotThrow(() -> {
            exporter.logTelemetryData(testData1, testHeader);
        });
    }
}
