package utils;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utility class for validating user inputs in the Google Calendar iCal CLI application.
 * Provides validation methods for calendar indices, dates, filenames, and other input parameters.
 */
public class InputValidator {

    // Date format pattern used throughout the application
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Validates a calendar index input against the available calendars list.
     *
     * @param calendarIndexStr the calendar index as a string
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateCalendarIndex(String calendarIndexStr , List<CalendarListEntry> calendars) {
        // If no calendars are available, return an error
        if (calendars == null || calendars.isEmpty()) {
            return new ValidationResult(false, "No available calendars found. Please add a calendar first.");
        }

        // Check if input is null or empty
        if (calendarIndexStr == null || calendarIndexStr.trim().isEmpty()) {
            return new ValidationResult(false, "Calendar index cannot be empty");
        }

        try {
            // Parse the index as integer
            int index = Integer.parseInt(calendarIndexStr.trim());

            // Check if index is within valid range
            if (index < 0) {
                return new ValidationResult(false, "Calendar index cannot be negative");
            }

            if (index >= calendars.size()) {
                return new ValidationResult(false,
                    String.format("Calendar index %d is out of range. Available indices: 0-%d",
                                index, calendars.size() - 1));
            }

            return new ValidationResult(true, null);

        } catch (NumberFormatException e) {
            return new ValidationResult(false,
                String.format("Invalid calendar index format: '%s'. Please enter a valid number.", calendarIndexStr));
        }
    }

    /**
     * Validates a date string in YYYY-MM-DD format.
     *
     * @param dateStr the date string to validate
     * @param fieldName the name of the field being validated (for error messages)
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateDate(String dateStr, String fieldName) {
        // Check if input is null or empty
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " cannot be empty");
        }

        try {
            // Parse the date using the expected format
            LocalDate parsedDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);

            // Check if date is reasonable (not too far in the past or future)
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusYears(10); // 10 years ago
            LocalDate maxDate = now.plusYears(10);  // 10 years from now

            if (parsedDate.isBefore(minDate)) {
                return new ValidationResult(false,
                    String.format("%s is too far in the past. Please use a date after %s",
                                fieldName, minDate.format(DATE_FORMATTER)));
            }

            if (parsedDate.isAfter(maxDate)) {
                return new ValidationResult(false,
                    String.format("%s is too far in the future. Please use a date before %s",
                                fieldName, maxDate.format(DATE_FORMATTER)));
            }

            return new ValidationResult(true, null);

        } catch (DateTimeParseException e) {
            return new ValidationResult(false,
                String.format("Invalid %s format: '%s'. Please use YYYY-MM-DD format (e.g., 2025-07-29)",
                            fieldName.toLowerCase(), dateStr));
        }
    }

    /**
     * Validates that the start date is before or equal to the end date.
     *
     * @param startDateDT the start date string
     * @param endDateStr the end date string
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateDateRange(DateTime startDateDT, String endDateStr) {
        // Validate end date
        ValidationResult endValidation = validateDate(endDateStr, "End date");
        if (!endValidation.isValid()) {
            return endValidation;
        }

        try {
            // Parse end dates for comparison
            String startDateStr = startDateDT.toStringRfc3339().substring(0, 10); // Extract YYYY-MM-DD part
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr.trim(), DATE_FORMATTER);

            // Check if start date is after end date
            if (startDate.isAfter(endDate)) {
                return new ValidationResult(false,
                    String.format("Start date (%s) cannot be after end date (%s)", startDateStr, endDateStr));
            }

            // Check if date range is reasonable (not too long)
            if (startDate.plusYears(2).isBefore(endDate)) {
                return new ValidationResult(false,
                    "Date range is too large (more than 2 years). Please select a smaller range.");
            }

            return new ValidationResult(true, null);

        } catch (DateTimeParseException e) {
            // This shouldn't happen since we validated individual dates above
            return new ValidationResult(false, "Error parsing date range");
        }
    }

    /**
     * Validates a filename for the iCal export.
     *
     * @param fileName the filename to validate
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateFileName(String fileName) {
        // Check if input is null or empty
        if (fileName == null || fileName.trim().isEmpty()) {
            return new ValidationResult(false, "Filename cannot be empty");
        }

        String trimmedFileName = fileName.trim();

        // Check filename length
        if (trimmedFileName.length() > 255) {
            return new ValidationResult(false, "Filename is too long (maximum 255 characters)");
        }

        // Check for invalid characters in filename
        String invalidChars = "<>:\"|?*";
        for (char c : invalidChars.toCharArray()) {
            if (trimmedFileName.indexOf(c) != -1) {
                return new ValidationResult(false,
                    String.format("Filename contains invalid character '%c'. Invalid characters: %s", c, invalidChars));
            }
        }

        // Check for reserved names (for Windows)
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

        String fileNameWithoutExtension = trimmedFileName.toLowerCase();
        if (fileNameWithoutExtension.endsWith(".ics")) {
            fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.length() - 4);
        }

        for (String reserved : reservedNames) {
            if (fileNameWithoutExtension.equals(reserved.toLowerCase())) {
                return new ValidationResult(false,
                    String.format("'%s' is a reserved filename. Please choose a different name.", reserved));
            }
        }

        // Check if filename ends with .ics or is a valid name for adding .ics
        if (!trimmedFileName.toLowerCase().endsWith(".ics")) {
            // Validate that we can add .ics extension
            if ((trimmedFileName + ".ics").length() > 255) {
                return new ValidationResult(false,
                    "Filename is too long when .ics extension is added (maximum 255 characters total)");
            }
        }

        return new ValidationResult(true, null);
    }

    /**
     * Normalizes a filename by adding .ics extension if not present.
     * Should only be called after successful validation.
     *
     * @param fileName the filename to normalize
     * @return the normalized filename with .ics extension
     */
    public static String normalizeFileName(String fileName) {
        if (fileName == null) {
            return "calendar_export.ics";
        }

        String trimmedFileName = fileName.trim();
        if (trimmedFileName.isEmpty()) {
            return "calendar_export.ics";
        }

        // Add .ics extension if not present
        if (!trimmedFileName.toLowerCase().endsWith(".ics")) {
            return trimmedFileName + ".ics";
        }

        return trimmedFileName;
    }

    /**
     * Converts a validated date string to Google API DateTime format.
     * Should only be called after successful date validation.
     *
     * @param dateStr the validated date string in YYYY-MM-DD format
     * @param isEndDate true if this is an end date (sets time to 23:59:59), false for start date (sets time to 00:00:00)
     * @return DateTime object ready for Google Calendar API
     */
    public static DateTime convertToDateTime(String dateStr, boolean isEndDate) {
        try {
            String timeComponent = isEndDate ? "T23:59:59Z" : "T00:00:00Z";
            return DateTime.parseRfc3339(dateStr.trim() + timeComponent);
        } catch (Exception e) {
            // Fallback to current time if parsing fails (shouldn't happen with validated input)
            return new DateTime(System.currentTimeMillis());
        }
    }

    /**
     * Gets the CalenderID from a CalendarListEntry by its index.
     *
     * @param calendarIndex the index of the calendar in the list
     * @return the Calendar ID as a string, or null if index is invalid
     */
    public static String getCalendarIdByIndex(int calendarIndex, List<CalendarListEntry> calendars) {
        // Check if index is valid
        if (calendarIndex < 0 || calendarIndex >= calendars.size()) {
            return null;
        }

        // Return the Calendar ID for the specified index
        return calendars.get(calendarIndex).getId();
    }

    /**
     * Validates a calendar name input against the available calendars list.
     *
     * @param calendarName the calendar name as a string
     * @param calendars the list of available calendars
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateCalendarName(String calendarName, List<CalendarListEntry> calendars) {
        // Check if input is null or empty
        if (calendarName == null || calendarName.trim().isEmpty()) {
            return new ValidationResult(false, "Calendar name cannot be empty");
        }

        String trimmedName = calendarName.trim();

        // Check if calendar name exists in the list (case-insensitive)
        for (CalendarListEntry calendar : calendars) {
            if (calendar.getSummary().equalsIgnoreCase(trimmedName)) {
                return new ValidationResult(true, null);
            }
        }

        // If not found, provide helpful error message with available calendars
        StringBuilder availableCalendars = new StringBuilder();
        for (int i = 0; i < calendars.size(); i++) {
            availableCalendars.append("'").append(calendars.get(i).getSummary()).append("'");
            if (i < calendars.size() - 1) {
                availableCalendars.append(", ");
            }
        }

        return new ValidationResult(false,
            String.format("Calendar '%s' not found. Available calendars: %s",
                         trimmedName, availableCalendars));
    }

    /**
     * Finds a calendar by name from the list of available calendars.
     *
     * @param calendarName the calendar name to find
     * @param calendars the list of available calendars
     * @return the CalendarListEntry if found, null otherwise
     */
    public static CalendarListEntry findCalendarByName(String calendarName, List<CalendarListEntry> calendars) {
        if (calendarName == null || calendars == null) {
            return null;
        }

        String trimmedName = calendarName.trim();
        for (CalendarListEntry calendar : calendars) {
            if (calendar.getSummary().equalsIgnoreCase(trimmedName)) {
                return calendar;
            }
        }
        return null;
    }
    /**
     * Validates a file path for the iCal export.
     * Checks if the path is not null or empty or not accessible.
     *
     * @param filePath the file path to validate
     * @return ValidationResult containing validation status and error message if invalid
     */
    public static ValidationResult validateFilePath(String filePath) {
        // Check if input is null or empty
        if (filePath == null || filePath.trim().isEmpty()) {
            return new ValidationResult(false, "File path cannot be empty");
        }

        String trimmedFilePath = filePath.trim();

        // Check if the path is accessible
        java.io.File file = new java.io.File(trimmedFilePath);
        if (!file.canWrite()) {
            return new ValidationResult(false, "Cannot write to the specified file path: " + trimmedFilePath);
        }

        return new ValidationResult(true, null);
    }

    /**
     * Inner class to represent validation results.
     * Contains the validation status and error message if validation failed.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        /**
         * Creates a new ValidationResult.
         *
         * @param valid true if validation passed, false otherwise
         * @param errorMessage error message if validation failed, null if valid
         */
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        /**
         * Returns whether the validation passed.
         *
         * @return true if validation passed, false otherwise
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Returns the error message if validation failed.
         *
         * @return error message or null if validation passed
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
