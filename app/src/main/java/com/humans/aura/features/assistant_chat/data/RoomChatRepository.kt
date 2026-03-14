package com.humans.aura.features.assistant_chat.data

import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.services.database.dao.ChatDao
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomChatRepository(
    private val chatDao: ChatDao,
    private val timeProvider: TimeProvider,
) : ChatRepository {

    override fun observeSessions(): Flow<List<ChatSession>> =
        chatDao.observeSessions().map { entities -> entities.map(ChatSessionEntity::toDomain) }

    override fun observeMessages(sessionId: Long): Flow<List<ChatMessage>> =
        chatDao.observeMessages(sessionId).map { entities -> entities.map(ChatMessageEntity::toDomain) }

    override suspend fun getRecentMessages(
        sessionId: Long,
        limit: Int,
    ): List<ChatMessage> =
        chatDao.getRecentMessages(sessionId, limit)
            .reversed()
            .map(ChatMessageEntity::toDomain)

    override suspend fun ensureActiveSession(): ChatSession {
        val existing = chatDao.getLatestSession()
        if (existing != null) {
            return existing.toDomain()
        }

        val now = timeProvider.currentTimeMillis()
        val id = chatDao.insertSession(
            ChatSessionEntity(
                title = "Daily assistant",
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                isSyncedToD1 = false,
            ),
        )
        return ChatSession(
            id = id,
            title = "Daily assistant",
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            isSyncedToD1 = false,
        )
    }

    override suspend fun appendUserMessage(
        sessionId: Long,
        originalText: String,
        normalizedEnglishText: String,
        sourceLanguageCode: String,
    ): ChatMessage {
        val now = timeProvider.currentTimeMillis()
        val id = chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = ChatRole.USER.name,
                originalText = originalText,
                normalizedEnglishText = normalizedEnglishText,
                sourceLanguageCode = sourceLanguageCode,
                createdAtEpochMillis = now,
                isSyncedToD1 = false,
            ),
        )
        chatDao.updateSessionTimestamp(sessionId, now)
        return ChatMessage(
            id = id,
            sessionId = sessionId,
            role = ChatRole.USER,
            originalText = originalText,
            normalizedEnglishText = normalizedEnglishText,
            sourceLanguageCode = sourceLanguageCode,
            createdAtEpochMillis = now,
            isSyncedToD1 = false,
        )
    }

    override suspend fun appendAssistantMessage(
        sessionId: Long,
        content: String,
    ): ChatMessage {
        val now = timeProvider.currentTimeMillis()
        val id = chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = ChatRole.ASSISTANT.name,
                originalText = content,
                normalizedEnglishText = content,
                sourceLanguageCode = "en",
                createdAtEpochMillis = now,
                isSyncedToD1 = false,
            ),
        )
        chatDao.updateSessionTimestamp(sessionId, now)
        return ChatMessage(
            id = id,
            sessionId = sessionId,
            role = ChatRole.ASSISTANT,
            originalText = content,
            normalizedEnglishText = content,
            sourceLanguageCode = "en",
            createdAtEpochMillis = now,
            isSyncedToD1 = false,
        )
    }
}
