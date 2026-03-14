package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.domain.models.DaySummaryContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendChatMessageUseCaseTest {

    @Test
    fun invoke_includes_new_user_message_in_history_and_returns_reply() = runTest {
        val repository = FakeChatRepository()
        val ai = RecordingAiTextGenerator()
        val useCase = SendChatMessageUseCase(
            chatRepository = repository,
            conversationContextRepository = FakeConversationContextRepository(),
            buildChatPromptUseCase = BuildChatPromptUseCase(),
            aiTextGenerator = ai,
        )

        val reply = useCase(
            originalText = "hola",
            normalizedEnglishText = "hello",
            sourceLanguageCode = "es",
        )

        assertEquals("Sure", reply)
        assertEquals("hello", ai.requests.single().conversationHistory.last().normalizedEnglishText)
        assertEquals("Sure", repository.assistantMessages.single().originalText)
    }

    private class RecordingAiTextGenerator : AiTextGenerator {
        val requests = mutableListOf<AiRequest>()

        override suspend fun generate(request: AiRequest): AiResponse {
            requests += request
            return AiResponse("Sure", "gemini-test")
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

    private class FakeChatRepository : ChatRepository {
        private val session = ChatSession(1, "Daily assistant", 0L, 0L, false)
        val assistantMessages = mutableListOf<ChatMessage>()

        override fun observeSessions(): Flow<List<ChatSession>> = emptyFlow()

        override fun observeMessages(sessionId: Long): Flow<List<ChatMessage>> = emptyFlow()

        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessage> = listOf(
            ChatMessage(9, sessionId, ChatRole.ASSISTANT, "Previous", "Previous", "en", 0L, false),
        )

        override suspend fun ensureActiveSession(): ChatSession = session

        override suspend fun appendUserMessage(
            sessionId: Long,
            originalText: String,
            normalizedEnglishText: String,
            sourceLanguageCode: String,
        ): ChatMessage = ChatMessage(10, sessionId, ChatRole.USER, originalText, normalizedEnglishText, sourceLanguageCode, 1L, false)

        override suspend fun appendAssistantMessage(sessionId: Long, content: String): ChatMessage =
            ChatMessage(11, sessionId, ChatRole.ASSISTANT, content, content, "en", 2L, false).also(assistantMessages::add)
    }
}
