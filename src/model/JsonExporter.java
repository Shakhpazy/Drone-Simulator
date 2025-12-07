package model;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * A class to compile AnomalyReports into a JSON file.
 * @author nlevin11
 * @version 11-6
 */
public class JsonExporter implements ReportExporter{
    @Override
    public void export(List<AnomalyReport> reports, String filepath) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(filepath)) {
            gson.toJson(reports, writer);
            System.out.println("Successfully exported " + reports.size() + " reports to " + filepath);
        } catch (IOException e){
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }
}
