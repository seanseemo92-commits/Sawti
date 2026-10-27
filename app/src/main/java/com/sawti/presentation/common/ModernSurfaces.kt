package com.sawti.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardElevation
import kotlinx.coroutines.delay

private val Sky = Color(0xFF38BDF8)
private val DarkCeramic = Color(0xFF161D2A)

/** Low-opacity radial hue tint that makes food/health/family cards distinct but calm. */
@Composable
fun CeramicImageTint(accent: Color, modifier: Modifier = Modifier) {
    val dark = LocalIsDark.current
    Box(
        modifier.fillMaxSize().background(
            Brush.radialGradient(
                listOf(
                    accent.copy(alpha = if (dark) 0.20f else 0.115f),
                    accent.copy(alpha = if (dark) 0.045f else 0.025f),
                    Color.Transparent,
                )
            )
        )
    )
}

/** Real playback activity (not a guessed timer) drives an optional, gentle breathing aura. */
@Composable
fun VoiceAura(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return
    val performance = LocalPerformanceMode.current
    if (performance) {
        // Static soft glow in low-motion mode; no permanent frame invalidations.
        Box(
            modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Sky.copy(alpha = 0.18f), Sky.copy(alpha = 0.07f), Color.Transparent)
                )
            )
        )
        return
    }
    val transition = rememberInfiniteTransition(label = "speech-aura")
    val breath by transition.animateFloat(
        initialValue = 0.83f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aura-radius",
    )
    Canvas(modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.60f * breath
        val alpha = 0.27f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Sky.copy(alpha = alpha),
                    Sky.copy(alpha = alpha * 0.47f),
                    Color.Transparent,
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = radius,
            ),
            radius = radius,
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}

/**
 * First paint uses a GPU alpha/translation (no relayout): stagger by row at 35ms,
 * cap delay to 245ms for long lists. Performance Mode instantly renders all cards.
 */
@Composable
fun StaggeredCascade(
    key: Any,
    row: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val performance = LocalPerformanceMode.current
    val fraction = remember(key) { Animatable(0f) }
    LaunchedEffect(key, performance) {
        if (performance) fraction.snapTo(1f)
        else {
            fraction.snapTo(0f)
            delay((row.coerceIn(0, 7) * 35L))
            fraction.animateTo(1f, animationSpec = tween(360, easing = FastOutSlowInEasing))
        }
    }
    val shiftPx = with(LocalDensity.current) { 16.dp.toPx() }
    Box(
        modifier.graphicsLayer {
            alpha = if (performance) 1f else fraction.value
            translationY = if (performance) 0f else (1f - fraction.value) * shiftPx
        }
    ) { content() }
}
