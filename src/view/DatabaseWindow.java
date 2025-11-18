package view;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

public class DatabaseWindow extends PropertyEnabledJFrame {

    public static final int IDX_DRONE_ID = 0;
    public static final int IDX_ANOMALY = 1;
    public static final int IDX_FROM_DATE = 2;
    public static final int IDX_TO_DATE = 3;

    private static final Dimension SIZE = new Dimension(1100, 700);

    private static final ArrayList<JTextArea> ENTRIES = new ArrayList<>();

    private static final JPanel RESULT_PANEL = new JPanel();

    private static final JPanel QUERY_PANEL = new JPanel();

    public DatabaseWindow() {
        initWindow();
    }

    public void addReport(final String theReport) {
        JTextArea rep = new JTextArea(theReport);
        rep.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        rep.setEditable(false);
        rep.setMaximumSize(new Dimension(RESULT_PANEL.getWidth(), rep.getPreferredSize().height));
        RESULT_PANEL.add(rep);
        ENTRIES.add(rep);
        revalidate();
        repaint();
    }

    public void clearReports() {
        for (JTextArea rep : ENTRIES) {
            RESULT_PANEL.remove(rep);
        }
        revalidate();
        repaint();
    }

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

    private void initPanels() {
        RESULT_PANEL.setLayout(new BoxLayout(RESULT_PANEL, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(RESULT_PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(10);
        add(sp, BorderLayout.CENTER);

        QueryTextField idField = new QueryTextField("(e.g. 1, 2, ...)");

        JComboBox<String> typeField = new JComboBox<>();
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

    private static class QueryTextField extends JTextArea {

        private final String myDef;

        private QueryTextField(final String theDefault) {
            super(theDefault);
            myDef = theDefault;
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent theE) {
                    if (theDefault.equals(getText())) {
                        setText("");
                    }
                }
                @Override
                public void focusLost(FocusEvent theE) {
                    if ("".equals(getText())) {
                        setText(theDefault);
                    }
                }
            });
        }

        public String getTextNotDef() {
            return myDef.equals(getText()) ? "" : getText();
        }
    }

    private static class FieldPanel extends JPanel {

        private FieldPanel(String theLabel, JComponent theField) {
            super();
            int gap = 5;
            setLayout(new GridLayout(0, 1, gap, gap));
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
