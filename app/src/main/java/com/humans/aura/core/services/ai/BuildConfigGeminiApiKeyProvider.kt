package com.humans.aura.core.services.ai

import com.humans.aura.BuildConfig
import com.humans.aura.core.domain.interfaces.GeminiConfigurationRepository

class BuildConfigGeminiApiKeyProvider : GeminiApiKeyProvider, GeminiConfigurationRepository {
    override fun requireApiKey(): String = BuildConfig.GEMINI_API_KEY

    override fun isApiKeyConfigured(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()
}
