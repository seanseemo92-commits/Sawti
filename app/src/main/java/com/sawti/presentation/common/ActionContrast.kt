package com.sawti.presentation.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.sawti.presentation.theme.Ink

/** Input is the opaque, composited button background. */
internal fun actionContentColor(background: Color): Color {
    val luminance = background.luminance()
    fun contrast(candidate: Color): Float {
        val other = candidate.luminance()
        return (maxOf(luminance, other) + 0.05f) / (minOf(luminance, other) + 0.05f)
    }
    return if (contrast(Ink) >= contrast(Color.White)) Ink else Color.White
}
