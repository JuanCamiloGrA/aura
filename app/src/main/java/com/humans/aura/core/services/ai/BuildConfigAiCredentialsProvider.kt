package com.humans.aura.core.services.ai

import com.humans.aura.BuildConfig
import com.humans.aura.core.domain.interfaces.AiCredentialsProvider

class BuildConfigAiCredentialsProvider : AiCredentialsProvider {
    override fun requireApiKey(): String = BuildConfig.GEMINI_API_KEY
}
