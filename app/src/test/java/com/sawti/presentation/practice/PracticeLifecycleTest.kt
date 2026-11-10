package com.sawti.presentation.practice

import android.app.Application
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.sawti.audio.AudioDispatcher
import com.sawti.audio.AudioFocusController
import com.sawti.audio.AudioPlayer
import com.sawti.audio.SoftChime
import com.sawti.domain.model.Category
import com.sawti.domain.model.WordItem
import com.sawti.domain.repository.ProgressRepository
import com.sawti.domain.repository.WordRepository
import com.sawti.speech.ManualTtsShadow
import com.sawti.speech.SpeechManager
import java.lang.reflect.Proxy
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowTextToSpeech

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, shadows = [ManualTtsShadow::class])
@LooperMode(LooperMode.Mode.PAUSED)
class PracticeLifecycleTest {
    private val scheduler = TestCoroutineScheduler()
    private lateinit var model: PracticeViewModel
    private lateinit var engine: ManualTtsShadow
    private var logged = 0

    @Before fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        val app = RuntimeEnvironment.getApplication() as Application
        val focus = AudioFocusController(app)
        ShadowTextToSpeech.addLanguageAvailability(Locale.forLanguageTag("ar"))
        val speech = SpeechManager(app, focus)
        engine = Shadow.extract(ShadowTextToSpeech.getLastTextToSpeechInstance())
        engine.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        val cards = listOf(
            WordItem(1, "transport", "سيارة", "سَيَّارَة", "🚗"),
            WordItem(2, "transport", "قطار", "قِطَار", "🚆"),
            WordItem(3, "transport", "طائرة", "طَائِرَة", "✈️"),
        )
        val words = Proxy.newProxyInstance(WordRepository::class.java.classLoader,
            arrayOf(WordRepository::class.java)) { _, method, _ ->
            when (method.name) {
                "observeCategories" -> flowOf(listOf(Category("transport", "المواصلات", 0, false, cards)))
                else -> error("Unexpected repository call: ${method.name}")
            }
        } as WordRepository
        val progress = Proxy.newProxyInstance(ProgressRepository::class.java.classLoader,
            arrayOf(ProgressRepository::class.java)) { _, method, _ ->
            check(method.name == "log")
            logged++
            Unit
        } as ProgressRepository
        model = PracticeViewModel(words, progress, AudioDispatcher(speech, AudioPlayer(focus)), SoftChime())
        scheduler.runCurrent()
        model.onEnterPractice()
        model.setExercise(Exercise.CHOICE)
    }

    @After fun close() {
        model.onLeavePractice()
        Dispatchers.resetMain()
    }

    @Test fun leavingBeforeTheQuestionDelayPreventsBackgroundSpeech() {
        scheduler.runCurrent()
        model.onLeavePractice()
        scheduler.advanceTimeBy(1000)
        scheduler.runCurrent()
        assertTrue(engine.requests.isEmpty())
        model.onEnterPractice()
        scheduler.runCurrent()
        scheduler.advanceTimeBy(350)
        scheduler.runCurrent()
        assertEquals(1, engine.requests.size)
    }

    @Test fun replayTapsReplacePendingQuestionsInsteadOfQueuingThem() {
        model.replayQuestion()
        model.replayQuestion()
        scheduler.runCurrent()
        scheduler.advanceTimeBy(350)
        scheduler.runCurrent()
        assertEquals(1, engine.requests.size)
    }

    @Test fun solvedQuestionDoesNotAdvanceWhileAwayAndResumesOnce() {
        val initial = model.uiState.value
        model.choose(initial.card!!.item.id)
        scheduler.runCurrent()
        assertEquals(1, logged)
        model.onLeavePractice()
        scheduler.advanceTimeBy(3000)
        scheduler.runCurrent()
        assertEquals(initial.index, model.uiState.value.index)
        assertTrue(model.uiState.value.solved)
        model.onEnterPractice()
        scheduler.runCurrent()
        scheduler.advanceTimeBy(1800)
        scheduler.runCurrent()
        assertEquals(initial.index + 1, model.uiState.value.index)
        assertEquals(1, logged)
    }
}
