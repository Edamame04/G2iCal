package utils;

import calendar.CalendarEvent;
import calendar.MyEventBuilder;
import com.google.api.services.calendar.model.Event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory class for creating CalendarEvent instances from various sources.
 * Provides methods to create events from Google Calendar Events or raw data.
 */
public class EventFactory {

    /**
     * Creates a CalendarEvent from a Google Calendar Event.
     * @param googleEvent Google Calendar Event to convert
     * @return CalendarEvent implementation
     */
    public static CalendarEvent createFromGoogle(Event googleEvent) {
        return EventConverter.convert(googleEvent);
    }

    /**
     * Creates a CalendarEvent from raw data.
     * This method creates an event with basic information and uses the builder pattern
     * for more complex event creation with additional iCal fields.
     *
     * @param uid Unique identifier for the event
     * @param summary Event title/summary
     * @param description Event description
     * @param location Event location
     * @param start Start date and time
     * @param end End date and time
     * @param organizer Event organizer
     * @param attendees List of attendees
     * @return CalendarEvent implementation
     */
    public static CalendarEvent createFromData(String uid, String summary, String description,
                                             String location, LocalDateTime start, LocalDateTime end,
                                             String organizer, List<String> attendees) {
        return new MyEventBuilder()
                .setUid(uid)
                .setSummary(summary)
                .setDescription(description)
                .setLocation(location)
                .setStart(start)
                .setEnd(end)
                .setOrganizer(organizer)
                .setAttendees(attendees)
                .build();
    }

    /**
     * Creates a simple CalendarEvent with just basic information.
     * @param summary Event title/summary
     * @param start Start date and time
     * @param end End date and time
     * @return CalendarEvent implementation
     */
    public static CalendarEvent createSimple(String summary, LocalDateTime start, LocalDateTime end) {
        return new MyEventBuilder()
                .setSummary(summary)
                .setStart(start)
                .setEnd(end)
                .build();
    }

    /**
     * Creates a CalendarEvent builder for more complex event creation.
     * This allows for fluent API usage when creating events with many optional fields.
     * @return MyEventBuilder instance for fluent event creation
     */
    public static MyEventBuilder builder() {
        return new MyEventBuilder();
    }
}
