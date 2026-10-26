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

val AppBarBlue = Color(0xFF1E293B)
val Ink = Color(0xFF1E293B)
val Canvas = Color(0xFFF8FAFC)
val HintAmber = Color(0xFFF59E0B)
val RecordRed = Color(0xFFC62828)
val CaregiverTint = Color(0xFFF1EBDD)
val CaregiverInk = Color(0xFF66513B)

/** Neutral foundations with muted category accents. Kept the old palette indices stable. */
@Immutable
data class CategoryPalette(val pastel: Color, val banner: Color, val accent: Color)

private data class BasePalette(val pastel: Color, val banner: Color)

private val Palettes = listOf(
    BasePalette(Color(0xFFE8EDF4), Color(0xFF334155)), // 0 family: slate navy
    BasePalette(Color(0xFFE7ECF3), Color(0xFF475569)), // 1 daily essentials
    BasePalette(Color(0xFFF4EAE4), Color(0xFF985F4B)), // 2 fruit: terracotta
    BasePalette(Color(0xFFE8F1EB), Color(0xFF4F7664)), // 3 vegetables: sage
    BasePalette(Color(0xFFF5EBE4), Color(0xFF96624A)), // 4 food: warm clay
    BasePalette(Color(0xFFEBEBF1), Color(0xFF685F78)), // 5 clothing: muted lilac
    BasePalette(Color(0xFFF0EDE8), Color(0xFF746B60)), // 6 home: warm stone
    BasePalette(Color(0xFFE7ECF2), Color(0xFF445E78)), // 7 actions: deep denim
    BasePalette(Color(0xFFEAF0EA), Color(0xFF5A755B)), // 8 nature: sage
    BasePalette(Color(0xFFE5EDF0), Color(0xFF436978)), // 9 body: steel teal
    BasePalette(Color(0xFFF5ECE2), Color(0xFF986441)), // 10 cuisine: warm amber
    BasePalette(Color(0xFFEBEDF0), Color(0xFF586474)), // 11 routine: stone slate
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

private val DarkSurface = Color(0xFF1E293B)

private val LightScheme = lightColorScheme(
    primary = AppBarBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF1E293B),
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF475569),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF233C54),
    onPrimaryContainer = Color(0xFFF0F9FF),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * The app is Arabic-first, so the whole UI is laid out right-to-left regardless of the phone's language.
 * [themeMode] = System / Light / Dark; the navy app bar stays #1E293B in every mode.
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
