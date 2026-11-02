package com.sawti.speech

import android.app.Application
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.sawti.audio.AudioFocusController
import com.sawti.domain.model.ArabicDialect
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowTextToSpeech
import java.util.Locale

/** Android control-flow tests only: the shadow synthesizes no audible speech. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class SpeechManagerArabicTest {
    private fun manager(): SpeechManager {
        val context = RuntimeEnvironment.getApplication()
        return SpeechManager(context, AudioFocusController(context))
    }

    @Test fun pendingSpeechIsCanceledWhenOnlyEnglishIsAvailableAndRecoversAfterArabicInstall() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.US)
        val manager = manager()
        val engine = ShadowTextToSpeech.getLastTextToSpeechInstance()
        val shadow = shadowOf(engine)
        var canceled = 0
        var done = 0
        manager.speak("نَبِّيْ إِمِّيَّهْ", onDone = { done++ }, onCanceled = { canceled++ })
        shadow.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, canceled)
        assertEquals(0, done)
        assertNull(shadow.lastSpokenText)
        assertEquals(SpeechIssue.MissingArabicVoice, manager.issue.value)
        assertFalse(manager.voiceStatus.value.arabicVoiceAvailable)

        ShadowTextToSpeech.addLanguageAvailability(Locale.forLanguageTag("ar"))
        manager.speak("نَبِّيْ إِمِّيَّهْ", onDone = { done++ }, onCanceled = { canceled++ })
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals("نَبِّيْ إِمِّيَّهْ", shadow.lastSpokenText)
        assertEquals(1, canceled)
        assertEquals(1, done)
        assertNull(manager.issue.value)
        assertTrue(manager.voiceStatus.value.arabicVoiceAvailable)
    }

    @Test fun dialectChangesPreserveWrittenSpeechAndGeneralArabicEngine() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.forLanguageTag("ar"))
        val manager = manager()
        val shadow = shadowOf(ShadowTextToSpeech.getLastTextToSpeechInstance())
        shadow.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        for (dialect in ArabicDialect.entries) {
            manager.setDialect(dialect, null)
            manager.speak("نَبِّيْ إِمِّيَّهْ")
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals("نَبِّيْ إِمِّيَّهْ", shadow.lastSpokenText)
            assertEquals("ar", shadow.currentLanguage.toLanguageTag())
            assertEquals(dialect, manager.voiceStatus.value.requested)
        }
    }

    @Test fun anObservedForeignVoiceCannotPassAsArabicAfterLanguageSuccess() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.forLanguageTag("ar"))
        val manager = manager()
        val engine = ShadowTextToSpeech.getLastTextToSpeechInstance()
        val shadow = shadowOf(engine)
        // The shadow retains an explicit voice on setLanguage, modeling a mismatched engine response.
        engine.setVoice(Voice("foreign", Locale.US, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, emptySet()))
        shadow.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        var canceled = 0
        manager.speak("مَاء", onCanceled = { canceled++ })
        assertEquals(1, canceled)
        assertNull(shadow.lastSpokenText)
        assertEquals(SpeechIssue.MissingArabicVoice, manager.issue.value)
    }
}
