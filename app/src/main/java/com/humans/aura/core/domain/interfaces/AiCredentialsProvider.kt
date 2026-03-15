package com.humans.aura.core.domain.interfaces

fun interface AiCredentialsProvider {
    fun requireApiKey(): String

    fun isApiKeyConfigured(): Boolean = requireApiKey().isNotBlank()
}
