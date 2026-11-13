package com.sawti.presentation.quran

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.sawti.R
import com.sawti.domain.model.ThemeMode
import com.sawti.domain.model.TouchDwell
import com.sawti.presentation.common.LocalTouchDwell
import com.sawti.presentation.theme.SawtiTheme
import java.io.File
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
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class QuranLayoutTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var captureView: View
    private val verse = QuranVerse(107, 3, 6200, "وَلَا يَحُضُّ عَلَىٰ طَعَامِ ٱلْمِسْكِينِ")

    private fun show(mode: ThemeMode, large: Boolean = false, active: Boolean = false,
        dwell: TouchDwell = TouchDwell.OFF, onPlay: () -> Unit = {}, onFull: () -> Unit = {},
        tracked: Boolean = false, trackingPositionMs: Int = 2000) {
        compose.setContent {
            SawtiTheme(themeMode = mode, performanceMode = true) {
                captureView = LocalView.current
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, if (large) 2f else 1f),
                    LocalTouchDwell provides dwell) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        QuranContent(QuranUiState(surahId = 107, reciter = QuranReciter.HUSARY_MUALLIM,
                            verses = listOf(verse), savedAyahs = 1,
                            fullCues = if (tracked) listOf(QuranFullCue(verse.itemKey,100,5000)) else emptyList(),
                            audio = if (tracked) RecitationStatus(
                                phase = RecitationPhase.PLAYING, remaining = 1,
                                fullSelectionId = 107, positionMs = trackingPositionMs)
                                else if (active) RecitationStatus(6200, RecitationPhase.PLAYING, 1)
                                else RecitationStatus()),
                            107, {}, {}, {}, { onPlay() }, {}, {}, {}, onPlayFull = onFull)
                    }
                }
            }
        }
    }

    private fun save(name: String) {
        val dir = File("build/outputs/quran-review").apply { mkdirs() }
        // Compose 1.7 captureToImage waits for a hardware frame-commit callback
        // absent in Robolectric. Draw the measured native-graphics view directly.
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(captureView.width, captureView.height, Bitmap.Config.ARGB_8888)
            captureView.draw(Canvas(bitmap))
            File(dir, "$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }

    @Test fun lightLayoutAndRepeatedAyahTaps() {
        var taps = 0
        show(ThemeMode.LIGHT, onPlay = { taps++ })
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed().performClick().performClick()
        assertEquals(2, taps)
        save("quran-light")
    }

    @Test fun darkActiveAyahKeepsLocalStopAndReadableText() {
        show(ThemeMode.DARK, active = true)
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("quran-text-6200", useUnmergedTree = true).assertIsDisplayed()
        save("quran-dark")
    }

    @Test fun largeTextAndLongReciterNameRemainReachable() {
        show(ThemeMode.LIGHT, large = true)
        compose.onNodeWithText(QuranReciter.HUSARY_MUALLIM.displayName).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed()
        save("quran-large-text")
    }

    @Test fun fullRecitationButtonIsBelowReciterSettingsAndFiresOneAction() {
        var starts = 0
        show(ThemeMode.LIGHT, onFull = { starts++ })
        compose.onNodeWithTag("quran-full-play").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, starts) }
    }

    @Test fun continuousRecitationVisuallyTracksCurrentVerse() {
        show(ThemeMode.LIGHT, tracked = true)
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("quran-tracking-ayah-6200", useUnmergedTree = true).assertExists()
    }

    @Test fun verseGlowDoesNotDisappearAfterFirstReadingOrDuringRepeatedAyah() {
        // Original segment cue ends at 5s, but reciter may repeat its ayah
        // inside the same uninterrupted source before starting the next.
        show(ThemeMode.LIGHT, tracked = true, trackingPositionMs = 7000)
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("quran-tracking-ayah-6200", useUnmergedTree = true).assertExists()
    }

    @Test fun selectedPassagesHaveTheirOwnGoldSeparatorAfterThirteenSurahs() {
        compose.setContent {
            SawtiTheme(themeMode = ThemeMode.LIGHT, performanceMode = true) {
                QuranContent(QuranUiState(), null, {}, {}, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithTag("quran-content").performScrollToIndex(15)
        compose.onNodeWithTag("quran-selected-verses-divider").assertIsDisplayed()
        compose.onNodeWithText("الآيات المختارة").assertIsDisplayed()
    }

    @Test fun packagedQuranFontSupportsQuranicMarks() {
        val typeface = ResourcesCompat.getFont(RuntimeEnvironment.getApplication(), R.font.amiri_quran)
        assertNotNull(typeface)
        val paint = Paint().apply { this.typeface = typeface; textSize = 60f }
        for (sample in listOf("ب", "ٱ", "ى", "ق", "ۖ", "ۗ", "ۡ")) {
            assertTrue("Packaged Quran font lacks $sample", paint.hasGlyph(sample))
        }
    }

    @Test fun ayahRespectsTheSameTouchDwellAsOtherSawtiCards() {
        var taps = 0
        show(ThemeMode.LIGHT, dwell = TouchDwell.LONG, onPlay = { taps++ })
        val card = compose.onNodeWithTag("ayah-6200").performScrollTo()
        card.performTouchInput { down(center); advanceEventTime(80); up() }
        compose.runOnIdle { assertEquals(0, taps) }
        card.performTouchInput { down(center); advanceEventTime(600); up() }
        compose.runOnIdle { assertEquals(1, taps) }
    }

    @Test @Config(sdk = [36], qualifiers = "w320dp-h640dp")
    fun android16CompactQuranRetainsReadableVerse() {
        show(ThemeMode.LIGHT, large = true)
        compose.onNodeWithTag("ayah-6200").performScrollTo().assertIsDisplayed()
    }

    @Test @Config(sdk = [36], qualifiers = "w840dp-h1100dp")
    fun android16TabletQuranRetainsFullPlaybackControl() {
        show(ThemeMode.DARK, large = true)
        compose.onNodeWithTag("quran-full-play").performScrollTo().assertIsDisplayed()
    }

}
