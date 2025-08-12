package calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern implementation for creating MyEvent objects.
 * This provides a more flexible and readable way to construct events with all iCal fields.
 */
public class MyEventBuilder {
    // Basic event identification
    String uid;
    String summary;
    String description;
    String location;

    // Date and time fields
    LocalDateTime start;
    LocalDateTime end;
    LocalDateTime created;
    LocalDateTime lastModified;

    // People and organization
    String organizer;
    List<String> attendees = new ArrayList<>();

    // Event properties
    String status; // CONFIRMED, TENTATIVE, CANCELLED
    String transparency; // OPAQUE, TRANSPARENT
    String classification; // PUBLIC, PRIVATE, CONFIDENTIAL
    Integer priority; // 0-9, where 0 is undefined, 1 is highest, 9 is lowest

    // Recurrence and timing
    String recurrenceRule; // RRULE
    List<LocalDateTime> recurrenceDates = new ArrayList<>(); // RDATE
    List<LocalDateTime> exceptionDates = new ArrayList<>(); // EXDATE

    // Additional fields
    String url;
    List<String> categories = new ArrayList<>();
    String comment;
    String contact;

    // Alarm/reminder
    Integer alarmMinutesBefore; // Simple alarm implementation

    // Basic identification setters
    public MyEventBuilder setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public MyEventBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public MyEventBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public MyEventBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    // Date and time setters
    public MyEventBuilder setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public MyEventBuilder setEnd(LocalDateTime end) {
        this.end = end;
        return this;
    }

    public MyEventBuilder setCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public MyEventBuilder setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    // People and organization setters
    public MyEventBuilder setOrganizer(String organizer) {
        this.organizer = organizer;
        return this;
    }

    public MyEventBuilder addAttendee(String attendee) {
        this.attendees.add(attendee);
        return this;
    }

    public MyEventBuilder setAttendees(List<String> attendees) {
        this.attendees = new ArrayList<>(attendees);
        return this;
    }

    // Event properties setters
    public MyEventBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public MyEventBuilder setStatusConfirmed() {
        this.status = "CONFIRMED";
        return this;
    }

    public MyEventBuilder setStatusTentative() {
        this.status = "TENTATIVE";
        return this;
    }

    public MyEventBuilder setStatusCancelled() {
        this.status = "CANCELLED";
        return this;
    }

    public MyEventBuilder setTransparency(String transparency) {
        this.transparency = transparency;
        return this;
    }

    public MyEventBuilder setTransparencyOpaque() {
        this.transparency = "OPAQUE";
        return this;
    }

    public MyEventBuilder setTransparencyTransparent() {
        this.transparency = "TRANSPARENT";
        return this;
    }

    public MyEventBuilder setClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public MyEventBuilder setClassificationPublic() {
        this.classification = "PUBLIC";
        return this;
    }

    public MyEventBuilder setClassificationPrivate() {
        this.classification = "PRIVATE";
        return this;
    }

    public MyEventBuilder setClassificationConfidential() {
        this.classification = "CONFIDENTIAL";
        return this;
    }

    public MyEventBuilder setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public MyEventBuilder setPriorityHigh() {
        this.priority = 1;
        return this;
    }

    public MyEventBuilder setPriorityNormal() {
        this.priority = 5;
        return this;
    }

    public MyEventBuilder setPriorityLow() {
        this.priority = 9;
        return this;
    }

    // Recurrence setters
    public MyEventBuilder setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
        return this;
    }

    public MyEventBuilder addRecurrenceDate(LocalDateTime recurrenceDate) {
        this.recurrenceDates.add(recurrenceDate);
        return this;
    }

    public MyEventBuilder setRecurrenceDates(List<LocalDateTime> recurrenceDates) {
        this.recurrenceDates = new ArrayList<>(recurrenceDates);
        return this;
    }

    public MyEventBuilder addExceptionDate(LocalDateTime exceptionDate) {
        this.exceptionDates.add(exceptionDate);
        return this;
    }

    public MyEventBuilder setExceptionDates(List<LocalDateTime> exceptionDates) {
        this.exceptionDates = new ArrayList<>(exceptionDates);
        return this;
    }

    // Additional fields setters
    public MyEventBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public MyEventBuilder addCategory(String category) {
        this.categories.add(category);
        return this;
    }

    public MyEventBuilder setCategories(List<String> categories) {
        this.categories = new ArrayList<>(categories);
        return this;
    }

    public MyEventBuilder setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public MyEventBuilder setContact(String contact) {
        this.contact = contact;
        return this;
    }

    // Alarm setter
    public MyEventBuilder setAlarmMinutesBefore(Integer alarmMinutesBefore) {
        this.alarmMinutesBefore = alarmMinutesBefore;
        return this;
    }

    // Convenience methods for common alarm times
    public MyEventBuilder setAlarm15MinutesBefore() {
        this.alarmMinutesBefore = 15;
        return this;
    }

    public MyEventBuilder setAlarm30MinutesBefore() {
        this.alarmMinutesBefore = 30;
        return this;
    }

    public MyEventBuilder setAlarm1HourBefore() {
        this.alarmMinutesBefore = 60;
        return this;
    }

    public MyEventBuilder setAlarm1DayBefore() {
        this.alarmMinutesBefore = 1440;
        return this;
    }

    /**
     * Builds and returns the MyEvent object with all configured properties.
     * @return A new MyEvent instance with the builder's configuration
     */
    public MyEvent build() {
        return new MyEvent(this);
    }
}
