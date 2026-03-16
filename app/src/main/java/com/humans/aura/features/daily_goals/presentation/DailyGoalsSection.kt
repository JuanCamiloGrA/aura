package com.humans.aura.features.daily_goals.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.presentation.components.TaskTitleEditorDialog
import com.humans.aura.features.voice.presentation.VoiceCaptureButton
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DailyGoalsSection(
    viewModel: DailyGoalsViewModel = koinViewModel(),
    titleEditorVoiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed ->
        VoiceCaptureButton(
            onSendTranscript = onTranscribed,
            idleLabel = "HOLD TO SPEAK TITLE",
        )
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyGoalsSection(
        uiState = uiState,
        onMainTitleChanged = viewModel::onMainTitleChanged,
        onSubtaskChanged = viewModel::onSubtaskChanged,
        onOpenTitleEditor = viewModel::openTitleEditor,
        onEditingTitleChanged = viewModel::onEditingTitleChanged,
        onSaveEditedTitle = viewModel::saveEditedTitle,
        onDismissTitleEditor = viewModel::dismissTitleEditor,
        onToggleSubtask = viewModel::toggleSubtask,
        onSaveTodayGoal = viewModel::saveTodayGoal,
        onClearTodayGoal = viewModel::clearTodayGoal,
        titleEditorVoiceCaptureButton = titleEditorVoiceCaptureButton,
    )
}

@Composable
fun DailyGoalsSection(
    uiState: DailyGoalsUiState,
    onMainTitleChanged: (String) -> Unit,
    onSubtaskChanged: (Int, String) -> Unit,
    onOpenTitleEditor: () -> Unit = {},
    onEditingTitleChanged: (String) -> Unit = {},
    onSaveEditedTitle: () -> Unit = {},
    onDismissTitleEditor: () -> Unit = {},
    onToggleSubtask: (Long, Boolean) -> Unit,
    onSaveTodayGoal: () -> Unit,
    onClearTodayGoal: () -> Unit,
    titleEditorVoiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed ->
        VoiceCaptureButton(
            onSendTranscript = onTranscribed,
            idleLabel = "HOLD TO SPEAK TITLE",
        )
    },
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        // ── Section Header ──────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "SET ONE CLEAR DIRECTION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Daily focus",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Progress Loading ────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isLoading || uiState.isSaving || uiState.isTogglingSubtask,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = MaterialTheme.colorScheme.onBackground,
                trackColor = MaterialTheme.colorScheme.outline,
            )
        }

        // ── Goal Highlight Hero ─────────────────────────────────────────
        GoalHighlight(goal = uiState.goal, onEdit = onOpenTitleEditor)

        // ── Input Fields ────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MinimalTextField(
                value = uiState.mainTitleInput,
                onValueChange = onMainTitleChanged,
                placeholder = "Main title",
                supportingText = "Keep it singular and specific.",
                onDone = {
                    keyboardController?.hide()
                    onSaveTodayGoal()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_title_input"),
            )

            uiState.subtaskInputs.forEachIndexed { index, value ->
                MinimalTextField(
                    value = value,
                    onValueChange = { onSubtaskChanged(index, it) },
                    placeholder = "Subtask ${index + 1}",
                    onDone = { keyboardController?.hide() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_goal_subtask_$index"),
                )
            }
        }

        // ── Action Buttons ──────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
                    .testTag("save_daily_goal_button"),
                onClick = {
                    keyboardController?.hide()
                    onSaveTodayGoal()
                },
                enabled = uiState.mainTitleInput.isNotBlank() && !uiState.isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = if (uiState.isSaving) "SAVING..." else "SAVE FOCUS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
            }

            TextButton(
                onClick = onClearTodayGoal,
                modifier = Modifier.height(50.dp),
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Goal Summary Block ──────────────────────────────────────────
        GoalSummaryBlock(
            goal = uiState.goal,
            onEditTitle = onOpenTitleEditor,
            onToggleSubtask = onToggleSubtask,
            isToggleEnabled = !uiState.isTogglingSubtask,
        )

        // ── Today Activity Block ────────────────────────────────────────
        TodayActivityBlock(uiState.todayActivities)
    }

    if (uiState.isTitleEditorVisible) {
        TaskTitleEditorDialog(
            title = "Edit main task",
            subtitle = "Update the one outcome that leads today.",
            value = uiState.editingTitle,
            placeholder = "Rename main title",
            isSaving = uiState.isSaving,
            onValueChange = onEditingTitleChanged,
            onDismiss = onDismissTitleEditor,
            onSave = onSaveEditedTitle,
            voiceCaptureButton = titleEditorVoiceCaptureButton,
        )
    }
}

// ── Goal Highlight Hero ─────────────────────────────────────────────────────

@Composable
private fun GoalHighlight(
    goal: DailyGoal?,
    onEdit: () -> Unit,
) {
    val ratio = if (goal == null || goal.totalSubtasks == 0) {
        0f
    } else {
        goal.completedSubtasks.toFloat() / goal.totalSubtasks.toFloat()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = ratio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "progress_anim",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = goal?.mainTitle ?: "No focus saved yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (goal != null) {
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEdit,
                    )
                    .padding(vertical = 2.dp)
                    .testTag("daily_goal_current_title")
            } else {
                Modifier
            },
        )

        Text(
            text = if (goal == null) {
                "Capture the one outcome that should define today."
            } else {
                "${goal.completedSubtasks}/${goal.totalSubtasks} subtasks complete"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (goal != null) {
            Text(
                text = "Tap the main title to rename it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Progress Bar ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(2.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

// ── Minimal Text Field ──────────────────────────────────────────────────────

@Composable
private fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onDone: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

// ── Goal Summary Block ──────────────────────────────────────────────────────

@Composable
private fun GoalSummaryBlock(
    goal: DailyGoal?,
    onEditTitle: () -> Unit,
    onToggleSubtask: (Long, Boolean) -> Unit,
    isToggleEnabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "TODAY",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (goal == null) {
            Text(
                text = "No daily goal stored for today yet. Add a main title and subtasks to track completion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Text(
            text = goal.mainTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEditTitle,
                )
                .padding(vertical = 2.dp)
                .testTag("daily_goal_summary_title"),
        )

        Text(
            text = "Progress: ${goal.completedSubtasks}/${goal.totalSubtasks} subtasks complete",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goal.subtasks.forEach { subtask ->
                SubtaskChip(
                    subtask = subtask,
                    enabled = isToggleEnabled,
                    onToggle = { onToggleSubtask(subtask.id, !subtask.isCompleted) },
                )
            }
        }
    }
}

// ── Subtask Chip ────────────────────────────────────────────────────────────

@Composable
private fun SubtaskChip(
    subtask: GoalSubtask,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (subtask.isCompleted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "chip_bg_${subtask.id}",
    )

    val textColor by animateColorAsState(
        targetValue = if (subtask.isCompleted) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "chip_text_${subtask.id}",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(14.dp),
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("goal_subtask_chip_${subtask.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${if (subtask.isCompleted) "[x]" else "[ ]"} ${subtask.title}",
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = textColor,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.width(12.dp))

        TextButton(
            onClick = onToggle,
            enabled = enabled,
        ) {
            Text(
                text = if (subtask.isCompleted) "Undo" else "Done",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

// ── Today Activity Block ────────────────────────────────────────────────────

@Composable
private fun TodayActivityBlock(activities: List<Activity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "TIMELINE TODAY",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatClock(activity.startTimeEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text = activity.status.name.lowercase().replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatClock(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
