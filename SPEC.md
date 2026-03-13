### 1. Vision and "Core Value"

* **Problem:** Traditional time tracking generates too much friction, leading users to abandon logging and leaving them with unaccounted "ghost time" and without an objective analysis of why they fail to meet their daily goals.
* **Value Proposition:** An unstoppable and frictionless timer that predicts your tasks using AI, forcing you to be accountable in real time and acting as a relentless coach that analyzes your performance every night.
* **Success Metric (MVP KPI):** * Time to log a new task < 1 second.
* 100% offline operation guaranteed (0 crashes due to lack of network).



---

### 2. Functional Definition (User Stories - MVP)

| ID | User Story | Acceptance Criteria (Definition of Done) | Priority |
| --- | --- | --- | --- |
| **F01** | As a User, I want to press "New Activity" so that the current activity closes and a new one starts instantly. | 1. The DB executes an atomic transaction (closes previous, opens new). 2. The `endTime` of the previous is identical to the `startTime` of the new. 3. The UI updates reactively without reloading. | **Critical** |
| **F02** | As a User, I want to see an automatic prediction of the name of my next task so I don't have to type it. | 1. The system queries Room for tasks at the same time on past days. 2. The UI shows the suggested title pre-filled. 3. I can confirm with a single touch or override. | High |
| **F03** | As a User, I want to quickly mark a task as "Inaccurate" or "Lost" to maintain the honesty of the log. | 1. There are quick status buttons in the stopwatch UI. 2. The `ActivityStatus` enum is updated in Room. | High |
| **F04** | As a User, I want to set a "Main Title" and subtasks for the current day. | 1. A record is created in `DailyGoalEntity`. 2. Subtasks are associated via `goalId`. 3. The UI shows completion progress. | Medium |
| **F05** | As a System, I want to detect the text input "Sleep" to start the day closure flow. | 1. The keyword or intent is detected in the input. 2. The Wallpaper is updated to black/night. 3. A `SyncWorker` is queued with `is_ai_generation_pending = true`. | High |

---

### 3. Technical Architecture (The "How" of the MVP)

* **Technological Stack:**
* **Frontend:** Native Kotlin with Jetpack Compose (functional UI, no complex animations in the MVP).
* **Backend (Local):** Room Database (prepared for KMP).
* **State Management:** Standard Android `StateFlow` and `ViewModel`.
* **Events:** Custom `IntentMediator` in the Core layer.


* **Estrategia Offline-First & Sync:**
* All data logging occurs **exclusively** against Room.
* Cada entidad tiene el flag `isSyncedToD1 = false`.
* (The remote backend and asynchronous synchronization will be built in Phase 2, but the database is already prepared).


* **AI Infrastructure (MVP):**
* Task Prediction: Based purely on local SQL heuristics (queries by time range from the last 7 days).
* Gemma/Nano and remote Gemini are prepared in the interfaces (`domain/interfaces/AIEngine`), but will be formally implemented in Phase 2.



---

### 4. Non-Functional Requirements (ISO 25010)

* **Performance:** The atomic transactions of the timer in Room must complete in < 50ms to ensure the "unstoppable" feel.
* **Availability (Resilience):** If the app is destroyed by the operating system (OOM kill), upon reopening it must instantly recover the current activity by querying the record where `endTime IS NULL`.
* **Maintainability:** Strict compliance with the `AGENTS.md` file. Pull Requests (or AI-generated commits) that mix Android/Room imports in the `domain/` layer will not be accepted.

---

### 5. Implementation Roadmap

#### Milestone 0 (Foundation) - *Preparing the Ground*

1. Initialize the Android Studio repository (Kotlin).
2. Create the `AGENTS.md` file in the root with the architecture rules.
3. Establish the folder structure `core/` and `features/` (Feature-Sliced Clean Architecture).
4. Configure Room Database in `core/services/database/` with the tables `ActivityEntity`, `DailyGoalEntity`, etc.
5. Configure Dependency Injection (Koin or Hilt) to decouple the layers.

#### Milestone 1 (MVP - Happy Path) - *The Functional Stopwatch*

1. **Feature `stopwatch` (Domain and Data):** Implement the DAO, the Mappers, and the `LogNewActivityUseCase`.
2. **Feature `stopwatch` (UI):** Create the main Compose screen with the running timer, the text field, and the giant button.
3. **Local Prediction:** Implement the SQL logic to search the history and auto-complete the text field.
4. **Feature `daily_goals`:** Implement the creation of the day's "Main Title" and the visualization of the past activities list.
5. **The "Sleep" Event:** Create the base `IntentMediator` and emit the Wallpaper change event when the sleep activity is logged.
