package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class is the main window / dashboard for the autonomous drone monitoring
 * system.
 */
public class MonitorDashboard extends JFrame {

    /** This constant determines the size of the window. */
    private static final Dimension mySize = new Dimension(1100, 700);

    /** Constructor to initialize the window. */
    public MonitorDashboard() {
        super();
        initWindow();
        initMenuBar();
    }

    /** Boilerplate JFrame setup. */
    private void initWindow() {
        setSize(mySize);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    /**
     * Sets up the menu bar with File, Help, and Data tabs.
     */
    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();

        // File
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveCSV = new JMenuItem("Save as .csv");
        saveCSV.addActionListener(theEvent -> saveAsCSV());
        fileMenu.add(saveCSV);
        JMenuItem savePDF = new JMenuItem("Save as .pdf");
        savePDF.addActionListener(theEvent -> saveAsPDF());
        fileMenu.add(savePDF);
        bar.add(fileMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        JMenuItem inst = new JMenuItem("Instructions...");
        inst.addActionListener(theEvent -> openInstructions());
        helpMenu.add(inst);
        bar.add(helpMenu);

        // Data
        JMenu dataMenu = new JMenu("Data");
        JMenuItem openDB = new JMenuItem("Open Database...");
        openDB.addActionListener(theEvent -> openDatabase());
        dataMenu.add(openDB);
        bar.add(dataMenu);

        setJMenuBar(bar);
        bar.setVisible(true);
    }

    private void saveAsCSV() {
        System.out.println("eventually, this will save as .csv");
    }

    private void saveAsPDF() {
        System.out.println("eventually, this will save as .pdf");
    }

    private void openInstructions() {
        System.out.println("eventually, this will open instructions");
    }

    private void openDatabase() {
        System.out.println("eventually, this will open database");
    }

}
