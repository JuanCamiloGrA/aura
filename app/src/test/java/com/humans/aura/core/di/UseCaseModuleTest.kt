package com.humans.aura.core.di

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryContext
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.features.assistant_chat.domain.AssistantReplySpeaker
import com.humans.aura.features.assistant_chat.domain.BuildChatPromptUseCase
import com.humans.aura.features.assistant_chat.domain.EnsureChatSessionUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatMessagesUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatSessionsUseCase
import com.humans.aura.features.assistant_chat.domain.SendChatMessageUseCase
import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayActivitiesUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SaveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ToggleGoalSubtaskUseCase
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import com.humans.aura.features.day_summary.domain.AssembleDaySummaryContextUseCase
import com.humans.aura.features.day_summary.domain.BuildDaySummaryPromptUseCase
import com.humans.aura.features.day_summary.domain.CreatePendingDaySummaryUseCase
import com.humans.aura.features.day_summary.domain.GeneratePendingDaySummariesUseCase
import com.humans.aura.features.day_summary.domain.ObserveLatestSummaryUseCase
import com.humans.aura.features.day_summary.domain.ObserveRecentSummariesUseCase
import com.humans.aura.features.day_summary.data.DaySummaryContextJsonEncoder
import com.humans.aura.features.day_summary.data.DaySummaryReflectionParser
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.EnsureInitialActivityUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import com.humans.aura.features.voice.domain.SpeakAssistantReplyUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class UseCaseModuleTest {

    @Test
    fun use_case_module_resolves_all_factories() = runTest {
        val fakeActivityRepository = FakeActivityRepository()
        val fakeAppLaunchRepository = FakeAppLaunchRepository()
        val fakeDailyGoalRepository = FakeDailyGoalRepository()
        val fakeDaySummaryRepository = FakeDaySummaryRepository()
        val fakeConversationContextRepository = FakeConversationContextRepository()
        val fakeChatRepository = FakeChatRepository()
        val fakeAiTextGenerator = FakeAiTextGenerator()
        val fakeTextToSpeechEngine = FakeTextToSpeechEngine()
        val fakeWallpaperController = FakeWallpaperController()
        val fakeTimeProvider = FakeTimeProvider()
        val fakeSyncScheduler = FakeSyncScheduler()

        val app = koinApplication {
            modules(
                module {
                    single<ActivityRepository> { fakeActivityRepository }
                    single<AppLaunchRepository> { fakeAppLaunchRepository }
                    single<DailyGoalRepository> { fakeDailyGoalRepository }
                    single<DaySummaryRepository> { fakeDaySummaryRepository }
                    single<ConversationContextRepository> { fakeConversationContextRepository }
                    single<ChatRepository> { fakeChatRepository }
                    single<AiTextGenerator> { fakeAiTextGenerator }
                    single<TextToSpeechEngine> { fakeTextToSpeechEngine }
                    single<WallpaperController> { fakeWallpaperController }
                    single<TimeProvider> { fakeTimeProvider }
                    single<SyncScheduler> { fakeSyncScheduler }
                    single {
                        Json {
                            ignoreUnknownKeys = true
                            explicitNulls = false
                        }
                    }
                    single { DaySummaryContextJsonEncoder(get()) }
                    single { DaySummaryReflectionParser(get()) }
                },
                useCaseModule,
            )
        }

        try {
            with(app.koin) {
                get<ObserveCurrentActivityUseCase>()
                get<ObserveRecentActivitiesUseCase>()
                get<EnsureInitialActivityUseCase>()
                get<LogNewActivityUseCase>()
                get<PredictNextActivityTitleUseCase>()
                get<UpdateCurrentActivityStatusUseCase>()
                get<ClearActivitiesUseCase>()
                get<ObserveTodayGoalUseCase>()
                get<ObserveTodayActivitiesUseCase>()
                get<SaveTodayGoalUseCase>()
                get<ToggleGoalSubtaskUseCase>()
                get<ClearTodayGoalUseCase>()
                get<HandleSleepIntentUseCase>()
                get<CreatePendingDaySummaryUseCase>()
                get<AssembleDaySummaryContextUseCase>()
                get<BuildDaySummaryPromptUseCase>()
                get<GeneratePendingDaySummariesUseCase>()
                get<ObserveLatestSummaryUseCase>()
                get<ObserveRecentSummariesUseCase>()
                get<BuildChatPromptUseCase>()
                get<EnsureChatSessionUseCase>()
                get<ObserveChatMessagesUseCase>()
                get<ObserveChatSessionsUseCase>()
                get<SendChatMessageUseCase>()
                get<NormalizeTranscriptToEnglishUseCase>()
                get<SpeakAssistantReplyUseCase>()
                get<AssistantReplySpeaker>().invoke("AURA reply")
            }

            assertEquals(listOf("AURA reply"), fakeTextToSpeechEngine.spokenTexts)
        } finally {
            app.close()
        }
    }

    private class FakeActivityRepository : ActivityRepository {
        override suspend fun hasLoggedActivities(): Boolean = false

        override fun observeCurrentActivity(): Flow<Activity?> = MutableStateFlow(null)
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = MutableStateFlow(emptyList())
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = MutableStateFlow(emptyList())
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = error("unused")
        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null
        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit
        override suspend fun clearAll() = Unit
    }

    private class FakeAppLaunchRepository : AppLaunchRepository {
        override suspend fun hasCompletedInitialStopwatchBootstrap(): Boolean = true

        override suspend fun markInitialStopwatchBootstrapCompleted() = Unit
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        override fun observeTodayGoal(): Flow<DailyGoal?> = MutableStateFlow(null)
        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = null
        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<com.humans.aura.core.domain.models.GoalSubtaskDraft>) = Unit
        override suspend fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) = Unit
        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeDaySummaryRepository : DaySummaryRepository {
        override fun observeLatestSummary(): Flow<DaySummary?> = MutableStateFlow(null)
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = MutableStateFlow(emptyList())
        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary = error("unused")
        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()
        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }

    private class FakeConversationContextRepository : ConversationContextRepository {
        override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext = emptyContext
        override suspend fun buildChatContext(limit: Int): DaySummaryContext = emptyContext
    }

    private class FakeChatRepository : ChatRepository {
        override fun observeSessions(): Flow<List<ChatSession>> = MutableStateFlow(emptyList())
        override fun observeMessages(sessionId: Long): Flow<List<ChatMessage>> = MutableStateFlow(emptyList())
        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessage> = emptyList()
        override suspend fun ensureActiveSession(): ChatSession = ChatSession(1, "Daily assistant", 1L, 1L, false)
        override suspend fun appendUserMessage(sessionId: Long, originalText: String, normalizedEnglishText: String, sourceLanguageCode: String): ChatMessage =
            ChatMessage(1, sessionId, ChatRole.USER, originalText, normalizedEnglishText, sourceLanguageCode, 1L, false)

        override suspend fun appendAssistantMessage(sessionId: Long, content: String): ChatMessage =
            ChatMessage(2, sessionId, ChatRole.ASSISTANT, content, content, "en", 2L, false)
    }

    private class FakeAiTextGenerator : AiTextGenerator {
        override suspend fun generate(request: AiRequest): AiResponse = AiResponse(text = request.prompt, modelName = "fake")
    }

    private class FakeTextToSpeechEngine : TextToSpeechEngine {
        val spokenTexts = mutableListOf<String>()

        override suspend fun speak(text: String) {
            spokenTexts += text
        }

        override fun stop() = Unit
    }

    private class FakeWallpaperController : WallpaperController {
        override suspend fun setWorkModeWallpaper(title: String) = Unit
        override suspend fun setNightModeWallpaper() = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeSyncScheduler : SyncScheduler {
        override fun scheduleDayClosureSync() = Unit
    }

    private companion object {
        val emptyContext = DaySummaryContext(
            dayStartEpochMillis = 0L,
            activities = emptyList(),
            dailyGoal = null,
            recentSummaries = emptyList(),
            completionRatio = 0f,
            focusMinutes = 0L,
            lostMinutes = 0L,
            longestActivityTitle = null,
        )
    }
}
