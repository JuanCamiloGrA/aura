package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.ChatRepository

class ObserveChatMessagesUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(sessionId: Long) = chatRepository.observeMessages(sessionId)
}
