package com.sawti.presentation

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.sawti.R
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.*
import com.sawti.presentation.common.SentenceStrip
import com.sawti.presentation.home.HomeScreen
import com.sawti.presentation.navigation.CaregiverBanner
import com.sawti.presentation.onboarding.SpotlightOverlay
import com.sawti.presentation.onboarding.SpotlightRegistry
import com.sawti.presentation.onboarding.TourGuide
import com.sawti.presentation.practice.*
import com.sawti.presentation.progress.ProgressScreen
import com.sawti.presentation.theme.SawtiTheme
import java.io.File
import java.lang.reflect.Proxy
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w320dp-h640dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class DesignAuditLayoutTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var view: View
    private val baseline = System.getenv("SAWTI_AUDIT_BASELINE") == "1"
    private val issues = mutableListOf<String>()
    private val calls = mutableListOf<String>()
    private val actions = Proxy.newProxyInstance(PracticeActions::class.java.classLoader,
        arrayOf(PracticeActions::class.java)) { proxy, method, args ->
        when (method.name) {
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            "toString" -> "Audit practice actions"
            else -> { calls += method.name; Unit }
        }
    } as PracticeActions

    private fun show(mode: ThemeMode, fontScale: Float = 2f, content: @Composable () -> Unit) {
        compose.setContent {
            SawtiTheme(themeMode = mode, performanceMode = true) {
                view = LocalView.current
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(Modifier.fillMaxSize().testTag("audit-root")) { content() }
                    }
                }
            }
        }
    }
    private fun checkText(text: String, scroll: Boolean = false): SemanticsNodeInteraction {
        val node = compose.onNodeWithText(text, useUnmergedTree = true)
        if (scroll) runCatching { node.performScrollTo() }
        runCatching { node.assertIsDisplayed() }.onFailure { issues += "Not visible: $text" }
        runCatching {
            val bounds = node.fetchSemanticsNode().boundsInRoot
            val root = compose.onNodeWithTag("audit-root").fetchSemanticsNode().boundsInRoot
            if (bounds.width <= 1f || bounds.height <= 1f || bounds.left < root.left - 1f || bounds.right > root.right + 1f)
                issues += "Outside horizontal viewport: $text"
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            if (layouts.any { layout -> layout.didOverflowWidth || layout.didOverflowHeight ||
                    (0 until layout.lineCount).any { layout.isLineEllipsized(it) } }) issues += "Text clipped: $text"
        }.onFailure { issues += "Cannot measure $text: ${it.javaClass.simpleName}" }
        return node
    }
    private fun tap(node: SemanticsNodeInteraction) {
        if (baseline) runCatching { node.performClick() }.onFailure { issues += "Control cannot be tapped" }
        else node.performClick()
    }
    private fun save(name: String) {
        compose.runOnIdle {
            val directory = File("build/outputs/design-audit-" + if (baseline) "before" else "after").apply { mkdirs() }
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(directory, "$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
            File(directory, "$name.json").writeText(JSONObject().put("case", name).put("baseline", baseline)
                .put("issues", org.json.JSONArray(issues)).toString())
            println("DESIGN_AUDIT_CASE $name baseline=$baseline issues=${issues.size}: ${issues.joinToString()}")
        }
        if (!baseline) assertTrue("$name: ${issues.joinToString()}", issues.isEmpty())
    }
    private fun home(mode: ThemeMode, scale: Float, name: String) {
        var opened = false
        show(mode, scale) { HomeScreen(listOf(Category("greetings", "التحيات", 5, false, emptyList())), null, {}, { opened = true }, isLoading = false) }
        tap(checkText("القرآن الكريم"))
        if (!baseline) assertTrue(opened)
        save(name)
    }
    @Test fun homeLightNormal() = home(ThemeMode.LIGHT, 1f, "home-light-normal")
    @Test fun homeLightLarge() = home(ThemeMode.LIGHT, 2f, "home-light-large")
    @Test fun homeDarkLarge() = home(ThemeMode.DARK, 2f, "home-dark-large")
    private fun sentence(mode: ThemeMode, name: String) {
        var played = 0
        show(mode) { SentenceStrip(listOf(SentencePiece(1, "مع السلامة", "مَعْ السَّلَامَهْ", null, "")), null,
            { played++ }, {}, {}) }
        checkText("جملتي")
        tap(checkText("انطق"))
        if (!baseline) assertEquals(1, played)
        save(name)
    }
    @Test fun sentenceLightLarge() = sentence(ThemeMode.LIGHT, "sentence-light-large")
    @Test fun sentenceDarkLarge() = sentence(ThemeMode.DARK, "sentence-dark-large")
    @Test fun recallLargeControlsRemainReachableAndCanBeTapped() {
        val item = WordSeed.itemEntities().first { it.semanticKey == "greeting.goodbye" }.toDomain().copy(id = 1)
        show(ThemeMode.LIGHT) { PracticeScreen(PracticeUiState(ready = true, total = 8,
            card = PracticeCard(item, 5), revealed = true), actions) }
        val context = RuntimeEnvironment.getApplication()
        checkText(context.getString(R.string.practice_mode_recall), true)
        tap(checkText(context.getString(R.string.practice_known), true))
        tap(checkText(context.getString(R.string.practice_next), true))
        if (!baseline) assertTrue(calls.containsAll(listOf("known", "skip")))
        save("practice-recall-large")
    }
    @Test fun choiceLargeModeLabelIsNotClipped() {
        val item = WordSeed.itemEntities().first { it.semanticKey == "food.water" }.toDomain().copy(id = 1)
        val card = PracticeCard(item, 0)
        show(ThemeMode.DARK) { PracticeScreen(PracticeUiState(exercise = Exercise.CHOICE, ready = true,
            total = 8, card = card, choices = listOf(card, card.copy(item = item.copy(id = 2)))), actions) }
        val context = RuntimeEnvironment.getApplication()
        checkText(context.getString(R.string.practice_mode_choice), true)
        tap(checkText(context.getString(R.string.choice_replay), true))
        if (!baseline) assertTrue("replayQuestion" in calls)
        save("practice-choice-large")
    }
    @Test fun progressLargeResetRemainsReachable() {
        show(ThemeMode.DARK) { ProgressScreen(ProgressSummary(recallAttempts = 125, selfRecalled = 100,
            cued = 20, shown = 5, choiceAttempts = 60, choiceCorrect = 50), {}) }
        val context = RuntimeEnvironment.getApplication()
        checkText(context.getString(R.string.progress_reset), true)
        save("progress-dark-large")
    }
    @Test fun caregiverLargeSettingsAndProgressAreReachable() {
        var opened = 0
        show(ThemeMode.LIGHT) { CaregiverBanner({ opened++ }, { opened++ }) }
        val context = RuntimeEnvironment.getApplication()
        tap(checkText(context.getString(R.string.progress_tab)))
        tap(checkText(context.getString(R.string.settings_tab)))
        if (!baseline) assertEquals(2, opened)
        save("caregiver-large")
    }
    @Test fun tourLargeAllThreeControlsRemainReachable() {
        var taps = 0
        val step = TourGuide.steps(TourGuide.HOME, setOf("daily"))[1]
        show(ThemeMode.DARK) { SpotlightOverlay(step, 1, 3, SpotlightRegistry(), true,
            { taps++ }, { taps++ }, { taps++ }) }
        for (label in listOf("تخطي", "السابق", "التالي")) tap(checkText(label, true))
        if (!baseline) assertEquals(3, taps)
        save("tour-dark-large")
    }

}
