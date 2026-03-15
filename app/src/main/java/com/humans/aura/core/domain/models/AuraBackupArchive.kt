package com.humans.aura.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuraBackupArchive(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val appPreferences: AppPreferencesSnapshot,
    val activities: List<AuraBackupActivityRecord>,
    val dailyGoals: List<AuraBackupDailyGoalRecord>,
    val goalSubtasks: List<AuraBackupGoalSubtaskRecord>,
    val daySummaries: List<AuraBackupDaySummaryRecord>,
    val chatSessions: List<AuraBackupChatSessionRecord>,
    val chatMessages: List<AuraBackupChatMessageRecord>,
) {
    fun toSummary(): AuraBackupSummary = AuraBackupSummary(
        exportedAtEpochMillis = exportedAtEpochMillis,
        activitiesCount = activities.size,
        dailyGoalsCount = dailyGoals.size,
        goalSubtasksCount = goalSubtasks.size,
        daySummariesCount = daySummaries.size,
        chatSessionsCount = chatSessions.size,
        chatMessagesCount = chatMessages.size,
    )
}

@Serializable
data class AuraBackupActivityRecord(
    val id: Long,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val status: String,
    val isSyncedToD1: Boolean,
)

@Serializable
data class AuraBackupDailyGoalRecord(
    val id: Long,
    val dayStartEpochMillis: Long,
    val mainTitle: String,
    val isSyncedToD1: Boolean,
)

@Serializable
data class AuraBackupGoalSubtaskRecord(
    val id: Long,
    val goalId: Long,
    val title: String,
    val isCompleted: Boolean,
    val position: Int,
    val isSyncedToD1: Boolean,
)

@Serializable
data class AuraBackupDaySummaryRecord(
    val id: Long,
    val dayStartEpochMillis: Long,
    val summaryText: String?,
    val rawContextJson: String,
    val promptVersion: String,
    val modelName: String,
    val generationStatus: String,
    val errorMessage: String?,
    val lastAttemptEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)

@Serializable
data class AuraBackupChatSessionRecord(
    val id: Long,
    val title: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)

@Serializable
data class AuraBackupChatMessageRecord(
    val id: Long,
    val sessionId: Long,
    val role: String,
    val originalText: String,
    val normalizedEnglishText: String,
    val sourceLanguageCode: String,
    val createdAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)
