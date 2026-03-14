package com.humans.aura.features.assistant_chat.data

import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.domain.models.ChatSession
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity

fun ChatSessionEntity.toDomain(): ChatSession = ChatSession(
    id = id,
    title = title,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    sessionId = sessionId,
    role = ChatRole.valueOf(role),
    originalText = originalText,
    normalizedEnglishText = normalizedEnglishText,
    sourceLanguageCode = sourceLanguageCode,
    createdAtEpochMillis = createdAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)
