package com.example.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.example.data.ai.engines.VoicePersonality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class VyomVoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    var activePersonality: VoicePersonality = VoicePersonality.NATURAL
        set(value) {
            field = value
            applyPersonality(value)
        }

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech?.language = Locale.ENGLISH
                applyPersonality(activePersonality)
            }
        }
    }

    private fun applyPersonality(personality: VoicePersonality) {
        textToSpeech?.let {
            it.setSpeechRate(personality.speechRate)
            it.setPitch(personality.pitch)
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        // Stop any current speaking (barge-in support)
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.ERROR
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = rmsdB.coerceIn(0f, 10f) / 10f
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.THINKING
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.IDLE
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _recognizedText.value = text
                        _voiceState.value = VoiceState.THINKING
                        if (text.isNotBlank()) {
                            onResult(text)
                        } else {
                            _voiceState.value = VoiceState.IDLE
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _recognizedText.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignored
        }
        if (_voiceState.value == VoiceState.LISTENING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady || textToSpeech == null) {
            _voiceState.value = VoiceState.IDLE
            return
        }

        // Clean markdown symbols for natural TTS
        val cleaned = text.replace(Regex("[*#`_~>]"), "").trim()
        if (cleaned.isBlank()) return

        _voiceState.value = VoiceState.SPEAKING
        textToSpeech?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "VYOM_VOICE_UTTERANCE")
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            // Ignored
        }
    }
}
