package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import kotlinx.coroutines.delay

/**
 * Brand-identical splash: the original transparent logo already contains the complete
 * Arabic "صوتي" lettering and SAWTI wordmark. Keeping that artwork together avoids
 * the missing glyphs and font mismatch in earlier implementations.
 *
 * Curves are drawn relative to the real canvas size, not sampled from a phone-shaped
 * bitmap: no stretch, cropping or hard horizontal bands on tablets/landscape screens.
 */
@Composable
fun SawtiSplashScreen(onNavigateToMain: () -> Unit) {
    // Follow the app's explicit Light/Dark/System preference, not Android mode alone.
    val isDark = LocalIsDark.current
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        delay(1900)
        onNavigateToMain()
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isDark) {
                // Midnight navy that matches the original dark app icon.
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF071629),
                            Color(0xFF0B2540),
                            Color(0xFF081A31),
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h),
                    ),
                )
                // Very soft light behind the golden emblem: no solid purple waves at night.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF356080).copy(alpha = 0.25f),
                            Color(0xFF173655).copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.5f, h * 0.47f),
                        radius = maxOf(w, h) * 0.53f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD7A75D).copy(alpha = 0.075f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.47f),
                        radius = w * 0.53f,
                    ),
                )
            } else {
                // Icon palette: airy cyan at top left, periwinkle at top right.
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF4CCCF0),
                            0.43f to Color(0xFF61A9F5),
                            0.78f to Color(0xFF777EED),
                            1.00f to Color(0xFF8666E5),
                        ),
                        start = Offset.Zero,
                        end = Offset(w * 1.03f, h * 0.58f),
                    ),
                )

                // Light translucent corner curves tie the screen to the app icon.
                val leftGlow = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.58f, 0f)
                    cubicTo(w * 0.41f, h * 0.14f, w * 0.22f, h * 0.18f, 0f, h * 0.16f)
                    close()
                }
                drawPath(
                    path = leftGlow,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6CE1F5).copy(alpha = 0.47f),
                            Color(0xFFB3E5FF).copy(alpha = 0.03f),
                        ),
                        start = Offset.Zero,
                        end = Offset(w * 0.70f, h * 0.25f),
                    ),
                )
                val rightGlow = Path().apply {
                    moveTo(w * 0.51f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.22f)
                    cubicTo(w * 0.76f, h * 0.21f, w * 0.66f, h * 0.15f, w * 0.51f, 0f)
                    close()
                }
                drawPath(
                    path = rightGlow,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFDADEFF).copy(alpha = 0.27f),
                            Color(0xFFAFA7F8).copy(alpha = 0.18f),
                        ),
                        start = Offset(w * 0.55f, 0f),
                        end = Offset(w, h * 0.24f),
                    ),
                )

                // Wide sweeping lavender area, with the boundary behind the logo:
                // the Arabic/English title stays entirely on a pale surface.
                val lowerWave = Path().apply {
                    moveTo(0f, h * 0.39f)
                    cubicTo(
                        w * 0.12f, h * 0.385f,
                        w * 0.25f, h * 0.58f,
                        w * 0.48f, h * 0.54f,
                    )
                    cubicTo(
                        w * 0.69f, h * 0.50f,
                        w * 0.73f, h * 0.48f,
                        w, h * 0.51f,
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = lowerWave,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFDDE4FF),
                            0.45f to Color(0xFFF3EDFF),
                            1f to Color(0xFFEAF7FF),
                        ),
                        startY = h * 0.40f,
                        endY = h,
                    ),
                )

                // Gentle near-white lower corner, distinct from the central wave.
                val bottomGlow = Path().apply {
                    moveTo(0f, h * 0.79f)
                    cubicTo(w * 0.23f, h * 0.69f, w * 0.47f, h * 0.84f, w, h * 0.80f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = bottomGlow,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFECDFFF).copy(alpha = 0.37f),
                            Color(0xFFDDF5FF).copy(alpha = 0.38f),
                        ),
                        start = Offset(0f, h * 0.74f),
                        end = Offset(w, h),
                    ),
                )
            }
        }

        // Use PNGs with visually centered alpha content. The source logo has
        // unequal transparent margins (dark ink is biased right, light ink left).
        // Measure INSIDE safe insets so small phones, tablets, landscape and
        // display cutouts have the same predictable, centered layout.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            val logoWidth = minOf(
                maxWidth * 0.72f,
                if (maxWidth >= 600.dp) 380.dp else 270.dp,
                maxHeight * 0.50f * (410f / 430f),
            )
            Image(
                painter = painterResource(
                    if (isDark) R.drawable.sawti_wordmark_centered_dark
                    else R.drawable.sawti_wordmark_centered_light
                ),
                contentDescription = "شعار صوتي — SAWTI",
                modifier = Modifier
                    .width(logoWidth)
                    .aspectRatio(410f / 430f)
                    .offset(y = (-8).dp)
                    .alpha(reveal.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
