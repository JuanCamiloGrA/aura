package com.humans.aura.core.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import com.humans.aura.core.coordination.AppIntentCoordinator
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.services.ai.GeminiApiKeyProvider
import com.humans.aura.core.services.ai.GeminiModelSelector
import com.humans.aura.core.services.sync.AuraWorkerFactory
import com.humans.aura.features.day_summary.data.DaySummaryContextJsonEncoder
import com.humans.aura.features.day_summary.data.DaySummaryReflectionParser
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryContext
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class CoreModuleTest {

    @Test
    fun core_module_resolves_singletons() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = koinApplication {
            androidContext(context)
            modules(
                module {
                    single<ActivityRepository> { FakeActivityRepository() }
                    single<DailyGoalRepository> { FakeDailyGoalRepository() }
                    single<DaySummaryRepository> { FakeDaySummaryRepository() }
                    single<ConversationContextRepository> { FakeConversationContextRepository() }
                    single<ChatRepository> { FakeChatRepository() }
                    single { DaySummaryContextJsonEncoder(get()) }
                    single { DaySummaryReflectionParser(get()) }
                },
                coreModule,
                useCaseModule,
            )
        }

        try {
            with(app.koin) {
                get<Json>()
                get<IntentMediator>()
                get<TimeProvider>()
                get<WorkManager>()
                get<SyncScheduler>()
                get<WallpaperController>()
                get<GeminiApiKeyProvider>()
                get<GeminiModelSelector>()
                get<AiTextGenerator>()
                get<TextToSpeechEngine>()
                get<AppIntentCoordinator>()
                get<AuraWorkerFactory>()

                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    get<SpeechRecognizer>()
                }
            }
        } finally {
            app.close()
        }
    }

    private class FakeActivityRepository : ActivityRepository {
        override fun observeCurrentActivity(): Flow<Activity?> = MutableStateFlow(null)
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = MutableStateFlow(emptyList())
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = MutableStateFlow(emptyList())
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = error("unused")
        override suspend fun predictNextTitle(nowEpochMillis: Long): com.humans.aura.features.stopwatch.domain.ActivityPrediction? = null
        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit
        override suspend fun clearAll() = Unit
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
