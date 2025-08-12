/**
 * View.java
 * This class represents the main user interface for the G2iCal application.
 * It handles the display of calendars, event data, and user interactions.
 * The view is designed to be responsive and user-friendly.

 * Many methods in this class are package-private to only allow access from the Controller class.
 */

package ui.main;

import com.toedter.calendar.JDateChooser;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class View extends JFrame {
    private final Controller controller; // Reference to the controller

    // UI components that need to be accessed in multiple methods
    private JComboBox<String> calendarDropdown;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JButton loadDataButton;
    private DefaultTableModel tableModel;
    private JButton exportButton;
    private JLabel statusLabel;
    private JMenuItem saveMenuItem;

    //Style constants
    private static final Color LIGHT_BLUE = new Color(135, 206, 250);

    /**
     * Constructor for the main view.
     * Initializes the user interface and sets up the layout.
     * Package-private to restrict access to the controller.
     *
     * @param controller The controller that handles user interactions.
     */
    View(Controller controller) {
        this.controller = controller;
        initializeUI();
    }

    /**
     * Initializes the user interface components and layout.
     */
    private void initializeUI() {
        // Program name and basic settings for the JFrame
        setTitle("G2iCal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 300)); // Set minimum size for responsiveness
        setPreferredSize(new Dimension(480, 700)); // default size
        setLocationRelativeTo(null);

        // Set Menu Bar
        createMenuBar();

        // Create main content panel
        createMainContent();

        pack(); // Size to preferred size
        setVisible(true);
    }

    /**
     * Creates the menu bar for the main view and adds it to the JFrame.
     */
    private void createMenuBar() {
        // Detect operating system
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

        // Create and set up the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create File menu
        JMenu fileMenu = new JMenu("File");
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(isMac ? "meta S" : "ctrl S"));
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new ExportActionListener() {
        });
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(isMac ? "meta N" : "ctrl N"));
        newItem.addActionListener(e -> controller.reset());
        JMenuItem exitItem = new JMenuItem("Exit to Start Screen");
        exitItem.addActionListener(e -> controller.exitToStartScreen());
        if (!isMac) {
            JMenuItem quitItem = new JMenuItem("Quit");
            quitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
            quitItem.addActionListener(e -> System.exit(0));
        }

        fileMenu.add(saveMenuItem);
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        if (!isMac) fileMenu.add(new JMenuItem("Quit"));


        // Account menu
        JMenu accountMenu = new JMenu("Account");
        JMenuItem accountInfoItem = new JMenuItem("Logged in as: " + controller.getUserName());
        accountInfoItem.setEnabled(false);
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> controller.logout());

        accountMenu.add(accountInfoItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);

        // Create Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem settingsItem = new JMenuItem("Application Settings");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(isMac ? "meta COMMA" : "ctrl COMMA"));
        settingsItem.addActionListener(e -> showSettingsDialog());

        settingsMenu.add(settingsItem);

        // Help menu for non-Mac systems (on macOS, the Help menu is handled by setupMacOSMenus)
        JMenu helpMenu = null;
        if (!isMac) {
            helpMenu = new JMenu("Help");
            JMenuItem aboutItem = new JMenuItem("About G2iCal");
            aboutItem.addActionListener(e -> showAboutDialog());
            helpMenu.add(aboutItem);
        }

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(accountMenu);
        menuBar.add(settingsMenu);
        if (!isMac) menuBar.add(helpMenu);

        // Handle macOS-specific menus
        if (isMac && Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            // Handle About menu
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler(e -> showAboutDialog());
            }
            // Handle Quit menu
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler((e, response) -> {
                    System.exit(0);
                    response.performQuit();
                });
            }
        }

        setJMenuBar(menuBar);
    }

    /**
     * Creates the main content panel with all UI components
     * Engineering
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel - Form with calendar selection and date range
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Center panel - Event table
        JPanel tablePanel = createEventTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Bottom panel - Export and status
        JPanel bottomPanel = createExportPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Row 1: Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel titleLabel = new JLabel("Select a calendar and date range for the export:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        formPanel.add(titleLabel, gbc);

        // Row 2: Calendar selection
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel calendarLabel = new JLabel("Calendar:");
        formPanel.add(calendarLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        calendarDropdown = new JComboBox<>();
        calendarDropdown.setPreferredSize(new Dimension(300, 28));
        formPanel.add(calendarDropdown, gbc);

        // Row 3: Start date
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel fromLabel = new JLabel("Start Date:");
        formPanel.add(fromLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        startDateChooser = createStyledDateChooser();
        formPanel.add(startDateChooser, gbc);

        // Row 4: End date
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel toLabel = new JLabel("End Date:");
        formPanel.add(toLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        endDateChooser = createStyledDateChooser();
        formPanel.add(endDateChooser, gbc);

        // Row 5: Load button (in column 2)
        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST; // Changed from WEST to EAST
        gbc.insets = new Insets(8, 8, 8, 8); // Extra top margin for the button

        loadDataButton = new JButton("Load Calendar Data");
        loadDataButton.setBackground(LIGHT_BLUE);
        loadDataButton.setPreferredSize(new Dimension(150, 28));
        loadDataButton.addActionListener(new LoadDataActionListener());
        formPanel.add(loadDataButton, gbc);

        // Set default dates (from 30 days ago to today)
        Calendar cal = Calendar.getInstance();
        endDateChooser.setDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        startDateChooser.setDate(cal.getTime());

        return formPanel;
    }

    /**
     * Creates a styled date chooser with a clean rounded border.
     * The date chooser has a connected appearance with the Load Data button.
     * It uses a custom border to achieve the desired look.
     *
     * @return A JDateChooser with custom styling.
     */
    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(120, 25));

        // Style the date chooser to have connected appearance with clean rounded corners
        SwingUtilities.invokeLater(() -> {
            // Get the text field component
            JTextField textField = ((JTextField) dateChooser.getDateEditor().getUiComponent());

            // Find the calendar button by iterating through components
            JButton calendarButton = null;
            for (Component comp : dateChooser.getComponents()) {
                if (comp instanceof JButton) {
                    calendarButton = (JButton) comp;
                    break;
                }
            }

            if (calendarButton != null) {
                // Create clean rounded borders like the Load Data button
                textField.setBorder(new CleanRoundedBorder(12, true, false)); // Left side rounded
                calendarButton.setBorder(new CleanRoundedBorder(12, false, true)); // Right side rounded

                // Style the button to match
                calendarButton.setContentAreaFilled(false);
                calendarButton.setFocusPainted(false);
                calendarButton.setOpaque(true);
                calendarButton.setBackground(Color.WHITE);

                // Style the text field
                textField.setOpaque(true);
                textField.setBackground(Color.WHITE);
            }
        });

        return dateChooser;
    }

    /**
     * Creates the event table panel with a responsive design.
     * The table displays calendar events in a read-only format.
     *
     * @return A JPanel containing the event table.
     */
    private JPanel createEventTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        String[] columnNames = {"Name", "Start", "End", "Location", "Description", "..."};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        JTable eventTable = new JTable(tableModel);
        eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        eventTable.setFillsViewportHeight(true); // Make table fill available space
        eventTable.setRowSelectionAllowed(false); // Disable row selection
        eventTable.setColumnSelectionAllowed(false); // Disable column selection

        // Set responsive column widths (proportional rather than fixed)
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Start
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(120); // End
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Location
        eventTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Description
        eventTable.getColumnModel().getColumn(5).setWidth(10); // More column

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Remove fixed size to make it truly responsive
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the export panel with a status label and export button.
     * The export button is initially disabled until data is loaded.
     *
     * @return A JPanel containing the export controls.
     */
    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));

        // Status label on the left - make it responsive
        statusLabel = new JLabel("Select a calendar and date range, then click 'Load Data' to begin");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        // Wrap long text if needed
        statusLabel.setPreferredSize(new Dimension(0, 20)); // Height fixed, width flexible
        panel.add(statusLabel, BorderLayout.CENTER);

        // Export button on the right
        exportButton = new JButton("Export");
        exportButton.setBackground(LIGHT_BLUE);
        exportButton.setMinimumSize(new Dimension(80, 35));
        exportButton.setPreferredSize(new Dimension(100, 35));
        exportButton.addActionListener(new ExportActionListener());
        exportButton.setEnabled(false); // Initially disabled until data is loaded

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(exportButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Resets the view to its initial state.
     * Clears the table, resets status label, and disables export button.
     * Package-private to restrict access to the controller.
     */
    void reset() {
        tableModel.setRowCount(0); // Clear existing data from the table
        statusLabel.setText("Select a calendar and date range, then click 'Load Data' to begin"); // Reset status label
        setExportButtonEnabled(false); // Disable export button
    }

    /**
     * Action listener for the Load Data button
     * Validates inputs and calls the controller to load calendar events.
     * Gets the selected values from the dropdown and date pickers and validates them.
     * If validation fails, shows an error dialog.
     * If validation passes, it calls the controller to load events.
     */
    private class LoadDataActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = calendarDropdown.getSelectedIndex();
            if (selectedIndex == -1) {
                showErrorDialog("Please select a calendar.");
                return;
            }

            // Get the calendar ID using the selected index
            String[] calendarIds = (String[]) calendarDropdown.getClientProperty("calendarIds");
            String selectedCalendarId = calendarIds[selectedIndex];

            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            // Validate inputs
            if (startDate == null || endDate == null) {
                showErrorDialog("Please select both valid start and end dates.");
                return;
            }

            if (selectedCalendarId == null || selectedCalendarId.isEmpty()) {
                showErrorDialog("Please select a calendar.");
                return;
            }

            if (startDate.after(endDate)) {
                showErrorDialog("Start date must be before or equal to end date.");
                return;
            }

            // Check if date range is too large (more than 1 year)
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.YEAR, 1);
            if (endDate.after(cal.getTime())) {
                showErrorDialog("Date range cannot exceed 1 year.");
                return;
            }

            statusLabel.setText("Loading data...");
            loadDataButton.setEnabled(false);

            // Format dates for controller
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = formatter.format(startDate);
            String toDateStr = formatter.format(endDate);

            // Call controller method to load events
            controller.loadCalendarEvents(selectedCalendarId, fromDateStr, toDateStr);
            loadDataButton.setEnabled(true);
        }
    }

    /**
     * Action listener for the Export button
     */
    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            statusLabel.setText("Exporting...");
            exportButton.setEnabled(false);

            // Call controller method to export events
            controller.exportEvents();
            exportButton.setEnabled(true);
        }
    }

    /**
     * Updates the calendar dropdown with available calendars
     *
     * @param calendarIds   Array of calendar IDs
     * @param calendarNames Array of calendar names
     */
    public void updateCalendarList(String[] calendarIds, String[] calendarNames) {
        calendarDropdown.removeAllItems();

        // Store calendar IDs for later retrieval
        calendarDropdown.putClientProperty("calendarIds", calendarIds);

        // Add calendar names to dropdown
        for (String name : calendarNames) {
            calendarDropdown.addItem(name);
        }

        // Select first item if available
        if (calendarNames.length > 0) {
            calendarDropdown.setSelectedIndex(0);
        }
    }

    /**
     * Updates the event table with loaded events
     *
     * @param events 2D array of event data
     */
    public void updateEventTable(Object[][] events) {
        tableModel.setRowCount(0); // Clear existing data
        for (Object[] event : events) {
            tableModel.addRow(event);
        }
    }

    /**
     * Sets the status message
     *
     * @param message The status message to display
     */
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    /**
     * Enables or disables the export button and save menu item
     *
     * @param enabled true to enable, false to disable
     */
    public void setExportButtonEnabled(boolean enabled) {
        exportButton.setEnabled(enabled);
        saveMenuItem.setEnabled(enabled);
    }

    /* Dialogs */

    /**
     * Shows the About dialog with application information.
     * This method is called when the user selects "About G2iCal" from the Help menu.
     */
    private void showAboutDialog() {
        String aboutMessage = "G2iCal\n" + "Version: Beta 1.0\n" + "G2iCal is a simple application for exporting your google-calendar to a ical file.\n\n" + "© 2025 Dominik Mitzel\n" + "MN: 6633800\n";

        JOptionPane.showOptionDialog(this, aboutMessage, "About G2iCal", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{}, // Empty array = no buttons
                null);
    }

    /**
     * Show Settings dialog.
     * In the settings dialog, the user can change the application settings.
     */
    private void showSettingsDialog() {
        // Get current file name and file path from the controller
        String fileName = controller.getFileName();
        String filePath = controller.getFilePath();

        // Create the settings dialog
        JDialog settingsDialog = new JDialog(this, "Application Settings", true);
        settingsDialog.setSize(550, 250);
        settingsDialog.setResizable(false);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Export file name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Export File Name:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField fileNameField = new JTextField(fileName, 20);
        fileNameField.setBorder(new CleanRoundedBorder(12, true, true));

        // Set same height constraints as locationField
        fileNameField.setPreferredSize(new Dimension(300, 28));
        fileNameField.setMinimumSize(new Dimension(200, 28));
        fileNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        formPanel.add(fileNameField, gbc);

        // Export file path
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Export Location:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Use Box layout instead of BorderLayout to better control sizing
        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));

        JTextField locationField = new JTextField(filePath, 20);
        locationField.setEditable(true);
        locationField.setBorder(new CleanRoundedBorder(12, true, false));

        // Force exact height control
        locationField.setPreferredSize(new Dimension(300, 28));
        locationField.setMinimumSize(new Dimension(200, 28));
        locationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton browseButton = new JButton("Browse...");
        browseButton.setBorder(new CleanRoundedBorder(12, false, true));
        browseButton.setPreferredSize(new Dimension(90, 28));
        browseButton.setMinimumSize(new Dimension(90, 28));
        browseButton.setMaximumSize(new Dimension(90, 28));

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Export Directory");
            fileChooser.setCurrentDirectory(new java.io.File(locationField.getText()));

            if (fileChooser.showOpenDialog(settingsDialog) == JFileChooser.APPROVE_OPTION) {
                locationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Add components with proper spacing
        locationPanel.add(locationField);
        locationPanel.add(Box.createHorizontalStrut(0)); // No gap between components for connected appearance
        locationPanel.add(browseButton);

        // Ensure the panel itself has fixed height
        locationPanel.setPreferredSize(new Dimension(400, 28));
        locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        formPanel.add(locationPanel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("Reset");
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(LIGHT_BLUE);

        // Reset to defaults action
        resetButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(settingsDialog, "Are you sure you want to reset all settings to defaults?", "Reset Settings", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                fileNameField.setText("calendar_export.ics");
                locationField.setText(System.getProperty("user.home") + java.io.File.separator + "Downloads");
            }
        });

        cancelButton.addActionListener(e -> settingsDialog.dispose());

        saveButton.addActionListener(e -> {
            String newFileName = fileNameField.getText().trim();
            String newFilePath = locationField.getText().trim();

            // Validate file name and path
            InputValidator.ValidationResult fileNameValidator = InputValidator.validateFileName(newFileName);
            if (!fileNameValidator.isValid()) {
                JOptionPane.showMessageDialog(settingsDialog, fileNameValidator.getErrorMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newFilePath.isEmpty()) {
                JOptionPane.showMessageDialog(settingsDialog, "Export location cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if directory exists
            java.io.File directory = new java.io.File(newFilePath);
            if (!directory.exists() || !directory.isDirectory()) {
                JOptionPane.showMessageDialog(settingsDialog, "The specified directory does not exist or is not accessible.", "Invalid Directory", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Save the settings and close the dialog
            controller.updateSettings(newFileName, newFilePath);
            settingsDialog.dispose();
        });

        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Add panels to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        settingsDialog.add(mainPanel);
        settingsDialog.setVisible(true);
    }

    /**
     * Show error dialog with a message.
     * It is package-private to restrict access to the package.
     *
     * @param message The error message to display.
     */
    void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class CleanRoundedBorder implements Border {
        /**
         * Custom border with rounded corners for a clean appearance.
         * This border can be used for text fields, buttons, etc.
         * It allows for left and right rounding to create a connected appearance.
         */
        // Radius for the rounded corners
        private final int radius;
        // Flags to determine if left or right corners should be rounded
        private final boolean leftRounded;
        private final boolean rightRounded;

        /**
         * Constructor for CleanRoundedBorder.
         *
         * @param radius       The radius of the rounded corners.
         * @param leftRounded  If true, the left corners will be rounded.
         * @param rightRounded If true, the right corners will be rounded.
         */
        public CleanRoundedBorder(int radius, boolean leftRounded, boolean rightRounded) {
            this.radius = radius;
            this.leftRounded = leftRounded;
            this.rightRounded = rightRounded;
        }

        /**
         * Paints the border around the component.
         * This method creates a rounded rectangle shape based on the specified radius and whether corners are rounded.
         *
         * @param c      The component for which this border is being painted.
         * @param g      The graphics context in which to paint.
         * @param x      The x-coordinate of the top-left corner of the border.
         * @param y      The y-coordinate of the top-left corner of the border.
         * @param width  The width of the border area.
         * @param height The height of the border area.
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(209, 209, 209)); // Light gray border
            g2d.setStroke(new BasicStroke(1.0f));

            // Create the shape
            Shape shape;
            if (leftRounded && !rightRounded) {
                // Left rounded, right straight
                shape = new java.awt.geom.RoundRectangle2D.Float(x, y, width + radius, height - 1, radius, radius);
            } else if (!leftRounded && rightRounded) {
                // Right rounded, left straight - fix the width calculation
                shape = new java.awt.geom.RoundRectangle2D.Float(x - radius, y, width + radius - 1, height - 1, radius, radius);
            } else {
                // Fully rounded
                shape = new java.awt.geom.RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            }

            g2d.draw(shape);
            g2d.dispose();
        }

        /**
         * Returns the insets of the border.
         * This method provides the space that the border occupies around the component.
         *
         * @param c The component for which this border insets are being requested.
         * @return Insets object representing the border insets.
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 4, 2, 4);
        }

        /**
         * Checks if the border is opaque.
         * This method indicates whether the border should be painted as opaque.
         *
         * @return true if the border is opaque, false otherwise.
         */
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
