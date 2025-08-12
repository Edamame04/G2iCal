import utils.CalendarApiConnector;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class G2iCalGUI {
    public static void main(String[] args) {
        // Set look and feel
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac"))
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            FlatMacLightLaf.setup();
        } catch (Exception e) {
            System.out.println("Failed to set FlatLaf: " + e.getMessage());
        }
        // Create and show the GUI
        if (!CalendarApiConnector.isAuthenticated()) {
            ui.start.Controller.getInstance();
        } else  {
            try {
                CalendarApiConnector.getInstance();
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            new ui.main.Controller();
        }
    }
}