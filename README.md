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
- [Running Tests](#running-tests)
- [Contributing](#contributing)

---

## Motivation

Traditional time-tracking tools create enough friction that users stop logging mid-day. The result is "ghost time" — unaccounted hours that make honest self-analysis impossible. AURA enforces two hard constraints:

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

| Rule | Rationale |
|---|---|
| No Android or Room imports inside `domain/` | Keeps models portable for KMP migration |
| All DB/API/Sensor access through interfaces defined in `core/domain/interfaces/` | Inversion of control, full testability |
| External DTOs and `@Entity` classes live only in `data/` | Mappers are the sole conversion point |
| Business logic lives exclusively in `UseCase` classes | Single responsibility, trivially unit-testable |
| UI state is owned by `ViewModel` via `StateFlow` | Unidirectional data flow, survives configuration changes |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Database | Room 2 (schema migrations included) |
| Dependency Injection | Koin |
| Async / Reactive | Kotlin Coroutines + StateFlow / SharedFlow |
| Background Work | WorkManager (`CoroutineWorker`) |
| Unit Testing | JUnit 4 · Turbine · Koin Test |
| Instrumented Testing | Espresso · Compose UI Test · Room In-Memory |
| Coverage | Jacoco (`jacocoFullReport` Gradle task) |
| Min SDK | 29 (Android 10) |
| Target / Compile SDK | 36 |

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

| Milestone | Status | Description |
|---|---|---|
| **M0 — Foundation** | Complete | Repo, AGENTS.md, folder structure, Room DB, Koin DI |
| **M1 — MVP** | Complete | Functional stopwatch, local prediction, daily goals, Sleep event |
| **M2 — AI & Sync** | Planned | Gemma/Nano on-device inference, Gemini remote fallback, Cloudflare D1 sync |

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
