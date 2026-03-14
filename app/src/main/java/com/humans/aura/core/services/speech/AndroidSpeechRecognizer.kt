package com.humans.aura.core.services.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.Manifest
import androidx.core.content.ContextCompat
import com.humans.aura.core.domain.interfaces.SpeechRecognizer as SpeechRecognizerContract
import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidSpeechRecognizer(
    private val hasRecordAudioPermission: () -> Boolean,
    private val recognitionClient: RecognitionClient,
    private val errorMessageResolver: (Int) -> String = ::defaultErrorMessage,
) : SpeechRecognizerContract {

    constructor(
        context: Context,
        speechRecognizerFactory: (Context) -> SpeechRecognizer = SpeechRecognizer::createSpeechRecognizer,
    ) : this(
        hasRecordAudioPermission = {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        },
        recognitionClient = AndroidRecognitionClient(context, speechRecognizerFactory),
        errorMessageResolver = ::defaultErrorMessage,
    )

    private val mutableCaptureState = MutableStateFlow<VoiceCaptureState>(VoiceCaptureState.Idle)
    override val captureState: Flow<VoiceCaptureState> = mutableCaptureState.asStateFlow()

    init {
        recognitionClient.setListener(
            object : RecognitionClient.Listener {
                override fun onReadyForSpeech() {
                    mutableCaptureState.value = VoiceCaptureState.Listening
                }

                override fun onResults(transcripts: List<String>) {
                    mutableCaptureState.value = VoiceCaptureState.TranscriptReady(
                        transcript = transcripts.firstOrNull().orEmpty(),
                        detectedLanguageCode = "auto",
                    )
                }

                override fun onPartialResults(transcripts: List<String>) {
                    val transcript = transcripts.firstOrNull().orEmpty()
                    if (transcript.isNotBlank()) {
                        mutableCaptureState.value = VoiceCaptureState.TranscriptReady(
                            transcript = transcript,
                            detectedLanguageCode = "auto",
                            isPartial = true,
                        )
                    }
                }

                override fun onError(error: Int) {
                    mutableCaptureState.value = VoiceCaptureState.Error(errorMessageResolver(error))
                }
            },
        )
    }

    override fun startListening() {
        if (!hasRecordAudioPermission()) {
            mutableCaptureState.value = VoiceCaptureState.Error("Microphone permission denied")
            return
        }

        mutableCaptureState.value = VoiceCaptureState.Listening
        recognitionClient.startListening()
    }

    override fun stopListening() {
        recognitionClient.stopListening()
    }

    override fun cancelListening() {
        recognitionClient.cancel()
        mutableCaptureState.value = VoiceCaptureState.Idle
    }
}

interface RecognitionClient {
    fun setListener(listener: Listener)

    fun startListening()

    fun stopListening()

    fun cancel()

    interface Listener {
        fun onReadyForSpeech()

        fun onResults(transcripts: List<String>)

        fun onPartialResults(transcripts: List<String>)

        fun onError(error: Int)
    }
}

private class AndroidRecognitionClient(
    private val context: Context,
    private val speechRecognizerFactory: (Context) -> SpeechRecognizer,
) : RecognitionClient {
    private val recognizer = speechRecognizerFactory(context)
    private var listener: RecognitionClient.Listener? = null

    override fun setListener(listener: RecognitionClient.Listener) {
        this.listener = listener
        recognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    listener.onReadyForSpeech()
                }

                override fun onResults(results: Bundle?) {
                    listener.onResults(results.toTranscriptList())
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    listener.onPartialResults(partialResults.toTranscriptList())
                }

                override fun onError(error: Int) {
                    listener.onError(error)
                }

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() = Unit

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )
    }

    override fun startListening() {
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            },
        )
    }

    override fun stopListening() {
        recognizer.stopListening()
    }

    override fun cancel() {
        recognizer.cancel()
    }
}

private fun Bundle?.toTranscriptList(): List<String> =
    this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.toList().orEmpty()

private fun defaultErrorMessage(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
    -> "Speech recognition network unavailable"

    else -> "Speech error: $error"
}
