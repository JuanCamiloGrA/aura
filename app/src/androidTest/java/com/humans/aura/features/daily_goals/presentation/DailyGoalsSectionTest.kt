package com.humans.aura.features.daily_goals.presentation

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DailyGoalsSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_renders_goal_inputs_and_callbacks() {
        var saved = 0

        composeRule.setContent {
            AuraTheme {
                DailyGoalsSection(
                    uiState = DailyGoalsUiState(
                        goal = DailyGoal(
                            id = 1,
                            dayStartEpochMillis = 0L,
                            mainTitle = "Protect deep work",
                            subtasks = listOf(GoalSubtask(1, 1, "Scope", false, 0, false)),
                            isAiGenerationPending = true,
                            isSyncedToD1 = false,
                        ),
                        mainTitleInput = "Protect deep work",
                        subtaskInputs = listOf("Scope", "", ""),
                        todayActivities = listOf(Activity(1, "Focus", 0L, null, ActivityStatus.ACTIVE, false)),
                    ),
                    onMainTitleChanged = {},
                    onSubtaskChanged = { _, _ -> },
                    onSaveTodayGoal = { saved += 1 },
                    onClearTodayGoal = {},
                )
            }
        }

        composeRule.onNodeWithTag("daily_goal_title_input").fetchSemanticsNode()
        composeRule.onNodeWithTag("save_daily_goal_button").assertIsEnabled().performClick()

        assertEquals(1, saved)
    }
}
