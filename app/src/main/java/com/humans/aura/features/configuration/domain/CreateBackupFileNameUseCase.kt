package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.TimeProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CreateBackupFileNameUseCase(
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): String {
        val dateTime = Instant.fromEpochMilliseconds(timeProvider.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return buildString {
            append("aura-backup-")
            append(dateTime.date.year.toString().padStart(4, '0'))
            append(dateTime.date.monthNumber.toString().padStart(2, '0'))
            append(dateTime.date.dayOfMonth.toString().padStart(2, '0'))
            append('-')
            append(dateTime.hour.toString().padStart(2, '0'))
            append(dateTime.minute.toString().padStart(2, '0'))
            append(dateTime.second.toString().padStart(2, '0'))
            append(".aura")
        }
    }
}
