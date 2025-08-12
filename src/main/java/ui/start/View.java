/**
 * startView.java
 * This class represents the initial view of the G2iCal application.
 * It provides a user interface for logging in with Google and displays
 * an image and description of the application.
 */

package ui.start;

import javax.swing.*;
import java.awt.*;

public class View extends JFrame {

    private final Controller controller; // Reference to the controller

    // UI components that need to be accessed in multiple methods
    private JLabel clearCacheLabel; // Add instance variable for clear cache label
    private JDialog loginDialog; // instance variable for the dialog

    /**
     * Constructor for the start view.
     * Initializes the user interface and sets up the layout.
     * Package-private to restrict access to the package.
     *
     * @param controller The controller that handles user interactions.
     */
    View(Controller controller) {
        this.controller = controller;
        initializeUI();
    }

    /**
     * Initializes the user interface components and layout.
     * Sets up the main panel, title, image area, login button, and description.
     * This method is called in the constructor to set up the initial view.
     */
    private void initializeUI() {
        // Program name and basic settings for the JFrame
        setTitle("G2iCal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);

        // Main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Title
        JLabel titleLabel = new JLabel("G2iCal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Image 
        JPanel imagePanel = new JPanel(); // Create a panel for the image
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setPreferredSize(new Dimension(350, 200));
        imagePanel.setMaximumSize(new Dimension(350, 200));
        imagePanel.setMinimumSize(new Dimension(350, 200));
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        try { // Load image
            ImageIcon icon = new ImageIcon("src/main/resources/UI/calendar_welcome.png");
            if (icon.getIconWidth() > 0) {
                // Calculate scaled dimensions while preserving aspect ratio
                int originalWidth = icon.getIconWidth();
                int originalHeight = icon.getIconHeight();
                int maxWidth = 300;
                int maxHeight = 180;

                // Calculate scale factor to fit within bounds while preserving ratio
                double scaleX = (double) maxWidth / originalWidth;
                double scaleY = (double) maxHeight / originalHeight;
                double scale = Math.min(scaleX, scaleY);

                int scaledWidth = (int) (originalWidth * scale);
                int scaledHeight = (int) (originalHeight * scale);

                // Scale the image with preserved aspect ratio
                Image img = icon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        imagePanel.setLayout(new BorderLayout()); // Use BorderLayout to center the image
        imagePanel.add(imageLabel, BorderLayout.CENTER); // Add the image label to the center of the panel


        // Login button with Google icon
        JButton loginButton = new JButton("Login with Google");
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setMaximumSize(new Dimension(200, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        try { // Load Google icon for the button
            ImageIcon googleIcon = new ImageIcon("src/main/resources/UI/google_icon.png");
            if (googleIcon.getIconWidth() > 0) {
                // Scale the icon to fit the button
                Image img = googleIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                loginButton.setIcon(new ImageIcon(img));
                loginButton.setHorizontalTextPosition(SwingConstants.RIGHT);
                loginButton.setIconTextGap(8);
            }
        } catch (Exception e) {
            System.out.println("Failed to load Google icon: " + e.getMessage());
        }
        // Action listener for the login button
        loginButton.addActionListener(e -> {
            // Call the method to start the login process in the controller
            controller.startLoginProcess();
        });

        // Clear cache label
        clearCacheLabel = new JLabel("Clear Cache");
        clearCacheLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        clearCacheLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearCacheLabel.setHorizontalAlignment(SwingConstants.CENTER);
        clearCacheLabel.setForeground(Color.lightGray);
        clearCacheLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        clearCacheLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                controller.clearCache();
            }
        });

        // Description text
        JLabel descriptionLabel = new JLabel("<html><center>G2iCal is a simple application for exporting your google<br>calendar to a ical file.</center></html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setForeground(Color.lightGray);

        // Add components with spacing
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(50)); // Spacing
        mainPanel.add(imagePanel);
        mainPanel.add(Box.createVerticalGlue()); // Push to bottom
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(10)); // Spacing
        mainPanel.add(clearCacheLabel);
        mainPanel.add(Box.createVerticalStrut(90)); // Spacing
        mainPanel.add(descriptionLabel);

        // Add the main panel to the frame
        add(mainPanel);
        setVisible(true);
    }

    /**
     * Displays a login prompt dialog with a link to Google login.
     * It is package-private to restrict access to the package.
     *
     * @param link The URL to the Google login page.
     */
    void showLoginPrompt(String link) {
        // Create a custom dialog with better formatting
        loginDialog = new JDialog(this, "Google Login", false);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setSize(400, 200);
        loginDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Center panel for all content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        // Main message
        JLabel titleLabel = new JLabel("Login with your Google credentials");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Instruction message
        JLabel instructionLabel = new JLabel("<html><center>The login page should open automatically in your browser.</center></html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Fallback link if the browser does not open
        JLabel secInstructionLabel = new JLabel("<html><center>If not, please click the link below:</center></html>");
        secInstructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        secInstructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        secInstructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        secInstructionLabel.setForeground(Color.lightGray);


        // Clickable link - properly centered
        JLabel linkLabel = getJLabel(link);

        // Add components to center panel with spacing
        centerPanel.add(Box.createVerticalGlue()); // Push content to the center
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(15)); // Spacing
        centerPanel.add(instructionLabel);
        centerPanel.add(Box.createVerticalStrut(10)); // Spacing
        centerPanel.add(secInstructionLabel);
        centerPanel.add(linkLabel);
        centerPanel.add(Box.createVerticalGlue()); // Push content to the center

        panel.add(centerPanel, BorderLayout.CENTER);
        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }

    /**
     * Creates a JLabel with a clickable link that opens the specified URL in the default browser.
     *
     * @param link The URL to open when the label is clicked.
     * @return A JLabel with the clickable link.
     */
    private JLabel getJLabel(String link) {
        JLabel linkLabel = new JLabel("<html><center><a href='#' style='color: #C0C0C0; text-decoration: underline;'>open login manually</a></center></html>");
        linkLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        linkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.setForeground(Color.lightGray);

        // Add mouse listener to open link
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(link));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(loginDialog, "Could not open browser. Please copy this link:\n" + link, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return linkLabel;
    }

    /**
     * Show clear cache success.
     * This method updates the clear cache label to indicate success,
     * disables clicking on it, and re-enables it after a delay.
     * It also changes the label color to indicate success.
     * After 5 seconds, it reverts the label color back to light gray
     * and re-enables clicking.
     * It is package-private to restrict access to the package.
     */
    void showClearCacheSuccess() {
        clearCacheLabel.setForeground(new Color(21, 176, 62));
        clearCacheLabel.setText("Cache cleared successfully!");

        // Disable clicking by removing the mouse listener and changing cursor
        clearCacheLabel.removeMouseListener(clearCacheLabel.getMouseListeners()[0]);
        clearCacheLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        // Create a timer to revert the color back to light gray after 10 seconds
        Timer timer = new Timer(5000, e -> {
            clearCacheLabel.setForeground(Color.lightGray);

            // Re-enable clicking by adding the mouse listener back and changing cursor
            clearCacheLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    controller.clearCache();
                }
            });
            clearCacheLabel.setText("Clear Cache");
            clearCacheLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        });
        timer.setRepeats(false); // Only execute once
        timer.start();
    }


    /**
     * Show error dialog with a message.
     * This method displays a dialog with the specified error message.
     * It is used to inform the user about errors that occur during the application
     * login process or other operations.
     * It is package-private to restrict access to the package.
     *
     * @param message The error message to display.
     */
    void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Get the login dialog.
     * This method returns the current login dialog instance.
     * It is package-private to restrict access to the package.
     *
     * @return The current login dialog instance, or null if it does not exist.
     */
    JDialog getLoginDialog() {
        return loginDialog;
    }
}


