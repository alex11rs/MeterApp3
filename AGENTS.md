# Project Warning

Do not run Gradle compilation, sync, clean, wrapper updates, or cache-clearing commands unless the user explicitly asks for it in the current session.

Forbidden without explicit user approval:
- `gradlew`
- `gradlew.bat`
- Android Studio Gradle sync or rebuild
- deleting `.gradle*`, `build/`, `.cxx/`, `.externalNativeBuild/`
- changing Gradle wrapper files or Gradle version

This project has previously been destabilized by unsolicited Gradle actions.

If build verification seems necessary, stop and ask the user first.

# Project Overview

MeterApp3 is an Android application for tracking utility meter readings (electricity, water, etc.), supporting multiple apartments, tariffs, and data export. Built with Kotlin and Jetpack Compose.

## Architecture

- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM with ViewModels and Repository pattern
- **Data Storage**: Room database for local persistence
- **Camera & OCR**: CameraX for camera access, Google Cloud Vision for the active scanner flow
- **Background Tasks**: WorkManager for reminders
- **Networking**: OkHttp for HTTP requests

Key components:
- `MainActivity.kt`: Main entry point with screen navigation
- `MeterRepository.kt`: Data access layer
- `MainScreenViewModel.kt`: Business logic for main screen
- `MeterDatabase.kt` / `MeterDao.kt`: Room database layer
- `ScannerViewModel.kt` / `CloudScannerRecognizer.kt`: scanner orchestration and cloud OCR pipeline
- Screens and ViewModels are split between root package files and `ui/*` feature folders (`main`, `reading`, `details`, `history`, `summary`, `scanner`, `apartment`, `settings`)

## Project Structure

- `app/src/main/java/ru/pepega/meterapp3/`: Main source code
  - `data/`: Data models and database entities
  - `ui/main`, `ui/reading`, `ui/details`, `ui/history`, `ui/summary`, `ui/scanner`, `ui/apartment`, `ui/settings`: feature-specific Compose UI and ViewModels
  - `ui/`: UI components and screens
  - `scanner/`: Camera frame processing, cloud recognition helpers, and scanner utilities
  - `reminders/`: Notification and reminder system
  - `sharing/`: Data export features
  - `theme/`: Theming and colors
  - `backup/`: Backup utilities

## Coding Conventions

- **Language**: Kotlin
- **Package**: `ru.pepega.meterapp3`
- **Commit Messages**: Write in Russian
- **UI**: Use Jetpack Compose for all new UI components
- **State Management**: Use Compose state and ViewModels
- **Database**: Use Room for data persistence

## Restrictions

- Do not reintroduce a separate local scanner mode unless explicitly requested
- Treat scanner flow as cloud-first: current UI/settings expose only Google Cloud Vision recognition
- Do not hardcode scanner API keys in source files; scanner key must come from user input/import inside app settings
- Legacy scanner naming may still remain in some state/session helpers; do not assume those names mean there is an active local OCR mode
- Avoid unsolicited Gradle builds or syncs (see warning above)

## Dependencies

Key libraries (from `app/build.gradle.kts`):
- Compose BOM: `androidx.compose:compose-bom:2025.01.01`
- CameraX: `androidx.camera:*` version 1.5.0
- Google Cloud Vision requests are sent via OkHttp
- Room: `androidx.room:*` version 2.6.1
- OkHttp: `com.squareup.okhttp3:okhttp:4.12.0`

Build plugins (from `build.gradle.kts`):
- Android Gradle Plugin: `com.android.application` version 9.1.1
- Built-in Kotlin in AGP 9.1 is enabled; `org.jetbrains.kotlin.android` is no longer applied explicitly
- Kotlin Compose Plugin: `org.jetbrains.kotlin.plugin.compose` version 2.2.10
- KSP Plugin: `com.google.devtools.ksp` version 2.3.2

## Gradle Notes

- The project currently builds with AGP built-in Kotlin and Compose plugin.
- `gradle.properties` intentionally contains `android.disallowKotlinSourceSets=false` as a compatibility flag for the current KSP setup.
- Do not remove that flag blindly unless KSP handling of generated sources has been verified on the current AGP/Kotlin toolchain.
