package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClearChatConversationUseCaseTest {

    @Test
    fun invoke_clears_repository_conversation() = runTest {
        val repository = FakeChatRepository()

        ClearChatConversationUseCase(repository)()

        assertEquals(1, repository.clearCalls)
    }

    private class FakeChatRepository : ChatRepository {
        var clearCalls = 0

        override fun observeSessions(): Flow<List<ChatSession>> = emptyFlow()

        override fun observeMessages(sessionId: Long): Flow<List<ChatMessage>> = emptyFlow()

        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessage> = emptyList()

        override suspend fun ensureActiveSession(): ChatSession = error("unused")

        override suspend fun clearConversation() {
            clearCalls += 1
        }

        override suspend fun appendUserMessage(
            sessionId: Long,
            originalText: String,
            normalizedEnglishText: String,
            sourceLanguageCode: String,
        ): ChatMessage = error("unused")

        override suspend fun appendAssistantMessage(sessionId: Long, content: String): ChatMessage = error("unused")
    }
}
