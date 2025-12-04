package view;

import java.awt.Color;

/**
 * Centralized color scheme for the dark theme UI.
 * All colors used throughout the application should be defined here
 * for easy maintenance and theme changes.
 *
 * @author Team
 * @version 1.0
 */
public final class ColorScheme {

    // Private constructor to prevent instantiation
    private ColorScheme() {
        throw new AssertionError("ColorScheme should not be instantiated.");
    }

    // Main Background Colors
    /** Main window background color */
    public static final Color BACKGROUND_MAIN = new Color(0x1E1E1E);
    
    /** Panel background color */
    public static final Color BACKGROUND_PANEL = new Color(0x2D2D2D);
    
    /** Secondary panel background (slightly lighter) */
    public static final Color BACKGROUND_SECONDARY = new Color(0x3C3C3C);

    // Text Colors
    /** Primary text color (light gray, readable on dark) */
    public static final Color TEXT_PRIMARY = new Color(0xE0E0E0);
    
    /** Secondary text color (medium gray for less important text) */
    public static final Color TEXT_SECONDARY = new Color(0xB0B0B0);
    
    /** Text color for menu bars (dark, readable on white/light backgrounds) */
    public static final Color TEXT_MENU = new Color(0x1E1E1E);

    // Border Colors
    /** Standard border color */
    public static final Color BORDER = new Color(0x4A4A4A);
    
    /** Very dark border color */
    public static final Color BORDER_DARK = new Color(0x1A1A1A);

    // Accent Colors
    /** Selected/Active color (muted green) */
    public static final Color ACCENT_SELECTED = new Color(0x4CAF50);
    
    /** Selected highlight color (lighter green) */
    public static final Color ACCENT_SELECTED_LIGHT = new Color(0x66BB6A);
    
    /** Drone color on map (bright red) */
    public static final Color DRONE = new Color(0xFF5252);
    
    /** Selected drone color on map (green) */
    public static final Color DRONE_SELECTED = new Color(0x4CAF50);
    
    /** Grid lines color */
    public static final Color GRID_LINE = new Color(0x404040);

    // Special Colors
    /** White color for selected text */
    public static final Color WHITE = Color.WHITE;
    
    /** Invisible caret color */
    public static final Color CARET_INVISIBLE = new Color(0, 0, 0, 0);
}
