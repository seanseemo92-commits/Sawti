package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Approved full-bleed Light/Dark welcome art, with a real accessible Continue
 * hit target. All art is visible immediately (no black/blank first frames).
 *
 * The artwork's faces/wordmark/dialogue/CTA remain uniformly scaled. Only
 * decorative intervals are lengthened for taller phones. No automatic exit.
 */
const val SAWTI_WELCOME_BACKGROUND_ENTER_MS = 1000
const val SAWTI_WELCOME_SCENE_ENTER_MS = 950
const val SAWTI_WELCOME_COPY_ENTER_MS = 650
const val SAWTI_WELCOME_CTA_ENTER_MS = 600

@Composable
fun SawtiAuroraSplash(
    onFinished: () -> Unit,
    preparingMain: Boolean = false,
) {
    val view = LocalView.current
    val dark = LocalIsDark.current
    val reducedMotion = LocalPerformanceMode.current
    val currentFinish by rememberUpdatedState(onFinished)
    var clicked by remember { mutableStateOf(false) }
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            entrance.snapTo(1f)
        } else {
            entrance.snapTo(0f)
            entrance.animateTo(
                1f,
                tween(SAWTI_WELCOME_BACKGROUND_ENTER_MS, easing = FastOutSlowInEasing)
            )
        }
    }

    // Decode the mode-specific artwork once. The app theme overrides Android
    // when explicitly selected in Settings.
    val art = ImageBitmap.imageResource(
        if (dark) R.drawable.sawti_welcome_dark else R.drawable.sawti_welcome_light
    )

    val lightReference = ImageBitmap.imageResource(R.drawable.sawti_welcome_light)
    val referenceOverlay = remember(lightReference, dark) {
        if (dark) WelcomeButtonArtwork.referenceOverlay(lightReference) else null
    }
    val darkBacking = remember(art, dark) {
        if (dark) WelcomeButtonArtwork.darkBacking(art) else null
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // These dp dimensions use the same source->viewport geometry as Canvas
        // uses in physical pixels: density cancels out of the ratios.
        val geo = WelcomeArtLayout.measure(maxWidth.value, maxHeight.value)
        Box(
            Modifier
                .fillMaxSize()
                // Consume touches outside the actual Continue button so the
                // precomposed Home behind the splash cannot be tapped.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val g = WelcomeArtLayout.measure(size.width, size.height)
                drawRect(
                    brush = Brush.verticalGradient(
                        if (dark) {
                            listOf(Color(0xFF06112E), Color(0xFF101C4F), Color(0xFF06112E))
                        } else {
                            listOf(Color(0xFF0756AA), Color(0xFF6A69E4), Color(0xFFF0F9FF))
                        }
                    )
                )
                val x = g.left.roundToInt()
                val width = g.width.roundToInt().coerceAtLeast(1)
                for (i in WelcomeArtLayout.extraWeights.indices) {
                    val start = WelcomeArtLayout.boundaries[i]
                    val end = WelcomeArtLayout.boundaries[i + 1]
                    val yTop = g.y(start.toFloat()).roundToInt()
                    val yBottom = g.y(end.toFloat()).roundToInt()
                    drawImage(
                        image = art,
                        srcOffset = IntOffset(0, start),
                        srcSize = IntSize(WelcomeArtLayout.SOURCE_WIDTH, end - start),
                        dstOffset = IntOffset(x, yTop),
                        dstSize = IntSize(width, (yBottom - yTop).coerceAtLeast(1)),
                        filterQuality = FilterQuality.High,
                    )
                }

                if (dark && referenceOverlay != null && darkBacking != null) {
                    // The entire CTA is in an unstretched slice. Work in the
                    // original artwork coordinates, independent of density/RTL.
                    withTransform({
                        translate(g.left, g.y(1200f))
                        scale(g.scale, g.scale, pivot = androidx.compose.ui.geometry.Offset.Zero)
                        translate(0f, -1200f)
                    }) {
                        val oldPill = Path().apply {
                            addRoundRect(RoundRect(Rect(132f, 1239f, 810f, 1358f), CornerRadius(59.5f)))
                        }
                        clipPath(oldPill) {
                            drawImage(
                                image = darkBacking,
                                dstOffset = IntOffset(132, 1239),
                                dstSize = IntSize(678, 119),
                                filterQuality = FilterQuality.Low,
                            )
                        }
                        drawImage(
                            image = referenceOverlay,
                            dstOffset = IntOffset(
                                WelcomeButtonArtwork.LEFT, WelcomeButtonArtwork.TOP
                            ),
                            dstSize = IntSize(WelcomeButtonArtwork.WIDTH, WelcomeButtonArtwork.HEIGHT),
                            filterQuality = FilterQuality.High,
                        )
                    }
                }

                // The original illustration is never hidden. The following
                // brief ambient tint and cyan ribbon provide a restrained,
                // non-looping arrival without flashing dark frames or moving
                // people's faces around.
                val phase = entrance.value
                val glow = sin(phase * PI).toFloat().coerceAtLeast(0f)
                if (glow > 0.001f) {
                    val wave = Path().apply {
                        val y = g.y(600f)
                        moveTo(size.width * .08f, y + 8.dp.toPx())
                        cubicTo(
                            size.width * .32f, y - 13.dp.toPx(),
                            size.width * .61f, y + 13.dp.toPx(),
                            size.width * .92f, y
                        )
                    }
                    drawPath(
                        path = wave,
                        color = Color(0xFF64E7FF).copy(alpha = glow * 0.24f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Both appearances share the light reference's exact hit target
            // and the same restrained, clipped press glow.
            val actualButton = with(WelcomeArtLayout) { geo.buttonOnScreen() }
            val pressSource = remember { MutableInteractionSource() }
            val pressed by pressSource.collectIsPressedAsState()
            val pressOpacity by animateFloatAsState(
                targetValue = if (pressed) 0.12f else 0f,
                animationSpec = tween(durationMillis = 115),
                label = "welcome-button-press",
            )
            Box(
                modifier = Modifier
                    .absoluteOffset(x = actualButton.left.dp, y = actualButton.top.dp)
                    .width(actualButton.width.dp)
                    .height(actualButton.height.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .drawBehind {
                        // A tiny brand-colored glow INSIDE the existing pill.
                        // Compose's default ripple was painting a second,
                        // larger grey rounded rectangle over the artwork.
                        if (pressOpacity > 0.001f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF21B7F5).copy(alpha = pressOpacity),
                                        Color(0xFFBFA6FF).copy(alpha = pressOpacity * 0.8f),
                                    )
                                ),
                                cornerRadius = CornerRadius(size.height / 2f),
                            )
                        }
                    }
                    .semantics { contentDescription = "متابعة" }
                    .clickable(
                        enabled = !clicked,
                        role = Role.Button,
                        interactionSource = pressSource,
                        indication = null,
                    ) {
                        if (!clicked) {
                            clicked = true
                            // A single short system haptic, respecting the user's
                            // touch-feedback setting; no vibration permission or
                            // flags that bypass device preferences.
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            currentFinish()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (preparingMain) {
                    // Only shown if the user pressed Continue before category
                    // data finished loading. Avoids a black spinner screen.
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.width(24.dp).height(24.dp)
                    )
                }
            }
        }
    }
}

