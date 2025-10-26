package view;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {

    /** This constant determines the size of the window. */
    private static final Dimension SIZE = new Dimension(930, 530);

    public MapPanel() {
        super();
        initPanel();
    }

    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}
