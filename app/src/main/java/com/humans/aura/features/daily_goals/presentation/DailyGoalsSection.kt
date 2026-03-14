package com.humans.aura.features.daily_goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
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
        onToggleSubtask = viewModel::toggleSubtask,
        onSaveTodayGoal = viewModel::saveTodayGoal,
        onClearTodayGoal = viewModel::clearTodayGoal,
    )
}

@Composable
fun DailyGoalsSection(
    uiState: DailyGoalsUiState,
    onMainTitleChanged: (String) -> Unit,
    onSubtaskChanged: (Int, String) -> Unit,
    onToggleSubtask: (Long, Boolean) -> Unit,
    onSaveTodayGoal: () -> Unit,
    onClearTodayGoal: () -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderRow(
                title = "Daily focus",
                eyebrow = "Set one clear direction",
            )

            if (uiState.isLoading || uiState.isSaving || uiState.isTogglingSubtask) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            GoalHighlight(goal = uiState.goal)

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_title_input"),
                value = uiState.mainTitleInput,
                onValueChange = onMainTitleChanged,
                label = { Text("Main title") },
                supportingText = { Text("Keep it singular and specific.") },
                singleLine = true,
            )

            uiState.subtaskInputs.forEachIndexed { index, value ->
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_goal_subtask_$index"),
                    value = value,
                    onValueChange = { onSubtaskChanged(index, it) },
                    label = { Text("Subtask ${index + 1}") },
                    singleLine = true,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.testTag("save_daily_goal_button"),
                    onClick = onSaveTodayGoal,
                    enabled = uiState.mainTitleInput.isNotBlank() && !uiState.isSaving,
                ) {
                    Text(text = "Save focus")
                }
                OutlinedButton(onClick = onClearTodayGoal) {
                    Text(text = "Clear")
                }
            }

            GoalSummaryBlock(
                goal = uiState.goal,
                onToggleSubtask = onToggleSubtask,
                isToggleEnabled = !uiState.isTogglingSubtask,
            )
            TodayActivityBlock(uiState.todayActivities)
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    eyebrow: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GoalHighlight(goal: DailyGoal?) {
    val ratio = if (goal == null || goal.totalSubtasks == 0) 0f else goal.completedSubtasks.toFloat() / goal.totalSubtasks.toFloat()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(28.dp),
            )
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = goal?.mainTitle ?: "No focus saved yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = if (goal == null) {
                    "Capture the one outcome that should define today."
                } else {
                    "${goal.completedSubtasks}/${goal.totalSubtasks} subtasks complete"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GoalSummaryBlock(
    goal: DailyGoal?,
    onToggleSubtask: (Long, Boolean) -> Unit,
    isToggleEnabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

        goal.subtasks.forEach { subtask ->
            SubtaskChip(
                subtask = subtask,
                enabled = isToggleEnabled,
                onToggle = { onToggleSubtask(subtask.id, !subtask.isCompleted) },
            )
        }
    }
}

@Composable
private fun SubtaskChip(
    subtask: GoalSubtask,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (subtask.isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("goal_subtask_chip_${subtask.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${if (subtask.isCompleted) "[x]" else "[ ]"} ${subtask.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (subtask.isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onToggle, enabled = enabled) {
                Text(if (subtask.isCompleted) "Undo" else "Done")
            }
        }
    }
}

@Composable
private fun TodayActivityBlock(activities: List<Activity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Timeline today",
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

        activities.takeLast(5).forEach { activity ->
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
