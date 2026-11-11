package com.sawti.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** One background spans the toolbar and the transparent system status bar. */
@Immutable
data class HeaderColors(val start: Color, val middle: Color, val end: Color)

fun headerColors(sectionColor: Color?, dark: Boolean): HeaderColors {
    if (sectionColor == null) return if (dark) {
        HeaderColors(Color(0xFF071629), Color(0xFF123554), Color(0xFF081A31))
    } else {
        HeaderColors(Color(0xFF5845B2), Color(0xFF4859C4), Color(0xFF2566AB))
    }
    val base = if (dark) lerp(sectionColor, Color.Black, 0.32f) else sectionColor
    return HeaderColors(lerp(base, Color.Black, 0.10f), base, lerp(base, Color.Black, 0.18f))
}

/** Pick the same readable ink for app controls and the system status icons. */
fun headerContentColor(colors: HeaderColors): Color {
    val lightContrast = listOf(colors.start, colors.middle, colors.end)
        .minOf { 1.05f / (it.luminance() + 0.05f) }
    val darkContrast = listOf(colors.start, colors.middle, colors.end)
        .minOf { (it.luminance() + 0.05f) / 0.05f }
    return if (lightContrast >= darkContrast) Color.White else Color.Black
}
