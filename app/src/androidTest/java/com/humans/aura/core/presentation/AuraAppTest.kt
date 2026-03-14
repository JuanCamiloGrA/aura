package com.humans.aura.core.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Rule
import org.junit.Test

class AuraAppTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun app_renders_main_sections() {
        composeRule.setContent {
            AuraTheme {
                AuraApp(
                    stopwatchSection = { androidx.compose.material3.Text("Stopwatch") },
                    dailyGoalsSection = { androidx.compose.material3.Text("Daily goals") },
                    daySummarySection = { androidx.compose.material3.Text("Day summary") },
                    assistantChatSection = { androidx.compose.material3.Text("Assistant chat") },
                )
            }
        }

        composeRule.onNodeWithText("AURA").fetchSemanticsNode()
        composeRule.onNodeWithText("Stopwatch").fetchSemanticsNode()
        composeRule.onNodeWithText("Daily goals").fetchSemanticsNode()
        composeRule.onNodeWithText("Day summary").fetchSemanticsNode()
    }

    @Test
    fun app_navigation_switches_between_dashboard_chat_and_summary() {
        composeRule.setContent {
            AuraTheme {
                AuraApp(
                    stopwatchSection = { androidx.compose.material3.Text("Stopwatch") },
                    dailyGoalsSection = { androidx.compose.material3.Text("Daily goals") },
                    daySummarySection = { androidx.compose.material3.Text("Day summary") },
                    assistantChatSection = { androidx.compose.material3.Text("Assistant chat") },
                )
            }
        }

        composeRule.onNodeWithTag("nav_assistant").performClick()
        composeRule.onNodeWithText("Assistant chat").fetchSemanticsNode()

        composeRule.onNodeWithTag("nav_summary").performClick()
        composeRule.onNodeWithText("Day summary").fetchSemanticsNode()
    }
}
