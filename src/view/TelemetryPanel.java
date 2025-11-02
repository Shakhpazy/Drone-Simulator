package view;

import javax.swing.*;
import java.awt.*;

public class TelemetryPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(930, 170);

    public TelemetryPanel() {
        super();
        initPanel();
    }

    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }


}
