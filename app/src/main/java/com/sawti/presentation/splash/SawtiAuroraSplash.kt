package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * New Sawti Aurora welcome screen.
 *
 * No old splash bitmaps or extracted tracing assets are used: the emblem is the
 * actual vector artwork already used by the launcher. All other shapes are drawn
 * directly at device resolution, avoiding the artifacts of upscaled raster logos.
 * No database, audio or navigation state is mutated during the welcome animation.
 */
@Composable
fun SawtiAuroraSplash(onFinished: () -> Unit) {
    val dark = LocalIsDark.current
    val reducedMotion = LocalPerformanceMode.current
    val currentFinish by rememberUpdatedState(onFinished)
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(reducedMotion) {
        entrance.snapTo(if (reducedMotion) 1f else 0f)
        if (!reducedMotion) {
            entrance.animateTo(1f, animationSpec = tween(720, easing = FastOutSlowInEasing))
        }
        delay(if (reducedMotion) 1500L else 1600L)
        currentFinish()
    }

    // In performance mode there is no infinite animation or unnecessary redraw.
    val breath = if (reducedMotion) 0.5f else {
        val transition = rememberInfiniteTransition(label = "brand-breath")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "brand-breath-fraction",
        )
        value
    }

    val title = if (dark) Color(0xFFF5F5FF) else Color(0xFF172B5C)
    val secondary = if (dark) Color(0xFFB5C5E3) else Color(0xFF40588F)
    val highlight = if (dark) Color(0xFFEAC17C) else Color(0xFF6E53DC)

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.linearGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF071527), Color(0xFF102B48), Color(0xFF101C39))
                    } else {
                        listOf(Color(0xFFEAFBFF), Color(0xFFCADFFF), Color(0xFFEAE5FF))
                    },
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
            )

            // A soft, fully procedural aurora halo; no big phone-shaped background PNG.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (dark) {
                        listOf(
                            Color(0xFF4D8CC8).copy(alpha = 0.20f),
                            Color(0xFF7758D7).copy(alpha = 0.12f),
                            Color.Transparent,
                        )
                    } else {
                        listOf(
                            Color(0xFF9AA8FF).copy(alpha = 0.55f),
                            Color(0xFF78DDF5).copy(alpha = 0.17f),
                            Color.Transparent,
                        )
                    },
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = h * 0.48f,
                ),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = h * 0.48f,
            )

            // Two smooth ribbons echo the sound waves inside Sawti's icon.
            val highWave = Path().apply {
                moveTo(0f, h * 0.21f)
                cubicTo(w * 0.22f, h * 0.12f, w * 0.57f, h * 0.30f, w, h * 0.16f)
                lineTo(w, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path = highWave,
                brush = Brush.linearGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF164563).copy(alpha = 0.44f), Color(0xFF3E347A).copy(alpha = 0.31f))
                    } else {
                        listOf(Color(0xFF62D8EC).copy(alpha = 0.32f), Color(0xFF9E94F5).copy(alpha = 0.31f))
                    },
                    start = Offset.Zero,
                    end = Offset(w, h * 0.24f),
                )
            )

            val lowWave = Path().apply {
                moveTo(0f, h * 0.78f)
                cubicTo(w * 0.27f, h * 0.70f, w * 0.58f, h * 0.93f, w, h * 0.81f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = lowWave,
                brush = Brush.linearGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF1F3557).copy(alpha = 0.36f), Color(0xFF503A8D).copy(alpha = 0.27f))
                    } else {
                        listOf(Color(0xFFB9B7FF).copy(alpha = 0.24f), Color(0xFF9DDCF7).copy(alpha = 0.38f))
                    },
                    start = Offset(0f, h * 0.76f),
                    end = Offset(w, h),
                )
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            val compact = maxHeight < 540.dp || maxWidth < 330.dp
            val orb = minOf(
                maxWidth * 0.58f,
                maxHeight * (if (compact) 0.34f else 0.32f),
                if (compact) 178.dp else 238.dp,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = entrance.value
                        translationY = (1f - entrance.value) * 26.dp.toPx()
                        scaleX = 0.94f + entrance.value * 0.06f
                        scaleY = scaleX
                    },
            ) {
                Box(
                    modifier = Modifier.size(orb + 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2
                        val ringColor = if (dark) Color(0xFFB1C6ED) else Color(0xFF6258C9)
                        drawCircle(
                            color = ringColor.copy(alpha = 0.14f + breath * 0.11f),
                            radius = radius * (0.90f + breath * 0.03f),
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx()),
                        )
                        drawCircle(
                            color = ringColor.copy(alpha = 0.12f),
                            radius = radius * 0.81f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(orb)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (dark) {
                                        listOf(Color(0xFF1E4164), Color(0xFF152744), Color(0xFF24285D))
                                    } else {
                                        listOf(Color(0xFF727BEB), Color(0xFF6865D9), Color(0xFF3D9CD4))
                                    }
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = if (dark) {
                                        listOf(Color(0xFFB5CBF0).copy(alpha = 0.45f), Color.White.copy(alpha = 0.06f))
                                    } else {
                                        listOf(Color.White.copy(alpha = 0.83f), Color(0xFFC0C9FF).copy(alpha = 0.37f))
                                    }
                                ),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        (if (dark) Color(0xFFEDC47D) else Color.White)
                                            .copy(alpha = 0.08f + breath * 0.11f),
                                        Color.Transparent,
                                    ),
                                    radius = size.minDimension * 0.52f,
                                ),
                            )
                        }
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(orb * 0.94f),
                        )
                    }
                }

                Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
                Text(
                    text = "صوتي",
                    fontSize = if (compact) 38.sp else 48.sp,
                    fontWeight = FontWeight.Black,
                    color = title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "SAWTI",
                    fontSize = if (compact) 13.sp else 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 5.sp,
                    color = highlight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )

                Spacer(Modifier.height(if (compact) 14.dp else 26.dp))
                Text(
                    text = "التواصل يبدأ بكلمة",
                    fontSize = if (compact) 13.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )

                Spacer(Modifier.height(if (compact) 14.dp else 24.dp))
                Canvas(Modifier.size(width = 100.dp, height = 26.dp)) {
                    val widths = 5.dp.toPx()
                    val gap = 9.dp.toPx()
                    val centerX = size.width / 2f
                    val midY = size.height / 2f
                    for (index in 0 until 7) {
                        val phase = index.toFloat() * 0.9f
                        val amplitude = if (reducedMotion) 0.50f else {
                            0.35f + 0.65f * ((sin(phase + breath * 3.1415927f) + 1f) / 2f)
                        }
                        val height = (7f + (9f - kotlin.math.abs(index - 3) * 1.15f) * amplitude)
                            .dp.toPx()
                        drawRoundRect(
                            color = highlight.copy(alpha = 0.35f + amplitude * 0.55f),
                            topLeft = Offset(centerX + (index - 3) * (widths + gap) - widths / 2f, midY - height / 2f),
                            size = Size(widths, height),
                            cornerRadius = CornerRadius(widths / 2f),
                        )
                    }
                }
            }
        }
    }
}
