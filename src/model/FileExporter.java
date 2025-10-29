package model;

import java.util.List;

public class FileExporter {

    /**
     * Exports all reports using the provided file format.
     * @param db            The database class of methods for interacting with the database.
     * @param filePath      The string representing the user's file path for the output file.
     * @param exporter      The file format to use (e.g., CsvExporter, JsonExporter, PdfExporter).
     */
    public void exportAllReports(AnomalyDatabase db, String filePath, ReportExporter exporter){
        List<AnomalyReport> reports = db.findAllReports();

        // Delegate export to proper file format.
        exporter.export(reports, filePath);
    }
}
