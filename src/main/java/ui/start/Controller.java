/**
 * Controller.java
 * This class handles the logic for the start screen of the application.
 * It manages the view and the login process with the Calendar API.
 */

package ui.start;

import utils.CalendarApiConnector;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Controller {
    private final View view; // The view for the start screen
    private static Controller instance;

    /**
     * Constructor for the Controller class.
     * Initializes the view and sets up the login process.
     */
    private Controller() {
        instance = this;
        // Initialize the StartView
        this.view = new View(this);
    }

    /**
     * Returns the singleton instance of the Controller.
     * This method is used to access the controller from other parts of the application.
     *
     * @return The singleton instance of the Controller.
     */
    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
            return instance;
        }
        return instance;
    }

    /**
     * Starts the login process by showing the login prompt and initiating the OAuth flow.
     */
    public void startLoginProcess() {
        try {
            view.showLoginPrompt(CalendarApiConnector.getLoginUrl());

            // Start OAuth flow in a separate thread after a small delay to allow UI to render
            SwingUtilities.invokeLater(() -> new Thread(() -> {
                try {
                    // Small delay to ensure the login dialog is fully rendered
                    Thread.sleep(500);

                    CalendarApiConnector.getInstance();
                    // This is called when the OAuth flow completes successfully
                    SwingUtilities.invokeLater(() -> {
                        hideView();
                        new ui.main.Controller();
                    });
                } catch (IOException | GeneralSecurityException e) {
                    SwingUtilities.invokeLater(() -> view.showErrorDialog("Error during login process: " + e.getMessage()));
                    System.err.println("Error during login process: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Login process was interrupted: " + e.getMessage());
                }
            }).start());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the cache of the CalendarApiConnector.
     * This method is called when the user wants to clear the cached credentials.
     */
    public void clearCache() {
        // Clear the cache in the CalendarApiConnector
        try {
            CalendarApiConnector.clearCredentials();
            view.showClearCacheSuccess();
        } catch (IOException e) {
            view.showErrorDialog("Failed to clear cache: " + e.getMessage());
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Hide the start view. The view will not be visible anymore, but can be shown again later.
     * This method is useful for transitioning to another view or when the application is ready to proceed
     * after the login process.
     */
    private void hideView() {
        // Remove the dialog if it exists
        JDialog loginDialog = view.getLoginDialog();
        if (loginDialog !=null){
            loginDialog.dispose();
        }
        // Hide the start view by setting it invisible
        view.setVisible(false);
    }

    /**
     * Show view after hiding it.
     * This method is useful for showing the view again after it has been hidden,
     * for example, after a successful logout or when the user wants to return to the start
     */
    public void showView() {
        view.setVisible(true);
    }
}