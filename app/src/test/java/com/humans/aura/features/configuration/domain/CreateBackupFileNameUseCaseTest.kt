package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.TimeProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateBackupFileNameUseCaseTest {

    @Test
    fun creates_timestamped_aura_file_name() {
        val useCase = CreateBackupFileNameUseCase(
            timeProvider = object : TimeProvider {
                override fun currentTimeMillis(): Long = 1_736_728_496_000
                override fun currentDayStartEpochMillis(): Long = 0L
            },
        )

        val expectedDateTime = Instant.fromEpochMilliseconds(1_736_728_496_000)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val expected = buildString {
            append("aura-backup-")
            append(expectedDateTime.date.year.toString().padStart(4, '0'))
            append(expectedDateTime.date.monthNumber.toString().padStart(2, '0'))
            append(expectedDateTime.date.dayOfMonth.toString().padStart(2, '0'))
            append('-')
            append(expectedDateTime.hour.toString().padStart(2, '0'))
            append(expectedDateTime.minute.toString().padStart(2, '0'))
            append(expectedDateTime.second.toString().padStart(2, '0'))
            append(".aura")
        }

        assertEquals(expected, useCase())
    }
}
