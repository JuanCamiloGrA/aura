package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.models.DaySummaryContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DaySummaryContextJsonEncoder(
    private val json: Json,
) {
    fun encode(context: DaySummaryContext): String =
        json.encodeToString(DaySummaryContextPayload.from(context))
}

@Serializable
private data class DaySummaryContextPayload(
    val dayStartEpochMillis: Long,
    val activities: List<ActivityPayload>,
    val dailyGoal: DailyGoalPayload?,
    val recentSummaries: List<RecentSummaryPayload>,
    val completionRatio: Float,
    val focusMinutes: Long,
    val lostMinutes: Long,
    val longestActivityTitle: String?,
){
    companion object {
        fun from(context: DaySummaryContext): DaySummaryContextPayload =
            DaySummaryContextPayload(
                dayStartEpochMillis = context.dayStartEpochMillis,
                activities = context.activities.map { activity ->
                    ActivityPayload(
                        id = activity.id,
                        title = activity.title,
                        startTimeEpochMillis = activity.startTimeEpochMillis,
                        endTimeEpochMillis = activity.endTimeEpochMillis,
                        status = activity.status.name,
                    )
                },
                dailyGoal = context.dailyGoal?.let { goal ->
                    DailyGoalPayload(
                        id = goal.id,
                        mainTitle = goal.mainTitle,
                        completedSubtasks = goal.completedSubtasks,
                        totalSubtasks = goal.totalSubtasks,
                        subtasks = goal.subtasks.map { subtask ->
                            GoalSubtaskPayload(
                                id = subtask.id,
                                title = subtask.title,
                                isCompleted = subtask.isCompleted,
                                position = subtask.position,
                            )
                        },
                    )
                },
                recentSummaries = context.recentSummaries.map { summary ->
                    RecentSummaryPayload(
                        id = summary.id,
                        dayStartEpochMillis = summary.dayStartEpochMillis,
                        summaryText = summary.summaryText,
                        generationStatus = summary.generationStatus.name,
                    )
                },
                completionRatio = context.completionRatio,
                focusMinutes = context.focusMinutes,
                lostMinutes = context.lostMinutes,
                longestActivityTitle = context.longestActivityTitle,
            )
    }
}

@Serializable
private data class ActivityPayload(
    val id: Long,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val status: String,
)

@Serializable
private data class DailyGoalPayload(
    val id: Long,
    val mainTitle: String,
    val completedSubtasks: Int,
    val totalSubtasks: Int,
    val subtasks: List<GoalSubtaskPayload>,
)

@Serializable
private data class GoalSubtaskPayload(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val position: Int,
)

@Serializable
private data class RecentSummaryPayload(
    val id: Long,
    val dayStartEpochMillis: Long,
    val summaryText: String?,
    val generationStatus: String,
)
