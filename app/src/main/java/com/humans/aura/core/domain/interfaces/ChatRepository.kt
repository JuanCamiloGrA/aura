package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeSessions(): Flow<List<ChatSession>>

    fun observeMessages(sessionId: Long): Flow<List<ChatMessage>>

    suspend fun getRecentMessages(
        sessionId: Long,
        limit: Int = 20,
    ): List<ChatMessage>

    suspend fun ensureActiveSession(): ChatSession

    suspend fun appendUserMessage(
        sessionId: Long,
        originalText: String,
        normalizedEnglishText: String,
        sourceLanguageCode: String,
    ): ChatMessage

    suspend fun appendAssistantMessage(
        sessionId: Long,
        content: String,
    ): ChatMessage
}
