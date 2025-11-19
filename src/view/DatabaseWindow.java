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
 * @version 11/19/2025
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
     * Size of the GUI window.
     */
    private static final Dimension SIZE = new Dimension(1100, 700);

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
        rep.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        rep.setEditable(false);
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
            ENTRIES.remove(rep);
        }
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
        initPanels();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Drone Anomaly Database");
    }

    /**
     * Initializes the RESULTS_PANEL and QUERY_PANEL
     * objects.
     */
    private void initPanels() {
        // Results Panel Setup
        RESULT_PANEL.setLayout(new BoxLayout(RESULT_PANEL, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(RESULT_PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(10);
        add(sp, BorderLayout.CENTER);

        // Query Panel Setup
        QueryTextField idField = new QueryTextField("(e.g. 1, 2, ...)");

        JComboBox<String> typeField = new JComboBox<>();
        typeField.addItem("");
        typeField.addItem("Abnormal Battery Drain Rate");
        typeField.addItem("Battery Failure");
        typeField.addItem("Dangerous Change in Altitude");
        typeField.addItem("GPS Spoofing");
        typeField.addItem("Out of Bounds");

        QueryTextField fromDate = new QueryTextField("MM/DD/YYYY");
        QueryTextField toDate = new QueryTextField("MM/DD/YYYY");
        JButton goButt = new JButton("GO");
        goButt.addActionListener(_ -> {
            String[] arr = {
                    idField.getTextNotDef(),
                    (String) typeField.getSelectedItem(),
                    fromDate.getTextNotDef(),
                    toDate.getTextNotDef()};
            myPCS.firePropertyChange(PROPERTY_DATABASE_QUERY, null, arr);
        });

        QUERY_PANEL.setLayout(new GridLayout(1, 0));
        QUERY_PANEL.add(new FieldPanel("Anomaly Type", typeField));
        QUERY_PANEL.add(new FieldPanel("DroneID", idField));
        QUERY_PANEL.add(new FieldPanel("Begin Date", fromDate));
        QUERY_PANEL.add(new FieldPanel("End Date", toDate));
        QUERY_PANEL.add(goButt);

        add(QUERY_PANEL, BorderLayout.NORTH);
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
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // Focus Listener updates text when focus changes.
            addFocusListener(new FocusAdapter() {
                // Set text empty if the default text is displayed,
                // meaning no input is given yet but the user clicked
                // inside the text field.
                @Override
                public void focusGained(final FocusEvent theE) {
                    if (theDefault.equals(getText())) {
                        setText("");
                    }
                }
                // Set text back to default if user clicked out
                // without entering any text.
                @Override
                public void focusLost(final FocusEvent theE) {
                    if ("".equals(getText())) {
                        setText(theDefault);
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
            JLabel lbl = new JLabel(theLabel, JLabel.LEFT);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            setAlignmentX(LEFT_ALIGNMENT);
            theField.setAlignmentX(LEFT_ALIGNMENT);
            add(lbl);
            add(theField);
        }
    }

}
