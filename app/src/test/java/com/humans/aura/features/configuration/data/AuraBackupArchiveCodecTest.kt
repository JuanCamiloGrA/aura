package com.humans.aura.features.configuration.data

import com.humans.aura.core.domain.models.AppPreferencesSnapshot
import com.humans.aura.core.domain.models.AuraBackupActivityRecord
import com.humans.aura.core.domain.models.AuraBackupArchive
import com.humans.aura.features.configuration.domain.InvalidBackupArchiveException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuraBackupArchiveCodecTest {

    private val codec = AuraBackupArchiveCodec(
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        },
    )

    @Test
    fun encode_and_decode_round_trip_preserves_archive() {
        val archive = AuraBackupArchive(
            schemaVersion = AuraBackupArchiveCodec.SUPPORTED_SCHEMA_VERSION,
            exportedAtEpochMillis = 123L,
            appPreferences = AppPreferencesSnapshot(hasCompletedInitialStopwatchBootstrap = true),
            activities = listOf(
                AuraBackupActivityRecord(
                    id = 1L,
                    title = "Deep work",
                    startTimeEpochMillis = 10L,
                    endTimeEpochMillis = 20L,
                    status = "ACCURATE",
                    isSyncedToD1 = false,
                ),
            ),
            dailyGoals = emptyList(),
            goalSubtasks = emptyList(),
            daySummaries = emptyList(),
            chatSessions = emptyList(),
            chatMessages = emptyList(),
        )

        val decoded = codec.decode(codec.encode(archive))

        assertEquals(archive, decoded)
    }

    @Test
    fun decode_rejects_non_zip_payload() {
        val exception = kotlin.runCatching {
            codec.decode("not-a-zip".encodeToByteArray())
        }.exceptionOrNull()

        assertTrue(exception is InvalidBackupArchiveException)
    }
}
