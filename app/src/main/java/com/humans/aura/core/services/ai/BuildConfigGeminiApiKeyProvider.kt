package com.humans.aura.core.services.ai

import com.humans.aura.BuildConfig

class BuildConfigGeminiApiKeyProvider : GeminiApiKeyProvider {
    override fun requireApiKey(): String = BuildConfig.GEMINI_API_KEY
}
