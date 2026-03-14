package com.humans.aura.features.assistant_chat.presentation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AssistantChatSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_renders_messages_and_send_callback() {
        var sends = 0
        composeRule.setContent {
            AuraTheme {
                AssistantChatSection(
                    uiState = AssistantChatUiState(
                        activeSession = ChatSession(1, "Daily assistant", 1L, 1L, false),
                        messages = listOf(
                            ChatMessage(1, 1, ChatRole.USER, "How did I do?", "How did I do?", "en", 1L, false),
                            ChatMessage(2, 1, ChatRole.ASSISTANT, "You protected focus.", "You protected focus.", "en", 2L, false),
                        ),
                        draftMessage = "Plan tomorrow",
                    ),
                    onDraftChanged = {},
                    onSendMessage = { sends += 1 },
                    voiceCaptureButton = { Text("Voice") },
                )
            }
        }

        composeRule.onNodeWithTag("assistant_chat_send_button").assertIsEnabled().performClick()
        composeRule.onNodeWithText("You").assertIsDisplayed()
        composeRule.onNodeWithText("AURA").assertIsDisplayed()

        assertEquals(1, sends)
    }

    @Test
    fun section_renders_empty_state_loading_error_and_disabled_send() {
        composeRule.setContent {
            AuraTheme {
                AssistantChatSection(
                    uiState = AssistantChatUiState(
                        isLoading = true,
                        isSending = true,
                        draftMessage = "",
                        lastErrorMessage = "Unable to reach AURA right now",
                    ),
                    onDraftChanged = {},
                    onSendMessage = {},
                    voiceCaptureButton = { Text("Voice") },
                )
            }
        }

        composeRule.onNodeWithText("Ask AURA about your day, goals, or what to do next.").assertIsDisplayed()
        composeRule.onNodeWithText("Unable to reach AURA right now").assertIsDisplayed()
        composeRule.onNodeWithTag("assistant_chat_send_button").assertIsNotEnabled()
        composeRule.onNodeWithText("Voice").assertIsDisplayed()
    }

    @Test
    fun section_renders_sending_state_without_empty_prompt_when_messages_exist() {
        composeRule.setContent {
            AuraTheme {
                AssistantChatSection(
                    uiState = AssistantChatUiState(
                        isSending = true,
                        draftMessage = "Need a reset",
                        messages = listOf(
                            ChatMessage(1, 1, ChatRole.ASSISTANT, "Take one breath.", "Take one breath.", "en", 1L, false),
                        ),
                    ),
                    onDraftChanged = {},
                    onSendMessage = {},
                    voiceCaptureButton = { Text("Voice") },
                )
            }
        }

        composeRule.onNodeWithText("Thinking...").assertIsDisplayed()
        composeRule.onNodeWithTag("assistant_chat_send_button").assertIsNotEnabled()
        composeRule.onAllNodesWithText("Ask AURA about your day, goals, or what to do next.").assertCountEquals(0)
        composeRule.onNodeWithText("AURA").assertIsDisplayed()
    }
}
