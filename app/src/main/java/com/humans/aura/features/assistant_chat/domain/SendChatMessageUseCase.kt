package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiTask

class SendChatMessageUseCase(
    private val chatRepository: ChatRepository,
    private val conversationContextRepository: ConversationContextRepository,
    private val buildChatPromptUseCase: BuildChatPromptUseCase,
    private val aiTextGenerator: AiTextGenerator,
) {
    suspend operator fun invoke(
        originalText: String,
        normalizedEnglishText: String,
        sourceLanguageCode: String,
    ): String {
        val session = chatRepository.ensureActiveSession()
        val recentMessages = chatRepository.getRecentMessages(session.id)
        val userMessage = chatRepository.appendUserMessage(
            sessionId = session.id,
            originalText = originalText,
            normalizedEnglishText = normalizedEnglishText,
            sourceLanguageCode = sourceLanguageCode,
        )

        val updatedHistory = (recentMessages + userMessage).takeLast(20)
        val context = conversationContextRepository.buildChatContext(limit = 7)
        val prompt = buildChatPromptUseCase(context, updatedHistory, normalizedEnglishText)
        val response = aiTextGenerator.generate(
            AiRequest(
                task = AiTask.CHAT,
                systemInstruction = CHAT_SYSTEM_INSTRUCTION,
                prompt = prompt,
                conversationHistory = updatedHistory,
            ),
        )
        chatRepository.appendAssistantMessage(session.id, response.text)
        return response.text
    }

    companion object {
        const val CHAT_SYSTEM_INSTRUCTION = "You are AURA, a contextual assistant with access to the user's recent work patterns."
    }
}
