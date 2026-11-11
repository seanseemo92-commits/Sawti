package com.sawti.presentation.quran

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
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
class QuranPassageLayoutTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var capture: View
    private val app: Application get() = RuntimeEnvironment.getApplication()

    private fun show(id: Int?, dark: Boolean = false, large: Boolean = false,
        active: Boolean = false, onPlay: (QuranVerse) -> Unit = {}, onChoose: (Int?) -> Unit = {}) {
        val parts = id?.let { QuranPassages.load(app, it) }.orEmpty()
        val playing = parts.firstOrNull()
        compose.setContent {
            SawtiTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, performanceMode = true) {
                capture = LocalView.current
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, if (large) 2f else 1f)) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        QuranContent(QuranUiState(surahId = id, verses = parts, savedAyahs = parts.size,
                            reciter = QuranReciter.HUSARY_MUALLIM,
                            audio = if (active && playing != null) RecitationStatus(playing.globalNumber,
                                RecitationPhase.PLAYING, 3, segmentKey = playing.segmentKey) else RecitationStatus()),
                            id, onChoose, {}, {}, onPlay, {}, {}, {})
                    }
                }
            }
        }
    }

    private fun save(name: String) {
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(capture.width, capture.height, Bitmap.Config.ARGB_8888)
            capture.draw(Canvas(bitmap))
            val file = File("build/outputs/quran-passages-review/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
    }

    @Test fun ayatAlKursiUsesTheSameScrollableCardsAndRepeatedTaps() {
        val taps = mutableListOf<String>()
        show(2001, onPlay = { taps += it.itemKey })
        compose.onNodeWithTag("segment-2_255_1").performScrollTo().assertIsDisplayed().performClick().performClick()
        assertEquals(listOf("segment-2_255_1", "segment-2_255_1"), taps)
        save("kursi-light")
        compose.onNodeWithTag("segment-2_255_9").performScrollTo().assertIsDisplayed().performClick()
        assertEquals("segment-2_255_9", taps.last())
    }

    @Test fun onlySelectedPartOfAnAyahGetsTheActiveStopAction() {
        show(2001, dark = true, active = true)
        compose.onNodeWithTag("segment-2_255_1").performScrollTo().assertIsDisplayed()
        save("kursi-dark-active")
        compose.onAllNodesWithText("إيقاف التلاوة · المتبقي 3").assertCountEquals(1)
        compose.onNodeWithTag("segment-2_255_2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("segment-2_255_9").performScrollTo().assertIsDisplayed()
    }

    @Test fun lastBaqarahPartAndLongReciterNameStayReachableAtDoubleTextSize() {
        show(2002, large = true)
        compose.onNodeWithText(QuranReciter.HUSARY_MUALLIM.displayName).performScrollTo().assertIsDisplayed()
        save("baqarah-large-settings")
        compose.onNodeWithTag("segment-2_284_1").performScrollTo().assertIsDisplayed()
        save("baqarah-large-first")
        compose.onNodeWithTag("segment-2_286_8").performScrollTo().assertIsDisplayed()
        save("baqarah-large-last")
    }

    @Test fun bothNewEntriesCanBeSelectedIndependentlyInTheExistingList() {
        var chosen: Int? = null
        show(null, onChoose = { chosen = it })
        compose.onNodeWithTag("surah-2001").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(2001, chosen)
        compose.onNodeWithTag("surah-2002").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(2002, chosen)
        save("passages-list")
    }
}
