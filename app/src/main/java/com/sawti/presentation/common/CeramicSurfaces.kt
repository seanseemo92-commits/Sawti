package com.sawti.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardElevation
import kotlinx.coroutines.delay

/** Polished adaptive surface without hard-coloured card chrome. */
@Composable
fun CeramicCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    accent: Color = Color(0xFF38BDF8),
    content: @Composable () -> Unit,
) {
    val dark = LocalIsDark.current
    val border = if (isActive) accent.copy(alpha = 0.50f)
        else if (dark) Color.White.copy(alpha = 0.14f) else Color(0xFF94A3B8).copy(alpha = 0.23f)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (dark) Color(0xFF161D2A) else Color.White,
        border = BorderStroke(1.dp, border),
        shadowElevation = cardElevation(3.dp),
        content = content,
    )
}

/** The mild radial tint is behind the picture and never changes the photo's actual pixels. */
@Composable
fun BoxScope.CeramicImageTint(accent: Color) {
    Canvas(Modifier.matchParentSize()) {
        drawRect(
            Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.105f), accent.copy(alpha = 0.028f), Color.Transparent),
                center = center,
                radius = size.maxDimension * 0.82f,
            )
        )
    }
}

/** Only animates while the real audio dispatcher reports that speech is active. */
@Composable
fun BoxScope.VoiceAura(active: Boolean) {
    val performance = LocalPerformanceMode.current
    if (!active) return
    val pulse = if (performance) 0.65f else {
        val transition = rememberInfiniteTransition(label = "voice-aura")
        val value by transition.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(tween(1500), repeatMode = RepeatMode.Reverse),
            label = "voice-aura-opacity",
        )
        value
    }
    Canvas(Modifier.matchParentSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF38BDF8).copy(alpha = 0.24f * pulse),
                    Color(0xFF38BDF8).copy(alpha = 0.10f * pulse),
                    Color.Transparent),
                center = center,
                radius = size.minDimension * 0.67f,
            ),
            radius = size.minDimension * 0.67f,
        )
    }
}

/** Short per-row entrance; unanimated in Performance Mode for older devices. */
@Composable
fun StaggeredCascade(
    key: String,
    row: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val performance = LocalPerformanceMode.current
    var shown by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key, performance) {
        if (!performance) delay((row.coerceIn(0, 12) * 35).toLong())
        shown = true
    }
    val opacity by animateFloatAsState(
        if (performance || shown) 1f else 0f,
        animationSpec = tween(260),
        label = "cascade-opacity",
    )
    val shift by animateFloatAsState(
        if (performance || shown) 0f else 16f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cascade-y",
    )
    Box(modifier.graphicsLayer { alpha = opacity; translationY = shift }) {
        content()
    }
}
