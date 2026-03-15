package com.humans.aura.features.configuration.domain

class InvalidBackupArchiveException(
    message: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)
