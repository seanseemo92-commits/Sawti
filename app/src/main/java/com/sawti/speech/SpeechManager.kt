package com.sawti.speech

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import com.sawti.domain.model.ArabicDialect
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SpeechIssue {
    data object EngineUnavailable : SpeechIssue
    data object MissingArabicVoice : SpeechIssue
}

data class InstalledArabicVoice(
    val name: String,
    val localeTag: String,
    val offline: Boolean,
)

data class DialectVoiceStatus(
    val initialized: Boolean = false,
    val requested: ArabicDialect = ArabicDialect.LIBYAN,
    val actualTag: String? = null,
    val actualVoice: String? = null,
    val regionalVoiceAvailable: Boolean = false,
    val voices: List<InstalledArabicVoice> = emptyList(),
) {
    val usingFallback: Boolean
        get() = initialized && !regionalVoiceAvailable
}

/**
 * Regional Arabic is selected when the device offers it. Android does not guarantee
 * native TTS voices for all nine accents, so availability/fallback is reported honestly.
 * Nothing in this class downloads voices or makes network requests.
 */
class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ready = false
    private var speechRate = 0.85f
    private var dialect = ArabicDialect.LIBYAN
    private var preferredVoice: String? = null
    private var pending: (() -> Unit)? = null

    private val _issue = MutableStateFlow<SpeechIssue?>(null)
    val issue: StateFlow<SpeechIssue?> = _issue.asStateFlow()
    private val _voiceStatus = MutableStateFlow(DialectVoiceStatus())
    val voiceStatus: StateFlow<DialectVoiceStatus> = _voiceStatus.asStateFlow()

    init { createEngine() }

    private fun createEngine() {
        ready = false
        tts = runCatching { TextToSpeech(appContext, this) }.getOrNull()
        if (tts == null) _issue.value = SpeechIssue.EngineUnavailable
    }

    override fun onInit(status: Int) {
        val engine = tts
        if (status != TextToSpeech.SUCCESS || engine == null) {
            _issue.value = SpeechIssue.EngineUnavailable
            runCatching { engine?.shutdown() }
            tts = null
            _voiceStatus.value = DialectVoiceStatus(requested = dialect)
            return
        }
        runCatching {
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            engine.setSpeechRate(speechRate)
        }
        ready = true
        configureEngine()
        pending?.also { pending = null; it.invoke() }
    }

    /** Called on every settings change. A missing regional voice never causes a crash. */
    fun setDialect(value: ArabicDialect, voiceName: String?) {
        if (dialect == value && preferredVoice == voiceName && ready && _voiceStatus.value.initialized) return
        dialect = value
        preferredVoice = voiceName
        if (ready) configureEngine()
    }

    private fun configureEngine() {
        val engine = tts ?: return
        val arabic = runCatching { engine.voices.orEmpty() }
            .getOrDefault(emptySet())
            .filter { it.locale.language == "ar" }
            .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))
        val country = Locale.forLanguageTag(dialect.localeTag).country
        val exact = arabic.filter { country.isNotEmpty() && it.locale.country.equals(country, ignoreCase = true) }
        // An explicitly chosen Arabic voice wins even if its country differs from ar-LY.
        // Only automatic selection prefers a regional voice, then an offline Arabic one.
        val selected = arabic.firstOrNull { it.name == preferredVoice }
            ?: exact.firstOrNull()
            ?: arabic.firstOrNull()
        var success = false
        if (selected != null) {
            success = runCatching { engine.setVoice(selected) == TextToSpeech.SUCCESS }.getOrDefault(false)
        }
        // Locale language selection can work even when getVoices() is empty.
        if (!success) {
            val choices = listOf(dialect.localeTag, "ar", "ar-SA", "ar-EG", "ar-AE", "ar-JO")
            for (tag in choices.distinct()) {
                val state = runCatching { engine.setLanguage(Locale.forLanguageTag(tag)) }
                    .getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
                if (state >= TextToSpeech.LANG_AVAILABLE) { success = true; break }
            }
        }
        val actualVoice = runCatching { engine.voice }.getOrNull()
        val actualTag = actualVoice?.locale?.toLanguageTag()
            ?: runCatching { engine.language?.toLanguageTag() }.getOrNull()
        val matched = if (country.isEmpty()) success else
            success && actualTag?.let { Locale.forLanguageTag(it).country.equals(country, ignoreCase = true) } == true
        _voiceStatus.value = DialectVoiceStatus(
            initialized = true,
            requested = dialect,
            actualTag = actualTag,
            actualVoice = actualVoice?.name,
            regionalVoiceAvailable = matched,
            voices = arabic.map { InstalledArabicVoice(it.name, it.locale.toLanguageTag(), !it.isNetworkConnectionRequired) },
        )
        _issue.value = if (success) null else SpeechIssue.MissingArabicVoice
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (tts == null) createEngine()
        val action = { speakNow(text) }
        if (ready) action() else pending = action
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        runCatching { tts?.setSpeechRate(rate) }
    }

    fun stop() {
        pending = null
        runCatching { tts?.stop() }
    }

    private fun speakNow(text: String) {
        val engine = tts ?: return
        runCatching {
            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sawti-${System.nanoTime()}")
            if (result == TextToSpeech.ERROR) {
                _issue.value = SpeechIssue.EngineUnavailable
            } else {
                // An earlier one-off audio failure must not leave a permanent warning.
                _issue.value = null
            }
        }.onFailure { _issue.value = SpeechIssue.EngineUnavailable }
    }
}
