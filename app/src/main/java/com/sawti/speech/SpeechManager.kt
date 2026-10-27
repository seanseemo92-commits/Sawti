package com.sawti.speech

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.sawti.audio.AudioFocusController
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
class SpeechManager(context: Context, private val focus: AudioFocusController) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var focusToken: Long? = null
    private var utteranceId: String? = null
    private var onSpeechStart: (() -> Unit)? = null
    private var onSpeechDone: (() -> Unit)? = null
    private var onSpeechCanceled: (() -> Unit)? = null
    private var tts: TextToSpeech? = null
    private var ready = false
    private var speechRate = 0.85f
    private var dialect = ArabicDialect.LIBYAN
    private var preferredVoice: String? = null
    private var pending: (() -> Unit)? = null
    private var pendingCancel: (() -> Unit)? = null

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
        // Device TTS callbacks are not guaranteed to arrive on our chosen thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post { finishInit(status) }
        } else {
            finishInit(status)
        }
    }

    private fun finishInit(status: Int) {
        val engine = tts
        if (status != TextToSpeech.SUCCESS || engine == null) {
            val cancel = pendingCancel
            pending = null
            pendingCancel = null
            cancel?.invoke()
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
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                main.post {
                    if (utteranceId != null && utteranceId == this@SpeechManager.utteranceId) {
                        onSpeechStart?.invoke()
                    }
                }
            }
            override fun onDone(utteranceId: String?) = finishUtterance(utteranceId)
            @Deprecated("Older TTS callback")
            override fun onError(utteranceId: String?) = finishUtterance(utteranceId, failed = true)
            override fun onError(utteranceId: String?, errorCode: Int) = finishUtterance(utteranceId, failed = true)
        })
        ready = true
        configureEngine()
        pending?.also { pending = null; pendingCancel = null; it.invoke() }
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

    fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onCanceled: () -> Unit = onDone,
    ) {
        if (text.isBlank()) { onCanceled(); return }
        if (tts == null) createEngine()
        if (tts == null) { onCanceled(); return }
        val action = { speakNow(text, onStart, onDone, onCanceled) }
        if (ready) action() else {
            val prior = pendingCancel
            pending = action
            pendingCancel = onCanceled
            prior?.invoke()
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        runCatching { tts?.setSpeechRate(rate) }
    }

    fun stop() {
        val canceled = onSpeechCanceled ?: pendingCancel
        pending = null
        pendingCancel = null
        utteranceId = null
        onSpeechStart = null
        onSpeechDone = null
        onSpeechCanceled = null
        val token = focusToken
        focusToken = null
        if (token != null) focus.release(token)
        runCatching { tts?.stop() }
        // Explicit interruption must NOT be interpreted as a completed sentence word.
        canceled?.invoke()
    }

    private fun finishUtterance(completedId: String?, failed: Boolean = false) {
        main.post {
            if (completedId != null && completedId == utteranceId) {
                utteranceId = null
                onSpeechStart = null
                val done = onSpeechDone
                val canceled = onSpeechCanceled
                onSpeechDone = null
                onSpeechCanceled = null
                // A callback can start a new word synchronously. Release this focus first.
                val token = focusToken
                focusToken = null
                if (token != null) focus.release(token)
                if (failed) canceled?.invoke() else done?.invoke()
            }
        }
    }

    private fun speakNow(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onCanceled: () -> Unit,
    ) {
        val engine = tts ?: run { onCanceled(); return }
        stop()
        val token = focus.acquire { stop() } ?: run { onCanceled(); return }
        val id = "sawti-${System.nanoTime()}"
        focusToken = token
        utteranceId = id
        onSpeechStart = onStart
        onSpeechDone = onDone
        onSpeechCanceled = onCanceled
        runCatching {
            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
            if (result == TextToSpeech.ERROR) {
                stop()
                _issue.value = SpeechIssue.EngineUnavailable
            } else {
                _issue.value = null
            }
        }.onFailure {
            stop()
            _issue.value = SpeechIssue.EngineUnavailable
        }
    }

}
