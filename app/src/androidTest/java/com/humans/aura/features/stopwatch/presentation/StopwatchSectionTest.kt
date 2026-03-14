package com.humans.aura.features.stopwatch.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.presentation.theme.AuraTheme
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StopwatchSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_renders_primary_controls_and_callbacks() {
        var draft = ""
        var logged = 0
        var inaccurate = 0
        var lost = 0
        var cleared = 0
        var refreshed = 0

        composeRule.setContent {
            AuraTheme {
                val draftState = remember { mutableStateOf(draft) }
                StopwatchSection(
                    uiState = StopwatchUiState(
                        currentActivity = Activity(1, "Focus", 0L, null, ActivityStatus.ACTIVE, false),
                        recentActivities = listOf(Activity(2, "Review", 0L, 10L, ActivityStatus.ACCURATE, false)),
                        draftTitle = draftState.value,
                        prediction = ActivityPrediction("Review", 2, 10L),
                        runningDurationLabel = "00:15:00",
                    ),
                    onDraftTitleChanged = {
                        draft = it
                        draftState.value = it
                    },
                    onUsePrediction = {
                        draft = "Review"
                        draftState.value = "Review"
                    },
                    onRefreshPrediction = { refreshed += 1 },
                    onLogNewActivity = { logged += 1 },
                    onMarkInaccurate = { inaccurate += 1 },
                    onMarkLost = { lost += 1 },
                    onClearAll = { cleared += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("stopwatch_input").performTextInput("Focus")
        composeRule.onNodeWithTag("new_activity_button").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("use_prediction_button").performClick()
        composeRule.onNodeWithText("Refresh").performClick()
        composeRule.onNodeWithTag("mark_inaccurate_button").performClick()
        composeRule.onNodeWithTag("mark_lost_button").performClick()
        composeRule.onNodeWithText("Clear activity history").performClick()
        composeRule.onNodeWithText("Status: ACTIVE").assertExists()
        composeRule.onNodeWithText("Started at", substring = true).assertExists()

        assertEquals(1, logged)
        assertEquals(1, inaccurate)
        assertEquals(1, lost)
        assertEquals(1, cleared)
        assertEquals(1, refreshed)
    }

    @Test
    fun section_renders_empty_state_and_disabled_shortcuts_without_activity() {
        composeRule.setContent {
            AuraTheme {
                StopwatchSection(
                    uiState = StopwatchUiState(),
                    onDraftTitleChanged = {},
                    onUsePrediction = {},
                    onRefreshPrediction = {},
                    onLogNewActivity = {},
                    onMarkInaccurate = {},
                    onMarkLost = {},
                    onClearAll = {},
                )
            }
        }

        composeRule.onNodeWithText("No open activity yet. Type a title or accept the suggestion and press New Activity.").assertIsDisplayed()
        composeRule.onNodeWithText("Your log is empty. The first tap should create the active activity instantly.").assertIsDisplayed()
        composeRule.onNodeWithText("Prediction is based on the same time window over the last 7 days.").assertIsDisplayed()
        composeRule.onNodeWithTag("new_activity_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("mark_inaccurate_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("mark_lost_button").assertIsNotEnabled()
    }

    @Test
    fun section_renders_loading_state_and_disables_new_activity_while_logging() {
        composeRule.setContent {
            AuraTheme {
                StopwatchSection(
                    uiState = StopwatchUiState(
                        draftTitle = "Focus",
                        isLoading = true,
                        isLogging = true,
                    ),
                    onDraftTitleChanged = {},
                    onUsePrediction = {},
                    onRefreshPrediction = {},
                    onLogNewActivity = {},
                    onMarkInaccurate = {},
                    onMarkLost = {},
                    onClearAll = {},
                )
            }
        }

        composeRule.onNodeWithTag("new_activity_button").assertIsNotEnabled()
    }
}
