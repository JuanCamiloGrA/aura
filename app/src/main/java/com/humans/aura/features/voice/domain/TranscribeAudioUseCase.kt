package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.AudioTranscriber
import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio

class TranscribeAudioUseCase(
    private val audioTranscriber: AudioTranscriber,
) {
    suspend operator fun invoke(audio: RecordedAudio): AudioTranscription = audioTranscriber.transcribe(audio)
}
