/** G2iCalGUI.java
 * Main class for the G2iCal application.
 * This class sets up the look and feel and initializes the application.
 * It checks if the user is authenticated with the Google Calendar API and starts the appropriate controller.
 */

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import utils.CalendarApiConnector;

public class G2iCalGUI {
    /**
     * Main method to start the G2iCal application.
     * It sets the look and feel and initializes the controller based on authentication status.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac"))
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            FlatMacLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf: " + e.getMessage());
        }
        // Create and show the GUI
        if (!CalendarApiConnector.isAuthenticated()) {
            ui.start.Controller.getInstance();
        } else  {
            new ui.main.Controller();
        }
    }
}