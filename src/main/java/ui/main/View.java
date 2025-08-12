/**
 * View.java
 * This class represents the main user interface for the G2iCal application.
 * It handles the display of calendars, event data, and user interactions.
 * The view is designed to be responsive and user-friendly.
 */

package ui.main;

import calendar.CalendarEvent;
import com.toedter.calendar.JDateChooser;

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
        setPreferredSize(new Dimension(900, 700)); // default size
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
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 10, 0));

        // Form label at the top
        JLabel calendarLabel = new JLabel("Select a calendar and date range for the export:");
        formPanel.add(calendarLabel, BorderLayout.NORTH);

        // Create responsive controls panel
        JPanel controlsPanel = new ResponsiveFormPanel();
        formPanel.add(controlsPanel, BorderLayout.CENTER);

        return formPanel;
    }

    /**
     * Custom panel that layouts form controls responsively based on available width
     */
    private class ResponsiveFormPanel extends JPanel {
        private static final int MIN_WIDTH_FOR_SINGLE_ROW = 800; // Threshold for single-row layout
        private JPanel calendarPanel;
        private JPanel datePanel;
        private JPanel buttonPanel;
        private boolean isInitialized = false;

        public ResponsiveFormPanel() {
            createComponents();
            // Start with the two-row layout as default
            layoutAsTwoRows();
        }

        private void createComponents() {
            // Calendar dropdown panel
            calendarPanel = new JPanel(new BorderLayout());
            calendarDropdown = new JComboBox<>();
            calendarPanel.add(calendarDropdown, BorderLayout.CENTER);

            // Date controls panel
            datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

            JLabel fromLabel = new JLabel("Start:");
            startDateChooser = createStyledDateChooser();
            JLabel toLabel = new JLabel("End:");
            endDateChooser = createStyledDateChooser();

            datePanel.add(fromLabel);
            datePanel.add(startDateChooser);
            datePanel.add(toLabel);
            datePanel.add(endDateChooser);

            // Load button panel
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            loadDataButton = new JButton("Load Data");
            loadDataButton.setBackground(LIGHT_BLUE);
            loadDataButton.addActionListener(new LoadDataActionListener());
            buttonPanel.add(loadDataButton);

            // Set default dates (from 30 days ago to today)
            Calendar cal = Calendar.getInstance();
            endDateChooser.setDate(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDateChooser.setDate(cal.getTime());

            isInitialized = true;
        }

        private void layoutAsSingleRow() {
            removeAll();
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Calendar dropdown
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.4;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(calendarPanel, gbc);

            // Date controls
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.4;
            gbc.fill = GridBagConstraints.NONE;
            add(datePanel, gbc);

            // Load button
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.weightx = 0.2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.EAST;
            add(buttonPanel, gbc);
        }

        private void layoutAsTwoRows() {
            removeAll();
            setLayout(new BorderLayout(5, 5));

            // First row: Calendar dropdown
            add(calendarPanel, BorderLayout.NORTH);

            // Second row: Date controls and button
            JPanel bottomRow = new JPanel(new BorderLayout());
            bottomRow.add(datePanel, BorderLayout.WEST);
            bottomRow.add(buttonPanel, BorderLayout.EAST);
            add(bottomRow, BorderLayout.CENTER);
        }

        private void updateLayout() {
            if (!isInitialized) return;

            int currentWidth = getWidth();
            if (currentWidth >= MIN_WIDTH_FOR_SINGLE_ROW) {
                layoutAsSingleRow();
            } else {
                layoutAsTwoRows();
            }

            revalidate();
            repaint();
        }

        @Override
        public void doLayout() {
            super.doLayout();
            // Only update layout after the panel has been properly sized
            SwingUtilities.invokeLater(this::updateLayout);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            // Only update layout after the panel has been properly sized
            SwingUtilities.invokeLater(this::updateLayout);
        }
    }

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
                textField.setBorder(new CleanRoundedBorder(8, true, false)); // Left side rounded
                calendarButton.setBorder(new CleanRoundedBorder(8, false, true)); // Right side rounded

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
    public void updateEventTable(CalendarEvent[][] events) {
        tableModel.setRowCount(0); // Clear existing data
        for (CalendarEvent[] event : events) {
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
        JPanel locationPanel = new JPanel(new BorderLayout(5, 0));
        JTextField locationField = new JTextField(filePath, 20);
        locationField.setEditable(false);
        JButton browseButton = new JButton("Browse...");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Export Directory");
            fileChooser.setCurrentDirectory(new java.io.File(locationField.getText()));

            if (fileChooser.showOpenDialog(settingsDialog) == JFileChooser.APPROVE_OPTION) {
                locationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        locationPanel.add(locationField, BorderLayout.CENTER);
        locationPanel.add(browseButton, BorderLayout.EAST);
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

            // Validate inputs
            if (newFileName.isEmpty()) {
                JOptionPane.showMessageDialog(settingsDialog, "File name cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
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
        private final int radius;
        private final boolean leftRounded;
        private final boolean rightRounded;

        public CleanRoundedBorder(int radius, boolean leftRounded, boolean rightRounded) {
            this.radius = radius;
            this.leftRounded = leftRounded;
            this.rightRounded = rightRounded;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(160, 160, 160)); // Light gray border
            g2d.setStroke(new BasicStroke(1.0f));

            // Create the shape
            Shape shape;
            if (leftRounded && !rightRounded) {
                // Left rounded, right straight
                shape = new java.awt.geom.RoundRectangle2D.Float(x, y, width + radius, height - 1, radius, radius);
            } else if (!leftRounded && rightRounded) {
                // Right rounded, left straight
                shape = new java.awt.geom.RoundRectangle2D.Float(x - radius, y, width + radius, height - 1, radius, radius);
            } else {
                // Fully rounded
                shape = new java.awt.geom.RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            }

            g2d.draw(shape);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 4, 2, 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
