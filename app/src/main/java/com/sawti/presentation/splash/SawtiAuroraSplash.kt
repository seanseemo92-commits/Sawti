package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Approved Sawti conversation welcome:
 * photo illustration of three conversing adults, independently rendered text,
 * flat responsive CTA, continuously flowing brand ribbons and a staged entrance.
 *
 * No two-second auto-navigation. Continue is the only way to dismiss the screen.
 * Window insets and viewport-based sizing avoid overlapping system UI.
 */
const val SAWTI_WELCOME_BACKGROUND_ENTER_MS = 1100
const val SAWTI_WELCOME_SCENE_ENTER_MS = 1050
const val SAWTI_WELCOME_COPY_ENTER_MS = 700
const val SAWTI_WELCOME_CTA_ENTER_MS = 600

@Composable
fun SawtiAuroraSplash(onFinished: () -> Unit) {
    val dark = LocalIsDark.current
    val reducedMotion = LocalPerformanceMode.current
    val latestOnFinish by rememberUpdatedState(onFinished)
    var continued by remember { mutableStateOf(false) }
    val backdrop = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val scene = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val typography = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val controls = remember { Animatable(if (reducedMotion) 1f else 0f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            backdrop.snapTo(1f)
            scene.snapTo(1f)
            typography.snapTo(1f)
            controls.snapTo(1f)
        } else {
            backdrop.snapTo(0f)
            scene.snapTo(0f)
            typography.snapTo(0f)
            controls.snapTo(0f)
            launch {
                backdrop.animateTo(1f, tween(SAWTI_WELCOME_BACKGROUND_ENTER_MS, easing = FastOutSlowInEasing))
            }
            launch {
                delay(170)
                scene.animateTo(1f, tween(SAWTI_WELCOME_SCENE_ENTER_MS, easing = FastOutSlowInEasing))
            }
            launch {
                delay(420)
                typography.animateTo(1f, tween(SAWTI_WELCOME_COPY_ENTER_MS, easing = FastOutSlowInEasing))
            }
            launch {
                delay(780)
                controls.animateTo(1f, tween(SAWTI_WELCOME_CTA_ENTER_MS, easing = FastOutSlowInEasing))
            }
        }
    }

    val motion = if (reducedMotion) 0f else {
        val loop = rememberInfiniteTransition(label = "communication-waves")
        val phase by loop.animateFloat(
            initialValue = 0f,
            targetValue = 6.2831855f,
            animationSpec = infiniteRepeatable(tween(7800, easing = LinearEasing), RepeatMode.Restart),
            label = "wave-phase"
        )
        phase
    }

    val navy = Color(0xFF0D245F)
    val lilac = Color(0xFF7552F2)
    val ice = Color(0xFF33BFF3)
    val ink = if (dark) Color(0xFFE8EDFF) else Color(0xFF23366C)
    val footerInk = if (dark) Color(0xFFD1DAF4) else Color(0xFF334C88)
    val footerBackground = if (dark) Color(0xFF17234C) else Color(0xFFECF1FF)

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.linearGradient(
                    colors = if (dark)
                        listOf(Color(0xFF061632), Color(0xFF22316D), Color(0xFF1E235C))
                    else
                        listOf(Color(0xFF102E74), Color(0xFF344BCD), Color(0xFF7152DE)),
                    start = Offset.Zero,
                    end = Offset(w, h * .78f)
                )
            )
            // Thin translucent ribbons enter from opposite corners and gently drift.
            val offset = sin(motion) * w * .034f
            val top = Path().apply {
                moveTo(0f, 0f)
                cubicTo(w * .33f, h * .07f, w * .38f + offset, h * .13f, w, h * .03f)
                lineTo(w, 0f)
                close()
            }
            drawPath(
                top,
                Brush.linearGradient(
                    listOf(Color(0xFF36C8F2).copy(alpha = .42f * backdrop.value),
                        Color(0xFF966EFF).copy(alpha = .42f * backdrop.value)),
                    start = Offset.Zero, end = Offset(w, h * .2f)
                )
            )
            val lower = Path().apply {
                moveTo(0f, h * .70f + offset * .12f)
                cubicTo(w * .25f, h * .59f, w * .48f, h * .84f, w, h * .67f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                lower,
                brush = Brush.verticalGradient(
                    colors = if (dark)
                        listOf(Color(0xFF6351C8), Color(0xFF263364), footerBackground)
                    else
                        listOf(Color(0xFF6F62EE), Color(0xFFC6D6FF), footerBackground),
                    startY = h * .68f,
                    endY = h
                )
            )
            val bottomCurve = Path().apply {
                moveTo(0f, h * .80f)
                cubicTo(w * .27f, h * .75f, w * .75f, h * .88f, w, h * .80f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                bottomCurve,
                Brush.verticalGradient(
                    listOf(footerBackground.copy(alpha = .65f), footerBackground),
                    startY = h * .78f, endY = h
                )
            )
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val compact = maxHeight < 660.dp || maxWidth < 340.dp
            val topSpacing = if (compact) 12.dp else 22.dp
            val sceneWidth = minOf(maxWidth - 16.dp, if (maxWidth >= 620.dp) 540.dp else 460.dp)
            val sceneHeight = minOf(sceneWidth * (444f / 512f), maxHeight * (if (compact) .37f else .42f))
            val footerSpacing = if (compact) 8.dp else 13.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 17.dp)
                    .padding(top = topSpacing, bottom = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = typography.value
                        translationY = (1f - typography.value) * 22.dp.toPx()
                    }
                ) {
                    // Every letter stays crisp because these are native Compose text layers.
                    Text(
                        text = "صوتي",
                        color = Color.White,
                        fontSize = if (compact) 48.sp else 61.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "S A W T I",
                        color = Color(0xFFD6DFFF),
                        fontSize = if (compact) 13.sp else 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(if (compact) 3.dp else 9.dp))
                    Text(
                        text = "كلمات تُعيد التواصل",
                        color = Color(0xFFF2F5FF),
                        fontSize = if (compact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .width(sceneWidth)
                        .height(sceneHeight)
                        .graphicsLayer {
                            alpha = scene.value
                            translationY = (1f - scene.value) * 38.dp.toPx()
                            scaleX = 1.08f - .08f * scene.value
                            scaleY = scaleX
                            translationX = sin(motion) * 3.dp.toPx()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.sawti_welcome_people),
                        contentDescription = "رجل مسن وشاب بلحية وامرأة محجبة يتواصلون معاً",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val shift = sin(motion) * 7.dp.toPx()
                        val central = Path().apply {
                            moveTo(w * .10f, h * .22f + shift)
                            cubicTo(w * .28f, h * .12f, w * .42f, h * .34f + shift, w * .56f, h * .24f)
                            cubicTo(w * .76f, h * .14f, w * .82f, h * .27f - shift, w * .94f, h * .18f)
                        }
                        drawPath(
                            central,
                            brush = Brush.horizontalGradient(
                                listOf(ice.copy(alpha = .0f), ice.copy(alpha = .38f),
                                    lilac.copy(alpha = .37f), lilac.copy(alpha = .0f))
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .graphicsLayer {
                            alpha = controls.value
                            translationY = (1f - controls.value) * 20.dp.toPx()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Flat, accessible brand-gradient button; no baked button pixels.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 52.dp else 60.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF268CF5), Color(0xFF6D3DEB))))
                            .clickable(
                                enabled = !continued,
                                role = Role.Button,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                            ) {
                                if (!continued) {
                                    continued = true
                                    latestOnFinish()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "متابعة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 21.sp else 25.sp
                        )
                        Text(
                            "›",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(Modifier.height(footerSpacing))
                    Text(
                        text = "المطور : إسماعيل بن سالم",
                        color = footerInk,
                        fontSize = if (compact) 11.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "ismail92sa@gmail.com",
                        color = footerInk,
                        fontSize = if (compact) 11.sp else 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "© 2026 جميع الحقوق محفوظة",
                        color = footerInk.copy(alpha = .8f),
                        fontSize = if (compact) 10.sp else 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
