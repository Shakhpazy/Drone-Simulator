package tests;

import model.BaselineCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class to test the Z-Score baseline calculator for AnomalyDetector.
 * @author nlevin11
 * @version 11-16
 */
public class BaselineCalculatorTest {
    @TempDir
    Path tempDir;

    /** A calculator for use in testing **/
    private BaselineCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new BaselineCalculator();
    }

    /**
     * A helper method for testing the calculator.
     * @param fileName          The filename to create a test log file.
     * @param content           The content of the test log file.
     * @return                  Returns a Path object for testing the calculator.
     * @throws IOException      Throws exception if not properly formatted.
     */
    private Path createTestLog(String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }

    /**
     * A helper method for testing the calculator.
     * @param propertiesFile    The Path object file for testing the calculator.
     * @return                  Returns a Properties object for use in testing.
     * @throws IOException      Throws exception if not properly formatted.
     */
    private Properties loadProperties(Path propertiesFile) throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(propertiesFile.toFile())) {
            props.load(reader);
        }
        return props;
    }

    @Test
    void testCalculateAndSaveStats_Success() throws IOException {
        String logContent = """
                id,timestamp,velocity,batteryLevel,orientation
                1,100,10.0,100.0,90
                2,101,5.0,80.0,0
                1,102,20.0,98.0,110,
                2,103,7.0,79.0,350
                """;
        Path logFile = createTestLog("test.csv", logContent);
        Path propsFile = tempDir.resolve("output.properties");

        calc.calculateAndSaveStats(logFile.toString(), propsFile.toString());

        // --- Verify calculations ---
        // velocityReadings: [10.0, 5.0, 20.0, 7.0]
        //   Mean: (10+5+20+7)/4 = 10.5
        //   StdDev: 6.55...
        // batteryDrainReadings: [ (100-98)=2.0, (80-79)=1.0 ]
        //   Mean: (2+1)/2 = 1.5
        //   StdDev: 0.707...
        // orientationDeltaReadings: [ (110-90)=20.0, (0->350, diff=10)=10.0 ]
        //   Mean: (20+10)/2 = 15.0
        //   StdDev: 7.071...

        assertTrue(Files.exists(propsFile));
        Properties props = loadProperties(propsFile);

        assertEquals(10.5, Double.parseDouble(props.getProperty("velocity.mean")), 0.001);
        assertEquals(6.658,  Double.parseDouble(props.getProperty("velocity.standardDev")), 0.001);

        assertEquals(1.5, Double.parseDouble(props.getProperty("batteryDrain.mean")), 0.001);
        assertEquals(0.707, Double.parseDouble(props.getProperty("batteryDrain.standardDev")), 0.001);

        assertEquals(15.0, Double.parseDouble(props.getProperty("orientationDelta.mean")), 0.001);
        assertEquals(7.071, Double.parseDouble(props.getProperty("orientationDelta.standardDev")), 0.001);
    }

    @Test
    void testCalculateAndSaveStats_OrientationWrapAround() throws IOException {
        String logContent = """
                id,timestamp,velocity,batteryLevel,orientation
                1,100,10.0,100.0,350
                1,102,10.0,98.0,10
                """;

        Path logFile = createTestLog("wrap.csv", logContent);
        Path propsFile = tempDir.resolve("output.properties");

        calc.calculateAndSaveStats(logFile.toString(), propsFile.toString());

        // orientationDeltaReadings: [ (350->10, diff=20.0) ]
        //   Mean: 20.0
        //   StdDev: 0.0 (since n < 2)

        assertTrue(Files.exists(propsFile));
        Properties props = loadProperties(propsFile);

        assertEquals(20.0, Double.parseDouble(props.getProperty("orientationDelta.mean")), 0.001);
        assertEquals(0.0, Double.parseDouble(props.getProperty("orientationDelta.standardDev")), 0.001);
    }

    @Test
    void testProcessLogFile_EmptyFile() throws IOException {
        Path logFile = createTestLog("empty.csv", "");
        Path propsFile = tempDir.resolve("output.properties");

        assertThrows(IOException.class, () ->
                calc.processLogFile(logFile.toString()));
    }

    @Test
    void testProcessLogFile_MissingHeaders() throws IOException {
        String logContent = "id,timestamp,vel,batt,orient";
        Path logFile = createTestLog("bad_header.csv", logContent);

        assertThrows(IOException.class, () ->
                calc.processLogFile(logFile.toString()));
    }

    @Test
    void testProcessLogFile_MalformedAndSkippedLines() throws IOException {
        String logContent = """
                id,timestamp,velocity,batteryLevel,orientation
                1,100,10.0,100.0,90
                not,a,number,line,
                1,102,20.0,98.0,110
                2,103,7.0,79.0,350
                just,one,column
                """;

        Path logFile = createTestLog("malformed.csv", logContent);
        Path propsFile = tempDir.resolve("output.properties");

        calc.calculateAndSaveStats(logFile.toString(), propsFile.toString());

        assertTrue(Files.exists(propsFile));
        Properties props = loadProperties(propsFile);

        assertEquals(12.333, Double.parseDouble(props.getProperty("velocity.mean")), 0.001);
        assertEquals(2.0, Double.parseDouble(props.getProperty("batteryDrain.mean")), 0.001);
        assertEquals(0.0, Double.parseDouble(props.getProperty("batteryDrain.standardDev")), 0.001);
        assertEquals(20.0, Double.parseDouble(props.getProperty("orientationDelta.mean")), 0.001);
    }
}
