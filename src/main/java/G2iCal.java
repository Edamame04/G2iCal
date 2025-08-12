import calendar.ICal;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import exceptions.ICalExportException;
import utils.CalendarApiConnector;
import utils.InputValidator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Main class for generating iCal formatted output from Google Calendar events.
 * This class uses the utils.ApiConnector singleton to interact with the Google Calendar API
 * and converts the retrieved events to iCal format.
 */
public class G2iCal {
    static String EXPORT_FILE_PATH = System.getProperty("user.home") + "/Downloads";

    /**
     * Main method that orchestrates the calendar selection and iCal generation process.
     * Takes user input from the arguments.
     * Arguments should be in the format: ⟨Start-Date⟩ ⟨End-Date⟩ ⟨Filename⟩ ⟨Calendar-Index⟩
     * If no arguments are provided or if the input is invalid, it will prompt the user interactively.
     * Then it will use them to set the calendar, time range and filename for the iCal export.
     *
     * @param args command line arguments for calendar index, start date, end date, and filename
     */
    public static void main(String... args) {
        // Display welcome message
        displayWelcomeMessage();

        // display program info if requested
        displayInfoIfPorted(args);

        // Initialize the API connector
        System.out.println("\nInitializing API Connector...");
        CalendarApiConnector apiConnector;
        try {
            apiConnector = CalendarApiConnector.getInstance();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        // Get the inputs for the calendar, start date, end date, and filename from the user
        DateTime startTime = getUserInputStartDate(args);
        DateTime endTime = getUserInputEndDate(startTime, args);
        String fileName = getUserInputFileName(args);
        String selectedCalendar = getUserInputCalendar(args);


        // Create the iCal object to hold the events and add the events to it
        System.out.println("\nFetching events from calendar: " + selectedCalendar);
        Events events;
        try {
            // Fetch events from the selected calendar
            events = apiConnector.getCalendarEventsByCalendarId(selectedCalendar, startTime, endTime);
            System.out.println("Found " + events.getItems().size() + " events in the specified date range.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ICal ical = new ICal(events);

        // Export the iCal file to the specified filename
        try {
            ical.exportICalToFile(EXPORT_FILE_PATH, fileName);
            System.out.println("\nSuccess! iCal file generated in " + EXPORT_FILE_PATH);
            exit(0);
        } catch (ICalExportException e) {
            System.err.println("Export Error: " + e.getMessage());
            exit(1);
        }
    }

    /* Helper methods for main flow*/

    /**
     * Prompts the user for a start date.
     * If a start date is provided as an argument, it validates and uses it.
     * Otherwise, it prompts the user for input until a valid date is provided.
     *
     * @param args command line arguments
     * @return DateTime object representing the start date
     */
    private static DateTime getUserInputStartDate(String... args) {
        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // If start date is provided as an argument, use it directly
        if (args.length > 0) {
            String startDate = args[0].trim();
            InputValidator.ValidationResult result = InputValidator.validateDate(startDate, "start");
            if (result.isValid()) {
                return InputValidator.convertToDateTime(startDate, false);
            } else {
                System.out.println("Error: " + result.getErrorMessage());
                System.out.println("Please provide a valid start date.");
            }
        }

        // Prompt the user for a start date
        return InputValidator.convertToDateTime(promptForValidInput(scanner, "Enter start date (YYYY-MM-DD): ",
                dateStr -> InputValidator.validateDate(dateStr, "Start date")), false);
    }

    /**
     * Prompts the user for a valid end date.
     * If an end date is provided as an argument, it validates and uses it.
     * Otherwise, it prompts the user for input until a valid end date is provided.
     *
     * @param args command line arguments
     * @return DateTime object representing the end date
     */
    private static DateTime getUserInputEndDate(DateTime startDate, String... args) {
        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // If start date is provided as an argument, use it directly
        if (args.length > 1) {
            String endDate = args[1].trim();
            InputValidator.ValidationResult result = InputValidator.validateDateRange(startDate, endDate);
            if (result.isValid()) {
                return InputValidator.convertToDateTime(endDate, true);
            } else {
                System.out.println("Error: " + result.getErrorMessage());
                System.out.println("Please provide a valid end date.");
            }
        }

        // Prompt the user for a start date
        return InputValidator.convertToDateTime(promptForValidInput(scanner, "Enter end date (YYYY-MM-DD): ",
                dateStr -> InputValidator.validateDateRange(startDate, dateStr)), true);

    }


    /**
     * Prompts the user for a filename to save the iCal file.
     * If a filename is provided as an argument, it validates and uses it.
     * Otherwise, it prompts the user for input until a valid filename is provided.
     *
     * @param args command line arguments
     * @return normalized filename with .ics extension
     */
    private static String getUserInputFileName(String... args) {
        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // If filename is provided as an argument, use it directly
        if (args.length > 2) {
            String fileName = args[2].trim();
            InputValidator.ValidationResult result = InputValidator.validateFileName(fileName);
            if (result.isValid()) {
                return InputValidator.normalizeFileName(fileName); // Normalize the filename to ensure it has .ics extension
            } else {
                System.out.println("Error: " + result.getErrorMessage());
                System.out.println("Please provide a valid filename.");
            }
        }

        // Prompt the user for a filename
        return InputValidator.normalizeFileName(promptForValidInput(scanner, "Enter output filename: ",
                InputValidator::validateFileName));
    }

    /**
     * Prompts the user for a calendar selection.
     * If a calendar index is provided as an argument, it validates and uses it.
     * Otherwise, it prompts the user for input until a valid calendar is selected.
     *
     * @param args command line arguments
     * @return ID of the selected calendar
     */
    private static String getUserInputCalendar(String... args) {
        // Fetch the list of available calendars
        List<CalendarListEntry> calendars;
        try {
            calendars = CalendarApiConnector.getInstance().getUserCalendars();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // If calendar index is provided as an argument, use it directly
        if (args.length > 3) {
            String calendarIndexStr = args[3].trim();
            InputValidator.ValidationResult result = InputValidator.validateCalendarIndex(calendarIndexStr, calendars);
            if (result.isValid()) {
                return InputValidator.getCalendarIdByIndex(Integer.parseInt(calendarIndexStr), calendars);
            } else {
                System.out.println("Error: " + result.getErrorMessage());
                System.out.println("Please provide a valid calendar index.");
            }
        }

        // Prompt the user for a calendar selection
        displayCalendarList(calendars);
        List<CalendarListEntry> finalCalendars = calendars; // Capture the list of calendars for use in the lambda
        return InputValidator.getCalendarIdByIndex(
                Integer.parseInt(
                        promptForValidInput(
                                scanner,
                                "Select a calendar by index: ",
                                input -> InputValidator.validateCalendarIndex(input, finalCalendars))),
                calendars);
    }


    /**
     * Prompts the user for valid input with retry mechanism.
     *
     * @param scanner   the Scanner for input
     * @param prompt    the prompt message
     * @param validator the validation function
     * @return valid input string
     */
    private static String promptForValidInput(Scanner scanner, String prompt, java.util.function.Function<String, InputValidator.ValidationResult> validator) {
        while (true) { // Loop until valid input is received or user exits
            // Display the prompt and read user input
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            // Validate the input using the provided validator function
            InputValidator.ValidationResult result = validator.apply(input);
            if (result.isValid()) {
                return input;
            } else {
                System.out.println("Error: " + result.getErrorMessage());
                System.out.println("Please try again.");
            }
        }
    }

    /**
     * Displays a welcome message with program branding.
     */
    private static void displayWelcomeMessage() {
        System.out.println("\nG2iCal - Google Calendar to iCal Converter");
    }

    /**
     * Displays the list of available calendars in a formatted manner.
     *
     * @param calendars list of calendar entries to display
     */

    private static void displayCalendarList(List<CalendarListEntry> calendars) {
        System.out.println("\nAvailable Calendars:");
        for (int i = 0; i < calendars.size(); i++) {
            CalendarListEntry calendar = calendars.get(i);
            System.out.printf("  [%d] %s\n", i, calendar.getSummary());
            System.out.printf("      ID: %s\n", calendar.getId());
            if (i < calendars.size() - 1) { // Add a separator between calendars
                System.out.println();
            }
        }
    }


    /**
     * Checks if the user has requested help or information about the program.
     * If so, it displays the program description, usage, and examples.
     * This method is called at the start of the main method to handle help requests.
     */
    private static void displayInfoIfPorted(String... args) {
        // Check for info argument first
        if (args.length > 0 && (args[0].equals("--info") || args[0].equals("-i") || args[0].equals("info") || args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("DESCRIPTION:");
            System.out.println("  This program exports events from your Google Calendar to iCal format (.ics files).");
            System.out.println("  You can use it in both command line and interactive modes.");
            System.out.println();
            System.out.println("USAGE:");
            System.out.println("  java G2iCal [start-date] [end-date] [filename] [calendar-index]");
            System.out.println("  java G2iCal --info    (show this help)");
            System.out.println("  java G2iCal           (interactive mode)");
            System.out.println();
            System.out.println("ARGUMENTS:");
            System.out.println("  start-date      Start date in YYYY-MM-DD format (e.g., 2025-07-29)");
            System.out.println("  end-date        End date in YYYY-MM-DD format (e.g., 2025-08-05)");
            System.out.println("  filename        Output filename (.ics extension added automatically if missing)");
            System.out.println("  calendar-index  Index of the calendar (0-based, shown when listing calendars)");
            System.out.println("  on invalid input, the program will prompt for valid input");
            System.out.println();
            System.out.println("EXAMPLES:");
            System.out.println("  java MainCLI 2025-07-29 2025-08-05 my_events 0");
            System.out.println("  java MainCLI 2025-08-01 2025-08-31 work_events.ics 1");
            System.out.println("  java MainCLI --help");
            System.out.println("  java MainCLI    # Interactive mode - program will prompt for inputs");
            System.out.println();
            System.out.println("REQUIREMENTS:");
            System.out.println("  - Valid Google Calendar API credentials (credentials.json)");
            System.out.println("  - Internet connection for API access");
            System.out.println("  - Java 11 or higher");
            exit(0); // Exit after displaying info
        }
    }
}
