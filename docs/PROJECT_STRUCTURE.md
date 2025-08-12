# G2iCal - Project Structure Documentation

## Project Overview
A Java application that exports Google Calendar events to iCal format (.ics files) using object-oriented design principles and patterns. Built with Gradle as the build system.

## Architecture Overview

```
google_calendar_iCal/
├── build.gradle                     # Gradle build configuration
├── settings.gradle.kts              # Gradle settings
├── gradlew                          # Gradle wrapper script (Unix)
├── gradlew.bat                      # Gradle wrapper script (Windows)
├── PROJECT_STRUCTURE.md             # This documentation file
├── test.ics                         # Sample iCal output file
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── G2iCal.java          # Command-line interface
│   │   │   ├── calendar/            # Domain objects package
│   │   │   │   ├── CalendarEvent.java    # Event interface (abstraction)
│   │   │   │   ├── ICal.java             # Main iCal container class
│   │   │   │   ├── MyEvent.java          # Concrete event implementation
│   │   │   │   └── MyEventBuilder.java   # Builder pattern for event creation
│   │   │   ├── exceptions/          # Custom exception handling
│   │   │   │   └── ICalExportException.java # Export-specific exceptions
│   │   │   └── utils/               # Utility classes package
│   │   │       ├── CalendarApiConnector.java # Google Calendar API integration
│   │   │       ├── EventConverter.java      # Event conversion logic
│   │   │       ├── EventFactory.java       # Factory pattern for event creation
│   │   │       └── InputValidator.java     # Input validation utilities
│   │   └── resources/
│   │       └── credentials.json     # Google API credentials
│   └── test/
│       ├── java/                    # Test source files
│       └── resources/               # Test resources
├── build/                           # Gradle build output directory
│   ├── classes/
│   │   └── java/
│   │       └── main/                # Compiled Java classes
│   ├── generated/                   # Generated source files
│   ├── reports/                     # Build reports
│   ├── resources/
│   │   └── main/                    # Processed resources
│   └── tmp/                         # Temporary build files
├── gradle/
│   └── wrapper/                     # Gradle wrapper files
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── tokens/                          # Token storage for Google API
    └── StoredCredential
```

## Build System
This project uses **Gradle** as the build system, providing:
- Dependency management
- Automated building and testing
- Cross-platform build scripts (gradlew/gradlew.bat)
- Integration with modern Java development workflows

## Object-Oriented Design Patterns Implemented

### 1. Builder Pattern
- **Class**: `MyEventBuilder`
- **Purpose**: Provides a fluent interface for constructing complex `MyEvent` objects
- **Benefits**: Improves readability and handles optional parameters elegantly

### 2. Factory Pattern
- **Class**: `EventFactory`
- **Purpose**: Creates appropriate event objects based on input parameters
- **Benefits**: Encapsulates object creation logic and provides flexibility

### 3. Interface Segregation
- **Interface**: `CalendarEvent`
- **Implementation**: `MyEvent`
- **Purpose**: Defines contract for calendar events, allowing for future extensions

## Package Structure

### calendar/
Contains the core domain objects representing calendar events and the iCal container.

### exceptions/
Houses custom exception classes for handling export-specific errors and validation failures.

### utils/
Utility classes that provide supporting functionality:
- API connectivity
- Data conversion
- Input validation
- Object creation

## Key Features
- Google Calendar API integration
- iCal format export (.ics files)
- Input validation and error handling
- Modular, extensible design
- Gradle build automation
