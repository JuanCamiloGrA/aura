package com.humans.aura.features.day_summary.presentation

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
import androidx.compose.material3.HorizontalDivider
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
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.domain.models.previewText
import org.koin.androidx.compose.koinViewModel

@Composable
fun DaySummarySection(
    viewModel: DaySummaryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DaySummarySection(uiState = uiState)
}

@Composable
fun DaySummarySection(
    uiState: DaySummaryUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_summary_section"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        // ── Section Header ──────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Review the latest reflection and its generation state.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Day summary",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Loading ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isLoading,
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

        // ── Status Hero Card ────────────────────────────────────────────
        SummaryStatusCard(uiState.latestSummary)

        // ── Recent Reflections ──────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.recentSummaries.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Recent reflections",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                uiState.recentSummaries.forEach { summary ->
                    Text(
                        text = summary.previewText()
                            ?: fallbackStatusText(summary.generationStatus, summary.errorMessage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Summary Status Card ─────────────────────────────────────────────────────

@Composable
private fun SummaryStatusCard(summary: DaySummary?) {
    val isError = summary?.generationStatus == SummaryGenerationStatus.FAILED

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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Status Pill ─────────────────────────────────────────────
        StatusLabel(summary?.generationStatus)

        // ── Content ─────────────────────────────────────────────────
        val previewText = summary?.previewText()
        val sections = summary?.toSectionModels().orEmpty()

        if (sections.isNotEmpty()) {
            // ── Structured Reflection Cards ─────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                sections.forEachIndexed { index, section ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.5.dp,
                        )
                    }
                    ReflectionCard(section)
                }
            }
        } else {
            // ── Fallback Text ───────────────────────────────────────
            val content = when {
                summary == null -> "No AI day summary has been generated yet."
                !previewText.isNullOrBlank() && summary.reflection == null -> previewText
                else -> fallbackStatusText(summary.generationStatus, summary.errorMessage)
            }

            val textColor = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.testTag(
                    if (!previewText.isNullOrBlank()) "day_summary_latest_text" else "day_summary_empty",
                ),
            )
        }
    }
}

// ── Status Label ────────────────────────────────────────────────────────────

@Composable
private fun StatusLabel(status: SummaryGenerationStatus?) {
    val label = when (status) {
        SummaryGenerationStatus.COMPLETED -> "Ready"
        SummaryGenerationStatus.PENDING -> "Generating"
        SummaryGenerationStatus.FAILED -> "Needs retry"
        null -> "No summary yet"
    }

    val isError = status == SummaryGenerationStatus.FAILED

    Box(
        modifier = Modifier
            .background(
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isError) {
                MaterialTheme.colorScheme.onError
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
        )
    }
}

// ── Reflection Card ─────────────────────────────────────────────────────────

@Composable
private fun ReflectionCard(section: DaySummarySectionModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        section.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "\u2022",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun fallbackStatusText(
    status: SummaryGenerationStatus,
    errorMessage: String?,
): String = when (status) {
    SummaryGenerationStatus.PENDING -> "AURA is packaging your day and will retry if needed."
    SummaryGenerationStatus.FAILED -> errorMessage ?: "Summary generation failed."
    SummaryGenerationStatus.COMPLETED -> "Summary ready"
}
