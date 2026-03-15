package com.humans.aura.core.services.speech

import app.cash.turbine.test
import com.humans.aura.core.domain.models.RecordedAudio
import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AndroidAudioRecorderTest {

    @Test
    fun start_recording_emits_permission_error_when_microphone_not_granted() = runTest {
        val recorder = AndroidAudioRecorder(
            hasRecordAudioPermission = { false },
            recordingSessionFactory = { FakeRecordingSession() },
            recordingDirectoryProvider = { createTempDir() },
        )

        recorder.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            recorder.startRecording()

            assertEquals(VoiceCaptureState.Error("Microphone permission denied"), awaitItem())
        }
    }

    @Test
    fun start_recording_emits_recording_state() = runTest {
        val session = FakeRecordingSession()
        val recorder = AndroidAudioRecorder(
            hasRecordAudioPermission = { true },
            recordingSessionFactory = { session },
            recordingDirectoryProvider = { createTempDir() },
        )

        recorder.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            recorder.startRecording()

            assertEquals(1, session.startCalls)
            assertEquals(VoiceCaptureState.Recording, awaitItem())
        }
    }

    @Test
    fun stop_recording_returns_recorded_audio_and_resets_to_idle() = runTest {
        val directory = createTempDir()
        val recorder = AndroidAudioRecorder(
            hasRecordAudioPermission = { true },
            recordingSessionFactory = { outputFile -> FakeRecordingSession(outputFile, writeOnStop = true) },
            recordingDirectoryProvider = { directory },
            fileNameProvider = { "clip.m4a" },
        )

        recorder.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())
            recorder.startRecording()
            assertEquals(VoiceCaptureState.Recording, awaitItem())

            val recordedAudio = recorder.stopRecording()

            assertEquals(VoiceCaptureState.Idle, awaitItem())
            assertEquals(
                RecordedAudio(
                    filePath = File(directory, "clip.m4a").absolutePath,
                    mimeType = "audio/mp4",
                    displayName = "clip.m4a",
                ),
                recordedAudio,
            )
        }
    }

    @Test
    fun stop_recording_returns_null_when_session_fails() = runTest {
        val recorder = AndroidAudioRecorder(
            hasRecordAudioPermission = { true },
            recordingSessionFactory = { FakeRecordingSession(stopError = IllegalStateException("failed")) },
            recordingDirectoryProvider = { createTempDir() },
        )

        recorder.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())
            recorder.startRecording()
            assertEquals(VoiceCaptureState.Recording, awaitItem())

            val recordedAudio = recorder.stopRecording()

            assertNull(recordedAudio)
            assertEquals(VoiceCaptureState.Error("failed"), awaitItem())
        }
    }

    @Test
    fun cancel_recording_deletes_file_and_emits_idle() = runTest {
        val directory = createTempDir()
        val outputFile = File(directory, "clip.m4a")
        val recorder = AndroidAudioRecorder(
            hasRecordAudioPermission = { true },
            recordingSessionFactory = { FakeRecordingSession(outputFile = it) },
            recordingDirectoryProvider = { directory },
            fileNameProvider = { outputFile.name },
        )

        recorder.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())
            recorder.startRecording()
            assertEquals(VoiceCaptureState.Recording, awaitItem())

            recorder.cancelRecording()

            assertEquals(VoiceCaptureState.Idle, awaitItem())
            assertTrue(!outputFile.exists())
        }
    }

    private fun createTempDir(): File = kotlin.io.path.createTempDirectory().toFile()

    private class FakeRecordingSession(
        private val outputFile: File? = null,
        private val writeOnStop: Boolean = false,
        private val stopError: Throwable? = null,
    ) : AndroidAudioRecorder.RecordingSession {
        var startCalls = 0

        override fun start() {
            startCalls += 1
        }

        override fun stop() {
            stopError?.let { throw it }
            if (writeOnStop) {
                outputFile?.writeBytes(byteArrayOf(1, 2, 3))
            }
        }

        override fun cancel() = Unit
    }
}
