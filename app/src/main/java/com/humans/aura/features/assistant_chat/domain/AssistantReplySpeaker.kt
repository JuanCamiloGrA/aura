package com.humans.aura.features.assistant_chat.domain

fun interface AssistantReplySpeaker {
    suspend operator fun invoke(reply: String)
}
