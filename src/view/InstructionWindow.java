package view;

import javax.swing.*;
import java.awt.*;

/**
 * This class creates a simple window with text to display the instructions
 * for how to use each aspect of the app.
 *
 * @author Evin Roen
 * @version 11/30/25
 */
class InstructionWindow extends JFrame {

    /**
     * Constructor to initialize window.
     *
     * @param theTitle the title of the window.
     * @param theLines the text of the window where each index is a new line.
     * @param theSize the size of the window.
     */
    InstructionWindow(final String theTitle, final String[] theLines, final Dimension theSize) {
        super();
        setLayout(new GridLayout(1, 1));
        setPreferredSize(theSize);
        getContentPane().setBackground(new Color(0x1E1E1E)); // Dark background
        setVisible(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(theTitle);
        initText(theLines);
        setLocationRelativeTo(null);
    }

    /**
     * Initializes the text area inside the window.
     *
     * @param theLines a String array where each index is a new line.
     */
    private void initText(final String[] theLines) {
        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setBackground(new Color(0x2D2D2D)); // Dark background
        txt.setForeground(new Color(0xE0E0E0)); // Light text
        txt.setCaretColor(new Color(0, 0, 0, 0)); // invisible
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setOpaque(true);

        for (String line : theLines) {
            txt.append(line + "\n");
        }
        add(txt);
        pack();
    }

}
