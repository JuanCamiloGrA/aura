package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio

interface AudioTranscriber {
    suspend fun transcribe(audio: RecordedAudio): AudioTranscription
}
