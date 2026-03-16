package com.humans.aura.features.configuration.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.R
import com.humans.aura.core.domain.models.AppThemeModePreference
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
        onThemeModeSelected = viewModel::setThemeModePreference,
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
    onThemeModeSelected: (AppThemeModePreference) -> Unit,
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

        ThemeModeCard(
            selectedThemeMode = uiState.themeModePreference,
            onThemeModeSelected = onThemeModeSelected,
        )

        uiState.lastRestoredSummary?.let { summary ->
            RestoreSummaryCard(summary)
        }
    }
}

@Composable
private fun ThemeModeCard(
    selectedThemeMode: AppThemeModePreference,
    onThemeModeSelected: (AppThemeModePreference) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(28.dp),
            )
            .padding(24.dp)
            .testTag("configuration_theme_mode_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Follow the device by default, keep a crisp light canvas, or force an OLED-black interface for night use.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ThemeOption.values().forEach { option ->
            ThemeModeOptionRow(
                option = option,
                selected = selectedThemeMode == option.themeModePreference,
                onClick = { onThemeModeSelected(option.themeModePreference) },
            )
        }
    }
}

@Composable
private fun ThemeModeOptionRow(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "theme_option_container",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "theme_option_border",
    )
    val iconContainerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.background
        },
        label = "theme_option_icon_container",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        label = "theme_option_icon_tint",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(option.testTag),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(iconContainerColor, RoundedCornerShape(18.dp)),
            ) {
                Icon(
                    painter = painterResource(option.iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.Center),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (option.badge != null) {
                        SelectionBadge(label = option.badge)
                    }
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SelectionIndicator(selected = selected)
        }
    }
}

@Composable
private fun SelectionBadge(
    label: String,
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    size: Dp = 22.dp,
) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(size)
            .border(1.5.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(999.dp))
            .padding(4.dp),
    ) {
        if (selected) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(999.dp)),
            )
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

private enum class ThemeOption(
    val themeModePreference: AppThemeModePreference,
    val title: String,
    val description: String,
    val badge: String?,
    val iconRes: Int,
    val testTag: String,
) {
    DEVICE(
        themeModePreference = AppThemeModePreference.DEVICE,
        title = "Device",
        description = "Sync with Android so AURA feels native all day.",
        badge = "Default",
        iconRes = R.drawable.ic_theme_device,
        testTag = "configuration_theme_option_device",
    ),
    LIGHT(
        themeModePreference = AppThemeModePreference.LIGHT,
        title = "Light",
        description = "Bright grayscale surfaces with strong contrast.",
        badge = null,
        iconRes = R.drawable.ic_theme_light,
        testTag = "configuration_theme_option_light",
    ),
    DARK(
        themeModePreference = AppThemeModePreference.DARK,
        title = "OLED dark",
        description = "Pure-black panels tuned for modern OLED screens.",
        badge = null,
        iconRes = R.drawable.ic_theme_dark,
        testTag = "configuration_theme_option_dark",
    ),
}

private const val BACKUP_MIME_TYPE = "application/zip"
private val RESTORE_MIME_TYPES = arrayOf("application/zip", "application/octet-stream", "*/*")
