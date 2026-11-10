package com.sawti.presentation.quran

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.sawti.R
import com.sawti.domain.model.ThemeMode
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
    private val verse = QuranVerse(107, 3, 6200, "وَلَا يَحُضُّ عَلَىٰ طَعَامِ ٱلْمِسْكِينِ")

    private fun show(mode: ThemeMode, large: Boolean = false, active: Boolean = false, onPlay: () -> Unit = {}) {
        compose.setContent {
            SawtiTheme(themeMode = mode, performanceMode = true) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, if (large) 2f else 1f)) {
                    Box(Modifier.fillMaxSize()) {
                        QuranContent(QuranUiState(surahId = 107, reciter = QuranReciter.HUSARY_MUALLIM,
                            verses = listOf(verse), savedAyahs = 1,
                            audio = if (active) RecitationStatus(6200, RecitationPhase.PLAYING, 1) else RecitationStatus()),
                            107, {}, {}, {}, { onPlay() }, {}, {}, {})
                    }
                }
            }
        }
    }

    private fun save(name: String) {
        val dir = File("build/outputs/quran-review").apply { mkdirs() }
        compose.onNodeWithTag("quran-content").captureToImage().asAndroidBitmap().let { bitmap ->
            File(dir, "$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
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

    @Test fun packagedQuranFontSupportsQuranicMarks() {
        val typeface = ResourcesCompat.getFont(RuntimeEnvironment.getApplication(), R.font.amiri_quran)
        assertNotNull(typeface)
        val paint = Paint().apply { this.typeface = typeface; textSize = 60f }
        for (sample in listOf("ب", "ٱ", "ى", "ق", "ۖ", "ۗ", "ۡ")) {
            assertTrue("Packaged Quran font lacks $sample", paint.hasGlyph(sample))
        }
    }
}
