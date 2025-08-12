package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Settings class to manage application settings with JSON persistence.
 */
public class Settings {
    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String DEFAULT_FILE_NAME = "calendar_export.ics";
    private static final String DEFAULT_FILE_PATH = System.getProperty("user.home") + File.separator + "Downloads";

    // Settings fields
    private String fileName;
    private String filePath;

    // Singleton instance
    private static Settings instance;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Private constructor for singleton pattern.
     */
    private Settings() {
        // Initialize with defaults
        this.fileName = DEFAULT_FILE_NAME;
        this.filePath = DEFAULT_FILE_PATH;
    }

    /**
     * Get the singleton instance of Settings.
     * @return Settings instance
     */
    public static Settings getInstance() {
        if (instance == null) {
            instance = loadSettings();
        }
        return instance;
    }

    /**
     * Load settings from JSON file or create default settings if file doesn't exist.
     * @return Settings instance
     */
    private static Settings loadSettings() {
        Path settingsPath = getSettingsFilePath();

        if (Files.exists(settingsPath)) {
            try {
                String json = Files.readString(settingsPath);
                Settings loadedSettings = gson.fromJson(json, Settings.class);

                // Validate loaded settings
                if (loadedSettings.fileName == null || loadedSettings.fileName.trim().isEmpty()) {
                    loadedSettings.fileName = DEFAULT_FILE_NAME;
                }
                if (loadedSettings.filePath == null || loadedSettings.filePath.trim().isEmpty()) {
                    loadedSettings.filePath = DEFAULT_FILE_PATH;
                }

                return loadedSettings;
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("Error loading settings, using defaults: " + e.getMessage());
                return new Settings();
            }
        } else {
            // Create new settings with defaults
            Settings newSettings = new Settings();
            newSettings.saveSettings();
            return newSettings;
        }
    }

    /**
     * Save current settings to JSON file.
     */
    public void saveSettings() {
        try {
            Path settingsPath = getSettingsFilePath();

            // Ensure the parent directory exists
            Files.createDirectories(settingsPath.getParent());

            String json = gson.toJson(this);
            Files.writeString(settingsPath, json);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    /**
     * Get the path to the settings file in the resources folder.
     * @return Path to settings file
     */
    private static Path getSettingsFilePath() {
        try {
            // Get the resources directory path
            String resourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources";
            return Paths.get(resourcesPath, SETTINGS_FILE_NAME);
        } catch (Exception e) {
            // Fallback to current directory if resources path fails
            System.err.println("Could not access resources folder, using current directory: " + e.getMessage());
            return Paths.get(SETTINGS_FILE_NAME);
        }
    }

    /**
     * Update settings with new values.
     * @param fileName New file name
     * @param filePath New file path
     */
    public void updateSettings(String fileName, String filePath) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            this.fileName = fileName.trim();
        }
        if (filePath != null && !filePath.trim().isEmpty()) {
            this.filePath = filePath.trim();
        }
        saveSettings();
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    // Setters
    public void setFileName(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            this.fileName = fileName.trim();
        }
    }

    public void setFilePath(String filePath) {
        if (filePath != null && !filePath.trim().isEmpty()) {
            this.filePath = filePath.trim();
        }
    }

    /**
     * Reset settings to defaults.
     */
    public void resetToDefaults() {
        this.fileName = DEFAULT_FILE_NAME;
        this.filePath = DEFAULT_FILE_PATH;
        saveSettings();
    }

    /**
     * Get the full path to the export file.
     * @return Full path including file name
     */
    public String getFullExportPath() {
        return Paths.get(filePath, fileName).toString();
    }
}
