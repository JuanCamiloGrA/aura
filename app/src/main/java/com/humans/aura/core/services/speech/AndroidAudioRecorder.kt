package com.humans.aura.core.services.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.humans.aura.core.domain.interfaces.AudioRecorder
import com.humans.aura.core.domain.models.RecordedAudio
import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AndroidAudioRecorder(
    private val hasRecordAudioPermission: () -> Boolean,
    private val recordingSessionFactory: (File) -> RecordingSession,
    private val recordingDirectoryProvider: () -> File,
    private val fileNameProvider: () -> String = {
        "aura-voice-${System.currentTimeMillis()}.m4a"
    },
) : AudioRecorder {

    constructor(context: Context) : this(
        hasRecordAudioPermission = {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        },
        recordingSessionFactory = { outputFile -> MediaRecorderSession(context, outputFile) },
        recordingDirectoryProvider = { File(context.cacheDir, RECORDINGS_DIRECTORY_NAME) },
    )

    private val mutableCaptureState = MutableStateFlow<VoiceCaptureState>(VoiceCaptureState.Idle)
    override val captureState: Flow<VoiceCaptureState> = mutableCaptureState.asStateFlow()

    private var activeSession: RecordingSession? = null
    private var activeOutputFile: File? = null

    override fun startRecording() {
        if (!hasRecordAudioPermission()) {
            mutableCaptureState.value = VoiceCaptureState.Error("Microphone permission denied")
            return
        }

        clearActiveSession(deleteFile = true)
        val outputDirectory = recordingDirectoryProvider().apply { mkdirs() }
        val outputFile = File(outputDirectory, fileNameProvider())
        val session = recordingSessionFactory(outputFile)
        try {
            session.start()
            activeSession = session
            activeOutputFile = outputFile
            mutableCaptureState.value = VoiceCaptureState.Recording
        } catch (error: Exception) {
            session.cancel()
            outputFile.delete()
            mutableCaptureState.value = VoiceCaptureState.Error(error.message ?: "Unable to start audio recording")
        }
    }

    override fun stopRecording(): RecordedAudio? {
        val session = activeSession ?: return null
        val outputFile = activeOutputFile ?: return null
        activeSession = null
        activeOutputFile = null
        try {
            session.stop()
            mutableCaptureState.value = VoiceCaptureState.Idle
            return RecordedAudio(
                filePath = outputFile.absolutePath,
                mimeType = AUDIO_MIME_TYPE,
                displayName = outputFile.name,
            )
        } catch (error: Exception) {
            outputFile.delete()
            mutableCaptureState.value = VoiceCaptureState.Error(error.message ?: "Unable to finish audio recording")
            return null
        }
    }

    override fun cancelRecording() {
        clearActiveSession(deleteFile = true)
        mutableCaptureState.value = VoiceCaptureState.Idle
    }

    private fun clearActiveSession(deleteFile: Boolean) {
        activeSession?.cancel()
        activeSession = null
        val outputFile = activeOutputFile
        activeOutputFile = null
        if (deleteFile) {
            outputFile?.delete()
        }
    }

    interface RecordingSession {
        fun start()

        fun stop()

        fun cancel()
    }

    private class MediaRecorderSession(
        private val context: Context,
        private val outputFile: File,
    ) : RecordingSession {
        private var recorder: MediaRecorder? = null
        private var hasStarted = false

        override fun start() {
            recorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16_000)
                setAudioEncodingBitRate(64_000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            hasStarted = true
        }

        override fun stop() {
            val activeRecorder = recorder ?: return
            try {
                if (hasStarted) {
                    activeRecorder.stop()
                }
            } finally {
                release(activeRecorder)
            }
        }

        override fun cancel() {
            val activeRecorder = recorder ?: return
            try {
                if (hasStarted) {
                    runCatching { activeRecorder.stop() }
                }
            } finally {
                release(activeRecorder)
            }
        }

        private fun release(activeRecorder: MediaRecorder) {
            runCatching { activeRecorder.reset() }
            activeRecorder.release()
            recorder = null
            hasStarted = false
        }

        private fun createMediaRecorder(): MediaRecorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
    }

    private companion object {
        const val AUDIO_MIME_TYPE = "audio/mp4"
        const val RECORDINGS_DIRECTORY_NAME = "voice-recordings"
    }
}
