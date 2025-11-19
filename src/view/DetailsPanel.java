package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class represents the detailed section underneath the log panel.
 * When a simple log is clicked on, the detailed log is shown here.
 */
class DetailsPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(170, 170);

    /**
     * This field stores and displays the text for the report.
     */
    private JTextArea myTextArea;

    /**
     * Constructor to initialize object.
     */
    DetailsPanel() {
        super();
        initPanel();
        initTextArea();
    }

    /**
     * Initializes the panel settings.
     */
    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridLayout(1, 1));
    }

    /**
     * Initializes the text area settings.
     */
    private void initTextArea() {
        myTextArea = new JTextArea("...");
        myTextArea.setEditable(false);
        myTextArea.setLineWrap(true);
        myTextArea.setWrapStyleWord(true);
        myTextArea.setCaretColor(new Color(0, 0, 0, 0));
        add(new JScrollPane(myTextArea));
    }

    /**
     * Sets the report for which the detailed description shall
     * be displayed.
     *
     * @param theDetailedReport the detailed report to display.
     */
    public void setReport(final String theDetailedReport) {
        if (theDetailedReport == null) {
            throw new IllegalArgumentException("Detailed report must not be null.");
        }
        myTextArea.setText(theDetailedReport);
    }
}
