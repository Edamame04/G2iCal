/**
 * controller.java
 * This class serves as the controller for the main application screen.
 * It handles user interactions, manages the view, and communicates with the Google Calendar API.
 * Many methods in this class are package-private to only allow access from the View class.
 */
package ui.main;

import calendar.ICal;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import exceptions.ICalExportException;
import utils.CalendarApiConnector;
import utils.InputValidator;
import utils.Settings;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Controller {
    private final View view; // The view for the main screen
    private final CalendarApiConnector calendarApiConnector;
    private final Settings settings; // Settings instance
    private ICal iCal;

    /**
     * Constructor for the Controller class.
     * Initializes the view and sets up the main application.
     */
    public Controller() {
        // Get the CalendarApiConnector first
        try {
            this.calendarApiConnector = CalendarApiConnector.getInstance();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        // Initialize settings
        this.settings = Settings.getInstance();

        // Initialize the View after calendarApiConnector is ready
        this.view = new View(this);

        loadInitialData();
    }

    /**
     * Loads initial data when the application starts
     */
    void loadInitialData() {
        try {
            List<CalendarListEntry> calendars = calendarApiConnector.getUserCalendars();
            String[] calenderIds = new String[calendars.size()];
            String[] calenderNames = new String[calendars.size()];
            for (int i = 0; i < calendars.size(); i++) {
                calenderNames[i] = calendars.get(i).getSummary();
                calenderIds[i] = calendars.get(i).getId();
            }
            view.updateCalendarList(calenderIds, calenderNames);
        } catch (IOException e) {
            view.showErrorDialog("There was an error loading calendars: " + e.getMessage());
        }

    }

    /**
     * Gets the current user's name from Google account
     *
     * @return The user's display name
     */
    String getUserName() {
        try {
            return calendarApiConnector.getUser();
        } catch (IOException e) {
            view.showErrorDialog("There was an error loading the User: " + e.getMessage());
        }
        return "Unknown User";
    }

    /**
     * Gets the current export file name from settings
     *
     * @return The export file name
     */
    String getFileName() {
        return settings.getFileName();
    }

    /**
     * Gets the current export file path from settings
     *
     * @return The export file path
     */
    String getFilePath() {
        return settings.getFilePath();
    }

    /**
     * Updates the application settings
     *
     * @param fileName New export file name
     * @param filePath New export file path
     */
    void updateSettings(String fileName, String filePath) {
        settings.updateSettings(fileName, filePath);
    }

    /**
     * This method closes the current view and reinitializes the start controller.
     * It does not log out the user from Google Calendar.
     */
    void exitToStartScreen() {
        closeView();
        ui.start.Controller.getInstance().showView(); // Reinitialize the start controller
    }

    /**
     * Handles the logout action with the Google Calendar API.
     */
    public void logout() {
        // Removing the stored credentials
        try {
            CalendarApiConnector.logout();
        } catch (IOException e) {
            view.showErrorDialog("There was an error logging out: " + e.getMessage());
        }
        exitToStartScreen();
    }

    /**
     * Loads calendar events for the specified date range and calendar
     *
     * @param calendarId The selected calendar ID
     * @param fromDate   Start date (YYYY-MM-DD format)
     * @param toDate     End date (YYYY-MM-DD format)
     */
    public void loadCalendarEvents(String calendarId, String fromDate, String toDate) {
        try {
            // Validate date inputs
            InputValidator.ValidationResult startResult = InputValidator.validateDate(fromDate, "start");
            if (!startResult.isValid()) {
                view.showErrorDialog("Invalid start date: " + startResult.getErrorMessage());
                return;
            }

            InputValidator.ValidationResult endResult = InputValidator.validateDateRange(
                    InputValidator.convertToDateTime(fromDate, false), toDate);
            if (!endResult.isValid()) {
                view.showErrorDialog("Invalid date range: " + endResult.getErrorMessage());
                return;
            }

            // Convert dates to DateTime objects
            DateTime startTime = InputValidator.convertToDateTime(fromDate, false);
            DateTime endTime = InputValidator.convertToDateTime(toDate, true);

            // Fetch events from Google Calendar API
            Events events = calendarApiConnector.getCalendarEventsByCalendarId(calendarId, startTime, endTime);
            iCal = new ICal(events);


            // Update the view with loaded events
            view.updateEventTable(iCal.toTableModel());
            view.setStatusMessage("Loaded " + events.getItems().size() + " events successfully");
            view.setExportButtonEnabled(true);

        } catch (Exception e) {
            view.showErrorDialog("Failed to load events: " + e.getMessage());
            view.setExportButtonEnabled(false);
        }
    }

    /**
     * Exports the currently loaded events to iCal format
     */
    public void exportEvents() {
        // Ensure events are loaded before exporting
        if (iCal != null) {
            // Get the file name and path from settings
            String fileName = getFileName();
            String filePath = getFilePath();

            // Export the iCal data to a file
            try {
                iCal.exportICalToFile(filePath, fileName);
                view.setStatusMessage("Exported events to " + filePath +"/"+ fileName +" successfully!");
            } catch (ICalExportException e) {
                view.showErrorDialog("Failed to export events: " + e.getMessage());
            }
        } else  {
            view.showErrorDialog("No events loaded to export.");
        }
    }

    /**
     * Resets the controller state, clearing the iCal data and resetting the view.
     */
    public void reset() {
        this.iCal = null;
        view.reset();
    }

    /**
     * Closes the main view
     */
    private void closeView() {
        view.setVisible(false); // Hide the view
        view.dispose(); // Dispose of the view resources
    }
}
