package com.humans.aura.features.stopwatch.presentation

import androidx.compose.ui.test.assertIsEnabled
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
                    onRefreshPrediction = {},
                    onLogNewActivity = { logged += 1 },
                    onMarkInaccurate = {},
                    onMarkLost = {},
                    onClearAll = {},
                )
            }
        }

        composeRule.onNodeWithTag("stopwatch_input").performTextInput("Focus")
        composeRule.onNodeWithTag("new_activity_button").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("use_prediction_button").fetchSemanticsNode()
        composeRule.onNodeWithText("Honesty shortcuts").fetchSemanticsNode()

        assertEquals(1, logged)
    }
}
