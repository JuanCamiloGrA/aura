package com.humans.aura.features.configuration.data

import com.humans.aura.core.domain.interfaces.AppPreferencesRepository
import com.humans.aura.core.domain.interfaces.BackupRepository
import com.humans.aura.core.services.database.ActivityPredictionEntity
import com.humans.aura.core.domain.models.AppPreferencesSnapshot
import com.humans.aura.core.domain.models.AuraBackupArchive
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZipBackupRepositoryTest {

    @Test
    fun export_backup_collects_all_tables_and_preferences() = runTest {
        val activityDao = FakeActivityDao(
            activities = mutableListOf(
                ActivityEntity(1, "Focus", 10L, 20L, "ACCURATE", false),
            ),
        )
        val dailyGoalDao = FakeDailyGoalDao(
            goals = mutableListOf(DailyGoalEntity(1, 100L, "Ship", false)),
            subtasks = mutableListOf(GoalSubtaskEntity(1, 1, "Write", true, 0, false)),
        )
        val daySummaryDao = FakeDaySummaryDao(
            summaries = mutableListOf(DailySummaryEntity(1, 100L, "Done", "{}", "v1", "model", "COMPLETED", null, 1L, 1L, 2L, false)),
        )
        val chatDao = FakeChatDao(
            sessions = mutableListOf(ChatSessionEntity(1, "Daily", 1L, 2L, false)),
            messages = mutableListOf(ChatMessageEntity(1, 1, "USER", "Hi", "Hi", "en", 2L, false)),
        )
        val appPreferencesRepository = FakeAppPreferencesRepository(
            snapshot = AppPreferencesSnapshot(hasCompletedInitialStopwatchBootstrap = true),
        )
        val codec = FakeAuraBackupArchiveCodec()
        val repository: BackupRepository = ZipBackupRepository(
            activityDao = activityDao,
            dailyGoalDao = dailyGoalDao,
            daySummaryDao = daySummaryDao,
            chatDao = chatDao,
            appPreferencesRepository = appPreferencesRepository,
            codec = codec,
            transactionRunner = ImmediateBackupTransactionRunner(),
        )

        repository.exportBackup(exportedAtEpochMillis = 999L)

        val exported = requireNotNull(codec.encodedArchive)
        assertEquals(999L, exported.exportedAtEpochMillis)
        assertEquals(1, exported.activities.size)
        assertEquals(1, exported.dailyGoals.size)
        assertEquals(1, exported.goalSubtasks.size)
        assertEquals(1, exported.daySummaries.size)
        assertEquals(1, exported.chatSessions.size)
        assertEquals(1, exported.chatMessages.size)
        assertTrue(exported.appPreferences.hasCompletedInitialStopwatchBootstrap)
    }

    @Test
    fun restore_backup_replaces_local_content_and_restores_preferences() = runTest {
        val activityDao = FakeActivityDao()
        val dailyGoalDao = FakeDailyGoalDao()
        val daySummaryDao = FakeDaySummaryDao()
        val chatDao = FakeChatDao()
        val appPreferencesRepository = FakeAppPreferencesRepository(
            snapshot = AppPreferencesSnapshot(hasCompletedInitialStopwatchBootstrap = false),
        )
        val archive = AuraBackupArchive(
            schemaVersion = AuraBackupArchiveCodec.SUPPORTED_SCHEMA_VERSION,
            exportedAtEpochMillis = 1234L,
            appPreferences = AppPreferencesSnapshot(hasCompletedInitialStopwatchBootstrap = true),
            activities = listOf(com.humans.aura.core.domain.models.AuraBackupActivityRecord(7, "Plan", 1L, null, "ACTIVE", false)),
            dailyGoals = listOf(com.humans.aura.core.domain.models.AuraBackupDailyGoalRecord(4, 11L, "Protect focus", false)),
            goalSubtasks = listOf(com.humans.aura.core.domain.models.AuraBackupGoalSubtaskRecord(5, 4, "Review", false, 0, false)),
            daySummaries = listOf(com.humans.aura.core.domain.models.AuraBackupDaySummaryRecord(6, 11L, "Solid", "{}", "v1", "model", "COMPLETED", null, 1L, 1L, 1L, false)),
            chatSessions = listOf(com.humans.aura.core.domain.models.AuraBackupChatSessionRecord(8, "Session", 1L, 2L, false)),
            chatMessages = listOf(com.humans.aura.core.domain.models.AuraBackupChatMessageRecord(9, 8, "ASSISTANT", "Ready", "Ready", "en", 3L, false)),
        )
        val codec = FakeAuraBackupArchiveCodec(decodedArchive = archive)
        val repository: BackupRepository = ZipBackupRepository(
            activityDao = activityDao,
            dailyGoalDao = dailyGoalDao,
            daySummaryDao = daySummaryDao,
            chatDao = chatDao,
            appPreferencesRepository = appPreferencesRepository,
            codec = codec,
            transactionRunner = ImmediateBackupTransactionRunner(),
        )

        val summary = repository.restoreBackup(byteArrayOf(1, 2, 3))

        assertEquals(1, activityDao.activities.size)
        assertEquals("Plan", activityDao.activities.single().title)
        assertEquals(1, dailyGoalDao.goals.size)
        assertEquals(1, dailyGoalDao.subtasks.size)
        assertEquals(1, daySummaryDao.summaries.size)
        assertEquals(1, chatDao.sessions.size)
        assertEquals(1, chatDao.messages.size)
        assertEquals(true, appPreferencesRepository.restoredSnapshot?.hasCompletedInitialStopwatchBootstrap)
        assertEquals(1, summary.activitiesCount)
        assertEquals(1, summary.chatMessagesCount)
    }

    private class FakeAppPreferencesRepository(
        private val snapshot: AppPreferencesSnapshot,
    ) : AppPreferencesRepository {
        var restoredSnapshot: AppPreferencesSnapshot? = null

        override suspend fun snapshot(): AppPreferencesSnapshot = snapshot

        override suspend fun restore(snapshot: AppPreferencesSnapshot) {
            restoredSnapshot = snapshot
        }
    }

    private class FakeAuraBackupArchiveCodec(
        var decodedArchive: AuraBackupArchive? = null,
    ) : AuraBackupArchiveCodec(
        kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        },
    ) {
        var encodedArchive: AuraBackupArchive? = null

        override fun encode(archive: AuraBackupArchive): ByteArray {
            encodedArchive = archive
            return byteArrayOf(9, 9, 9)
        }

        override fun decode(bytes: ByteArray): AuraBackupArchive = requireNotNull(decodedArchive)
    }

    private class ImmediateBackupTransactionRunner : BackupTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }

    private class FakeActivityDao(
        val activities: MutableList<ActivityEntity> = mutableListOf(),
    ) : ActivityDao {
        override suspend fun countActivities(): Int = activities.size
        override fun observeCurrentActivity(): Flow<ActivityEntity?> = MutableStateFlow(null)
        override fun observeRecentActivities(limit: Int): Flow<List<ActivityEntity>> = MutableStateFlow(emptyList())
        override fun observeActivitiesForDay(dayStartEpochMillis: Long, dayEndEpochMillis: Long): Flow<List<ActivityEntity>> = MutableStateFlow(emptyList())
        override suspend fun insert(activity: ActivityEntity): Long = activity.id
        override suspend fun getById(id: Long): ActivityEntity? = activities.firstOrNull { it.id == id }
        override suspend fun getAllActivities(): List<ActivityEntity> = activities.toList()
        override suspend fun closeOpenActivities(timestampEpochMillis: Long): Int = 0
        override suspend fun findPrediction(
            historyStartEpochMillis: Long,
            currentEpochMillis: Long,
            dayDurationMillis: Long,
            timeOfDayEpochMillis: Long,
            windowMillis: Long,
        ): ActivityPredictionEntity? = null
        override suspend fun updateCurrentActivityStatus(status: String): Int = 0
        override suspend fun insertAll(activities: List<ActivityEntity>) { this.activities += activities }
        override suspend fun deleteAllActivities() { activities.clear() }
    }

    private class FakeDailyGoalDao(
        val goals: MutableList<DailyGoalEntity> = mutableListOf(),
        val subtasks: MutableList<GoalSubtaskEntity> = mutableListOf(),
    ) : DailyGoalDao {
        override fun observeGoalForDay(dayStartEpochMillis: Long): Flow<DailyGoalWithSubtasks?> = MutableStateFlow(null)
        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoalEntity? = null
        override suspend fun getAllGoals(): List<DailyGoalEntity> = goals.toList()
        override suspend fun getAllSubtasks(): List<GoalSubtaskEntity> = subtasks.toList()
        override suspend fun getGoalWithSubtasksForDay(dayStartEpochMillis: Long): DailyGoalWithSubtasks? = null
        override suspend fun insertGoal(goal: DailyGoalEntity): Long = goal.id
        override suspend fun insertGoals(goals: List<DailyGoalEntity>) { this.goals += goals }
        override suspend fun updateGoal(goal: DailyGoalEntity) = Unit
        override suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>) { this.subtasks += subtasks }
        override suspend fun updateSubtaskCompletion(subtaskId: Long, isCompleted: Boolean) = Unit
        override suspend fun deleteSubtasksForGoal(goalId: Long) = Unit
        override suspend fun deleteGoalForDay(dayStartEpochMillis: Long) = Unit
        override suspend fun deleteAllGoals() {
            goals.clear()
            subtasks.clear()
        }
    }

    private class FakeDaySummaryDao(
        val summaries: MutableList<DailySummaryEntity> = mutableListOf(),
    ) : DaySummaryDao {
        override fun observeLatestSummary(): Flow<DailySummaryEntity?> = MutableStateFlow(null)
        override fun observeRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>> = MutableStateFlow(emptyList())
        override suspend fun getSummariesByStatus(status: String, limit: Int): List<DailySummaryEntity> = emptyList()
        override suspend fun getById(summaryId: Long): DailySummaryEntity? = null
        override suspend fun getByDayStart(dayStartEpochMillis: Long): DailySummaryEntity? = null
        override suspend fun getAllSummaries(): List<DailySummaryEntity> = summaries.toList()
        override suspend fun insert(summary: DailySummaryEntity): Long = summary.id
        override suspend fun insertAll(summaries: List<DailySummaryEntity>) { this.summaries += summaries }
        override suspend fun update(summary: DailySummaryEntity) = Unit
        override suspend fun deleteAllSummaries() { summaries.clear() }
    }

    private class FakeChatDao(
        val sessions: MutableList<ChatSessionEntity> = mutableListOf(),
        val messages: MutableList<ChatMessageEntity> = mutableListOf(),
    ) : ChatDao {
        override fun observeSessions(): Flow<List<ChatSessionEntity>> = MutableStateFlow(emptyList())
        override fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = MutableStateFlow(emptyList())
        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessageEntity> = emptyList()
        override suspend fun getLatestSession(): ChatSessionEntity? = null
        override suspend fun getAllSessions(): List<ChatSessionEntity> = sessions.toList()
        override suspend fun getAllMessages(): List<ChatMessageEntity> = messages.toList()
        override suspend fun insertSession(session: ChatSessionEntity): Long = session.id
        override suspend fun insertSessions(sessions: List<ChatSessionEntity>) { this.sessions += sessions }
        override suspend fun insertMessage(message: ChatMessageEntity): Long = message.id
        override suspend fun insertMessages(messages: List<ChatMessageEntity>) { this.messages += messages }
        override suspend fun updateSessionTimestamp(sessionId: Long, updatedAtEpochMillis: Long) = Unit
        override suspend fun deleteAllSessions() {
            sessions.clear()
            messages.clear()
        }
    }
}
