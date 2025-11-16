package view;

import javax.swing.*;
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
        setLayout(new BorderLayout());
        setVisible(false);
        initPanels();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Drone Anomaly Database");
    }

    private void initPanels() {
        RESULT_PANEL.setLayout(new BoxLayout(RESULT_PANEL, BoxLayout.Y_AXIS));
        add(new JScrollPane(RESULT_PANEL), BorderLayout.CENTER);

        QueryTextField idField = new QueryTextField("Drone ID");
        QueryTextField typeField = new QueryTextField("Anomaly Type");
        QueryTextField fromDate = new QueryTextField("From: (MM/DD/YYYY)");
        QueryTextField toDate = new QueryTextField("To: (MM/DD/YYYY)");
        JButton goButt = new JButton("GO");
        goButt.addActionListener(theEvent -> {
            String[] arr = {
                    idField.getTextNotDef(),
                    typeField.getTextNotDef(),
                    fromDate.getTextNotDef(),
                    toDate.getTextNotDef()};
            myPCS.firePropertyChange(PROPERTY_DATABASE_QUERY, null, arr);
        });

        QUERY_PANEL.add(idField);
        QUERY_PANEL.add(typeField);
        QUERY_PANEL.add(fromDate);
        QUERY_PANEL.add(toDate);
        QUERY_PANEL.add(goButt);
        add(QUERY_PANEL, BorderLayout.SOUTH);
    }

    private static class QueryTextField extends JTextArea {

        private static final Dimension SIZE = new Dimension(200, 50);

        private final String myDef;

        public QueryTextField(final String theDefault) {
            super(theDefault);
            setPreferredSize(SIZE);
            myDef = theDefault;
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

}
