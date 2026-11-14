package com.sawti.speech

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
    data object PlaybackFailed : SpeechIssue
}

data class InstalledArabicVoice(
    val name: String,
    val localeTag: String,
    val offline: Boolean,
)

data class DialectVoiceStatus(
    val initialized: Boolean = false,
    val requested: ArabicDialect = ArabicDialect.STANDARD,
    val actualTag: String? = null,
    val actualVoice: String? = null,
    val arabicVoiceAvailable: Boolean = false,
    val voices: List<InstalledArabicVoice> = emptyList(),
) {
    val usingFallback: Boolean
        get() = initialized && !arabicVoiceAvailable
}

/**
 * One stable Arabic TTS engine language is used for ALL dialects; colloquial speech
 * comes exclusively from the card's speechText. It never selects an ar-TN/ar-LY/
 * ar-DZ locale on dialect change. Installed engines differ in their Arabic support.
 * The selected engine may use its own network voice; this class does not install voice data.
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
    private var dialect = ArabicDialect.STANDARD
    private var preferredVoice: String? = null
    private var pending: (() -> Unit)? = null
    private var pendingCancel: (() -> Unit)? = null
    private var activeLanguage: Locale? = null
    private var lastEngineAttemptMs = 0L

    private val _issue = MutableStateFlow<SpeechIssue?>(null)
    val issue: StateFlow<SpeechIssue?> = _issue.asStateFlow()
    private val _voiceStatus = MutableStateFlow(DialectVoiceStatus())
    val voiceStatus: StateFlow<DialectVoiceStatus> = _voiceStatus.asStateFlow()

    init { createEngine() }

    private fun createEngine() {
        ready = false
        lastEngineAttemptMs = SystemClock.elapsedRealtime()
        tts = runCatching { TextToSpeech(appContext, this) }.getOrNull()
        if (tts == null) reportEngineInitializationFailure()
    }

    override fun onInit(status: Int) {
        // Device TTS callbacks are not guaranteed to arrive on our chosen thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post { finishInit(status) }
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
            reportEngineInitializationFailure()
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
            override fun onStop(utteranceId: String?, interrupted: Boolean) =
                finishUtterance(utteranceId, canceled = true)
            @Deprecated("Older TTS callback")
            override fun onError(utteranceId: String?) = finishUtterance(utteranceId, failed = true)
            override fun onError(utteranceId: String?, errorCode: Int) = finishUtterance(utteranceId, failed = true)
        })
        ready = true
        configureEngine()
        pending?.also { pending = null; pendingCancel = null; it.invoke() }
    }


    /** The UI dialect affects text/phonetics only, never the engine language. */
    fun setDialect(value: ArabicDialect, voiceName: String?) {
        val voiceChanged = preferredVoice != voiceName
        if (dialect == value && !voiceChanged && ready && _voiceStatus.value.initialized) return
        dialect = value
        preferredVoice = voiceName
        if (ready) {
            if (voiceChanged) configureEngine()
            else _voiceStatus.value = _voiceStatus.value.copy(requested = value)
        }
    }

    private fun supportedLanguage(engine: TextToSpeech, locale: Locale): Boolean =
        runCatching { engine.setLanguage(locale) >= TextToSpeech.LANG_AVAILABLE }.getOrDefault(false)

    /** Use only general Arabic. Missing Arabic data must not fall through to English/French. */
    private fun configureEngine() {
        val engine = tts ?: return
        val arabic = runCatching { engine.voices.orEmpty() }
            .getOrDefault(emptySet())
            .filter { it.locale.language == "ar" }
            .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))

        activeLanguage = ArabicTtsLocalePolicy.select { supportedLanguage(engine, it) }
        // Respect an explicitly chosen voice only if it uses the same general Arabic
        // language family. Never set an ar-TN/ar-LY/... voice on a dialect switch.
        val selected = arabic.firstOrNull {
            it.name == preferredVoice && ArabicTtsLocalePolicy.isGenericArabic(it.locale)
        }
        if (activeLanguage?.language == "ar" && selected != null) {
            runCatching { engine.setVoice(selected) }
        }
        refreshVoiceStatus(engine, arabic)
        _issue.value = if (_voiceStatus.value.arabicVoiceAvailable) null else SpeechIssue.MissingArabicVoice
    }

    private fun refreshVoiceStatus(engine: TextToSpeech, available: List<android.speech.tts.Voice>? = null) {
        val voice = runCatching { engine.voice }.getOrNull()
        val actualTag = voice?.locale?.toLanguageTag()
            ?: runCatching { engine.language?.toLanguageTag() }.getOrNull()
        val arabic = available ?: runCatching { engine.voices.orEmpty() }
            .getOrDefault(emptySet()).filter { it.locale.language == "ar" }
        _voiceStatus.value = DialectVoiceStatus(
            initialized = true,
            requested = dialect,
            actualTag = actualTag,
            actualVoice = voice?.name,
            arabicVoiceAvailable = ArabicTtsLocalePolicy.canSpeak(activeLanguage, actualTag),
            voices = arabic.map {
                InstalledArabicVoice(it.name, it.locale.toLanguageTag(), !it.isNetworkConnectionRequired)
            },
        )
    }

    private fun hasInstalledTtsEngine(): Boolean? = runCatching {
        appContext.packageManager.queryIntentServices(
            Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0
        ).isNotEmpty()
    }.getOrNull()

    /** Only show the "no engine installed" banner when package discovery confirms it. */
    private fun reportEngineInitializationFailure() {
        _issue.value = if (hasInstalledTtsEngine() == false) {
            SpeechIssue.EngineUnavailable
        } else {
            SpeechIssue.PlaybackFailed
        }
    }

    /**
     * Engine.speak can fail even after successful initialization (e.g. missing voice
     * data). Attempt the next fixed language fallback once, without changing dialect.
     * If every attempt fails, the engine returns to the language it had before.
     */
    private fun enqueueWithFallback(engine: TextToSpeech, text: String, id: String): Boolean {
        val initial = runCatching {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS
        }.getOrDefault(false)
        if (initial) return true
        val original = activeLanguage
        for (locale in ArabicTtsLocalePolicy.candidates()) {
            if (locale == activeLanguage || !supportedLanguage(engine, locale)) continue
            activeLanguage = locale
            refreshVoiceStatus(engine)
            if (!_voiceStatus.value.arabicVoiceAvailable) continue
            val retry = runCatching {
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS
            }.getOrDefault(false)
            if (retry) return true
        }
        // Do not leave a failed fallback language selected for the next utterance.
        original?.let { supportedLanguage(engine, it) }
        activeLanguage = original
        refreshVoiceStatus(engine)
        return false
    }

    fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onCanceled: () -> Unit = onDone,
    ) {
        if (text.isBlank()) { onCanceled(); return }
        if (text.length > TextToSpeech.getMaxSpeechInputLength()) {
            _issue.value = SpeechIssue.PlaybackFailed
            onCanceled()
            return
        }
        if (tts == null) {
            // A failing engine must not be re-created on every tap.
            if (SystemClock.elapsedRealtime() - lastEngineAttemptMs < ENGINE_RETRY_INTERVAL_MS) {
                onCanceled()
                return
            }
            createEngine()
        }
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

    /** Release the engine and pending callbacks when the owning application is disposed. */
    fun shutdown() {
        stop()
        pending = null
        pendingCancel = null
        onSpeechStart = null
        onSpeechDone = null
        onSpeechCanceled = null
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }

    private fun finishUtterance(completedId: String?, failed: Boolean = false, canceled: Boolean = false) {
        main.post {
            if (completedId != null && completedId == utteranceId) {
                utteranceId = null
                onSpeechStart = null
                val done = onSpeechDone
                val cancelCallback = onSpeechCanceled
                onSpeechDone = null
                onSpeechCanceled = null
                // A callback can start a new word synchronously. Release this focus first.
                val token = focusToken
                focusToken = null
                if (token != null) focus.release(token)
                if (failed) {
                    _issue.value = SpeechIssue.PlaybackFailed
                    cancelCallback?.invoke()
                } else if (canceled) cancelCallback?.invoke()
                else done?.invoke()
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
        // Retry configuration after a user installs voice data while the app stays open.
        if (!_voiceStatus.value.arabicVoiceAvailable) configureEngine()
        if (!_voiceStatus.value.arabicVoiceAvailable) {
            _issue.value = SpeechIssue.MissingArabicVoice
            onCanceled()
            return
        }
        // A brief notification may duck TTS, but must not cancel the word.
        // TTS does not support reliable mid-utterance pause/resume: a phone
        // call or other exclusive transient interruption still cancels it.
        val token = focus.acquire(onLost = { stop() }, onDuck = {}) ?: run { onCanceled(); return }
        val id = "sawti-${System.nanoTime()}"
        focusToken = token
        utteranceId = id
        onSpeechStart = onStart
        onSpeechDone = onDone
        onSpeechCanceled = onCanceled
        if (enqueueWithFallback(engine, text, id)) {
            _issue.value = null
        } else {
            stop()
            _issue.value = if (_voiceStatus.value.arabicVoiceAvailable)
                SpeechIssue.PlaybackFailed else SpeechIssue.MissingArabicVoice
        }
    }

    private companion object {
        const val ENGINE_RETRY_INTERVAL_MS = 5_000L
    }
}
