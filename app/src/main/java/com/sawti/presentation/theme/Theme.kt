package com.sawti.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.sawti.domain.model.ThemeMode

val AppBarBlue = Color(0xFF1E88E5)
val Ink = Color(0xFF1B2430)
val Canvas = Color(0xFFF4F7FB)
val HintAmber = Color(0xFFF59E0B)
val RecordRed = Color(0xFFC62828)
val CaregiverTint = Color(0xFFFFF3CD)
val CaregiverInk = Color(0xFF7A5200)

/** Soft pastel background + solid banner colour (white text is ≥ 4.5:1 on every banner). */
@Immutable
data class CategoryPalette(val pastel: Color, val banner: Color, val accent: Color)

private data class BasePalette(val pastel: Color, val banner: Color)

private val Palettes = listOf(
    BasePalette(Color(0xFFFCE4EC), Color(0xFFD81B60)), // 0 family
    BasePalette(Color(0xFFE0F2F1), Color(0xFF00796B)), // 1 daily
    BasePalette(Color(0xFFFBE9E7), Color(0xFFD84315)), // 2 fruits
    BasePalette(Color(0xFFE8F5E9), Color(0xFF388E3C)), // 3 vegetables
    BasePalette(Color(0xFFFFF3E0), Color(0xFFE65100)), // 4 food
    BasePalette(Color(0xFFF3E5F5), Color(0xFF7B1FA2)), // 5 clothes
    BasePalette(Color(0xFFEFEBE9), Color(0xFF5D4037)), // 6 home
    BasePalette(Color(0xFFE8EAF6), Color(0xFF3949AB)), // 7 actions
    BasePalette(Color(0xFFF9FBE7), Color(0xFF827717)), // 8 animals
    BasePalette(Color(0xFFE0F7FA), Color(0xFF00838F)), // 9 body
    BasePalette(Color(0xFFFFF8E1), Color(0xFF8D6E00)), // 10 Libyan foods
    BasePalette(Color(0xFFECEFF1), Color(0xFF455A64)), // 11 routine & faith
)

/** Replaces the system haptics when the caregiver turns them off. */
private object NoHaptics : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

/** True while the dark scheme is active. */
val LocalIsDark = staticCompositionLocalOf { false }

/** Performance Mode: no spring/alpha animations or shadows, smaller decoded images. */
val LocalPerformanceMode = staticCompositionLocalOf { false }

/** Card shadow that disappears in Performance Mode. */
@Composable
fun cardElevation(base: Dp): Dp = if (LocalPerformanceMode.current) 0.dp else base

/** Category colours; in dark mode the pastel becomes a deep tint and [CategoryPalette.accent] a lighter text colour. */
@Composable
fun paletteOf(index: Int): CategoryPalette {
    val base = Palettes[index.mod(Palettes.size)]
    return if (LocalIsDark.current) {
        CategoryPalette(
            pastel = base.banner.copy(alpha = 0.22f).compositeOver(DarkSurface),
            banner = base.banner,
            accent = lerp(base.banner, Color.White, 0.55f),
        )
    } else {
        CategoryPalette(base.pastel, base.banner, base.banner)
    }
}

private val DarkSurface = Color(0xFF1B222B)

private val LightScheme = lightColorScheme(
    primary = AppBarBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E9FB),
    onPrimaryContainer = Color(0xFF0B3A66),
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6ECF4),
    onSurfaceVariant = Color(0xFF45515F),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF00243F),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD6E9FB),
    background = Color(0xFF10151B),
    onBackground = Color(0xFFE8EDF3),
    surface = DarkSurface,
    onSurface = Color(0xFFE8EDF3),
    surfaceVariant = Color(0xFF2A3440),
    onSurfaceVariant = Color(0xFFB0BAC6),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * The app is Arabic-first, so the whole UI is laid out right-to-left regardless of the phone's language.
 * [themeMode] = System / Light / Dark; the blue app bar stays #1E88E5 in every mode.
 */
@Composable
fun SawtiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    performanceMode: Boolean = false,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalIsDark provides dark,
        LocalPerformanceMode provides performanceMode,
        LocalHapticFeedback provides (if (hapticsEnabled) LocalHapticFeedback.current else NoHaptics),
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = Typography(),
            shapes = AppShapes,
            content = content,
        )
    }
}
