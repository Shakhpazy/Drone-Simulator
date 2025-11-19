package view;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This class handles the definitions for anything related to
 * Property Change Events and handlers for the MonitorDashboard class.
 * \n
 * The purpose of having this second class is to prevent bloating the
 * main functionality of the MonitorDashboard class.
 *
 * @author Evin Roen
 * @version 11/29/2025
 */
class PropertyEnabledJFrame extends JFrame implements PropertyChangeListener {

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
     * Property change event for debug tick speed change.
     */
    public static final String PROPERTY_TICK_SPEED = "tick speed change";

    /**
     * Property change event for opening the database window.
     */
    public static final String PROPERTY_DATABASE_OPENED = "database window opened";

    /**
     * Property change event for new database query.
     */
    public static final String PROPERTY_DATABASE_QUERY = "database queried";

    /**
     * Property change supper object to fire change events.
     */
    protected final PropertyChangeSupport myPCS;

    /**
     * Constructor to initialize instance.
     */
    PropertyEnabledJFrame() {
        super();
        myPCS = new PropertyChangeSupport(this);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener theListener) {
        if (theListener == null) {
            throw new IllegalArgumentException("Listener object must not be null.");
        }
        myPCS.addPropertyChangeListener(theListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent theEvent) {

    }
}
