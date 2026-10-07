# Overview

Guided Journal is an Android journaling application designed to help users build a consistent habit of reflection and personal journaling.

The application provides guided prompts based on the time of day, allowing users to reflect on topics such as gratitude, goals, personal growth, relationships, wellness, spirituality, and daily experiences. Instead of requiring users to start with a completely blank page, the application provides questions and ideas that can help them begin writing.

Users can respond to the daily prompt, request additional prompts, browse the complete prompt library, select a prompt they are interested in, and save their responses as journal entries.

The application is designed to run on an Android phone or an Android Studio emulator. To test the application, open the project in Android Studio, allow Gradle to synchronize, select an Android emulator or connected Android device, and run the application. The Guided Journal splash screen will appear first, followed by the main journal screen.

The purpose of creating this software is to improve my skills in Kotlin, Android development, Jetpack Compose, local data persistence, database design, user interface development, and application architecture. The project also provides experience designing an interactive application that manages user-generated data and provides multiple ways for users to interact with that data.

The application uses a local Room database, so journal entries remain available after the application is closed or the device is restarted.

[Software Demo Video](https://youtu.be/F56HPNQD1q8)

The video demonstration will show the application running on an Android emulator or device, 
including the main screen, daily prompts, browsing prompts, selecting a prompt, writing and saving an entry, 
viewing journal history, editing an entry, and deleting an entry. 
The demonstration will also include a walkthrough of the major source-code components.

# Web Pages

Although this project is an Android application rather than a web application, it contains several interactive screens that provide the application's main functionality.

## Splash Screen

The splash screen is displayed when the application starts.

It contains:

- Guided Journal branding
- Application logo
- Application name
- Loading indicator
- Blue application background

After the splash screen finishes, the application transitions to the Home Screen.

## Home Screen

The Home Screen is the main dashboard of the application.

It displays:

- An inspirational daily quote
- Today's date
- The current time of day
- The current guided journal prompt
- Daily journaling progress
- A button for browsing all prompts
- Previous journal days and entries

The current prompt changes based on the time of day, with support for Morning, Afternoon, and Night prompts.

Users can select the displayed prompt to begin writing a journal entry.

Users can also choose to browse the complete prompt library instead of answering the currently displayed prompt.

## Write Entry Screen

The Write Entry Screen allows users to respond to a selected journal prompt.

Users can:

1. Read the selected prompt.
2. Enter a journal response.
3. Request additional prompts.
4. Select another prompt.
5. Save the completed journal entry.

When an entry is saved, the response and information about the selected prompt are stored in the local Room database.

Each saved entry contains information such as:

- Journal response
- Date
- Prompt text
- Prompt category
- Time of day

## Browse Prompts Screen

The Browse Prompts Screen allows users to explore the complete collection of journal prompts.

Prompts are organized by category so that users can easily find questions related to different areas of reflection.

The screen dynamically displays:

- Prompt categories
- Prompt text
- Time of day
- Required or optional status

Categories can be expanded and collapsed.

Users can tap any prompt they are interested in. The application then transitions to the Write Entry Screen with that prompt selected.

This allows users to choose what they want to write about rather than being limited to the automatically selected daily prompt.

## Request More Prompts

The Write Entry Screen also provides a way for users to request additional prompts.

When the user requests more prompts, the application retrieves several randomly selected prompts associated with the current time of day.

The user can then choose one of those prompts and begin writing.

This gives users more variety when they do not want to answer the currently displayed prompt.

## Past Day Screen

The Past Day Screen displays journal entries from a selected previous day.

Users can:

- Read previous journal responses
- View the original prompt
- See the prompt category
- See the time of day associated with the prompt
- Edit recent entries
- Delete entries

Journal entries are grouped by date so that users can easily review their previous journaling activity.

## Edit Entry Screen

The Edit Entry Screen allows users to modify a journal response.

An entry can be edited within 24 hours of being created.

After the 24-hour editing period expires, the entry becomes read-only.

The screen displays the original prompt while allowing the user to update the response.

## Screen Navigation

The application uses a single Android activity with Jetpack Compose.

The application transitions between screens based on the user's actions:

```text
Splash Screen
      |
      v
Home Screen
   |       \
   |        \
   v         v
Write      Browse
Entry      Prompts
   |           |
   |           |
   v           v
Save       Select Prompt
   |           |
   |___________|
         |
         v
    Write Entry

Home Screen
     |
     v
Past Day
     |
     v
Edit Entry
```

# Development Environment

The application was developed using Android Studio and Kotlin.

Tools
Android Studio
Android SDK
Android Emulator
Gradle
Git
GitHub
Jetpack Compose
Android Studio Compose Preview

The application can be tested using either a physical Android device or an Android Studio emulator.

# Programming Language

The application was written in Kotlin.

Kotlin is used for:

Android application code
User interface code
Database entities
Database access
Repository logic
Application navigation
User interaction
Data processing
User Interface

The application uses Jetpack Compose to build the user interface.

Jetpack Compose allows the screens to be created directly using Kotlin instead of traditional XML layout files.

The application uses Material 3 for its design components and follows a consistent blue theme.

# Database

The application uses Room as the database abstraction layer over SQLite.

Room is used to persist:

Journal entries
Journal prompts

The database allows information to remain available when the application is closed or the Android device is restarted.

# Database Processing

The project uses KSP (Kotlin Symbol Processing) for Room code generation.

# Application Architecture

The project uses a repository-based architecture.

The main flow of data is:
````text
Compose UI
|
v
JournalRepository
|
v
JournalDao
|
v
JournalDatabase
|
v
SQLite
````

# Main Source Files
MainActivity.kt

Starts the Android application, creates the database and repository, and displays the Compose application.

Entry.kt

Defines the data model for journal entries.

Prompt.kt

Defines the data model for journal prompts.

JournalDao.kt

Contains the Room database queries used to create, retrieve, update, and delete journal data.

JournalDatabase.kt

Creates and manages the Room database and its database migrations.

JournalRepository.kt

Contains the application's main data and business logic, including:

Daily quote selection

Time-of-day detection

Prompt initialization

Prompt selection

Random additional prompts

Prompt browsing

Journal entry creation

Journal entry retrieval

Journal entry updates

Journal entry deletion

24-hour editing validation

ui/theme/Color.kt

Contains the application's custom blue color definitions.

ui/theme/Theme.kt

Defines the Material 3 theme used throughout the application.

# Useful Websites
The following websites were useful when developing the Guided Journal application.

* [Android Developers](https://developer.android.com/)
* [Android Studio](https://developer.android.com/studio)
* [Build Your First Android App](https://developer.android.com/training/basics/firstapp)
* [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
* [Jetpack Compose Documentation](https://developer.android.com/develop/ui/compose)
* [Jetpack Compose Tutorials](https://developer.android.com/develop/ui/compose/tutorial)
* [Material 3 for Android](https://developer.android.com/develop/ui/compose/designsystems/material3)
* [Room Persistence Library](https://developer.android.com/training/data-storage/room)
* [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
* [Kotlin Android Documentation](https://kotlinlang.org/docs/android-overview.html)
* [Kotlin Symbol Processing](https://kotlinlang.org/docs/ksp-overview.html)
* [Git Documentation](https://git-scm.com/doc)
* [GitHub Documentation](https://docs.github.com/)

# Future Work
There are several features that could be added or improved in future versions of Guided Journal.

Add search functionality for journal entries.

Add the ability to mark prompts as favorites.

Add custom prompts created by the user.

Add a calendar view for browsing journal history.

Add mood tracking and journaling statistics.

Add daily reminder notifications.

Add backup and restore functionality.

Add optional cloud synchronization.

Add export functionality for journal entries.

Add more journal prompts and categories.

Improve prompt rotation to prevent recently answered prompts from appearing again.

Add automated database tests.

Add additional Jetpack Compose UI tests.

Improve application navigation using Jetpack Compose Navigation.

Move more screen state and business logic into ViewModels as the application grows.

Add settings that allow users to customize the journaling experience.

Add additional customization options for the application's theme and journaling experience.
