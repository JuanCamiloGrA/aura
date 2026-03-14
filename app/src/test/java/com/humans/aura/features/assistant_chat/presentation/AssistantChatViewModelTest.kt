package com.humans.aura.features.assistant_chat.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.domain.models.DaySummaryContext
import com.humans.aura.features.assistant_chat.domain.EnsureChatSessionUseCase
import com.humans.aura.features.assistant_chat.domain.BuildChatPromptUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatMessagesUseCase
import com.humans.aura.features.assistant_chat.domain.SendChatMessageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AssistantChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun send_message_clears_draft_and_delegates_to_use_case() = runTest {
        val repository = FakeChatRepository()
        val sendUseCase = SendChatMessageUseCase(
            chatRepository = repository,
            conversationContextRepository = FakeConversationContextRepository(),
            buildChatPromptUseCase = BuildChatPromptUseCase(),
            aiTextGenerator = FakeAiTextGenerator(),
        )
        val viewModel = AssistantChatViewModel(
            ensureChatSessionUseCase = EnsureChatSessionUseCase(repository),
            observeChatMessagesUseCase = ObserveChatMessagesUseCase(repository),
            sendChatMessageUseCase = sendUseCase,
        )
        advanceUntilIdle()

        viewModel.onDraftChanged("Plan my afternoon")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals("Plan my afternoon", repository.userMessages.single().originalText)
        assertEquals("", viewModel.uiState.value.draftMessage)
    }

    @Test
    fun ui_state_exposes_messages_for_active_session() = runTest {
        val repository = FakeChatRepository(
            messages = listOf(ChatMessage(1, 7, ChatRole.USER, "hello", "hello", "en", 1L, false)),
        )
        val viewModel = AssistantChatViewModel(
            ensureChatSessionUseCase = EnsureChatSessionUseCase(repository),
            observeChatMessagesUseCase = ObserveChatMessagesUseCase(repository),
            sendChatMessageUseCase = SendChatMessageUseCase(
                chatRepository = repository,
                conversationContextRepository = FakeConversationContextRepository(),
                buildChatPromptUseCase = BuildChatPromptUseCase(),
                aiTextGenerator = FakeAiTextGenerator(),
            ),
        )
        advanceUntilIdle()

        viewModel.uiState.test {
            val item = awaitItem()
            if (!item.isLoading) {
                assertEquals(1, item.messages.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private class FakeChatRepository(
        private val messages: List<ChatMessage> = emptyList(),
    ) : ChatRepository {
        private val session = ChatSession(7, "Daily assistant", 1L, 1L, false)
        private val messagesFlow = MutableStateFlow(messages)
        val userMessages = mutableListOf<ChatMessage>()
        val assistantMessages = mutableListOf<ChatMessage>()

        override fun observeSessions(): Flow<List<ChatSession>> = MutableStateFlow(listOf(session))
        override fun observeMessages(sessionId: Long): Flow<List<ChatMessage>> = messagesFlow
        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessage> = messages
        override suspend fun ensureActiveSession(): ChatSession = session
        override suspend fun appendUserMessage(sessionId: Long, originalText: String, normalizedEnglishText: String, sourceLanguageCode: String): ChatMessage {
            return ChatMessage(1, sessionId, ChatRole.USER, originalText, normalizedEnglishText, sourceLanguageCode, 1L, false)
                .also(userMessages::add)
        }

        override suspend fun appendAssistantMessage(sessionId: Long, content: String): ChatMessage {
            return ChatMessage(2, sessionId, ChatRole.ASSISTANT, content, content, "en", 2L, false)
                .also(assistantMessages::add)
        }
    }

    private class FakeConversationContextRepository : ConversationContextRepository {
        override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext = error("unused")

        override suspend fun buildChatContext(limit: Int): DaySummaryContext = DaySummaryContext(
            dayStartEpochMillis = 0L,
            activities = emptyList(),
            dailyGoal = null,
            recentSummaries = emptyList(),
            completionRatio = 0f,
            focusMinutes = 0L,
            lostMinutes = 0L,
            longestActivityTitle = null,
        )
    }

    private class FakeAiTextGenerator : AiTextGenerator {
        override suspend fun generate(request: AiRequest): AiResponse = AiResponse("Sure", "gemini-test")
    }
}
