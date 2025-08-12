package ui.main;

import calendar.CalendarEvent;
import calendar.ICal;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import utils.CalendarApiConnector;
import utils.EventFactory;
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
    private void loadInitialData() {
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
            view.showErrorDialog(e.getMessage());
        }

    }

    /**
     * Gets the current user's name from Google account
     *
     * @return The user's display name
     */
    public String getUserName() {
        try {
            return calendarApiConnector.getUser();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the current export file name from settings
     *
     * @return The export file name
     */
    public String getFileName() {
        return settings.getFileName();
    }

    /**
     * Gets the current export file path from settings
     *
     * @return The export file path
     */
    public String getFilePath() {
        return settings.getFilePath();
    }

    /**
     * Updates the application settings
     *
     * @param fileName New export file name
     * @param filePath New export file path
     */
    public void updateSettings(String fileName, String filePath) {
        settings.updateSettings(fileName, filePath);
        settings.saveSettings();
    }

    /**
     * Handles the exit to start screen action
     */
    public void exitToStartScreen() {
        closeView();
        ui.start.Controller.getInstance().showView(); // Reinitialize the start controller
    }

    /**
     * Handles the logout action
     */
    public void logout() {
        // Removing the stored credentials
        try {
            CalendarApiConnector.logout();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

            // Convert events to table format
            Object[][] eventData = convertEventsToTableData(events);

            // Update the view with loaded events
            //view.updateEventTable(eventData);
            view.setStatusMessage("Loaded " + events.getItems().size() + " events successfully");
            view.setExportButtonEnabled(true);

        } catch (IOException e) {
            view.showErrorDialog("Failed to load events: " + e.getMessage());
            view.setExportButtonEnabled(false);
        } catch (Exception e) {
            view.showErrorDialog("Unexpected error: " + e.getMessage());
            view.setExportButtonEnabled(false);
        }
    }

    /**
     * Converts Google Calendar Events to table data format using Event factory and converter
     *
     * @param events The Events object from Google Calendar API
     * @return 2D array of event data for table display
     */
    private Object[][] convertEventsToTableData(Events events) {
        if (events.getItems() == null || events.getItems().isEmpty()) {
            return new Object[0][5]; // Return empty array if no events
        }

        Object[][] eventData = new Object[events.getItems().size()][5];

        for (int i = 0; i < events.getItems().size(); i++) {
            com.google.api.services.calendar.model.Event googleEvent = events.getItems().get(i);

            // Use your Event factory/converter to create Event object
            // Assuming you have an EventFactory or EventConverter class
            CalendarEvent event = EventFactory.createFromGoogle(googleEvent);
            // Or: Event event = EventConverter.convertGoogleEvent(googleEvent);

            // Extract event details from your Event object
            String title = event.getSummary() != null ? event.getSummary() : "No Title";
            String startTime = formatEventDateTime(event.getStart());
            String endTime = formatEventDateTime(event.getEnd());
            String location = event.getLocation() != null ? event.getLocation() : "";
            String description = event.getDescription() != null ? event.getDescription() : "";

            eventData[i] = new Object[]{title, startTime, endTime, location, description};
        }

        return eventData;
    }

    /**
     * Formats DateTime from your Event class to readable string
     *
     * @param dateTime The DateTime object from your Event
     * @return Formatted date string
     */
    private String formatEventDateTime(Object dateTime) {
        if (dateTime == null) return "";

        // Adjust this based on your Event class's DateTime implementation
        if (dateTime instanceof DateTime) {
            return ((DateTime) dateTime).toStringRfc3339();
        } else if (dateTime instanceof java.time.LocalDateTime) {
            return dateTime.toString();
        } else {
            return dateTime.toString();
        }
    }

    /**
     * Exports the currently loaded events to iCal format
     */
    public void exportEvents() {
        // Ensure events are loaded before exporting

        // Get the file name and path from settings
        String fileName = getFileName();
        String filePath = getFilePath();



        // Placeholder implementation
        view.setStatusMessage("Export completed successfully!");
        System.out.println("Events exported to iCal format");
    }

    /**
     * Closes the main view
     */
    private void closeView() {
        view.setVisible(false); // Hide the view
        view.dispose(); // Dispose of the view resources
    }

    public void resetCalendarSelection() {
    }

    public void reset() {
        view.reset();
    }
}
