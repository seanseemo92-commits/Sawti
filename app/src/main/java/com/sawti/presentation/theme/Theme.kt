package com.sawti.presentation.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sawti.domain.model.ThemeMode

// ---------------------------------------------------------------------------
// Core color tokens
// ---------------------------------------------------------------------------

/** Deep obsidian slate used for the top app bar (both light and dark). */
val AppBarBlue = Color(0xFF0F172A)

/** Primary text color on light surfaces. */
val Ink = Color(0xFF0F172A)

/** Warm off-white canvas behind cards in light mode. */
val Canvas = Color(0xFFF8FAFC)

val HintAmber = Color(0xFFF59E0B)
val RecordRed = Color(0xFFC62828)
val CaregiverTint = Color(0xFFF1EBDD)
val CaregiverInk = Color(0xFF66513B)

/** Pure porcelain card surface (light mode). */
val CardSurfaceLight = Color(0xFFFFFFFF)

/** Deep titanium slate card surface (dark mode). */
val CardSurfaceDark = Color(0xFF151C28)

/** Obsidian slate canvas (dark mode). */
val CanvasDark = Color(0xFF0B0F17)

/** Primary text color on dark surfaces. */
val InkOnDark = Color(0xFFF1F5F9)

/** Azure/cyan used for the breathing voice aura and dark-mode accents. */
val VoiceAura = Color(0xFF38BDF8)

// ---------------------------------------------------------------------------
// Category palettes
// ---------------------------------------------------------------------------

@Immutable
data class CategoryPalette(val pastel: Color, val banner: Color, val accent: Color)

private data class BasePalette(val pastel: Color, val banner: Color)

private val Palettes = listOf(
    BasePalette(Color(0xFFE8EDF4), Color(0xFF334155)),
    BasePalette(Color(0xFFE7ECF3), Color(0xFF475569)),
    BasePalette(Color(0xFFF4EAE4), Color(0xFF985F4B)),
    BasePalette(Color(0xFFE8F1EB), Color(0xFF4F7664)),
    BasePalette(Color(0xFFF5EBE4), Color(0xFF96624A)),
    BasePalette(Color(0xFFEBEBF1), Color(0xFF685F78)),
    BasePalette(Color(0xFFF0EDE8), Color(0xFF746B60)),
    BasePalette(Color(0xFFE7ECF2), Color(0xFF445E78)),
    BasePalette(Color(0xFFEAF0EA), Color(0xFF5A755B)),
    BasePalette(Color(0xFFE5EDF0), Color(0xFF436978)),
    BasePalette(Color(0xFFF5ECE2), Color(0xFF986441)),
    BasePalette(Color(0xFFEBEDF0), Color(0xFF586474)),
)

private object NoHaptics : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

// ---------------------------------------------------------------------------
// Composition locals
// ---------------------------------------------------------------------------

val LocalIsDark = staticCompositionLocalOf { false }
val LocalPerformanceMode = staticCompositionLocalOf { false }

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

@Composable
fun cardElevation(base: Dp): Dp = if (LocalPerformanceMode.current) 0.dp else base

/** Standard card corner radius for the ceramic surface language. */
val CardShape = RoundedCornerShape(22.dp)

/**
 * Crisp translucent micro-border that replaces heavy drop shadows.
 * Light: slate at 6% alpha. Dark: white at 8% alpha.
 */
@Composable
fun cardBorder(): BorderStroke {
    val isDark = LocalIsDark.current
    return BorderStroke(
        width = 1.dp,
        color = if (isDark) {
            Color.White.copy(alpha = 0.08f)
        } else {
            Color(0xFF0F172A).copy(alpha = 0.06f)
        },
    )
}

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

/**
 * Consistent saturated card banners across category tiles and vocabulary cards.
 * The darker vibrant values preserve the readability of bold white Arabic text.
 */
fun categoryBannerColor(categoryId: String, fallback: Color): Color = when (categoryId) {
    "family" -> Color(0xFF1769A8)       // blue
    "cuisine" -> Color(0xFF99502B)       // warm brown
    "daily" -> Color(0xFF087E82)        // teal
    "routine" -> Color(0xFF7A399D)      // purple
    "vegetables" -> Color(0xFF2D793A)   // green
    "fruits" -> Color(0xFFAF5300)       // rich orange, readable with white
    "food" -> Color(0xFF9B512E)
    "home" -> Color(0xFF8C5635)
    "clothes" -> Color(0xFF67449A)
    "body" -> Color(0xFF176F8C)
    "actions" -> Color(0xFF295F9A)
    "animals" -> Color(0xFF357746)
    else -> fallback
}

// ---------------------------------------------------------------------------
// Color schemes
// ---------------------------------------------------------------------------

private val DarkSurface = CardSurfaceDark

private val LightScheme = lightColorScheme(
    primary = AppBarBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0F172A),
    background = Canvas,
    onBackground = Ink,
    surface = CardSurfaceLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF475569),
)

private val DarkScheme = darkColorScheme(
    primary = VoiceAura,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF233C54),
    onPrimaryContainer = Color(0xFFF0F9FF),
    background = CanvasDark,
    onBackground = InkOnDark,
    surface = DarkSurface,
    onSurface = InkOnDark,
    surfaceVariant = Color(0xFF273244),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// ---------------------------------------------------------------------------
// Theme entry point
// ---------------------------------------------------------------------------

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
