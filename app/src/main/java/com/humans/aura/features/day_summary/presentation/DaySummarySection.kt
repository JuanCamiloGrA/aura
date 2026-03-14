package com.humans.aura.features.day_summary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_summary_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Day summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Review the latest reflection and its generation state.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            SummaryStatusCard(uiState.latestSummary)

            if (uiState.recentSummaries.isNotEmpty()) {
                Text(
                    text = "Recent reflections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                uiState.recentSummaries.forEach { summary ->
                    Text(
                        text = summary.summaryText ?: fallbackStatusText(summary.generationStatus, summary.errorMessage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatusCard(summary: DaySummary?) {
    val containerColor = when (summary?.generationStatus) {
        SummaryGenerationStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        SummaryGenerationStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer
        SummaryGenerationStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        null -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (summary?.generationStatus) {
        SummaryGenerationStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
        SummaryGenerationStatus.COMPLETED -> MaterialTheme.colorScheme.onSecondaryContainer
        SummaryGenerationStatus.PENDING -> MaterialTheme.colorScheme.onPrimaryContainer
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(28.dp),
            )
            .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = when (summary?.generationStatus) {
                SummaryGenerationStatus.COMPLETED -> "Ready"
                SummaryGenerationStatus.PENDING -> "Generating"
                SummaryGenerationStatus.FAILED -> "Needs retry"
                null -> "No summary yet"
            },
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
        )

        val content = when {
            summary == null -> "No AI day summary has been generated yet."
            !summary.summaryText.isNullOrBlank() && summary.reflection == null -> summary.summaryText
            else -> fallbackStatusText(summary.generationStatus, summary.errorMessage)
        }

        val sections = summary?.toSectionModels().orEmpty()
        if (sections.isEmpty()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.testTag(
                    if (!summary?.summaryText.isNullOrBlank()) "day_summary_latest_text" else "day_summary_empty",
                ),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                        section.items.forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                            )
                        }
                    }
                }
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
