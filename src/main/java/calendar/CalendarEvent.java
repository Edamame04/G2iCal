package calendar;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface representing a calendar event that can be converted to iCal format.
 * This allows for different event implementations while maintaining a common contract.
 */
public interface CalendarEvent {
    // Basic event identification
    String getUid();
    String getSummary();
    String getDescription();
    String getLocation();

    // Date and time fields
    LocalDateTime getStart();
    LocalDateTime getEnd();
    LocalDateTime getCreated();
    LocalDateTime getLastModified();

    // People and organization
    String getOrganizer();
    List<String> getAttendees();

    // Event properties
    String getStatus(); // CONFIRMED, TENTATIVE, CANCELLED
    String getTransparency(); // OPAQUE, TRANSPARENT
    String getClassification(); // PUBLIC, PRIVATE, CONFIDENTIAL
    Integer getPriority(); // 0-9, where 0 is undefined, 1 is highest, 9 is lowest

    // Recurrence and timing
    String getRecurrenceRule(); // RRULE
    List<LocalDateTime> getRecurrenceDates(); // RDATE
    List<LocalDateTime> getExceptionDates(); // EXDATE

    // Additional fields
    String getUrl();
    List<String> getCategories();
    String getComment();
    String getContact();

    // Alarm/reminder
    Integer getAlarmMinutesBefore(); // Simple alarm implementation

    /**
     * Converts the event to iCal format string.
     * @return iCal formatted string representation of the event
     */
    String toICal();
}
