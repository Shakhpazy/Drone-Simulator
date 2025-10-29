package model;

import java.util.List;

public interface ReportExporter {
    /**
     * A method to export a list of reports to the given file path.
     * @param reports       The list of reports to export
     * @param filepath      The destination file path.
     */
    void export(List<AnomalyReport> reports, String filepath);
}
