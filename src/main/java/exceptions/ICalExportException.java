package exceptions;

/**
 * Custom exception for iCal export operations.
 * Provides more specific error handling for calendar-related operations.
 */
public class ICalExportException extends Exception {
    public ICalExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
