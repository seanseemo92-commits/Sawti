package com.sawti.presentation.practice

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.sawti.domain.model.WordItem
import com.sawti.presentation.theme.SawtiTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class PracticeLayoutTest {
    @get:Rule val compose = createComposeRule()

    private val actions = object : PracticeActions {
        override fun setExercise(exercise: Exercise) = Unit
        override fun hint() = Unit
        override fun reveal() = Unit
        override fun known() = Unit
        override fun skip() = Unit
        override fun choose(wordId: Long) = Unit
        override fun replayQuestion() = Unit
        override fun restart() = Unit
    }

    private fun card(id: Long, title: String, key: String) = PracticeCard(
        item = WordItem(id = id, categoryId = "fruits", subgroup = "", displayText = title,
            ttsPrompt = title, phonemicCue = "", sentencePrompt = null,
            audioPath = null, photoPath = null, phone = "", emoji = "🍑", semanticKey = key),
        palette = 2,
    )

    private val first = card(1, "مشمش", "fruit.apricot")
    private val second = card(2, "تفاح", "fruit.apple")

    private fun show(exercise: Exercise, fontScale: Float = 1f) {
        val state = PracticeUiState(exercise = exercise, ready = true,
            card = first, total = 10,
            choices = if (exercise == Exercise.CHOICE) listOf(first, second) else emptyList())
        compose.setContent {
            SawtiTheme(performanceMode = true) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    PracticeScreen(state, actions)
                }
            }
        }
    }

    @Test fun namingModeAlwaysHasVisiblePhotoArea() {
        show(Exercise.RECALL)
        val image = compose.onNodeWithTag("practice-recall-image", useUnmergedTree = true)
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Naming photo should have usable height", image.height > 140f)
    }

    @Test fun choiceModeKeepsBothPictureCardsSideBySide() {
        show(Exercise.CHOICE)
        val a = compose.onNodeWithTag("practice-choice-1", useUnmergedTree = true)
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val b = compose.onNodeWithTag("practice-choice-2", useUnmergedTree = true)
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Both choices must be on one row", abs(a.top - b.top) < 2f)
        assertTrue("Choices must not overlap", a.right <= b.left || b.right <= a.left)
        assertTrue("Choices must have visible image space", a.height > 100f && b.height > 100f)
    }

    @Test @Config(qualifiers = "w320dp-h720dp")
    fun compactPhoneAtDoubleFontKeepsBothOptionsReachable() {
        show(Exercise.CHOICE, fontScale = 2f)
        compose.onNodeWithTag("practice-choice-1", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("practice-choice-2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test @Config(qualifiers = "w320dp-h720dp")
    fun compactPhoneAtDoubleFontRetainsNamingImage() {
        show(Exercise.RECALL, fontScale = 2f)
        val bounds = compose.onNodeWithTag("practice-recall-image", useUnmergedTree = true)
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.height > 120f)
    }
}
