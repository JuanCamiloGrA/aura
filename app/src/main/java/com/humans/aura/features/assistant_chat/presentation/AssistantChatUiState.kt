package com.humans.aura.features.assistant_chat.presentation

import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatSession

data class AssistantChatUiState(
    val activeSession: ChatSession? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
)
