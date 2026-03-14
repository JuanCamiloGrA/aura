package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiTask

class NormalizeTranscriptToEnglishUseCase(
    private val aiTextGenerator: AiTextGenerator,
) {
    suspend operator fun invoke(transcript: String): String {
        if (transcript.isBlank()) {
            return transcript
        }

        return aiTextGenerator.generate(
            AiRequest(
                task = AiTask.TRANSLATION,
                systemInstruction = "Translate the user's message into concise English while preserving intent.",
                prompt = transcript,
            ),
        ).text
    }
}
