package com.humans.aura.features.configuration.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.AuraBackupSummary
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ConfigurationSection(
    viewModel: ConfigurationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE),
    ) { uri ->
        uri?.let { viewModel.exportBackup(it.toString()) }
    }
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it.toString()) }
    }

    ConfigurationSection(
        uiState = uiState,
        onExportBackup = {
            createDocumentLauncher.launch(viewModel.refreshSuggestedBackupFileName())
        },
        onRestoreBackup = {
            openDocumentLauncher.launch(RESTORE_MIME_TYPES)
        },
    )
}

@Composable
fun ConfigurationSection(
    uiState: ConfigurationUiState,
    onExportBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("configuration_section"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "SYSTEM CONTROL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "AURA backups use a .aura archive: a compact zip container with structured JSON inside for durable export and future-safe restore.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = uiState.isBusy,
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

        BackupActionCard(
            title = "Create backup",
            description = "Export your timeline, goals, summaries, chat history, and app state into a single archive you can keep anywhere.",
            buttonLabel = if (uiState.isExporting) "Creating backup..." else "Export .aura",
            note = "Suggested file name: ${uiState.suggestedBackupFileName}",
            enabled = !uiState.isBusy,
            onClick = onExportBackup,
            buttonTag = "configuration_export_backup_button",
        )

        BackupActionCard(
            title = "Restore backup",
            description = "Replace the current local database with a previously exported archive.",
            buttonLabel = if (uiState.isRestoring) "Restoring..." else "Restore archive",
            note = "Restore fully replaces local activities, goals, summaries, and assistant history.",
            enabled = !uiState.isBusy,
            onClick = onRestoreBackup,
            buttonTag = "configuration_restore_backup_button",
        )

        AnimatedVisibility(
            visible = uiState.statusMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            StatusCard(
                message = uiState.statusMessage.orEmpty(),
                isError = uiState.isStatusError,
            )
        }

        uiState.lastRestoredSummary?.let { summary ->
            RestoreSummaryCard(summary)
        }
    }
}

@Composable
private fun BackupActionCard(
    title: String,
    description: String,
    buttonLabel: String,
    note: String,
    enabled: Boolean,
    onClick: () -> Unit,
    buttonTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag(buttonTag),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.outline,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = buttonLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatusCard(
    message: String,
    isError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp)
            .testTag("configuration_status_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (isError) "Restore or export failed" else "Latest action",
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
    }
}

@Composable
private fun RestoreSummaryCard(summary: AuraBackupSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(24.dp)
            .testTag("configuration_restore_summary_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Restored archive",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Exported ${formatBackupTimestamp(summary.exportedAtEpochMillis)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryMetric(label = "Activities", value = summary.activitiesCount.toString(), modifier = Modifier.weight(1f))
            SummaryMetric(label = "Goals", value = summary.dailyGoalsCount.toString(), modifier = Modifier.weight(1f))
            SummaryMetric(label = "Chat", value = summary.chatMessagesCount.toString(), modifier = Modifier.weight(1f))
        }
        Text(
            text = "${summary.goalSubtasksCount} subtasks, ${summary.daySummariesCount} day summaries, ${summary.chatSessionsCount} chat sessions restored.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatBackupTimestamp(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

private const val BACKUP_MIME_TYPE = "application/zip"
private val RESTORE_MIME_TYPES = arrayOf("application/zip", "application/octet-stream", "*/*")
