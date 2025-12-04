package view;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

/**
 * This class controls the window for displaying and querying
 * the anomaly reports database.
 *
 * @author Evin Roen
 * @version 11/30/2025
 */
public class DatabaseWindow extends PropertyEnabledJFrame {

    /**
     * This constant represents the Drone ID's index
     * in the array of data that is input by the user
     * to filter the query results.
     */
    public static final int IDX_DRONE_ID = 0;

    /**
     * Anomaly Type index in the user query array.
     */
    public static final int IDX_ANOMALY = 1;

    /**
     * Begin Date index in the user query array.
     */
    public static final int IDX_FROM_DATE = 2;

    /**
     * End Date index in the user query array.
     */
    public static final int IDX_TO_DATE = 3;

    /**
     * Adjust scroll panel increment (default too slow)
     */
    private static final int SCROLL_INC = 10;

    /**
     * Size of the GUI window.
     */
    private static final Dimension SIZE = new Dimension(1100, 700);

    /**
     * Size of the instructions window.
     */
    private static final Dimension HELP_SIZE = new Dimension(400, 100);

    /**
     * List of database entries for storing and modifying the displayed reports.
     */
    private static final ArrayList<JTextArea> ENTRIES = new ArrayList<>();

    /**
     * JPanel for displaying the results of the query.
     */
    private static final JPanel RESULT_PANEL = new JPanel();

    /**
     * JPanel for displaying the query options.
     */
    private static final JPanel QUERY_PANEL = new JPanel();

    /**
     * Constructor to initialize the database window.
     */
    public DatabaseWindow() {
        initWindow();
    }

    /**
     * Creates and adds a report in the RESULTS_PANEL that
     * contains the given string.
     *
     * @param theReport the report string to display.
     */
    public void addReport(final String theReport) {
        if (theReport == null) {
            throw new IllegalArgumentException("Report cannot be null.");
        }
        JTextArea rep = new JTextArea(theReport);
        rep.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER));
        rep.setEditable(false);
        rep.setBackground(ColorScheme.BACKGROUND_PANEL);
        rep.setForeground(ColorScheme.TEXT_PRIMARY);
        rep.setMaximumSize(new Dimension(RESULT_PANEL.getWidth(), rep.getPreferredSize().height));
        RESULT_PANEL.add(rep);
        ENTRIES.add(rep);
        revalidate();
        repaint();
    }

    /**
     * Clears the ENTRIES list and removes
     * all entries from RESULTS_PANEL.
     */
    public void clearReports() {
        for (JTextArea rep : ENTRIES) {
            RESULT_PANEL.remove(rep);
        }
        ENTRIES.clear();
        revalidate();
        repaint();
    }

    /**
     * Initializes the JFrame window.
     */
    private void initWindow() {
        setPreferredSize(SIZE);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setVisible(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ColorScheme.BACKGROUND_MAIN);
        initMenuBar();
        initPanels();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Drone Anomaly Database");
    }

    /**
     * Initializes the JMenuBar for file saving.
     */
    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(ColorScheme.BACKGROUND_PANEL);
        bar.setForeground(ColorScheme.TEXT_MENU);

        // File
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(ColorScheme.TEXT_MENU);
        JMenuItem saveAs = new JMenuItem("Save current selection as...");
        saveAs.setForeground(ColorScheme.TEXT_MENU);
        saveAs.addActionListener(_ -> myPCS.firePropertyChange(PROPERTY_SAVE_AS, null, null));
        fileMenu.add(saveAs);
        bar.add(fileMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(ColorScheme.TEXT_MENU);
        JMenuItem inst = new JMenuItem("Instructions...");
        inst.setForeground(ColorScheme.TEXT_MENU);
        inst.addActionListener(_ -> openInstructions());
        helpMenu.add(inst);
        bar.add(helpMenu);

        setJMenuBar(bar);
    }

    /**
     * Opens the instruction window.
     */
    private void openInstructions() {
        String[] instructions = {
                "1) Use the filters to specify which reports you would like to see.",
                "2) Click GO to query the database.",
                "3) Go to \"File -> Save current selection...\" to save the results."
        };
        new InstructionWindow("Database Window Instructions", instructions, HELP_SIZE);
    }

    /**
     * Initializes the RESULTS_PANEL and QUERY_PANEL
     * objects.
     */
    private void initPanels() {
        // Results Panel Setup
        RESULT_PANEL.setLayout(new BoxLayout(RESULT_PANEL, BoxLayout.Y_AXIS));
        RESULT_PANEL.setBackground(ColorScheme.BACKGROUND_PANEL);
        JScrollPane sp = new JScrollPane(RESULT_PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(SCROLL_INC);
        sp.setBackground(ColorScheme.BACKGROUND_PANEL);
        sp.getViewport().setBackground(ColorScheme.BACKGROUND_PANEL);
        add(sp, BorderLayout.CENTER);

        // Query Panel Setup
        QueryTextField idField = new QueryTextField("(e.g. 1, 2, ...)");

        // Dropdown with anomaly types
        JComboBox<String> typeField = getTypeField();

        QueryTextField fromDate = new QueryTextField("MM/DD/YYYY");
        QueryTextField toDate = new QueryTextField("MM/DD/YYYY");
        JButton goButt = new JButton("GO");
        goButt.setBackground(ColorScheme.ACCENT_SELECTED);
        goButt.setForeground(ColorScheme.WHITE);
        goButt.setOpaque(true);
        goButt.setBorderPainted(false);
        goButt.addActionListener(_ -> {
            String[] arr = {
                    idField.getTextNotDef(),
                    (String) typeField.getSelectedItem(),
                    fromDate.getTextNotDef(),
                    toDate.getTextNotDef()};
            myPCS.firePropertyChange(PROPERTY_DATABASE_QUERY, null, arr);
        });

        QUERY_PANEL.setLayout(new GridLayout(1, 0));
        QUERY_PANEL.setBackground(ColorScheme.BACKGROUND_PANEL);
        QUERY_PANEL.add(new FieldPanel("Anomaly Type", typeField));
        QUERY_PANEL.add(new FieldPanel("DroneID", idField));
        QUERY_PANEL.add(new FieldPanel("Begin Date", fromDate));
        QUERY_PANEL.add(new FieldPanel("End Date", toDate));
        QUERY_PANEL.add(goButt);

        add(QUERY_PANEL, BorderLayout.NORTH);
    }

    private static JComboBox<String> getTypeField() {
        JComboBox<String> typeField = new JComboBox<>();
        typeField.setBackground(Color.WHITE); // Dark background
        typeField.setForeground(Color.BLACK); // Light text
        typeField.addItem("");
        typeField.addItem("Abnormal Battery Drain Rate");
        typeField.addItem("Battery Failure");
        typeField.addItem("Battery Close to Depletion");
        typeField.addItem("Dangerous Change in Altitude");
        typeField.addItem("GPS Spoofing");
        typeField.addItem("Dangerous Change in Speed");
        typeField.addItem("Out of Bounds");
        typeField.addItem("Abnormal Acceleration/Deceleration");
        typeField.addItem("Ground Collision");
        return typeField;
    }

    /**
     * This class represents a JTextArea with specific settings
     * suited to be used in the RESULTS_PANEL.
     *
     * @author Evin Roen
     * @version 11/19/2025
     */
    private static class QueryTextField extends JTextArea {

        /**
         * Default text to display when user input text is empty.
         */
        private final String myDef;

        /**
         * Constructor to initialize the text area.
         *
         * @param theDefault the default text to display when no input is given.
         * @throws IllegalArgumentException if default text is null.
         */
        private QueryTextField(final String theDefault) {
            super(theDefault);
            if (theDefault == null) {
                throw new IllegalArgumentException("Default text cannot be null.");
            }
            myDef = theDefault;
            setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER));
            setBackground(ColorScheme.BACKGROUND_PANEL);
            setForeground(ColorScheme.TEXT_SECONDARY);
            setCaretColor(ColorScheme.TEXT_PRIMARY);
            setOpaque(true);

            // Focus Listener updates text when focus changes.
            addFocusListener(new FocusAdapter() {
                // Set text empty if the default text is displayed,
                // meaning no input is given yet but the user clicked
                // inside the text field.
                @Override
                public void focusGained(final FocusEvent theE) {
                    if (theDefault.equals(getText())) {
                        setText("");
                        setForeground(ColorScheme.TEXT_PRIMARY);
                    }
                }
                // Set text back to default if user clicked out
                // without entering any text.
                @Override
                public void focusLost(final FocusEvent theE) {
                    if ("".equals(getText())) {
                        setText(theDefault);
                        setForeground(ColorScheme.TEXT_SECONDARY);
                    } else {
                        // Keep light text if user entered something
                        setForeground(ColorScheme.TEXT_PRIMARY);
                    }
                }
            });
        }

        /**
         * Gets the input text from the field without including the
         * default text. If default text is showing in the GUI, then
         * an empty string is returned.
         *
         * @return The input text if it exists, otherwise empty string ("").
         */
        public String getTextNotDef() {
            return myDef.equals(getText()) ? "" : getText();
        }
    }

    /**
     * This class represents one slot for a querying option field. It
     * is a combo of a JLabel and another component, for example a JTextField
     * or JComboBox.
     *
     * @author Evin Roen
     * @version 11/19/2025
     */
    private static class FieldPanel extends JPanel {

        /**
         * This constant is the hgap and vgap for the grid layout.
         */
        private static final int GAP = 5;

        /**
         * Constructor to initialize the FieldPanel.
         *
         * @param theLabel the label for the query field.
         * @param theField the component containing the input for the query field.
         */
        private FieldPanel(final String theLabel, final JComponent theField) {
            super();
            if (theLabel == null || theField == null) {
                throw new IllegalArgumentException("Label nor field component may be null.");
            }
            setLayout(new GridLayout(0, 1, GAP, GAP));
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            setBackground(ColorScheme.BACKGROUND_PANEL);
            JLabel lbl = new JLabel(theLabel, JLabel.LEFT);
            lbl.setForeground(ColorScheme.TEXT_PRIMARY);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            setAlignmentX(LEFT_ALIGNMENT);
            theField.setAlignmentX(LEFT_ALIGNMENT);
            add(lbl);
            add(theField);
        }
    }

}
