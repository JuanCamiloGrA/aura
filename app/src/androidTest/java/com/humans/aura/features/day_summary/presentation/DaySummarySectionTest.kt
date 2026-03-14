package com.humans.aura.features.day_summary.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryReflection
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
        composeRule.onNodeWithText("Recent reflections").fetchSemanticsNode()
        composeRule.onNodeWithText("Ready").fetchSemanticsNode()
        composeRule.onNodeWithText("Wins").fetchSemanticsNode()
        composeRule.onNodeWithText("Protected focus").fetchSemanticsNode()
        composeRule.onNodeWithText("Tomorrow Pivot").fetchSemanticsNode()
    }

    @Test
    fun section_renders_pending_and_failed_states() {
        composeRule.setContent {
            AuraTheme {
                DaySummarySection(
                    uiState = DaySummaryUiState(
                        latestSummary = fakeSummary(3, text = null, status = SummaryGenerationStatus.PENDING),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Generating").fetchSemanticsNode()
        composeRule.onNodeWithText("AURA is packaging your day and will retry if needed.").fetchSemanticsNode()
    }

    @Test
    fun section_renders_empty_failed_and_plain_text_completed_states() {
        composeRule.setContent {
            AuraTheme {
                androidx.compose.foundation.layout.Column {
                    DaySummarySection(uiState = DaySummaryUiState())
                    DaySummarySection(
                        uiState = DaySummaryUiState(
                            latestSummary = fakeSummary(
                                id = 4,
                                text = null,
                                status = SummaryGenerationStatus.FAILED,
                                errorMessage = "Gemini timeout",
                            ),
                        ),
                    )
                    DaySummarySection(
                        uiState = DaySummaryUiState(
                            latestSummary = plainTextSummary(),
                        ),
                    )
                }
            }
        }

        composeRule.onNodeWithText("No summary yet").fetchSemanticsNode()
        composeRule.onNodeWithText("No AI day summary has been generated yet.").fetchSemanticsNode()
        composeRule.onNodeWithText("Needs retry").fetchSemanticsNode()
        composeRule.onNodeWithText("Gemini timeout").fetchSemanticsNode()
        composeRule.onNodeWithTag("day_summary_latest_text").fetchSemanticsNode()
        composeRule.onNodeWithText("A plain language recap.").fetchSemanticsNode()
        composeRule.onAllNodesWithText("Review the latest reflection and its generation state.")
            .fetchSemanticsNodes()
    }

    private fun fakeSummary(
        id: Long,
        text: String?,
        status: SummaryGenerationStatus = SummaryGenerationStatus.COMPLETED,
        errorMessage: String? = null,
    ): DaySummary = DaySummary(
        id = id,
        dayStartEpochMillis = id * 1000L,
        summaryText = text,
        reflection = if (text == null) null else DaySummaryReflection(
            wins = listOf("Protected focus"),
            frictionPoints = listOf("Context switching"),
            tomorrowPivot = "Start with planning.",
        ),
        rawContextJson = "{}",
        promptVersion = "v1",
        modelName = "gemini-test",
        generationStatus = status,
        errorMessage = errorMessage,
        lastAttemptEpochMillis = 1L,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        isSyncedToD1 = false,
    )

    private fun plainTextSummary(): DaySummary = DaySummary(
        id = 99,
        dayStartEpochMillis = 99_000L,
        summaryText = "A plain language recap.",
        reflection = null,
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
