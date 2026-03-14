package com.humans.aura.features.assistant_chat.domain

import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.DaySummaryContext

class BuildChatPromptUseCase {
    operator fun invoke(
        context: DaySummaryContext,
        recentMessages: List<ChatMessage>,
        userMessageInEnglish: String,
    ): String = buildString {
        appendLine("You are AURA, a productivity and reflection copilot.")
        appendLine("Recent context:")
        appendLine("Goal: ${context.dailyGoal?.mainTitle ?: "No goal"}")
        appendLine("Focus minutes: ${context.focusMinutes}")
        appendLine("Lost minutes: ${context.lostMinutes}")
        appendLine("Recent summaries: ${context.recentSummaries.mapNotNull { it.summaryText }.joinToString(" | ")}")
        appendLine("Conversation history:")
        recentMessages.forEach { message ->
            appendLine("${message.role.name}: ${message.normalizedEnglishText}")
        }
        appendLine("USER: $userMessageInEnglish")
    }
}
