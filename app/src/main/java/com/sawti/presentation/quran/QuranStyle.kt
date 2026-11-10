package com.sawti.presentation.quran

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark

// A packaged Unicode Quran font; no network or device-installed font is required.
internal val QuranFont = FontFamily(Font(R.font.amiri_quran))

@Immutable
internal data class QuranColors(
    val accent: Color,
    val gold: Color,
    val header: Color,
    val activeSurface: Color,
    val pressedSurface: Color,
)

@Composable
internal fun quranColors(): QuranColors = if (LocalIsDark.current) {
    QuranColors(Color(0xFF8EDBB9), Color(0xFFE8CE91), Color(0xFF103A31),
        Color(0xFF18392F), Color(0xFF203E35))
} else {
    QuranColors(Color(0xFF1B6652), Color(0xFFE8CE91), Color(0xFF124E40),
        Color(0xFFEAF5EE), Color(0xFFF0F7F2))
}
