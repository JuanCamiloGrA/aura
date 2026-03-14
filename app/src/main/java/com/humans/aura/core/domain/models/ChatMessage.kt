package com.humans.aura.core.domain.models

data class ChatMessage(
    val id: Long,
    val sessionId: Long,
    val role: ChatRole,
    val originalText: String,
    val normalizedEnglishText: String,
    val sourceLanguageCode: String,
    val createdAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)
