package view;

import javax.swing.*;
import java.beans.PropertyChangeSupport;

/**
 * This class handles the definitions for anything related to
 * Property Change Events and handlers for the MonitorDashboard class.
 * \n
 * The purpose of having this second class is to prevent bloating the
 * main functionality of the MonitorDashboard class.
 */
class PropertyEnabledDashboard extends JFrame {

    /**
     * Property change event name for saving as CSV.
     */
    public static final String PROPERTY_SAVE_CSV = "save as csv";

    /**
     * Property change event name for saving as PDF.
     */
    public static final String PROPERTY_SAVE_PDF = "save as pdf";

    /**
     * Property change event for saving as JSON.
     */
    public static final String PROPERTY_SAVE_JSON = "save as json";

    /**
     * Property change supper object to fire change events.
     */
    protected final PropertyChangeSupport myPCS;

    /**
     * Constructor to initialize instance.
     */
    PropertyEnabledDashboard() {
        super();
        myPCS = new PropertyChangeSupport(this);
    }
}
