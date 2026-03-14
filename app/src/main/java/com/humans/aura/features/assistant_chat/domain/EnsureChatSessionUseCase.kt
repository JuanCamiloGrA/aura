package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.ChatRepository

class EnsureChatSessionUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke() = chatRepository.ensureActiveSession()
}
