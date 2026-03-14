package com.humans.aura.core.domain.models

data class AiRequest(
    val task: AiTask,
    val systemInstruction: String,
    val prompt: String,
    val conversationHistory: List<ChatMessage> = emptyList(),
)
