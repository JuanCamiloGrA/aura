package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse

interface AiTextGenerator {
    suspend fun generate(request: AiRequest): AiResponse
}
