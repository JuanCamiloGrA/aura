# Scope & Definition of Done — MVP V1.0

All features listed below must be implemented, unit-tested, and verified to operate fully offline on an Android device before the build is considered release-ready.

---

## 1. `features/stopwatch` — Core Timer

* [x] **Main UI:** Single-screen layout with a prominent text input, a "New Activity" button, and a scrollable log of the current day's activities beneath it.
* [x] **Atomic Transition:** On "New Activity", the system records a single timestamp $T$ in one DB transaction — the previous activity's `endTime` and the new activity's `startTime` are both set to $T$. No time gaps permitted.
* [x] **Crash Recovery:** On cold start, the app queries for the active record (`endTime IS NULL`), derives elapsed time from `startTime` to `now()`, and resumes the running display without user intervention.
* [x] **Activity Status Labeling:** Each activity supports inline status assignment: `Accurate`, `Inaccurate`, or `Lost`, stored as an `ActivityStatus` enum in Room.

---

## 2. `features/prediction` — Predictive Input

* [x] **Time-Slot Heuristic:** On activity creation, query Room for activity names logged within ±30 minutes of the current time across the past 7 days. Select the highest-frequency result as the candidate.
* [x] **Pre-filled Input:** The "New Activity" text field is pre-filled with the candidate name. Submitting without modification saves the activity under that name.
* [x] **Overwrite Behavior:** Any keystroke clears the suggestion and switches to free-text input.

---

## 3. `features/daily_goals` — Daily Focus

* [x] **Main Title:** Dedicated UI input to set a single "Main Title" for the day, persisted to `DailyGoalEntity`.
* [x] **Subtask List:** Support for adding and checking off subtasks linked to the active `DailyGoalEntity` via `goalId`.
* [x] **Wallpaper Integration:** On Main Title creation or update, invoke `WallpaperManager` to update the device lock screen wallpaper to reflect the active work state (solid color, text overlay, or equivalent).

---

## 4. `features/ai_coach` — End-of-Day Review

* [x] **Sleep Trigger:** Activity names matching "Sleep" (or equivalent intent classification) dispatch the `INTENT_SLEEP` event via `EventDispatcher`. _(Implemented as `AppIntent.SleepLogged` emitted via `IntentMediator`.)_
* [x] **Night Mode Reaction:** On `INTENT_SLEEP`, immediately set the wallpaper to pure black or a predefined night image.
* [x] **Context Packaging:** A `WorkManager` background task collects all of the day's activities, their status labels, and the Main Title, serialized as a structured JSON payload.
* [x] **Gemini API Call:** The JSON payload is sent to the Gemini API with a fixed system prompt. The response is expected to contain: Wins, Friction Points, and Tomorrow Pivot.
* [x] **Offline-First Persistence:** The `DailySummaryEntity` is written immediately. If the API call fails due to no connectivity, the record is flagged with `is_ai_generation_pending = true` and retried silently on the next available connection. _(Implemented via `SummaryGenerationStatus.PENDING` + `Result.retry()` in `SyncWorker`.)_
* [x] **Summary Screen:** A read-only screen, accessible from the app menu, displaying the most recent AI-generated daily summary.

---

## 5. Architectural Requirements

* [x] **AGENTS.md Compliance:** Feature-Sliced Clean Architecture enforced as defined in `AGENTS.md`. No Android or Room imports permitted in any `domain/` layer.
* [x] **KMP-Ready Schema:** Room Database with tables `activities`, `daily_goals`, `goal_subtasks`, and `daily_summaries`, each including the `isSyncedToD1` sync flag for future remote backend compatibility. _(Resolved by aligning DOE with the existing Room schema and `SPEC.md`; renaming the shipped subtask table for MVP would add unnecessary migration risk without functional benefit.)_
* [x] **Event Bus:** A singleton `EventDispatcher` implemented with Kotlin `SharedFlow`, providing fully decoupled inter-module communication. _(Implemented as `DefaultIntentMediator` — a `single<IntentMediator>` backed by `MutableSharedFlow<AppIntent>`.)_
