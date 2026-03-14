package com.humans.aura.core.domain.models

sealed class AiGenerationException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Retryable(
        message: String,
        cause: Throwable? = null,
    ) : AiGenerationException(message, cause)

    class NonRetryable(
        message: String,
        cause: Throwable? = null,
    ) : AiGenerationException(message, cause)
}
