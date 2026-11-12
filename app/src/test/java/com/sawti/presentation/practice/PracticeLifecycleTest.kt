package com.sawti.presentation.practice

import android.app.Application
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.sawti.audio.AudioDispatcher
import com.sawti.audio.AudioFocusController
import com.sawti.audio.AudioPlayer
import com.sawti.audio.SoftChime
import com.sawti.domain.model.Category
import com.sawti.domain.model.GreetingsCatalog
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
import kotlinx.coroutines.flow.flow
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
    private lateinit var voice: AudioDispatcher
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
        // Greeting photos are visible in picture naming, but nonsensical
        // in the \"أين...؟\" choice questions shown in the user's recording.
        val greetings = listOf(
            WordItem(4, GreetingsCatalog.CATEGORY_ID, "السلام عليكم", "السَّلَامُ عَلَيْكُمْ", "🤝"),
            WordItem(5, GreetingsCatalog.CATEGORY_ID, "صباح الخير", "صَبَاحُ الْخَيْرِ", "🌅"),
        )
        val words = Proxy.newProxyInstance(WordRepository::class.java.classLoader,
            arrayOf(WordRepository::class.java)) { _, method, _ ->
            when (method.name) {
                "observeCategories" -> flowOf(listOf(
                    Category("transport", "المواصلات", 0, false, cards),
                    Category(GreetingsCatalog.CATEGORY_ID, "التحيات", 0, false, greetings),
                ))
                else -> error("Unexpected repository call: ${method.name}")
            }
        } as WordRepository
        val progress = Proxy.newProxyInstance(ProgressRepository::class.java.classLoader,
            arrayOf(ProgressRepository::class.java)) { _, method, _ ->
            check(method.name == "log")
            logged++
            Unit
        } as ProgressRepository
        voice = AudioDispatcher(speech, AudioPlayer(focus))
        model = PracticeViewModel(words, progress, voice, SoftChime())
        scheduler.runCurrent()
        model.onEnterPractice()
        model.setExercise(Exercise.CHOICE)
    }

    @Test fun differentDishPhotosSurviveTheirSharedEmoji() {
        val dishes = listOf(
            WordItem(20, "cuisine", "طاجين تونسي", "طاجين", "🍳").copy(semanticKey = "dish.tunisian.tajine"),
            WordItem(21, "cuisine", "عجة بالمرقاز", "عجة", "🍳").copy(semanticKey = "dish.tunisian.merguez_omelette"),
        )
        val method = PracticeViewModel::class.java.getDeclaredMethod("buildPool", List::class.java, Exercise::class.java)
        method.isAccessible = true
        val pool = method.invoke(model, listOf(Category("cuisine", "الأكلات", 0, false, dishes)), Exercise.CHOICE) as List<*>
        assertEquals(2, pool.size)
    }

    @After fun close() {
        model.onLeavePractice()
        Dispatchers.resetMain()
    }

    @Test fun retryAfterAnObservationFailureReplacesStaleCardsAndCancelsOldSpeech() {
        recoverAfterObservationFailure { model.restart() }
    }

    @Test fun switchingExerciseAfterAnObservationFailureResubscribesToFreshCards() {
        recoverAfterObservationFailure { model.setExercise(Exercise.CHOICE) }
        assertEquals(Exercise.CHOICE, model.uiState.value.exercise)
        assertTrue(model.uiState.value.choices.all { it.item.id >= 200L })
    }

    private fun recoverAfterObservationFailure(retry: () -> Unit) {
        model.onLeavePractice()
        var subscriptions = 0
        val words = Proxy.newProxyInstance(WordRepository::class.java.classLoader,
            arrayOf(WordRepository::class.java)) { _, method, _ ->
            check(method.name == "observeCategories")
            flow {
                subscriptions++
                val id = if (subscriptions == 1) 100L else 200L
                emit(listOf(Category("transport", "المواصلات", 0, false, listOf(
                    WordItem(id, "transport", "سيارة", "سيارة", "🚗"),
                    WordItem(id + 1, "transport", "قطار", "قطار", "🚆"),
                ))))
                if (subscriptions == 1) throw java.io.IOException("interrupted observation")
            }
        } as WordRepository
        val progress = Proxy.newProxyInstance(ProgressRepository::class.java.classLoader,
            arrayOf(ProgressRepository::class.java)) { _, _, _ -> Unit } as ProgressRepository
        model = PracticeViewModel(words, progress, voice, SoftChime())
        scheduler.runCurrent()
        assertTrue(model.uiState.value.loadError)
        retry()
        assertFalse(model.uiState.value.ready)
        scheduler.runCurrent()
        val recovered = model.uiState.value
        assertEquals(2, subscriptions)
        assertFalse(recovered.loadError)
        assertTrue(recovered.ready)
        assertEquals(2, recovered.total)
        assertTrue(recovered.card!!.item.id >= 200L)
        scheduler.advanceTimeBy(1000)
        scheduler.runCurrent()
        assertTrue(engine.requests.isEmpty())
    }

    @Test fun greetingCategoryIsNotAWhereQuestionOrDistractor() {
        assertFalse(practiceCategoryEligible(GreetingsCatalog.CATEGORY_ID, Exercise.CHOICE))
        assertTrue(practiceCategoryEligible(GreetingsCatalog.CATEGORY_ID, Exercise.RECALL))
        assertFalse(practiceCategoryEligible("actions", Exercise.CHOICE))
        // Rebuild a shuffled question deck several times. Neither the target
        // nor its alternative may contain a greeting.
        repeat(12) {
            model.restart()
            val state = model.uiState.value
            assertEquals(Exercise.CHOICE, state.exercise)
            assertTrue(state.total > 0)
            assertNotNull(state.card)
            assertEquals("transport", state.card!!.item.categoryId)
            assertEquals(2, state.choices.size)
            assertTrue(state.choices.all { it.item.categoryId == "transport" })
        }
    }

    @Test fun greetingsStayAvailableInPictureNamingPractice() {
        model.setExercise(Exercise.RECALL)
        val initial = model.uiState.value
        assertEquals(5, initial.total)
        val seen = mutableSetOf<String>()
        while (!model.uiState.value.finished) {
            val card = model.uiState.value.card ?: break
            seen += card.item.categoryId
            model.skip()
        }
        assertTrue("Picture naming should still include greetings", GreetingsCatalog.CATEGORY_ID in seen)
        assertTrue("Other categories must remain", "transport" in seen)
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

