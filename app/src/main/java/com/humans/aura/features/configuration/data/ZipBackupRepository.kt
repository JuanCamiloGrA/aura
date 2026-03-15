package com.humans.aura.features.configuration.data

import com.humans.aura.core.domain.interfaces.AppPreferencesRepository
import com.humans.aura.core.domain.interfaces.BackupRepository
import com.humans.aura.core.domain.models.AuraBackupActivityRecord
import com.humans.aura.core.domain.models.AuraBackupArchive
import com.humans.aura.core.domain.models.AuraBackupChatMessageRecord
import com.humans.aura.core.domain.models.AuraBackupChatSessionRecord
import com.humans.aura.core.domain.models.AuraBackupDailyGoalRecord
import com.humans.aura.core.domain.models.AuraBackupDaySummaryRecord
import com.humans.aura.core.domain.models.AuraBackupGoalSubtaskRecord
import com.humans.aura.core.domain.models.AuraBackupSummary
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

class ZipBackupRepository(
    private val activityDao: ActivityDao,
    private val dailyGoalDao: DailyGoalDao,
    private val daySummaryDao: DaySummaryDao,
    private val chatDao: ChatDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val codec: AuraBackupArchiveCodec,
    private val transactionRunner: BackupTransactionRunner,
) : BackupRepository {

    override suspend fun exportBackup(exportedAtEpochMillis: Long): ByteArray {
        val archive = transactionRunner.runInTransaction {
            AuraBackupArchive(
                schemaVersion = AuraBackupArchiveCodec.SUPPORTED_SCHEMA_VERSION,
                exportedAtEpochMillis = exportedAtEpochMillis,
                appPreferences = appPreferencesRepository.snapshot(),
                activities = activityDao.getAllActivities().map(ActivityEntity::toBackupRecord),
                dailyGoals = dailyGoalDao.getAllGoals().map(DailyGoalEntity::toBackupRecord),
                goalSubtasks = dailyGoalDao.getAllSubtasks().map(GoalSubtaskEntity::toBackupRecord),
                daySummaries = daySummaryDao.getAllSummaries().map(DailySummaryEntity::toBackupRecord),
                chatSessions = chatDao.getAllSessions().map(ChatSessionEntity::toBackupRecord),
                chatMessages = chatDao.getAllMessages().map(ChatMessageEntity::toBackupRecord),
            )
        }

        return codec.encode(archive)
    }

    override suspend fun restoreBackup(bytes: ByteArray): AuraBackupSummary {
        val archive = codec.decode(bytes)

        transactionRunner.runInTransaction {
            chatDao.deleteAllSessions()
            dailyGoalDao.deleteAllGoals()
            daySummaryDao.deleteAllSummaries()
            activityDao.deleteAllActivities()

            if (archive.activities.isNotEmpty()) {
                activityDao.insertAll(archive.activities.map(AuraBackupActivityRecord::toEntity))
            }
            if (archive.dailyGoals.isNotEmpty()) {
                dailyGoalDao.insertGoals(archive.dailyGoals.map(AuraBackupDailyGoalRecord::toEntity))
            }
            if (archive.goalSubtasks.isNotEmpty()) {
                dailyGoalDao.insertSubtasks(archive.goalSubtasks.map(AuraBackupGoalSubtaskRecord::toEntity))
            }
            if (archive.daySummaries.isNotEmpty()) {
                daySummaryDao.insertAll(archive.daySummaries.map(AuraBackupDaySummaryRecord::toEntity))
            }
            if (archive.chatSessions.isNotEmpty()) {
                chatDao.insertSessions(archive.chatSessions.map(AuraBackupChatSessionRecord::toEntity))
            }
            if (archive.chatMessages.isNotEmpty()) {
                chatDao.insertMessages(archive.chatMessages.map(AuraBackupChatMessageRecord::toEntity))
            }
        }

        appPreferencesRepository.restore(archive.appPreferences)
        return archive.toSummary()
    }
}

private fun ActivityEntity.toBackupRecord(): AuraBackupActivityRecord = AuraBackupActivityRecord(
    id = id,
    title = title,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status,
    isSyncedToD1 = isSyncedToD1,
)

private fun DailyGoalEntity.toBackupRecord(): AuraBackupDailyGoalRecord = AuraBackupDailyGoalRecord(
    id = id,
    dayStartEpochMillis = dayStartEpochMillis,
    mainTitle = mainTitle,
    isSyncedToD1 = isSyncedToD1,
)

private fun GoalSubtaskEntity.toBackupRecord(): AuraBackupGoalSubtaskRecord = AuraBackupGoalSubtaskRecord(
    id = id,
    goalId = goalId,
    title = title,
    isCompleted = isCompleted,
    position = position,
    isSyncedToD1 = isSyncedToD1,
)

private fun DailySummaryEntity.toBackupRecord(): AuraBackupDaySummaryRecord = AuraBackupDaySummaryRecord(
    id = id,
    dayStartEpochMillis = dayStartEpochMillis,
    summaryText = summaryText,
    rawContextJson = rawContextJson,
    promptVersion = promptVersion,
    modelName = modelName,
    generationStatus = generationStatus,
    errorMessage = errorMessage,
    lastAttemptEpochMillis = lastAttemptEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

private fun ChatSessionEntity.toBackupRecord(): AuraBackupChatSessionRecord = AuraBackupChatSessionRecord(
    id = id,
    title = title,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

private fun ChatMessageEntity.toBackupRecord(): AuraBackupChatMessageRecord = AuraBackupChatMessageRecord(
    id = id,
    sessionId = sessionId,
    role = role,
    originalText = originalText,
    normalizedEnglishText = normalizedEnglishText,
    sourceLanguageCode = sourceLanguageCode,
    createdAtEpochMillis = createdAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupActivityRecord.toEntity(): ActivityEntity = ActivityEntity(
    id = id,
    title = title,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupDailyGoalRecord.toEntity(): DailyGoalEntity = DailyGoalEntity(
    id = id,
    dayStartEpochMillis = dayStartEpochMillis,
    mainTitle = mainTitle,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupGoalSubtaskRecord.toEntity(): GoalSubtaskEntity = GoalSubtaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    isCompleted = isCompleted,
    position = position,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupDaySummaryRecord.toEntity(): DailySummaryEntity = DailySummaryEntity(
    id = id,
    dayStartEpochMillis = dayStartEpochMillis,
    summaryText = summaryText,
    rawContextJson = rawContextJson,
    promptVersion = promptVersion,
    modelName = modelName,
    generationStatus = generationStatus,
    errorMessage = errorMessage,
    lastAttemptEpochMillis = lastAttemptEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupChatSessionRecord.toEntity(): ChatSessionEntity = ChatSessionEntity(
    id = id,
    title = title,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

private fun AuraBackupChatMessageRecord.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    role = role,
    originalText = originalText,
    normalizedEnglishText = normalizedEnglishText,
    sourceLanguageCode = sourceLanguageCode,
    createdAtEpochMillis = createdAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)
