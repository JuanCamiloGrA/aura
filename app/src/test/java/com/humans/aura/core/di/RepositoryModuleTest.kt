package com.humans.aura.core.di

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.AppPreferencesRepository
import com.humans.aura.core.domain.interfaces.BackupRepository
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.models.AppThemeModePreference
import com.humans.aura.core.domain.models.AppPreferencesSnapshot
import com.humans.aura.features.configuration.data.BackupTransactionRunner
import com.humans.aura.core.domain.interfaces.DaySummaryContextEncoder
import com.humans.aura.features.day_summary.data.DaySummaryContextJsonEncoder
import com.humans.aura.core.domain.interfaces.DaySummaryReflectionCodec
import com.humans.aura.features.day_summary.data.DaySummaryReflectionParser
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.services.database.ActivityPredictionEntity
import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.dao.ChatDao
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.dao.DaySummaryDao
import com.humans.aura.core.services.database.entity.ActivityEntity
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class RepositoryModuleTest {

    @Test
    fun repository_module_resolves_repositories_and_helpers() {
        val app = koinApplication {
            modules(
                module {
                    single<ActivityDao> { FakeActivityDao() }
                    single<DailyGoalDao> { FakeDailyGoalDao() }
                    single<DaySummaryDao> { FakeDaySummaryDao() }
                    single<ChatDao> { FakeChatDao() }
                    single<TimeProvider> { FakeTimeProvider() }
                    single<IntentMediator> { FakeIntentMediator() }
                    single<AppPreferencesRepository> { FakeAppPreferencesRepository() }
                    single<BackupTransactionRunner> { FakeBackupTransactionRunner() }
                    single {
                        Json {
                            ignoreUnknownKeys = true
                            explicitNulls = false
                        }
                    }
                },
                repositoryModule,
            )
        }

        try {
            with(app.koin) {
                get<ActivityRepository>()
                get<DailyGoalRepository>()
                get<DaySummaryRepository>()
                get<ConversationContextRepository>()
                get<ChatRepository>()
                get<BackupRepository>()
                get<DaySummaryReflectionCodec>()
                get<DaySummaryContextEncoder>()
            }
        } finally {
            app.close()
        }
    }

    private class FakeActivityDao : ActivityDao {
        override suspend fun countActivities(): Int = 0
        override fun observeCurrentActivity(): Flow<ActivityEntity?> = MutableStateFlow(null)
        override fun observeRecentActivities(limit: Int): Flow<List<ActivityEntity>> = MutableStateFlow(emptyList())
        override fun observeActivitiesForDay(dayStartEpochMillis: Long, dayEndEpochMillis: Long): Flow<List<ActivityEntity>> = MutableStateFlow(emptyList())
        override suspend fun insert(activity: ActivityEntity): Long = 1L
        override suspend fun getById(id: Long): ActivityEntity? = null
        override suspend fun getAllActivities(): List<ActivityEntity> = emptyList()
        override suspend fun closeOpenActivities(timestampEpochMillis: Long): Int = 0
        override suspend fun findPrediction(historyStartEpochMillis: Long, currentEpochMillis: Long, dayDurationMillis: Long, timeOfDayEpochMillis: Long, windowMillis: Long): ActivityPredictionEntity? = null
        override suspend fun updateCurrentActivityStatus(status: String): Int = 0
        override suspend fun updateCurrentActivityTitle(title: String): Int = 0
        override suspend fun insertAll(activities: List<ActivityEntity>) = Unit
        override suspend fun deleteAllActivities() = Unit
    }

    private class FakeDailyGoalDao : DailyGoalDao {
        override fun observeGoalForDay(dayStartEpochMillis: Long): Flow<DailyGoalWithSubtasks?> = MutableStateFlow(null)
        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoalEntity? = null
        override suspend fun getAllGoals(): List<DailyGoalEntity> = emptyList()
        override suspend fun getAllSubtasks(): List<GoalSubtaskEntity> = emptyList()
        override suspend fun getGoalWithSubtasksForDay(dayStartEpochMillis: Long): DailyGoalWithSubtasks? = null
        override suspend fun insertGoal(goal: DailyGoalEntity): Long = 1L
        override suspend fun insertGoals(goals: List<DailyGoalEntity>) = Unit
        override suspend fun updateGoal(goal: DailyGoalEntity) = Unit
        override suspend fun updateGoalTitle(dayStartEpochMillis: Long, mainTitle: String) = Unit
        override suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>) = Unit
        override suspend fun updateSubtaskCompletion(subtaskId: Long, isCompleted: Boolean) = Unit
        override suspend fun deleteSubtasksForGoal(goalId: Long) = Unit
        override suspend fun deleteGoalForDay(dayStartEpochMillis: Long) = Unit
        override suspend fun deleteAllGoals() = Unit
    }

    private class FakeDaySummaryDao : DaySummaryDao {
        override fun observeLatestSummary(): Flow<DailySummaryEntity?> = MutableStateFlow(null)
        override fun observeRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>> = MutableStateFlow(emptyList())
        override suspend fun getSummariesByStatus(status: String, limit: Int): List<DailySummaryEntity> = emptyList()
        override suspend fun getById(summaryId: Long): DailySummaryEntity? = null
        override suspend fun getByDayStart(dayStartEpochMillis: Long): DailySummaryEntity? = null
        override suspend fun getAllSummaries(): List<DailySummaryEntity> = emptyList()
        override suspend fun insert(summary: DailySummaryEntity): Long = 1L
        override suspend fun insertAll(summaries: List<DailySummaryEntity>) = Unit
        override suspend fun update(summary: DailySummaryEntity) = Unit
        override suspend fun deleteAllSummaries() = Unit
    }

    private class FakeChatDao : ChatDao {
        override fun observeSessions(): Flow<List<ChatSessionEntity>> = MutableStateFlow(emptyList())
        override fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = MutableStateFlow(emptyList())
        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessageEntity> = emptyList()
        override suspend fun getLatestSession(): ChatSessionEntity? = null
        override suspend fun getAllSessions(): List<ChatSessionEntity> = emptyList()
        override suspend fun getAllMessages(): List<ChatMessageEntity> = emptyList()
        override suspend fun insertSession(session: ChatSessionEntity): Long = 1L
        override suspend fun insertSessions(sessions: List<ChatSessionEntity>) = Unit
        override suspend fun insertMessage(message: ChatMessageEntity): Long = 1L
        override suspend fun insertMessages(messages: List<ChatMessageEntity>) = Unit
        override suspend fun updateSessionTimestamp(sessionId: Long, updatedAtEpochMillis: Long) = Unit
        override suspend fun deleteAllSessions() = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeIntentMediator : IntentMediator {
        private val sharedFlow = MutableSharedFlow<AppIntent>()
        override val intents: SharedFlow<AppIntent> = sharedFlow
        override suspend fun emit(intent: AppIntent) {
            sharedFlow.emit(intent)
        }
    }

    private class FakeAppPreferencesRepository : AppPreferencesRepository {
        override suspend fun snapshot(): AppPreferencesSnapshot = AppPreferencesSnapshot(
            hasCompletedInitialStopwatchBootstrap = false,
            themeModePreference = AppThemeModePreference.DEVICE,
        )

        override suspend fun restore(snapshot: AppPreferencesSnapshot) = Unit
    }

    private class FakeBackupTransactionRunner : BackupTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
}
