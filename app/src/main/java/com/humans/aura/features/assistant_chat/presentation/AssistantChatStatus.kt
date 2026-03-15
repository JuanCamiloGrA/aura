package com.humans.aura.features.assistant_chat.presentation

data class AssistantChatStatus(
    val isGeminiConfigured: Boolean = false,
    val providerLabel: String = "Gemini",
)
