package com.humans.aura.core.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
                )
            }
        }

        composeRule.onNodeWithText("AURA").fetchSemanticsNode()
        composeRule.onNodeWithText("Stopwatch").fetchSemanticsNode()
        composeRule.onNodeWithText("Daily goals").fetchSemanticsNode()
    }
}
