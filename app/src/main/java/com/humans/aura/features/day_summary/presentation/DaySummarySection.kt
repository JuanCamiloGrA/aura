package com.humans.aura.features.day_summary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Day summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val latest = uiState.latestSummary
            if (latest?.summaryText.isNullOrBlank()) {
                Text(
                    text = latest?.errorMessage ?: "No AI day summary has been generated yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("day_summary_empty"),
                )
            } else {
                Text(
                    text = latest?.summaryText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("day_summary_latest_text"),
                )
            }

            if (uiState.recentSummaries.isNotEmpty()) {
                Text(
                    text = "Recent reflections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                uiState.recentSummaries.forEach { summary ->
                    Text(
                        text = summary.summaryText ?: "Pending summary for ${summary.dayStartEpochMillis}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
