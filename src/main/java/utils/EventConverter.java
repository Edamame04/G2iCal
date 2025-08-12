/** EventConverter.java
 * The EventConverter class is responsible for converting a Google Calendar Event object into a custom MyEvent object.
 * It extracts key information such as the event ID, summary, description, location, organizer's email, and attendee emails.
 * It also handles both timed and all-day events by checking if the event times are set as DateTime or as all-day Date.
 * The start and end times are converted to LocalDateTime using the system's default timezone. Finally, it creates
 * and returns a new MyEvent instance with all the extracted and converted data.
 */

package utils;

// Imports Google Calendar API classes and Java time/date utilities
import calendar.CalendarEvent;
import calendar.MyEventBuilder;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting between different event formats.
 * Handles conversion from Google Calendar Event objects to CalendarEvent objects
 * with mapping of all commonly used iCal fields.
 */
public class EventConverter {

    /**
     * Converts a Google Calendar Event object to a CalendarEvent object.
     * Maps all available Google Calendar fields to corresponding iCal fields.
     *
     * @param googleEvent Google Calendar Event to convert
     * @return CalendarEvent instance with mapped fields
     */
    public static CalendarEvent convert(Event googleEvent) {
        if (googleEvent == null) {
            return null;
        }

        MyEventBuilder builder = new MyEventBuilder();

        // Basic identification
        builder.setUid(googleEvent.getId())
               .setSummary(googleEvent.getSummary())
               .setDescription(googleEvent.getDescription())
               .setLocation(googleEvent.getLocation());

        // Date and time conversion
        if (googleEvent.getStart() != null) {
            LocalDateTime startTime = convertEventDateTime(googleEvent.getStart());
            builder.setStart(startTime);
        }

        if (googleEvent.getEnd() != null) {
            LocalDateTime endTime = convertEventDateTime(googleEvent.getEnd());
            builder.setEnd(endTime);
        }

        if (googleEvent.getCreated() != null) {
            LocalDateTime created = convertDateTime(googleEvent.getCreated());
            builder.setCreated(created);
        }

        if (googleEvent.getUpdated() != null) {
            LocalDateTime updated = convertDateTime(googleEvent.getUpdated());
            builder.setLastModified(updated);
        }

        // Organizer information
        if (googleEvent.getOrganizer() != null) {
            String organizerEmail = googleEvent.getOrganizer().getEmail();
            String organizerName = googleEvent.getOrganizer().getDisplayName();
            if (organizerEmail != null) {
                String organizer = organizerName != null ?
                    "CN=" + organizerName + ":MAILTO:" + organizerEmail :
                    "MAILTO:" + organizerEmail;
                builder.setOrganizer(organizer);
            }
        }

        // Attendees
        if (googleEvent.getAttendees() != null) {
            List<String> attendees = getAttendees(googleEvent);
            builder.setAttendees(attendees);
        }

        // Event status
        if (googleEvent.getStatus() != null) {
            builder.setStatus(convertStatus(googleEvent.getStatus()));
        }

        // Transparency/visibility
        if (googleEvent.getTransparency() != null) {
            builder.setTransparency(googleEvent.getTransparency().toUpperCase());
        }

        // Classification/visibility
        if (googleEvent.getVisibility() != null) {
            builder.setClassification(convertVisibility(googleEvent.getVisibility()));
        }

        // URL/HTML link
        if (googleEvent.getHtmlLink() != null) {
            builder.setUrl(googleEvent.getHtmlLink());
        }

        // Recurrence rules
        if (googleEvent.getRecurrence() != null && !googleEvent.getRecurrence().isEmpty()) {
            // Google Calendar stores recurrence as a list of strings
            // We'll take the first RRULE if available
            for (String recRule : googleEvent.getRecurrence()) {
                if (recRule.startsWith("RRULE:")) {
                    builder.setRecurrenceRule(recRule.substring(6)); // Remove "RRULE:" prefix
                    break;
                }
            }
        }

        // Set some default values for iCal compliance
        if (googleEvent.getStatus() == null) {
            builder.setStatusConfirmed();
        }

        // Add reminder/alarm from Google Calendar reminders
        if (googleEvent.getReminders() != null &&
            googleEvent.getReminders().getOverrides() != null &&
            !googleEvent.getReminders().getOverrides().isEmpty()) {

            // Take the first popup reminder
            for (EventReminder reminder : googleEvent.getReminders().getOverrides()) {
                if ("popup".equals(reminder.getMethod())) {
                    builder.setAlarmMinutesBefore(reminder.getMinutes());
                    break;
                }
            }
        }

        return builder.build();
    }

    /**
     * Extracts attendees from a Google Calendar Event and formats them for iCal.
     * Each attendee is formatted as "CN=Display Name:MAILTO:email" or "MAILTO:email".
     * If response status is available, it is appended as ";PARTSTAT=status".
     *
     * @param googleEvent The Google Calendar Event containing attendees
     * @return List of formatted attendee strings
     */
    private static List<String> getAttendees(Event googleEvent) {
        List<String> attendees = new ArrayList<>();
        for (EventAttendee attendee : googleEvent.getAttendees()) {
            if (attendee.getEmail() != null) {
                String attendeeStr = attendee.getDisplayName() != null ?
                    "CN=" + attendee.getDisplayName() + ":MAILTO:" + attendee.getEmail() :
                    "MAILTO:" + attendee.getEmail();

                // Add response status if available
                if (attendee.getResponseStatus() != null) {
                    attendeeStr += ";PARTSTAT=" + convertResponseStatus(attendee.getResponseStatus());
                }

                attendees.add(attendeeStr);
            }
        }
        return attendees;
    }

    /**
     * Converts Google Calendar EventDateTime to LocalDateTime
     */
    private static LocalDateTime convertEventDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) return null;

        DateTime dateTime = eventDateTime.getDateTime();
        if (dateTime == null) {
            dateTime = eventDateTime.getDate();
        }

        return convertDateTime(dateTime);
    }

    /**
     * Converts Google Calendar DateTime to LocalDateTime
     */
    private static LocalDateTime convertDateTime(DateTime dateTime) {
        if (dateTime == null) return null;

        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(dateTime.getValue()),
            ZoneId.systemDefault()
        );
    }

    /**
     * Converts Google Calendar status to iCal status
     */
    private static String convertStatus(String googleStatus) {
        switch (googleStatus.toLowerCase()) {
            case "tentative":
                return "TENTATIVE";
            case "cancelled":
                return "CANCELLED";
            default:
                return "CONFIRMED";
        }
    }

    /**
     * Converts Google Calendar response status to iCal participation status
     */
    private static String convertResponseStatus(String responseStatus) {
        switch (responseStatus.toLowerCase()) {
            case "accepted":
                return "ACCEPTED";
            case "declined":
                return "DECLINED";
            case "tentative":
                return "TENTATIVE";
            default:
                return "NEEDS-ACTION";
        }
    }

    /**
     * Converts Google Calendar visibility to iCal classification
     */
    private static String convertVisibility(String visibility) {
        switch (visibility.toLowerCase()) {
            case "private":
                return "PRIVATE";
            case "confidential":
                return "CONFIDENTIAL";
            default:
                return "PUBLIC";
        }
    }
}