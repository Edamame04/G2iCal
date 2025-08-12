/**
 * MyEvent.java
 * MyEvent represents a calendar event with all necessary fields for iCal export.
 * It stores event details such as UID, summary, description, location, start/end times, organizer, and attendees.
 */

package calendar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CalendarEvent interface representing a calendar event with iCal support.
 * This class contains all commonly used iCal event fields and can export to iCal format.
 */
public class MyEvent implements CalendarEvent {
    // Basic event identification
    private String uid;
    private String summary;
    private String description;
    private String location;

    // Date and time fields
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime created;
    private LocalDateTime lastModified;

    // People and organization
    private String organizer;
    private List<String> attendees;

    // Event properties
    private String status; // CONFIRMED, TENTATIVE, CANCELLED
    private String transparency; // OPAQUE, TRANSPARENT
    private String classification; // PUBLIC, PRIVATE, CONFIDENTIAL
    private Integer priority; // 0-9, where 0 is undefined, 1 is highest, 9 is lowest

    // Recurrence and timing
    private String recurrenceRule; // RRULE
    private List<LocalDateTime> recurrenceDates; // RDATE
    private List<LocalDateTime> exceptionDates; // EXDATE

    // Additional fields
    private String url;
    private List<String> categories;
    private String comment;
    private String contact;

    // Alarm/reminder
    private Integer alarmMinutesBefore; // Simple alarm implementation

    // Date formatter for iCal format
    private static final DateTimeFormatter ICAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Default constructor - initializes lists and sets default values
     */
    public MyEvent() {
        this.uid = UUID.randomUUID().toString();
        this.attendees = new ArrayList<>();
        this.recurrenceDates = new ArrayList<>();
        this.exceptionDates = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.created = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.status = "CONFIRMED";
        this.transparency = "OPAQUE";
        this.classification = "PUBLIC";
        this.priority = 0;
    }

    /**
     * Constructor with builder - package private for use by MyEventBuilder
     */
    MyEvent(MyEventBuilder builder) {
        this.uid = builder.uid != null ? builder.uid : UUID.randomUUID().toString();
        this.summary = builder.summary;
        this.description = builder.description;
        this.location = builder.location;
        this.start = builder.start;
        this.end = builder.end;
        this.created = builder.created != null ? builder.created : LocalDateTime.now();
        this.lastModified = builder.lastModified != null ? builder.lastModified : LocalDateTime.now();
        this.organizer = builder.organizer;
        this.attendees = new ArrayList<>(builder.attendees);
        this.status = builder.status != null ? builder.status : "CONFIRMED";
        this.transparency = builder.transparency != null ? builder.transparency : "OPAQUE";
        this.classification = builder.classification != null ? builder.classification : "PUBLIC";
        this.priority = builder.priority != null ? builder.priority : 0;
        this.recurrenceRule = builder.recurrenceRule;
        this.recurrenceDates = new ArrayList<>(builder.recurrenceDates);
        this.exceptionDates = new ArrayList<>(builder.exceptionDates);
        this.url = builder.url;
        this.categories = new ArrayList<>(builder.categories);
        this.comment = builder.comment;
        this.contact = builder.contact;
        this.alarmMinutesBefore = builder.alarmMinutesBefore;
    }

    // Getters implementing CalendarEvent interface
    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public LocalDateTime getStart() {
        return start;
    }

    @Override
    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String getOrganizer() {
        return organizer;
    }

    @Override
    public List<String> getAttendees() {
        return new ArrayList<>(attendees);
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getTransparency() {
        return transparency;
    }

    @Override
    public String getClassification() {
        return classification;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    @Override
    public List<LocalDateTime> getRecurrenceDates() {
        return new ArrayList<>(recurrenceDates);
    }

    @Override
    public List<LocalDateTime> getExceptionDates() {
        return new ArrayList<>(exceptionDates);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getContact() {
        return contact;
    }

    @Override
    public Integer getAlarmMinutesBefore() {
        return alarmMinutesBefore;
    }

    // Setters for mutable operations
    public void setSummary(String summary) {
        this.summary = summary;
        updateLastModified();
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }

    public void setLocation(String location) {
        this.location = location;
        updateLastModified();
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
        updateLastModified();
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
        updateLastModified();
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Converts the event to iCal format string.
     * @return iCal formatted string representation of the event
     */
    @Override
    public String toICal() {
        StringBuilder ical = new StringBuilder();

        ical.append("BEGIN:VEVENT\r\n");

        // Required fields
        ical.append("UID:").append(uid).append("\r\n");
        if (start != null) {
            ical.append("DTSTART:").append(start.format(ICAL_DATE_FORMAT)).append("\r\n");
        }
        if (end != null) {
            ical.append("DTEND:").append(end.format(ICAL_DATE_FORMAT)).append("\r\n");
        }
        ical.append("DTSTAMP:").append(LocalDateTime.now().format(ICAL_DATE_FORMAT)).append("\r\n");

        // Optional fields
        if (summary != null && !summary.isEmpty()) {
            ical.append("SUMMARY:").append(escapeText(summary)).append("\r\n");
        }
        if (description != null && !description.isEmpty()) {
            ical.append("DESCRIPTION:").append(escapeText(description)).append("\r\n");
        }
        if (location != null && !location.isEmpty()) {
            ical.append("LOCATION:").append(escapeText(location)).append("\r\n");
        }
        if (organizer != null && !organizer.isEmpty()) {
            ical.append("ORGANIZER:").append(organizer).append("\r\n");
        }
        if (created != null) {
            ical.append("CREATED:").append(created.format(ICAL_DATE_FORMAT)).append("\r\n");
        }
        if (lastModified != null) {
            ical.append("LAST-MODIFIED:").append(lastModified.format(ICAL_DATE_FORMAT)).append("\r\n");
        }
        if (status != null && !status.isEmpty()) {
            ical.append("STATUS:").append(status).append("\r\n");
        }
        if (transparency != null && !transparency.isEmpty()) {
            ical.append("TRANSP:").append(transparency).append("\r\n");
        }
        if (classification != null && !classification.isEmpty()) {
            ical.append("CLASS:").append(classification).append("\r\n");
        }
        if (priority != null && priority > 0) {
            ical.append("PRIORITY:").append(priority).append("\r\n");
        }
        if (recurrenceRule != null && !recurrenceRule.isEmpty()) {
            ical.append("RRULE:").append(recurrenceRule).append("\r\n");
        }
        if (url != null && !url.isEmpty()) {
            ical.append("URL:").append(url).append("\r\n");
        }
        if (comment != null && !comment.isEmpty()) {
            ical.append("COMMENT:").append(escapeText(comment)).append("\r\n");
        }
        if (contact != null && !contact.isEmpty()) {
            ical.append("CONTACT:").append(escapeText(contact)).append("\r\n");
        }

        // Attendees
        for (String attendee : attendees) {
            ical.append("ATTENDEE:").append(attendee).append("\r\n");
        }

        // Categories
        if (!categories.isEmpty()) {
            ical.append("CATEGORIES:").append(String.join(",", categories)).append("\r\n");
        }

        // Recurrence dates
        for (LocalDateTime rdate : recurrenceDates) {
            ical.append("RDATE:").append(rdate.format(ICAL_DATE_FORMAT)).append("\r\n");
        }

        // Exception dates
        for (LocalDateTime exdate : exceptionDates) {
            ical.append("EXDATE:").append(exdate.format(ICAL_DATE_FORMAT)).append("\r\n");
        }

        // Alarm
        if (alarmMinutesBefore != null && alarmMinutesBefore > 0) {
            ical.append("BEGIN:VALARM\r\n");
            ical.append("TRIGGER:-PT").append(alarmMinutesBefore).append("M\r\n");
            ical.append("ACTION:DISPLAY\r\n");
            ical.append("DESCRIPTION:Reminder\r\n");
            ical.append("END:VALARM\r\n");
        }

        ical.append("END:VEVENT\r\n");

        return ical.toString();
    }

    /**
     * Escapes special characters in text fields for iCal format
     * This ensures that commas, semicolons, and newlines do not break the iCal format.
     * @param text The text to escape
     * @return Escaped text suitable for iCal format
     */
    private String escapeText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
