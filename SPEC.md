## AURA Product Specification

### Product Direction

AURA is an offline-first personal operating system for time awareness, reflection, and adaptive planning.
The app must remain usable without network connectivity, preserve a strict uninterrupted timeline of user activity, and progressively enrich that timeline with AI-generated understanding.

The product is implemented as a Kotlin Android app today, but every architectural decision must keep future Kotlin Multiplatform migration viable.

### Core Architecture Rules

1. Feature-Sliced Clean Architecture is mandatory.
2. The `domain/` layer contains only pure Kotlin models, interfaces, and use cases.
3. Android, Room, WorkManager, speech, and remote AI SDK details must never leak into `domain/`.
4. Every external integration must be represented through pure Kotlin interfaces defined in `core/domain/interfaces/`.
5. Room entities, DTOs, API payloads, and Android framework adapters live only in data or services layers.
6. Every persistence or network transformation must use explicit mappers.
7. Dependency injection must remain KMP-friendly and therefore use Koin.
8. Every milestone must maintain exhaustive automated testing for use cases, view models, DAOs, workers, and UI interactions.

### Product Milestones

#### Phase 0 - Foundation

- Compose application shell
- Koin dependency graph
- Offline-first Room persistence
- Feature slices for stopwatch and daily goals
- Minimal UI that proves storage, state, and architecture wiring

#### Milestone 1 - Core Logging Workflow

- Atomic "close current / open next" stopwatch transaction
- Local activity prediction from previous days
- Quick honesty status updates (`ACCURATE`, `INACCURATE`, `LOST`)
- Daily goal creation and update flows
- Sleep-trigger event pipeline

#### Milestone 2 - AI Capabilities

This milestone introduces three coordinated AI feature sets:

1. AI Day Summary (`day_summary`)
2. Voice Input and Output (`voice`)
3. Persistent AI Chat (`assistant_chat`)

The implementation order may vary, but all three must share common AI abstractions and local context infrastructure.

### Milestone 2 Functional Scope

#### M2.1 AI Day Summary

When the user logs the `Sleep` activity:

1. The current logical day must be closed.
2. A pending local `DailySummary` record must be created immediately.
3. The app must assemble a structured day-summary context from:
   - today's activities
   - today's goal and subtasks
   - recent previous day summaries
   - recent previous day goals and completions
   - derived metrics such as focus time, lost time, longest block, and completion ratio
4. The raw context and prompt metadata must be persisted locally for retries and debugging.
5. A background worker must ask the configured AI engine to generate a summary.
6. The resulting summary must be stored locally even before any future cloud sync.
7. If generation fails because of network or provider errors, the summary must remain pending and retryable.

Summary generation must use a remote Gemini text model first. The interface must remain engine-agnostic so the implementation can later swap to an on-device Gemma or Nano runtime without changing domain code.

#### M2.2 Voice Input and Output

The app must provide a push-to-talk interaction model with the following behavior:

- hold to talk
- release to send
- swipe to cancel

Requirements:

1. Voice capture must use a dedicated `voice` feature slice.
2. Android speech recognition and Android text-to-speech must be wrapped behind pure Kotlin interfaces.
3. Spoken user input must be normalized to English before being sent to the AI layer, even if originally spoken in Spanish.
4. The original transcript may be preserved locally if useful for product or debugging, but the AI-facing message should be English-normalized.
5. The UI must expose clear recording, cancel, transcribing, sending, and speaking states.
6. Gesture behavior must be covered by Android UI tests.

Milestone 2 default stack:

- STT: Android `SpeechRecognizer`
- TTS: Android `TextToSpeech`
- AI translation and response generation: remote Gemini fast text model

#### M2.3 AI Chat

The app must add a dedicated conversational page where the user can interact with the AI assistant using text or voice.

Requirements:

1. Chat sessions and messages must be stored locally.
2. The assistant must have access to contextual memory from:
   - recent activities
   - daily goals and subtasks
   - recent day summaries
3. The chat feature must support multi-turn history.
4. The local DB must remain the source of truth for conversations.
5. The feature must support both typed messages and voice-derived messages.

### Feature Ownership

#### `features/day_closure/`

Responsible only for sleep-trigger orchestration:

- detect the `Sleep` event
- trigger wallpaper or device-side night actions if needed
- create pending day-summary workflow entry
- enqueue background generation

It must not own summary generation logic itself.

#### `features/day_summary/`

Responsible for:

- creating pending summaries
- assembling context
- building prompts
- invoking the AI text engine
- persisting generated summaries
- exposing summary history and state

#### `features/assistant_chat/`

Responsible for:

- session creation
- message persistence
- contextual prompt assembly
- send/receive orchestration
- observing conversation history

#### `features/voice/`

Responsible for:

- gesture-based recording lifecycle
- speech capture orchestration
- transcript normalization
- cancellation behavior
- speaking assistant responses

### AI Abstractions

The domain layer must stay provider-neutral.

Required interfaces:

- `AiTextGenerator`
- `ConversationContextRepository`
- `DaySummaryRepository`
- `ChatRepository`
- `SpeechRecognizer`
- `TextToSpeechEngine`

The first implementation should use remote Gemini. The abstraction must not contain provider-specific names or payloads.

### Database Evolution

The database remains offline-first and every record that may later sync must expose `isSyncedToD1` or equivalent sync state.

#### Existing Core Tables

- `activities`
- `daily_goals`
- `goal_subtasks`

#### New Milestone 2 Tables

##### `daily_summaries`

Purpose: store AI day-closure outputs and retry state.

Suggested fields:

- `id`
- `day_start_epoch_millis`
- `summary_text`
- `raw_context_json`
- `prompt_version`
- `model_name`
- `generation_status`
- `error_message`
- `last_attempt_epoch_millis`
- `created_at_epoch_millis`
- `updated_at_epoch_millis`
- `is_synced_to_d1`

##### `chat_sessions`

Purpose: store local assistant conversation roots.

Suggested fields:

- `id`
- `title`
- `created_at_epoch_millis`
- `updated_at_epoch_millis`
- `is_synced_to_d1`

##### `chat_messages`

Purpose: store local chat history.

Suggested fields:

- `id`
- `session_id`
- `role`
- `source_language`
- `normalized_english_text`
- `original_text`
- `created_at_epoch_millis`
- `is_synced_to_d1`

### Migration Rules

1. Add `daily_summaries` in the next Room migration.
2. Add `chat_sessions` and `chat_messages` in the same migration if feasible.
3. Migrate AI-pending state away from `daily_goals` into `daily_summaries`.
4. Backfill any pending AI state during migration if old data exists.
5. Every Room migration must have an executing migration test; skipping is not acceptable.

### Worker Strategy

Milestone 2 must use background workers for retryable AI generation.

Rules:

1. Pending day summaries must be retried until they succeed or become non-retryable.
2. Worker code must call use cases, not DAOs directly where avoidable.
3. Worker execution must be tested.
4. Network-unavailable states must leave pending local records intact.

### Security and Privacy

1. AI requests must send only the data necessary for the task.
2. The app must keep a local record of prompt context used for day summaries.
3. Voice recording must only occur while the user is actively pressing the voice control.
4. Microphone permission denial must fail gracefully.
5. API keys or secrets must never be hardcoded in source control.

### Acceptance Criteria For Milestone 2

Milestone 2 is complete only when:

1. `Sleep` creates and eventually resolves a local day summary record.
2. A generated summary is visible from local storage after worker execution.
3. Voice input supports hold-to-talk, release-to-send, and swipe-to-cancel.
4. Spanish speech is normalized to English before AI submission.
5. The assistant chat page can send and persist multi-turn messages with historical context.
6. The AI engine remains fully swappable behind interfaces.
7. Unit, Room, worker, and Android UI tests all pass.
8. Coverage reporting remains available and green.

### Test Strategy

Milestone 2 requires:

- use case unit tests
- repository and mapper tests
- DAO and migration tests
- worker tests
- chat and day-summary view model tests
- Android UI tests for voice gesture behavior
- coverage report generation as part of validation
