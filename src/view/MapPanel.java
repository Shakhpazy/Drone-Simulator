package view;

import javax.swing.*;

public class MapPanel extends JPanel {

    private final int myWidth;
    private final int myHeight;

    public MapPanel(final int theWidth, final int theHeight) {
        myWidth = theWidth;
        myHeight = theHeight;
        setSize(myWidth, myHeight);
    }

}
