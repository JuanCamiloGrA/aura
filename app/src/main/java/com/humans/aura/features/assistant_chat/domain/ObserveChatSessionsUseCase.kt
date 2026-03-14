package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.ChatRepository

class ObserveChatSessionsUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke() = chatRepository.observeSessions()
}
