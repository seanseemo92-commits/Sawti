package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pixel-aligned implementation of the two user-approved welcome artworks.
 *
 * Both complete compositions (people, title, dialogue, ribbons, button and credits)
 * are the exact approved light/dark artwork, not reconstructed from cutouts.
 * The app theme selection (Light, Dark or System) determines the matching art.
 * The reference is cropped only at the top to exclude the screenshot's fake
 * clock and phone icons. Android renders its OWN live status bar.
 *
 * Animation layers show the same reference without changing its resting look.
 * Navigation is exclusively initiated by tapping the real, accessible button
 * overlay, never by elapsed time.
 */
const val SAWTI_WELCOME_BACKGROUND_ENTER_MS = 1000
const val SAWTI_WELCOME_SCENE_ENTER_MS = 950
const val SAWTI_WELCOME_COPY_ENTER_MS = 650
const val SAWTI_WELCOME_CTA_ENTER_MS = 600

private const val REFERENCE_ASPECT = 941f / 1600f

@Composable
fun SawtiAuroraSplash(onFinished: () -> Unit) {
    val reducedMotion = LocalPerformanceMode.current
    val dark = LocalIsDark.current
    val latestOnFinished by rememberUpdatedState(onFinished)
    var continued by remember { mutableStateOf(false) }

    val background = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val people = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val credits = remember { Animatable(if (reducedMotion) 1f else 0f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            background.snapTo(1f)
            people.snapTo(1f)
            credits.snapTo(1f)
        } else {
            background.snapTo(0f)
            people.snapTo(0f)
            credits.snapTo(0f)
            launch {
                background.animateTo(
                    1f,
                    tween(SAWTI_WELCOME_BACKGROUND_ENTER_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(220L)
                people.animateTo(
                    1f,
                    tween(SAWTI_WELCOME_SCENE_ENTER_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(630L)
                credits.animateTo(
                    1f,
                    tween(SAWTI_WELCOME_CTA_ENTER_MS, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val reference = painterResource(
        if (dark) R.drawable.sawti_welcome_dark else R.drawable.sawti_welcome_light
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (dark) listOf(
                        Color(0xFF06112E), Color(0xFF101C4F), Color(0xFF06112E)
                    ) else listOf(
                        Color(0xFF0756AA), Color(0xFF6A69E4), Color(0xFFF0F9FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            // Fitting the reference without cropping means both the button and
            // developer credits remain visible on short phones and on tablets.
            // The images use the same 941x1600 frame after removing mock system bars.
            val imageWidth = minOf(maxWidth, maxHeight * REFERENCE_ASPECT)
            val imageHeight = imageWidth / REFERENCE_ASPECT

            Box(
                modifier = Modifier.width(imageWidth).height(imageHeight),
                contentAlignment = Alignment.Center
            ) {
                // 1. Reveal the whole approved scene. The final reference is
                // shown at 100% opacity with no stretching or invented ribbons.
                Image(
                    painter = reference,
                    contentDescription = "صوتي، كلمات تعيد التواصل. رجل كبير وشاب بلحية وامرأة محجبة يتحاورون.",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = background.value
                            scaleX = 1.035f - 0.035f * background.value
                            scaleY = scaleX
                        }
                )

                // 2. A second pass over the upper people/dialogue area creates
                // a layered, cinematic reveal, ending at the EXACT same pixels.
                Image(
                    painter = reference,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = people.value
                            translationY = (1f - people.value) * 22.dp.toPx()
                            scaleX = 1.035f - 0.035f * people.value
                            scaleY = scaleX
                        }
                        .drawWithContent {
                            val contentScope = this
                            clipRect(
                                top = size.height * .29f,
                                bottom = size.height * .755f
                            ) { contentScope.drawContent() }
                        }
                )

                // 3. A separate footer/CTA reveal finishes the motion. The
                // source pixels retain the approved button's exact flat style.
                Image(
                    painter = reference,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = credits.value
                            translationY = (1f - credits.value) * 16.dp.toPx()
                        }
                        .drawWithContent {
                            val contentScope = this
                            clipRect(top = size.height * .755f) { contentScope.drawContent() }
                        }
                )

                // The button artwork is part of the approved image, but this
                // transparent overlay is a real Android control with TalkBack
                // semantics and ripple feedback. Its bounds correspond to
                // x≈107..827 and y≈1209..1345 of the 941x1600 image.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = imageWidth * (107f / 941f),
                            y = imageHeight * (1209f / 1600f)
                        )
                        .width(imageWidth * (720f / 941f))
                        .height(imageHeight * (136f / 1600f))
                        .clip(RoundedCornerShape(100.dp))
                        .semantics { contentDescription = "متابعة" }
                        .clickable(
                            enabled = !continued,
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White)
                        ) {
                            if (!continued) {
                                continued = true
                                latestOnFinished()
                            }
                        }
                )
            }
        }
    }
}
