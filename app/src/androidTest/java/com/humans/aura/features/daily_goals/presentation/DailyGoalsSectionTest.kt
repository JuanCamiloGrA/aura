package com.humans.aura.features.daily_goals.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
        var cleared = 0
        val subtaskChanges = mutableListOf<Pair<Int, String>>()

        composeRule.setContent {
            AuraTheme {
                var mainTitle by mutableStateOf("Protect deep work")
                var subtasks by mutableStateOf(listOf("Scope", "", ""))

                DailyGoalsSection(
                    uiState = DailyGoalsUiState(
                        goal = DailyGoal(
                            id = 1,
                            dayStartEpochMillis = 0L,
                            mainTitle = "Protect deep work",
                            subtasks = listOf(GoalSubtask(1, 1, "Scope", false, 0, false)),
                            isSyncedToD1 = false,
                        ),
                        mainTitleInput = mainTitle,
                        subtaskInputs = subtasks,
                        todayActivities = listOf(Activity(1, "Focus", 0L, null, ActivityStatus.ACTIVE, false)),
                    ),
                    onMainTitleChanged = { mainTitle = it },
                    onSubtaskChanged = { index, value ->
                        subtaskChanges += index to value
                        subtasks = subtasks.toMutableList().also { it[index] = value }
                    },
                    onSaveTodayGoal = { saved += 1 },
                    onClearTodayGoal = { cleared += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("daily_goal_title_input").performTextInput(" today")
        composeRule.onNodeWithTag("daily_goal_subtask_1").performTextInput("Plan")
        composeRule.onNodeWithTag("save_daily_goal_button").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Clear").performClick()
        composeRule.onNodeWithText("Progress: 0/1 subtasks complete").assertIsDisplayed()
        composeRule.onNodeWithText("[ ] Scope").assertIsDisplayed()
        composeRule.onNodeWithText("Focus", substring = true).assertIsDisplayed()

        assertEquals(1, saved)
        assertEquals(1, cleared)
        assertEquals(listOf(1 to "Plan"), subtaskChanges)
    }

    @Test
    fun section_renders_loading_and_empty_states_with_disabled_save() {
        composeRule.setContent {
            AuraTheme {
                DailyGoalsSection(
                    uiState = DailyGoalsUiState(
                        isLoading = true,
                        isSaving = true,
                    ),
                    onMainTitleChanged = {},
                    onSubtaskChanged = { _, _ -> },
                    onSaveTodayGoal = {},
                    onClearTodayGoal = {},
                )
            }
        }

        composeRule.onNodeWithText("No daily goal stored for today yet. Add a main title and subtasks to track completion.").assertIsDisplayed()
        composeRule.onNodeWithText("No activities logged for today yet.").assertIsDisplayed()
        composeRule.onNodeWithTag("save_daily_goal_button").assertIsNotEnabled()
    }

    @Test
    fun section_renders_completed_and_incomplete_subtasks() {
        composeRule.setContent {
            AuraTheme {
                DailyGoalsSection(
                    uiState = DailyGoalsUiState(
                        goal = DailyGoal(
                            id = 1,
                            dayStartEpochMillis = 0L,
                            mainTitle = "Ship milestone",
                            subtasks = listOf(
                                GoalSubtask(1, 1, "Factory", true, 0, false),
                                GoalSubtask(2, 1, "Coverage", false, 1, false),
                            ),
                            isSyncedToD1 = false,
                        ),
                        mainTitleInput = "Ship milestone",
                        subtaskInputs = listOf("Factory", "Coverage", ""),
                        todayActivities = listOf(Activity(1, "Review", 0L, 10L, ActivityStatus.ACCURATE, false)),
                    ),
                    onMainTitleChanged = {},
                    onSubtaskChanged = { _, _ -> },
                    onSaveTodayGoal = {},
                    onClearTodayGoal = {},
                )
            }
        }

        composeRule.onNodeWithText("Progress: 1/2 subtasks complete").assertIsDisplayed()
        composeRule.onNodeWithText("[x] Factory").assertIsDisplayed()
        composeRule.onNodeWithText("[ ] Coverage").assertIsDisplayed()
        composeRule.onNodeWithText("Review", substring = true).assertIsDisplayed()
    }
}
