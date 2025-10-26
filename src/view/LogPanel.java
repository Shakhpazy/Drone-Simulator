package view;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {

    /** This constant determines the size of the window. */
    private static final Dimension SIZE = new Dimension(170, 530);

    public LogPanel() {
        super();
        initPanel();
    }

    private void initPanel() {
        setPreferredSize(SIZE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}
