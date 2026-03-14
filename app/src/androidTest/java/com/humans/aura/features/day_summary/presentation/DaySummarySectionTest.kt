package com.humans.aura.features.day_summary.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Rule
import org.junit.Test

class DaySummarySectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_renders_latest_summary_and_history() {
        composeRule.setContent {
            AuraTheme {
                DaySummarySection(
                    uiState = DaySummaryUiState(
                        latestSummary = fakeSummary(1, "You protected focus and closed well."),
                        recentSummaries = listOf(
                            fakeSummary(1, "You protected focus and closed well."),
                            fakeSummary(2, "You drifted in the afternoon but recovered."),
                        ),
                    ),
                )
            }
        }

        composeRule.onNodeWithTag("day_summary_section").fetchSemanticsNode()
        composeRule.onNodeWithTag("day_summary_latest_text").fetchSemanticsNode()
        composeRule.onNodeWithText("Recent reflections").fetchSemanticsNode()
    }

    private fun fakeSummary(id: Long, text: String): DaySummary = DaySummary(
        id = id,
        dayStartEpochMillis = id * 1000L,
        summaryText = text,
        rawContextJson = "{}",
        promptVersion = "v1",
        modelName = "gemini-test",
        generationStatus = SummaryGenerationStatus.COMPLETED,
        errorMessage = null,
        lastAttemptEpochMillis = 1L,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        isSyncedToD1 = false,
    )
}
