package com.sawti.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderColorsTest {
    @Test fun sectionTargetsKeepIdentityInBothThemes() {
        val green = categoryBannerColor("vegetables", Color.Black)
        val blue = categoryBannerColor("family", Color.Black)
        assertEquals(green, headerColors(green, false).middle)
        assertEquals(blue, headerColors(blue, false).middle)
        assertTrue(headerColors(green, true).middle.luminance() < green.luminance())
    }

    @Test fun controlsStayReadableThroughoutSectionTransitions() {
        val ids = listOf("family", "cuisine", "daily", "routine", "vegetables", "fruits", "food", "home", "clothes", "body", "actions", "animals", "transport")
        for (dark in listOf(false, true)) {
            val palettes = listOf(headerColors(null, dark)) + ids.map { headerColors(categoryBannerColor(it, Color.Black), dark) }
            for (from in palettes) for (to in palettes) for (step in 0..20) {
                val t = step / 20f
                val value = HeaderColors(lerp(from.start, to.start, t), lerp(from.middle, to.middle, t), lerp(from.end, to.end, t))
                val ink = headerContentColor(value).luminance()
                for (background in listOf(value.start, value.middle, value.end)) {
                    val l = background.luminance()
                    val contrast = (maxOf(ink, l) + 0.05f) / (minOf(ink, l) + 0.05f)
                    assertTrue("Unreadable transition $from -> $to at $t", contrast >= 4.5f)
                }
            }
        }
    }
}
