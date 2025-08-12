/** calendar.ICal.java
 * This class represents an iCalendar (iCal) object that can store multiple events and export them in iCal format.
 * It supports adding events from Google Calendar and exporting to a file. Multiple iCal objects can be created
 * to represent different calendars or event sets.
 */

package calendar;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import exceptions.ICalExportException;
import utils.EventFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The calendar.ICal class provides functionality to store calendar events and export them in iCalendar (iCal) format.
 * It supports adding events from Google Calendar and exporting the iCal data to a file.
 */
public class ICal {
    /**
     * List to hold events in iCal format.
     */
    private List<CalendarEvent> events;

    /**
     * Default constructor: creates an empty iCal object.
     */
    public ICal() {
        this.events = new ArrayList<>();
    }
    
    /**
     * Constructor to initialize with a list of Google Calendar Events.
     * Makes a defensive copy to prevent external modification.
     *
     * @param events Google Calendar Events to initialize the iCal with
     */
    public ICal(Events events) {
        this.events = new ArrayList<>();
        if (events != null && events.getItems() != null) {
            for (Event event : events.getItems()) {
                addGoogleEvent(event); // Convert and add each Google Event
            }
        }
    }

    /**
     * Adds a Google Calendar event to the iCal object by converting it using EventFactory.
     *
     * @param event The Google Calendar Event to add
     */
    public void addGoogleEvent(Event event) {
        CalendarEvent calendarEvent = EventFactory.createFromGoogle(event);
        this.events.add(calendarEvent);
    }

    /**
     * Generates the iCal string representation of all events.
     *
     * @return The iCal formatted string containing all events
     */
    public String getICalString() {
        StringBuilder icalStringBuilder = new StringBuilder();
        icalStringBuilder.append("BEGIN:VCALENDAR\r\n");
        icalStringBuilder.append("VERSION:2.0\r\n");
        icalStringBuilder.append("PRODID:-//My Calendar iCal Exporter//EN\r\n");
        for (CalendarEvent event : events) {
            icalStringBuilder.append(event.toICal());
        }
        icalStringBuilder.append("END:VCALENDAR\r\n");

        return foldLines(icalStringBuilder.toString()); // Apply line folding
    }

    /**
     * Folds long lines according to iCal specification (75-character limit).
     *
     * @param icalString The raw iCal string
     * @return The iCal string with properly folded lines
     */
    private String foldLines(String icalString) {
        StringBuilder result = new StringBuilder();
        String[] lines = icalString.split("\r\n");

        for (String line : lines) {
            if (line.length() <= 75) {
                result.append(line).append("\r\n");
            } else {
                // Fold long lines
                result.append(line, 0, 75).append("\r\n");
                String remaining = line.substring(75);

                while (remaining.length() > 74) { // 74 because we need space for leading space
                    result.append(" ").append(remaining, 0, 74).append("\r\n");
                    remaining = remaining.substring(74);
                }

                if (!remaining.isEmpty()) {
                    result.append(" ").append(remaining).append("\r\n");
                }
            }
        }

        return result.toString();
    }
    
    /**
     * Exports the iCal data to a file at the specified path and filename.
     *
     * @param filePath The directory path where the file will be saved
     * @param fileName The name of the file to save the iCal data to
     * @return true if export was successful
     * @throws ICalExportException if writing to the file fails
     */
    public boolean exportICalToFile(String filePath, String fileName) throws ICalExportException {
        try {
            Files.write(
                    Paths.get(filePath, fileName),
                    getICalString().getBytes(),
                    java.nio.file.StandardOpenOption.WRITE,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );
            return true;
        } catch (IOException e) {
            throw new ICalExportException("Failed to export iCal to file: " + filePath + "/" + fileName, e);
        }
    }

    /**
     * Converts the events to a table model format for UI display.
     *
     * @return 2D Object array where each row represents an event with columns:
     *         [Name, Start, End, Location, Description, ...]
     */
    public Object[][] toTableModel() {
        if (events == null || events.isEmpty()) {
            return new Object[0][6]; // Return empty array with 6 columns
        }

        Object[][] tableData = new Object[events.size()][6];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < events.size(); i++) {
            CalendarEvent event = events.get(i);

            tableData[i][0] = event.getSummary() != null ? event.getSummary() : ""; // Name
            tableData[i][1] = event.getStart() != null ? event.getStart().format(formatter) : ""; // Start
            tableData[i][2] = event.getEnd() != null ? event.getEnd().format(formatter) : ""; // End
            tableData[i][3] = event.getLocation() != null ? event.getLocation() : ""; // Location
            tableData[i][4] = event.getDescription() != null ? event.getDescription() : ""; // Description
        }

        return tableData;
    }
}