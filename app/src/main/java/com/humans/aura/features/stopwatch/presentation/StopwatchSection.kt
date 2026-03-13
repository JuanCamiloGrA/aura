package com.humans.aura.features.stopwatch.presentation

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
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StopwatchSection(
    viewModel: StopwatchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StopwatchSection(
        uiState = uiState,
        onDraftTitleChanged = viewModel::onDraftTitleChanged,
        onUsePrediction = viewModel::usePrediction,
        onRefreshPrediction = viewModel::refreshPrediction,
        onLogNewActivity = viewModel::logNewActivity,
        onMarkInaccurate = viewModel::markInaccurate,
        onMarkLost = viewModel::markLost,
        onClearAll = viewModel::clearAll,
    )
}

@Composable
fun StopwatchSection(
    uiState: StopwatchUiState,
    onDraftTitleChanged: (String) -> Unit,
    onUsePrediction: () -> Unit,
    onRefreshPrediction: () -> Unit,
    onLogNewActivity: () -> Unit,
    onMarkInaccurate: () -> Unit,
    onMarkLost: () -> Unit,
    onClearAll: () -> Unit,
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
                text = "Stopwatch",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stopwatch_input"),
                value = uiState.draftTitle,
                onValueChange = onDraftTitleChanged,
                label = { Text("Next activity") },
                supportingText = {
                    val prediction = uiState.prediction
                    if (prediction != null) {
                        Text("Suggested from local history: ${prediction.title}")
                    } else {
                        Text("Prediction is based on the same time window over the last 7 days.")
                    }
                },
            )

            if (uiState.prediction != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        modifier = Modifier.testTag("use_prediction_button"),
                        onClick = onUsePrediction,
                    ) {
                        Text("Use suggestion")
                    }
                    OutlinedButton(onClick = onRefreshPrediction) {
                        Text("Refresh")
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_activity_button"),
                onClick = onLogNewActivity,
                enabled = uiState.draftTitle.isNotBlank() && !uiState.isLogging,
            ) {
                Text("New Activity")
            }

            CurrentActivityBlock(
                activity = uiState.currentActivity,
                runningDurationLabel = uiState.runningDurationLabel,
            )
            QuickStatusBlock(
                onMarkInaccurate = onMarkInaccurate,
                onMarkLost = onMarkLost,
                enabled = uiState.currentActivity != null,
            )
            RecentActivityBlock(uiState.recentActivities)

            OutlinedButton(onClick = onClearAll) {
                Text(text = "Clear activity history")
            }
        }
    }
}

@Composable
private fun CurrentActivityBlock(
    activity: Activity?,
    runningDurationLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Current activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (activity == null) {
            Text(
                text = "No open activity yet. Type a title or accept the suggestion and press New Activity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Text(text = activity.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Running: $runningDurationLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Status: ${activity.status.name.replace('_', ' ')}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Started at ${formatClock(activity.startTimeEpochMillis)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickStatusBlock(
    onMarkInaccurate: () -> Unit,
    onMarkLost: () -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Honesty shortcuts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                modifier = Modifier.testTag("mark_inaccurate_button"),
                onClick = onMarkInaccurate,
                enabled = enabled,
            ) {
                Text("Inaccurate")
            }
            OutlinedButton(
                modifier = Modifier.testTag("mark_lost_button"),
                onClick = onMarkLost,
                enabled = enabled,
            ) {
                Text("Lost")
            }
        }
    }
}

@Composable
private fun RecentActivityBlock(activities: List<Activity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Past activities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (activities.isEmpty()) {
            Text(
                text = "Your log is empty. The first tap should create the active activity instantly.",
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
                    append(" to ")
                    append(activity.endTimeEpochMillis?.let(::formatClock) ?: "running")
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
