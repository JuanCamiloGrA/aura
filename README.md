# AURA

**Frictionless time tracking with AI-powered task prediction.**

AURA is an offline-first Android application designed to eliminate the friction of manual time logging. A single tap closes the current activity and opens a new one — atomically, with zero gap. A local SQL heuristic pre-fills the next task title based on your historical patterns at that time of day, so you rarely have to type anything. Every night, a daily closure flow generates a performance summary and queues a background sync.

---

## Contents

- [Motivation](#motivation)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Roadmap](#roadmap)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Samsung Production APK Build Guide](#samsung-production-apk-build-guide)
- [Running Tests](#running-tests)
- [Contributing](#contributing)

---

## Motivation

Traditional time-tracking tools create enough friction that users stop logging midday. The result is "ghost time" — unaccounted hours that make honest self-analysis impossible. AURA enforces two hard constraints:

- **Time to log a new task < 1 second.**
- **100 % offline operation guaranteed** — no crash, no delay due to missing network.

Everything is written to a local Room database first. Remote sync is a background concern, not a blocker.

---

## Architecture

AURA follows a strict **Feature-Sliced Clean Architecture** with full Kotlin Multiplatform compatibility in mind.

```
core/
├── domain/
│   ├── models/          # Pure Kotlin data classes — the Single Source of Truth
│   └── interfaces/      # Abstract contracts for all external dependencies
├── di/                  # Koin module graph
├── events/              # IntentMediator (cross-feature SharedFlow event bus)
├── presentation/        # Root Composable, theme
└── services/            # Framework implementations (Room, WorkManager, etc.)

features/
├── stopwatch/           # Domain / Data / Presentation triad
├── daily_goals/         # Domain / Data / Presentation triad
└── day_closure/         # Domain only (Phase 2 will add data/presentation)
```

### Key rules

| Rule                                                                             | Rationale                                                |
|----------------------------------------------------------------------------------|----------------------------------------------------------|
| No Android or Room imports inside `domain/`                                      | Keeps models portable for KMP migration                  |
| All DB/API/Sensor access through interfaces defined in `core/domain/interfaces/` | Inversion of control, full testability                   |
| External DTOs and `@Entity` classes live only in `data/`                         | Mappers are the sole conversion point                    |
| Business logic lives exclusively in `UseCase` classes                            | Single responsibility, trivially unit-testable           |
| UI state is owned by `ViewModel` via `StateFlow`                                 | Unidirectional data flow, survives configuration changes |

---

## Tech Stack

| Layer                | Technology                                  |
|----------------------|---------------------------------------------|
| Language             | Kotlin                                      |
| UI                   | Jetpack Compose + Material 3                |
| Database             | Room 2 (schema migrations included)         |
| Dependency Injection | Koin                                        |
| Async / Reactive     | Kotlin Coroutines + StateFlow / SharedFlow  |
| Background Work      | WorkManager (`CoroutineWorker`)             |
| Unit Testing         | JUnit 4 · Turbine · Koin Test               |
| Instrumented Testing | Espresso · Compose UI Test · Room In-Memory |
| Coverage             | Jacoco (`jacocoFullReport` Gradle task)     |
| Min SDK              | 29 (Android 10)                             |
| Target / Compile SDK | 36                                          |

---

## Project Structure

```
app/src/main/java/com/humans/aura/
│
├── AuraApplication.kt                 # Koin bootstrap
├── MainActivity.kt                    # Single Activity host
│
├── core/
│   ├── di/                            # 5 Koin modules (Core, DB, Repository, UseCase, ViewModel)
│   ├── domain/
│   │   ├── interfaces/                # ActivityRepository, DailyGoalRepository,
│   │   │                              # IntentMediator, SyncScheduler,
│   │   │                              # TimeProvider, WallpaperController
│   │   └── models/                    # Activity, ActivityStatus, DailyGoal,
│   │                                  # GoalSubtask, GoalSubtaskDraft, AppIntent
│   ├── events/
│   │   └── DefaultIntentMediator.kt
│   ├── presentation/
│   │   ├── AuraApp.kt
│   │   └── theme/
│   └── services/
│       ├── database/                  # AuraDatabase, DAOs, entities, mappers
│       ├── sync/                      # SyncWorker, WorkManagerSyncScheduler
│       ├── time/                      # SystemTimeProvider
│       └── wallpaper/                 # AndroidWallpaperController
│
└── features/
    ├── stopwatch/
    │   ├── domain/                    # 6 use cases
    │   ├── data/                      # RoomActivityRepository, ActivityEntityMapper
    │   └── presentation/              # StopwatchViewModel, StopwatchSection
    ├── daily_goals/
    │   ├── domain/                    # 4 use cases
    │   ├── data/                      # RoomDailyGoalRepository, DailyGoalEntityMapper
    │   └── presentation/              # DailyGoalsViewModel, DailyGoalsSection
    └── day_closure/
        └── domain/                    # HandleSleepIntentUseCase
```

---

## Features

### Atomic Stopwatch (F01)

Tapping **New Activity** executes a single Room `@Transaction` that simultaneously sets the `endTime` of the previous entry and the `startTime` of the new one to the exact same millisecond. There is no gap. The timer is unstoppable by design.

On an OOM kill or forced restart, the active entry is recovered immediately by querying `WHERE end_time IS NULL` — no session state required.

### Local Task Prediction (F02)

Before you type, AURA queries the last 7 days of history for activities logged within ±1 hour of the current time of day. The most frequent match is pre-filled in the input field. Confirm with one tap or type something new.

### Honesty Shortcuts (F03)

Two quick-action buttons — **Inaccurate** and **Lost** — let you mark the current activity without interrupting the flow. The `ActivityStatus` enum (`ACTIVE` · `ACCURATE` · `INACCURATE` · `LOST`) is updated in Room immediately.

### Daily Goals (F04)

Set a **Main Title** and up to three subtasks for the day. A progress counter shows how many subtasks are marked complete. Today's full activity log is displayed alongside the goal for real-time comparison.

### Day Closure (F05)

Logging an activity titled **"Sleep"** triggers the day closure flow:
1. The wallpaper is set to a night mode state.
2. The daily goal is flagged as `isAiGenerationPending = true`.
3. A `SyncWorker` is enqueued via WorkManager for background processing.

The event is routed through the `IntentMediator` event bus — no direct coupling between the stopwatch and the closure logic.

---

## Roadmap

| Milestone           | Status   | Description                                                                |
|---------------------|----------|----------------------------------------------------------------------------|
| **M0 — Foundation** | Complete | Repo, AGENTS.md, folder structure, Room DB, Koin DI                        |
| **M1 — MVP**        | Complete | Functional stopwatch, local prediction, daily goals, Sleep event           |
| **M2 — AI & Sync**  | Planned  | Gemma/Nano on-device inference, Gemini remote fallback, Cloudflare D1 sync |

Every entity already carries an `isSyncedToD1: Boolean` flag. The `AIEngine` interface and `SyncWorker` stub are in place. Phase 2 wires them up.

---

## Prerequisites

- Android Studio Narwhal (2025.1) or later
- JDK 11+
- Android SDK with API 36 platform installed

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/your-org/aura.git
cd aura

# Open in Android Studio and sync Gradle, or build from the command line:
./gradlew assembleDebug
```

Run on a connected device or emulator (API 29+):

```bash
./gradlew installDebug
```

---

## Samsung Production APK Build Guide

This project is now configured with a dedicated `samsungRelease` production flavor optimized for a modern Samsung phone using `arm64-v8a`, release shrinking, and release signing support.

### What is optimized for Samsung

- `arm64-v8a` targeting for modern Samsung flagship and recent mid/high-end devices
- Release minification enabled with R8
- Resource shrinking enabled
- Non-essential dependency metadata excluded from the packaged artifact
- Signing config can be supplied from environment variables, user Gradle properties, or a dedicated untracked `keystore.properties`
- Release variant name: `samsungRelease`

### Prerequisites

Install and prepare the following before building:

- Android Studio Narwhal or later
- JDK 11+
- Android SDK Platform 36
- Android SDK Build-Tools installed from Android Studio SDK Manager
- A physical Samsung device with Developer Options enabled if you want to test the signed APK directly
- A Gemini API key if you want AI features to work in production builds

### Environment variables and secrets

This app reads release-signing values from the following names:

- `AURA_RELEASE_STORE_FILE`
- `AURA_RELEASE_STORE_PASSWORD`
- `AURA_RELEASE_KEY_ALIAS`
- `AURA_RELEASE_KEY_PASSWORD`

The Gemini key is read from:

- `GEMINI_API_KEY`

You can provide these values in any of these places:

1. System environment variables
2. User Gradle properties at `C:/Users/<you>/.gradle/gradle.properties`
3. A dedicated untracked `keystore.properties` file in the project root

Do not use `local.properties` for signing credentials. Android documents `local.properties` as local machine configuration, and Android Studio may regenerate or overwrite it.

### Recommended option for local development — user Gradle properties

For a personal Windows machine, the cleanest option is usually your user Gradle properties file:

```/dev/null/gradle.properties#L1-5
GEMINI_API_KEY=your_gemini_api_key_here
AURA_RELEASE_STORE_FILE=C:/Users/your-user/keystores/aura-release.jks
AURA_RELEASE_STORE_PASSWORD=your_store_password_here
AURA_RELEASE_KEY_ALIAS=aura
AURA_RELEASE_KEY_PASSWORD=your_key_password_here
```

Recommended location on Windows:

- `C:\Users\<you>\.gradle\gradle.properties`

Why this is recommended:

- outside the repository
- not overwritten by Android Studio project sync
- automatically picked up by Gradle
- convenient for local release builds

### Alternative project-local option — `keystore.properties`

Android’s signing documentation commonly recommends a dedicated `keystore.properties` file for signing values. If you prefer that pattern, create an untracked file in the project root:

```/dev/null/keystore.properties#L1-5
GEMINI_API_KEY=your_gemini_api_key_here
AURA_RELEASE_STORE_FILE=C:/Users/your-user/keystores/aura-release.jks
AURA_RELEASE_STORE_PASSWORD=your_store_password_here
AURA_RELEASE_KEY_ALIAS=aura
AURA_RELEASE_KEY_PASSWORD=your_key_password_here
```

If you use this option, keep the file out of version control.

### Option B — configure secrets as environment variables on Windows

PowerShell for the current session:

```/dev/null/powershell.ps1#L1-5
$env:GEMINI_API_KEY="your_gemini_api_key_here"
$env:AURA_RELEASE_STORE_FILE="C:/Users/your-user/keystores/aura-release.jks"
$env:AURA_RELEASE_STORE_PASSWORD="your_store_password_here"
$env:AURA_RELEASE_KEY_ALIAS="aura"
$env:AURA_RELEASE_KEY_PASSWORD="your_key_password_here"
```

Persist them for your user account:

```/dev/null/powershell.ps1#L1-5
[System.Environment]::SetEnvironmentVariable("GEMINI_API_KEY", "your_gemini_api_key_here", "User")
[System.Environment]::SetEnvironmentVariable("AURA_RELEASE_STORE_FILE", "C:/Users/your-user/keystores/aura-release.jks", "User")
[System.Environment]::SetEnvironmentVariable("AURA_RELEASE_STORE_PASSWORD", "your_store_password_here", "User")
[System.Environment]::SetEnvironmentVariable("AURA_RELEASE_KEY_ALIAS", "aura", "User")
[System.Environment]::SetEnvironmentVariable("AURA_RELEASE_KEY_PASSWORD", "your_key_password_here", "User")
```

After setting persistent variables, restart Android Studio so it picks them up.

### Creating a release keystore in Android Studio

If you do not already have a keystore:

1. Open the project in Android Studio.
2. Go to **Build > Generate Signed Bundle / APK**.
3. Choose **APK**.
4. Click **Create new...**
5. Save the keystore in a safe private location such as `C:\Users\<you>\keystores\aura-release.jks`
6. Choose:
   - **Key store password**: strong unique password
   - **Key alias**: `aura`
   - **Key password**: strong unique password
   - **Validity**: 25+ years recommended for personal long-term installs
7. Finish the wizard once to create the file.

Important:
- Keep this keystore backed up securely.
- Do not commit it to Git.
- If you lose it, you lose the identity used to sign future updates of the same installed app.

### Building the production APK in Android Studio

Use the signed production variant:

1. Open **Build Variants**
2. For the `app` module, select:
   - Flavor: `samsung`
   - Build type: `release`
3. Sync the project if Android Studio asks
4. Go to **Build > Generate Signed Bundle / APK**
5. Select **APK**
6. If your signing values are already configured, point Android Studio to the same keystore
7. Choose the `samsungRelease` variant
8. Finish the wizard to build the signed release APK

### Recommended signing options

When Android Studio shows signing options, keep modern signing enabled:

- V1 Signature: enabled
- V2 Signature: enabled
- V3 Signature: enabled
- V4 Signature: enabled

For modern Samsung phones, `V2` and above are especially important. Keeping all four enabled is the safest option for compatibility.

### Output location

Your production Samsung APK will be generated under the app build outputs folder, typically:

```AURA/app/build/outputs/apk/samsung/release#L1-1
AURA/app/build/outputs/apk/samsung/release/
```

Look for a file similar to:

- `app-samsung-release.apk`

### Installing the APK on your Samsung phone

1. Copy the APK to the phone, or use Android Studio device deployment
2. On the phone, allow installation from the source you are using
3. Install the APK
4. If updating an existing install, it must be signed with the same keystore as the installed version

### Important note about AI features in release builds

If `GEMINI_API_KEY` is not configured, the app can still build, but Gemini-backed features will not work correctly in production.

For a personal non-Play-Store APK, this is acceptable if you only want local/offline flows. If you want the full feature set, configure the key before building.

### Recommended release workflow

For your current use case, the clean path is:

1. Create one long-lived personal release keystore
2. Store signing values in user `gradle.properties`, or use environment variables if you prefer
3. Optionally use an untracked `keystore.properties` file if you want a project-local signing config file
4. Build `samsungRelease`
5. Install and test on your Samsung device
6. Keep the same keystore for every future update so Android accepts upgrades

### Notes for non-Play-Store distribution

Since you are not publishing to Play Store right now:

- APK output is the correct target for direct installation
- You do not need Play App Signing
- You should still keep your signing key stable for future upgrade installs
- You should still test the release build, not only debug, because release shrinking is enabled

## Running Tests

**Unit tests**

```bash
./gradlew testDebugUnitTest
```

**Instrumented tests** (requires a connected device or emulator)

```bash
./gradlew connectedDebugAndroidTest
```

**Full coverage report** (unit + instrumented, output at `app/build/reports/jacoco/`)

```bash
./gradlew jacocoFullReport
```

---

## Contributing

This project enforces the rules defined in [`AGENTS.md`](./AGENTS.md). All pull requests are expected to:

- Maintain zero Android/Room imports in the `domain/` layer.
- Provide unit tests for every new use case, mapper, and repository.
- Provide Compose UI tests for every new screen or significant UI change.
- Pass `./gradlew testDebugUnitTest` without failure before submission.
- Use atomic, conventional commit messages.
