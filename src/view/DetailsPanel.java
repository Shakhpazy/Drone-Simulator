package view;

import javax.swing.*;
import java.awt.*;

public class DetailsPanel extends JPanel {

    /** This constant determines the size of the panel. */
    private static final Dimension SIZE = new Dimension(170, 170);

    public DetailsPanel() {
        super();
        initPanel();
    }

    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}
