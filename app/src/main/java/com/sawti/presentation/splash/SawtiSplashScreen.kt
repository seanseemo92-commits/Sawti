package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark
import kotlinx.coroutines.delay

/**
 * Original SAWTI branding, as supplied: the emblem, its lettering and the graceful
 * artwork curves are one official bitmap. No Canvas/Path imitation of the emblem.
 * The edges continue into the surrounding day/night color field without stretching.
 */
@Composable
fun SawtiSplashScreen(onNavigateToMain: () -> Unit) {
    // Uses the resolved SawtiTheme setting: SYSTEM follows Android; explicit LIGHT/DARK
    // overrides match the rest of the app.
    val isDark = LocalIsDark.current
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
        delay(1350) // approximately two seconds total, then enter the main screen.
        onNavigateToMain()
    }

    val footerPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF162044)
    val footerSecondary = if (isDark) Color(0xFFBECBDD) else Color(0xFF475569)
    val footerMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF081D38) else Color(0xFFD9E2FA))
    ) {
        // At phone widths the artwork reaches both edges, as in the supplied mockup.
        // On shorter/wider devices shrink proportionally to keep the footer unobstructed.
        // Reserve a fixed safe footer band even on landscape/small screens.
        val artWidth = minOf(
            maxWidth,
            (maxHeight - 188.dp).coerceAtLeast(90.dp) * (410f / 430f)
        )
        val artHeight = artWidth * (430f / 410f)
        val art = if (isDark) R.drawable.ic_splash_logo_dark else R.drawable.ic_splash_logo_light

        Column(modifier = Modifier.fillMaxSize()) {
            // Color continuation is sampled from the actual top edge of the light/dark
            // branding PNG so that the authentic artwork has no abrupt rectangular border.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            if (isDark) listOf(Color(0xFF081D38), Color(0xFF071E38))
                            else listOf(Color(0xFF679CEA), Color(0xFF8876EC))
                        )
                    )
            ) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            if (isDark) listOf(Color(0xFF070D18), Color.Transparent)
                            else listOf(Color(0xFF38BDF8).copy(alpha = 0.50f), Color.Transparent)
                        )
                    )
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(artHeight),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(art),
                    contentDescription = "صوتي SAWTI",
                    modifier = Modifier
                        .width(artWidth)
                        .height(artHeight)
                        .graphicsLayer {
                            // Fade the source PNG only at its very outer margins:
                            // the official gold/white emblem and Arabic wordmark stay sharp.
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.018f to Color.Black,
                                    0.982f to Color.Black,
                                    1.0f to Color.Transparent,
                                ),
                                blendMode = BlendMode.DstIn
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.025f to Color.Black,
                                    0.975f to Color.Black,
                                    1.0f to Color.Transparent,
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                        .alpha(alpha.value),
                    contentScale = ContentScale.Fit
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            if (isDark) listOf(Color(0xFF081E36), Color(0xFF071C37))
                            else listOf(Color(0xFFDFDCFB), Color(0xFFC3D5F9))
                        )
                    )
            ) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            if (isDark) listOf(Color.Transparent, Color(0xFF0F172A))
                            else listOf(Color.Transparent, Color(0xFFF4E9FC).copy(alpha = 0.57f))
                        )
                    )
                )
            }
        }

        // Only the footer is rendered as editable text. No old Arabic footer copy,
        // no scrim, and nothing that could collide with system navigation buttons.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 18.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Developed by Ismail Bin Salem",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = footerPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Ismail92sa@gmail.com",
                fontSize = 12.sp,
                color = footerSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "v1.0.0 • © 2026 All Rights Reserved",
                fontSize = 11.sp,
                color = footerMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
