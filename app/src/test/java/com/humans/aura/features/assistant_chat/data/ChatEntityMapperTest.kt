package com.humans.aura.features.assistant_chat.data

import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatEntityMapperTest {

    @Test
    fun session_to_domain_maps_all_fields() {
        val entity = ChatSessionEntity(
            id = 4,
            title = "Daily assistant",
            createdAtEpochMillis = 100,
            updatedAtEpochMillis = 200,
            isSyncedToD1 = false,
        )

        val domain = entity.toDomain()

        assertEquals(4, domain.id)
        assertEquals("Daily assistant", domain.title)
        assertEquals(100, domain.createdAtEpochMillis)
        assertEquals(200, domain.updatedAtEpochMillis)
        assertEquals(false, domain.isSyncedToD1)
    }

    @Test
    fun message_to_domain_maps_all_fields() {
        val entity = ChatMessageEntity(
            id = 9,
            sessionId = 4,
            role = ChatRole.ASSISTANT.name,
            originalText = "Hola",
            normalizedEnglishText = "Hello",
            sourceLanguageCode = "es",
            createdAtEpochMillis = 300,
            isSyncedToD1 = false,
        )

        val domain = entity.toDomain()

        assertEquals(9, domain.id)
        assertEquals(4, domain.sessionId)
        assertEquals(ChatRole.ASSISTANT, domain.role)
        assertEquals("Hola", domain.originalText)
        assertEquals("Hello", domain.normalizedEnglishText)
        assertEquals("es", domain.sourceLanguageCode)
        assertEquals(300, domain.createdAtEpochMillis)
        assertEquals(false, domain.isSyncedToD1)
    }
}
