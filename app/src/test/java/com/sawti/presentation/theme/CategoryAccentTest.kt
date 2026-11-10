package com.sawti.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryAccentTest {
    @Test fun predefinedSectionsKeepTheirOwnAccent() {
        assertEquals(Color(0xFF2D793A), categoryBannerColor("vegetables", Color.Black))
        assertEquals(Color(0xFFAF5300), categoryBannerColor("fruits", Color.Black))
        assertEquals(Color(0xFF1769A8), categoryBannerColor("family", Color.Black))
        assertEquals(Color(0xFF7A399D), categoryBannerColor("routine", Color.Black))
        assertEquals(Color(0xFF99502B), categoryBannerColor("cuisine", Color.Black))
        assertEquals(Color(0xFF126C80), categoryBannerColor("transport", Color.Black))
        assertEquals(Color(0xFF087E82), categoryBannerColor("daily", Color.Black))
    }

    @Test fun customSectionsKeepAssignedPalette() {
        val customAccent = Color(0xFF6B4DB2)
        assertEquals(customAccent, categoryBannerColor("custom_123", customAccent))
    }
}
