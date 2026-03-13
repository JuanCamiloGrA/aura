package com.humans.aura.features.daily_goals.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.DailyGoal
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DailyGoalsSection(
    viewModel: DailyGoalsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyGoalsSection(
        uiState = uiState,
        onMainTitleChanged = viewModel::onMainTitleChanged,
        onSubtaskChanged = viewModel::onSubtaskChanged,
        onSaveTodayGoal = viewModel::saveTodayGoal,
        onClearTodayGoal = viewModel::clearTodayGoal,
    )
}

@Composable
fun DailyGoalsSection(
    uiState: DailyGoalsUiState,
    onMainTitleChanged: (String) -> Unit,
    onSubtaskChanged: (Int, String) -> Unit,
    onSaveTodayGoal: () -> Unit,
    onClearTodayGoal: () -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Daily goals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_title_input"),
                value = uiState.mainTitleInput,
                onValueChange = onMainTitleChanged,
                label = { Text("Main title") },
            )

            uiState.subtaskInputs.forEachIndexed { index, value ->
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_goal_subtask_$index"),
                    value = value,
                    onValueChange = { onSubtaskChanged(index, it) },
                    label = { Text("Subtask ${index + 1}") },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.testTag("save_daily_goal_button"),
                    onClick = onSaveTodayGoal,
                    enabled = uiState.mainTitleInput.isNotBlank() && !uiState.isSaving,
                ) {
                    Text(text = "Save today's goal")
                }
                OutlinedButton(onClick = onClearTodayGoal) {
                    Text(text = "Clear")
                }
            }

            GoalSummaryBlock(uiState.goal)
            TodayActivityBlock(uiState.todayActivities)
        }
    }
}

@Composable
private fun GoalSummaryBlock(goal: DailyGoal?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (goal == null) {
            Text(
                text = "No daily goal stored for today yet. Add a main title and subtasks to track completion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Text(text = goal.mainTitle, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Progress: ${goal.completedSubtasks}/${goal.totalSubtasks} subtasks complete",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (goal.isAiGenerationPending) {
            Text(
                text = "Night analysis is queued for sync.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        goal.subtasks.forEach { subtask ->
            Text(
                text = "${if (subtask.isCompleted) "[x]" else "[ ]"} ${subtask.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TodayActivityBlock(activities: List<Activity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Past activities today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (activities.isEmpty()) {
            Text(
                text = "No activities logged for today yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        activities.forEach { activity ->
            Text(
                text = buildString {
                    append(activity.title)
                    append(" - ")
                    append(formatClock(activity.startTimeEpochMillis))
                    append(" - ")
                    append(activity.status.name.lowercase().replaceFirstChar(Char::uppercase))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formatClock(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
