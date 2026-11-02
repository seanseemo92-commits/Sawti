package com.sawti.presentation.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.sawti.presentation.theme.Ink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionContrastTest {
    private fun ratio(a: Color, b: Color): Float {
        val x = a.luminance()
        val y = b.luminance()
        return (maxOf(x, y) + 0.05f) / (minOf(x, y) + 0.05f)
    }
    @Test fun amberUsesDarkContent() {
        val background = Color(0xFFF59E0B)
        assertEquals(Ink, actionContentColor(background))
        assertTrue(ratio(actionContentColor(background), background) >= 4.5f)
    }
    @Test fun navyUsesWhiteContent() {
        val background = Color(0xFF0F172A)
        assertEquals(Color.White, actionContentColor(background))
        assertTrue(ratio(actionContentColor(background), background) >= 4.5f)
    }
    @Test fun whiteBackgroundUsesInk() {
        assertEquals(Ink, actionContentColor(Color.White))
    }
    @Test fun hintColorsHaveContrastOnBothSurfaces() {
        assertTrue(ratio(Color(0xFF92400E), Color.White) >= 4.5f)
        assertTrue(ratio(Color(0xFFFBBF24), Color(0xFF151C28)) >= 4.5f)
    }
}
