package tests;

import model.AnomalyDatabase;
import model.AnomalyReport;

import java.util.UUID;

public class AnomalyDatabaseTest {

    public static void main(String[] args) {
        AnomalyDatabase dtbs = new AnomalyDatabase();
        dtbs.initialize();

        AnomalyReport report1 = new AnomalyReport(UUID.randomUUID(), 10L, "Crash", 1, "a", "b");
        AnomalyReport report2 = new AnomalyReport(UUID.randomUUID(), 10L, "Crash", 1, "b", "c");

        dtbs.insertReport(report1);
        dtbs.insertReport(report2);

        System.out.println(dtbs.findAllReports().size());
        System.out.println(dtbs.findAllReports());
    }



}
