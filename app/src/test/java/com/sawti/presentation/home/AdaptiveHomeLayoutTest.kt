package com.sawti.presentation.home

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import com.sawti.domain.model.Category
import com.sawti.presentation.theme.SawtiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AdaptiveHomeLayoutTest {
    @get:Rule val compose = createComposeRule()

    private fun render(fontScale: Float = 1f, categoryVisible: Boolean = true) {
        compose.setContent {
            SawtiTheme(performanceMode = true) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    HomeScreen(
                        categories = listOf(
                            Category("greetings", "التحيات", 5, false, emptyList()),
                            Category("family", "العائلة", 0, true, emptyList()),
                        ),
                        speakingItemId = null,
                        onOpen = {},
                        onOpenQuran = {},
                        isLoading = false,
                    )
                }
            }
        }
        compose.onNodeWithText("القرآن الكريم", useUnmergedTree = true).assertExists()
        if (categoryVisible) compose.onNodeWithText("التحيات", useUnmergedTree = true).assertExists()
    }

    @Test @Config(qualifiers = "w320dp-h640dp")
    fun compactPhoneAt320Dp() = render(fontScale = 2f)

    @Test @Config(qualifiers = "w320dp-h640dp")
    fun longMarkedArabicCategoryIsNotEllipsizedAtLargeTextScale() {
        val phrase = "أُرِيدُ الْمُسَاعَدَةَ لِلْوُصُولِ إِلَى الْمُسْتَشْفَى وَالْمُرَافَقَةِ فِي الْمَوَاعِيدِ"
        compose.setContent {
            SawtiTheme(performanceMode = true) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                    HomeScreen(
                        categories = listOf(Category("custom_adaptive", phrase, 2, false, emptyList())),
                        speakingItemId = null, onOpen = {}, onOpenQuran = {}, isLoading = false,
                    )
                }
            }
        }
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(phrase, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertTrue(results.single().lineCount > 2)
        assertFalse((0 until results.single().lineCount).any { results.single().isLineEllipsized(it) })
    }

    @Test @Config(qualifiers = "w360dp-h800dp")
    fun compactPhoneAt360Dp() = render(fontScale = 2f)

    @Test @Config(qualifiers = "w411dp-h320dp")
    fun landscapePhoneAt411Dp() = render(fontScale = 2f, categoryVisible = false)

    @Test @Config(qualifiers = "w600dp-h960dp")
    fun mediumWindowAt600Dp() = render(fontScale = 2f)

    @Test @Config(qualifiers = "w840dp-h1100dp")
    fun expandedTabletAt840Dp() = render(fontScale = 2f)
}
