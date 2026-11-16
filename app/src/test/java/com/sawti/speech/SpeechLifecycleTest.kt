package com.sawti.speech

import android.app.Application
import android.os.Bundle
import android.os.Looper
import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import com.sawti.audio.AudioFocusController
import com.sawti.audio.AudioDispatcher
import com.sawti.audio.AudioPlayer
import com.sawti.domain.model.WordItem
import java.util.Locale
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowTextToSpeech

/** Controls real engine callback order without simulating audible speech. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, shadows = [ManualTtsShadow::class])
@LooperMode(LooperMode.Mode.PAUSED)
class SpeechLifecycleTest {
    private lateinit var manager: SpeechManager
    private lateinit var engine: ManualTtsShadow

    @Before fun setup() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.forLanguageTag("ar"))
        val app = RuntimeEnvironment.getApplication() as Application
        manager = SpeechManager(app, AudioFocusController(app))
        engine = Shadow.extract(ShadowTextToSpeech.getLastTextToSpeechInstance())
        engine.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test fun engineStopCancelsOnceAndLateCallbacksCannotEndTheNextWord() {
        var done = 0
        var canceled = 0
        manager.speak("سَيَّارَة", onDone = { done++ }, onCanceled = { canceled++ })
        val first = engine.requests.single()
        engine.utteranceProgressListener.onStop(first, true)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0, done)
        assertEquals(1, canceled)
        assertNull(manager.issue.value)
        manager.speak("قِطَار", onDone = { done++ }, onCanceled = { canceled++ })
        val second = engine.requests.last()
        engine.utteranceProgressListener.onDone(first)
        engine.utteranceProgressListener.onStop(first, false)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0, done)
        assertEquals(1, canceled)
        engine.utteranceProgressListener.onDone(second)
        shadowOf(Looper.getMainLooper()).idle()
        manager.stop()
        assertEquals(1, done)
        assertEquals(1, canceled)
    }

    @Test fun dispatcherResetsCardSpeakingStateOnExternalTtsCancellation() {
        val app = RuntimeEnvironment.getApplication() as Application
        val focus = AudioFocusController(app)
        val dispatcher = AudioDispatcher(manager, AudioPlayer(focus))
        val card = WordItem(1, "custom_transport", "", "سيارة", "سَيَّارَة", "سَـ...", null,
            null, null, "", "", semanticKey = "transport.car")
        dispatcher.play(card)
        assertTrue(dispatcher.playback.value.isSpeaking)
        val first = engine.requests.last()
        // A late callback from the old utterance must not clear the new card.
        dispatcher.play(card.copy(id = 2, displayText = "قطار", ttsPrompt = "قِطَار"))
        assertEquals(2L, dispatcher.playback.value.activeWordId)
        engine.utteranceProgressListener.onStop(first, true)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(dispatcher.playback.value.isSpeaking)
        val second = engine.requests.last()
        engine.utteranceProgressListener.onStop(second, true)
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(dispatcher.playback.value.isSpeaking)
        assertNull(dispatcher.playback.value.activeWordId)
        manager.stop()
    }

    @Test fun builtInCardNeverFallsBackToAndroidTtsWithoutNeuralClip() {
        val app = RuntimeEnvironment.getApplication() as Application
        val dispatcher = AudioDispatcher(manager, AudioPlayer(AudioFocusController(app)))
        val card = WordItem(10, "transport", "", "سيارة", "سَيَّارَة", "سَـ...", null,
            null, null, "", "", semanticKey = "transport.car")
        val before = engine.requests.size
        dispatcher.play(card)
        assertEquals(before, engine.requests.size)
        assertFalse(dispatcher.playback.value.isSpeaking)
        assertNotNull(dispatcher.neuralError.value)
    }

    @Test fun notificationDoesNotCancelTtsButTransientPhoneCallDoes() {
        val app = RuntimeEnvironment.getApplication() as Application
        val focus = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        var canceled = 0
        manager.speak("أهلاً", onCanceled = { canceled++ })
        val listener = shadowOf(focus).lastAudioFocusRequest.listener
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0, canceled)
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, canceled)
        manager.stop()
        assertEquals(1, canceled)
    }

    @Test fun oversizedSpeechFailsWithoutSubmittingAnInvalidEngineRequest() {
        var canceled = 0
        manager.speak("س".repeat(TextToSpeech.getMaxSpeechInputLength() + 1), onCanceled = { canceled++ })
        assertEquals(1, canceled)
        assertTrue(engine.requests.isEmpty())
        assertEquals(SpeechIssue.PlaybackFailed, manager.issue.value)
        manager.speak("قِطَار")
        assertEquals(1, engine.requests.size)
        assertNull(manager.issue.value)
        manager.stop()
    }
}

@Implements(TextToSpeech::class)
class ManualTtsShadow : ShadowTextToSpeech() {
    val requests = mutableListOf<String>()

    @Implementation
    override fun speak(text: CharSequence, queueMode: Int, params: Bundle?, utteranceId: String): Int {
        requests.add(utteranceId)
        return TextToSpeech.SUCCESS
    }
}
