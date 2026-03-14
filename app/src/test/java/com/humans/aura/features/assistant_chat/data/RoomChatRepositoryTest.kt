package com.humans.aura.features.assistant_chat.data

import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.services.database.dao.ChatDao
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomChatRepositoryTest {

    @Test
    fun observe_methods_map_entities() = runTest {
        val dao = FakeChatDao(
            sessions = listOf(sessionEntity(id = 1)),
            messagesBySession = mutableMapOf(1L to listOf(messageEntity(id = 2, sessionId = 1))),
        )
        val repository = RoomChatRepository(dao, FakeTimeProvider(100))

        assertEquals(listOf(1L), repository.observeSessions().first().map { it.id })
        assertEquals(listOf(2L), repository.observeMessages(1).first().map { it.id })
    }

    @Test
    fun get_recent_messages_returns_oldest_first() = runTest {
        val dao = FakeChatDao(
            recentMessages = listOf(
                messageEntity(id = 3, createdAtEpochMillis = 300),
                messageEntity(id = 2, createdAtEpochMillis = 200),
                messageEntity(id = 1, createdAtEpochMillis = 100),
            ),
        )
        val repository = RoomChatRepository(dao, FakeTimeProvider(100))

        val messages = repository.getRecentMessages(sessionId = 1, limit = 3)

        assertEquals(listOf(1L, 2L, 3L), messages.map { it.id })
    }

    @Test
    fun ensure_active_session_returns_existing_session() = runTest {
        val dao = FakeChatDao(latestSession = sessionEntity(id = 8, updatedAtEpochMillis = 99))
        val repository = RoomChatRepository(dao, FakeTimeProvider(100))

        val session = repository.ensureActiveSession()

        assertEquals(8, session.id)
        assertEquals(0, dao.insertSessionCalls)
    }

    @Test
    fun ensure_active_session_creates_default_session_when_missing() = runTest {
        val dao = FakeChatDao()
        val repository = RoomChatRepository(dao, FakeTimeProvider(500))

        val session = repository.ensureActiveSession()

        assertEquals(1, session.id)
        assertEquals("Daily assistant", session.title)
        assertEquals(500, session.createdAtEpochMillis)
        assertEquals(1, dao.insertSessionCalls)
    }

    @Test
    fun append_user_message_persists_message_and_updates_session_timestamp() = runTest {
        val dao = FakeChatDao()
        val repository = RoomChatRepository(dao, FakeTimeProvider(600))

        val message = repository.appendUserMessage(
            sessionId = 4,
            originalText = "hola",
            normalizedEnglishText = "hello",
            sourceLanguageCode = "es",
        )

        assertEquals(1, message.id)
        assertEquals(ChatRole.USER, message.role)
        assertEquals("hola", message.originalText)
        assertEquals("hello", message.normalizedEnglishText)
        assertEquals("es", message.sourceLanguageCode)
        assertEquals(listOf(4L to 600L), dao.updatedSessions)
    }

    @Test
    fun append_assistant_message_persists_message_and_updates_timestamp() = runTest {
        val dao = FakeChatDao()
        val repository = RoomChatRepository(dao, FakeTimeProvider(700))

        val message = repository.appendAssistantMessage(sessionId = 4, content = "You are on track")

        assertEquals(1, message.id)
        assertEquals(ChatRole.ASSISTANT, message.role)
        assertEquals("You are on track", message.originalText)
        assertEquals("en", message.sourceLanguageCode)
        assertEquals(listOf(4L to 700L), dao.updatedSessions)
    }

    private class FakeChatDao(
        sessions: List<ChatSessionEntity> = emptyList(),
        messagesBySession: MutableMap<Long, List<ChatMessageEntity>> = mutableMapOf(),
        private val recentMessages: List<ChatMessageEntity> = emptyList(),
        private val latestSession: ChatSessionEntity? = null,
    ) : ChatDao {
        private val sessionsFlow = MutableStateFlow(sessions)
        private val messages = messagesBySession
        var insertSessionCalls = 0
        var insertMessageCalls = 0
        val updatedSessions = mutableListOf<Pair<Long, Long>>()

        override fun observeSessions(): Flow<List<ChatSessionEntity>> = sessionsFlow

        override fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = MutableStateFlow(messages[sessionId].orEmpty())

        override suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessageEntity> = recentMessages.take(limit)

        override suspend fun getLatestSession(): ChatSessionEntity? = latestSession

        override suspend fun insertSession(session: ChatSessionEntity): Long {
            insertSessionCalls += 1
            return 1L
        }

        override suspend fun insertMessage(message: ChatMessageEntity): Long {
            insertMessageCalls += 1
            val id = insertMessageCalls.toLong()
            val inserted = message.copy(id = id)
            messages[message.sessionId] = messages[message.sessionId].orEmpty() + inserted
            return id
        }

        override suspend fun updateSessionTimestamp(sessionId: Long, updatedAtEpochMillis: Long) {
            updatedSessions += sessionId to updatedAtEpochMillis
        }
    }

    private class FakeTimeProvider(
        private val now: Long,
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = now

        override fun currentDayStartEpochMillis(): Long = 0
    }

    private fun sessionEntity(
        id: Long = 0,
        title: String = "Daily assistant",
        createdAtEpochMillis: Long = 10,
        updatedAtEpochMillis: Long = 10,
        isSyncedToD1: Boolean = false,
    ) = ChatSessionEntity(
        id = id,
        title = title,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        isSyncedToD1 = isSyncedToD1,
    )

    private fun messageEntity(
        id: Long = 0,
        sessionId: Long = 1,
        role: String = ChatRole.USER.name,
        originalText: String = "hello",
        normalizedEnglishText: String = "hello",
        sourceLanguageCode: String = "en",
        createdAtEpochMillis: Long = 10,
        isSyncedToD1: Boolean = false,
    ) = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role,
        originalText = originalText,
        normalizedEnglishText = normalizedEnglishText,
        sourceLanguageCode = sourceLanguageCode,
        createdAtEpochMillis = createdAtEpochMillis,
        isSyncedToD1 = isSyncedToD1,
    )
}
