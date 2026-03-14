package com.humans.aura.core.domain.models

data class ChatSession(
    val id: Long,
    val title: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)
