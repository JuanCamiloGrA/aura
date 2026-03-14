package com.humans.aura.features.assistant_chat.presentation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
                        messages = listOf(ChatMessage(1, 1, ChatRole.USER, "How did I do?", "How did I do?", "en", 1L, false)),
                        draftMessage = "Plan tomorrow",
                    ),
                    onDraftChanged = {},
                    onSendMessage = { sends += 1 },
                    voiceCaptureButton = { Text("Voice") },
                )
            }
        }

        composeRule.onNodeWithTag("assistant_chat_send_button").assertIsEnabled().performClick()
        composeRule.onNodeWithText("User: How did I do?").assertIsDisplayed()

        assertEquals(1, sends)
    }

    @Test
    fun section_renders_empty_state_loading_and_disabled_send() {
        composeRule.setContent {
            AuraTheme {
                AssistantChatSection(
                    uiState = AssistantChatUiState(
                        isLoading = true,
                        isSending = true,
                        draftMessage = "",
                    ),
                    onDraftChanged = {},
                    onSendMessage = {},
                    voiceCaptureButton = { Text("Voice") },
                )
            }
        }

        composeRule.onNodeWithText("Ask AURA about your day, goals, or what to do next.").assertIsDisplayed()
        composeRule.onNodeWithTag("assistant_chat_send_button").assertIsNotEnabled()
        composeRule.onNodeWithText("Voice").assertIsDisplayed()
    }
}
