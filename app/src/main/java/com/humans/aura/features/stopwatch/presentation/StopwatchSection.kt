package com.humans.aura.features.stopwatch.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.presentation.components.TaskTitleEditorDialog
import com.humans.aura.features.voice.presentation.VoiceCaptureButton
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StopwatchSection(
    viewModel: StopwatchViewModel = koinViewModel(),
    voiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed ->
        VoiceCaptureButton(
            onSendTranscript = onTranscribed,
            idleLabel = "HOLD TO TALK FOR NEW ACTIVITY",
        )
    },
    titleEditorVoiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed ->
        VoiceCaptureButton(
            onSendTranscript = onTranscribed,
            idleLabel = "HOLD TO SPEAK TITLE",
        )
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StopwatchSection(
        uiState = uiState,
        onDraftTitleChanged = viewModel::onDraftTitleChanged,
        onUsePrediction = viewModel::usePrediction,
        onRefreshPrediction = viewModel::refreshPrediction,
        onLogNewActivity = viewModel::logNewActivity,
        onLogVoiceActivity = viewModel::logVoiceActivity,
        onOpenTitleEditor = viewModel::openTitleEditor,
        onEditingTitleChanged = viewModel::onEditingTitleChanged,
        onSaveEditedTitle = viewModel::saveCurrentActivityTitle,
        onDismissTitleEditor = viewModel::dismissTitleEditor,
        onMarkInaccurate = viewModel::markInaccurate,
        onMarkLost = viewModel::markLost,
        onClearHonestyLabel = viewModel::clearHonestyLabel,
        voiceCaptureButton = { voiceCaptureButton(viewModel::logVoiceActivity) },
        titleEditorVoiceCaptureButton = titleEditorVoiceCaptureButton,
    )
}

@Composable
fun StopwatchSection(
    uiState: StopwatchUiState,
    onDraftTitleChanged: (String) -> Unit,
    onUsePrediction: () -> Unit,
    onRefreshPrediction: () -> Unit,
    onLogNewActivity: () -> Unit,
    onLogVoiceActivity: (String) -> Unit,
    onOpenTitleEditor: () -> Unit = {},
    onEditingTitleChanged: (String) -> Unit = {},
    onSaveEditedTitle: () -> Unit = {},
    onDismissTitleEditor: () -> Unit = {},
    onMarkInaccurate: () -> Unit,
    onMarkLost: () -> Unit,
    onClearHonestyLabel: () -> Unit,
    voiceCaptureButton: @Composable () -> Unit = {
        VoiceCaptureButton(
            onSendTranscript = onLogVoiceActivity,
            idleLabel = "HOLD TO TALK FOR NEW ACTIVITY",
        )
    },
    titleEditorVoiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed ->
        VoiceCaptureButton(
            onSendTranscript = onTranscribed,
            idleLabel = "HOLD TO SPEAK TITLE",
        )
    },
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var draftFieldValue by remember { mutableStateOf(TextFieldValue(uiState.draftTitle)) }

    LaunchedEffect(uiState.draftTitle, uiState.isPredictionAutofilled) {
        val desiredSelection = if (uiState.isPredictionAutofilled && uiState.draftTitle.isNotBlank()) {
            TextRange(0, uiState.draftTitle.length)
        } else {
            TextRange(uiState.draftTitle.length)
        }
        if (draftFieldValue.text != uiState.draftTitle || draftFieldValue.selection != desiredSelection) {
            draftFieldValue = TextFieldValue(
                text = uiState.draftTitle,
                selection = desiredSelection,
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {

        // ── Timer Hero ──────────────────────────────────────────────────
        TimerHero(
            activity = uiState.currentActivity,
            runningDurationLabel = uiState.runningDurationLabel,
            onEditCurrentActivity = onOpenTitleEditor,
        )

        // ── Input + CTA ─────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            GhostTextField(
                value = draftFieldValue,
                ghostText = if (uiState.isPredictionAutofilled) null else uiState.prediction?.title,
                isPrediction = uiState.isPredictionAutofilled,
                onValueChange = { value ->
                    val previousText = draftFieldValue.text
                    draftFieldValue = value
                    if (value.text != previousText) {
                        onDraftTitleChanged(value.text)
                    }
                },
                onDone = {
                    keyboardController?.hide()
                    onLogNewActivity()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stopwatch_input"),
            )

            // ── Massive CTA ─────────────────────────────────────────────
            Button(
                onClick = {
                    keyboardController?.hide()
                    onLogNewActivity()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("new_activity_button"),
                enabled = uiState.draftTitle.isNotBlank() && !uiState.isLogging,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = if (uiState.isLogging) "LOGGING..." else "NEW ACTIVITY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            AnimatedVisibility(
                visible = uiState.isVoiceLoggingEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                voiceCaptureButton()
            }

            // ── Subtle prediction actions ───────────────────────────────
            AnimatedVisibility(
                visible = uiState.prediction != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onUsePrediction,
                        modifier = Modifier.testTag("use_prediction_button"),
                    ) {
                        Text(
                            text = "USE SUGGESTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onRefreshPrediction) {
                        Text(
                            text = "REFRESH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Status Shortcuts ────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.currentActivity != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            uiState.currentActivity?.let { currentActivity ->
                StatusShortcuts(
                    currentStatus = currentActivity.status,
                    onMarkInaccurate = onMarkInaccurate,
                    onMarkLost = onMarkLost,
                    onClearHonestyLabel = onClearHonestyLabel,
                )
            }
        }

        // ── Timeline ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.recentActivities.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Timeline(
                activities = uiState.recentActivities,
            )
        }
    }

    if (uiState.isTitleEditorVisible) {
        TaskTitleEditorDialog(
            title = "Edit current task",
            subtitle = "Refine what you are doing right now.",
            value = uiState.editingTitle,
            placeholder = "Rename current activity",
            isSaving = uiState.isLogging,
            onValueChange = onEditingTitleChanged,
            onDismiss = onDismissTitleEditor,
            onSave = onSaveEditedTitle,
            voiceCaptureButton = titleEditorVoiceCaptureButton,
        )
    }
}

// ── Timer Hero ──────────────────────────────────────────────────────────────

@Composable
private fun TimerHero(
    activity: Activity?,
    runningDurationLabel: String,
    onEditCurrentActivity: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (activity != null) "TRACKING" else "READY",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = runningDurationLabel,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = activity?.title ?: "No open activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (activity != null) FontWeight.Medium else FontWeight.Normal,
            color = if (activity != null) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (activity != null) {
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditCurrentActivity,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("current_activity_title")
            } else {
                Modifier
            },
        )

        if (activity != null) {
            Text(
                text = "Tap the task name to rename it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (activity != null && activity.status != ActivityStatus.ACCURATE) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = activity.status.name.replace('_', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

// ── Ghost Text Input ────────────────────────────────────────────────────────

@Composable
private fun GhostTextField(
    value: TextFieldValue,
    ghostText: String?,
    isPrediction: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor by animateColorAsState(
        targetValue = if (isPrediction) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(stiffness = 400f),
        label = "ghost_color",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                if (value.text.isEmpty()) {
                    Text(
                        text = ghostText ?: "What are you doing next?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (ghostText != null) 0.6f else 0.35f,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

// ── Status Shortcuts ────────────────────────────────────────────────────────

@Composable
private fun StatusShortcuts(
    currentStatus: ActivityStatus,
    onMarkInaccurate: () -> Unit,
    onMarkLost: () -> Unit,
    onClearHonestyLabel: () -> Unit,
) {
    val showClearLabelAction = currentStatus == ActivityStatus.INACCURATE ||
        currentStatus == ActivityStatus.LOST

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "HONESTY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onMarkInaccurate,
            modifier = Modifier.testTag("mark_inaccurate_button"),
        ) {
            Text(
                text = "INACCURATE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(
            onClick = onMarkLost,
            modifier = Modifier.testTag("mark_lost_button"),
        ) {
            Text(
                text = "LOST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(
            visible = showClearLabelAction,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            TextButton(
                onClick = onClearHonestyLabel,
                modifier = Modifier.testTag("clear_honesty_label_button"),
            ) {
                Text(
                    text = "CLEAR LABEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Timeline ────────────────────────────────────────────────────────────────

@Composable
private fun Timeline(
    activities: List<Activity>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "TIMELINE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        activities.takeLast(6).reversed().forEach { activity ->
            TimelineEntry(activity)
        }
    }
}

@Composable
private fun TimelineEntry(activity: Activity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatClock(activity.startTimeEpochMillis),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = activity.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = activity.endTimeEpochMillis?.let { end ->
                val minutes = (end - activity.startTimeEpochMillis) / 60_000
                "${minutes}m"
            } ?: "now",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatClock(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
