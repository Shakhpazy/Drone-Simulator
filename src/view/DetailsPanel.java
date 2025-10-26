package view;

import model.AnomalyReport;

import javax.swing.*;
import java.awt.*;

public class DetailsPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(170, 170);

    /**
     * This field stores and displays the text for the report.
     */
    private JTextArea myTextArea;

    public DetailsPanel() {
        super();
        initPanel();
        initTextArea();
    }

    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridLayout(1, 1));
    }

    private void initTextArea() {
        myTextArea = new JTextArea("...");
        myTextArea.setEditable(false);
        myTextArea.setLineWrap(true);
        myTextArea.setWrapStyleWord(true);
        myTextArea.setCaretColor(new Color(0, 0, 0, 0));
        add(myTextArea);
    }

    public void setReport(AnomalyReport theReport) {
        myTextArea.setText(theReport.detailedReport());
    }
}
