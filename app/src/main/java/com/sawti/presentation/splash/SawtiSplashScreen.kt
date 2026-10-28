package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import kotlinx.coroutines.delay

/**
 * SAWTI welcome: layered Compose waves, the original emblem-only artwork,
 * and a single typographic app title. No dark scrim or previous gradient overlay.
 */
@Composable
fun SawtiSplashScreen(onNavigateToMain: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        contentAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        delay(1900)
        onNavigateToMain()
    }

    val brandTitleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF162044)
    val brandSubtitleColor = if (isDark) Color(0xFFD4AF6A) else Color(0xFF162044)
    val footerPrimaryColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF162044)
    val footerSecondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF070D18) else Color(0xFF38BDF8))
    ) {
        if (!isDark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val purplePath = Path().apply {
                    moveTo(0f, height * 0.25f)
                    cubicTo(width * 0.35f, height * 0.15f, width * 0.65f, height * 0.45f, width, height * 0.35f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = purplePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    )
                )

                val lightPath = Path().apply {
                    moveTo(0f, height * 0.65f)
                    cubicTo(width * 0.3f, height * 0.55f, width * 0.7f, height * 0.75f, width, height * 0.62f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = lightPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8EAF6), Color(0xFFF3E5F5))
                    )
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val darkWavePath = Path().apply {
                    moveTo(0f, height * 0.3f)
                    cubicTo(width * 0.4f, height * 0.2f, width * 0.6f, height * 0.5f, width, height * 0.4f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = darkWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The provided full-logo PNG already includes "صوتي SAWTI". Use its
            // emblem-only companion so the title below is never duplicated.
            Image(
                painter = painterResource(
                    id = if (isDark) R.drawable.sawti_splash_mark_dark
                         else R.drawable.sawti_splash_mark_light
                ),
                contentDescription = "Sawti Emblem",
                modifier = Modifier
                    .width(190.dp)
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "صوتي",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = brandTitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "SAWTI",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 5.sp,
                color = brandSubtitleColor,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .padding(bottom = 28.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed by Ismail Bin Salem",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = footerPrimaryColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "ismail92sa@gmail.com",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = footerSecondaryColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "v1.0.0 • © 2026 All Rights Reserved",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = footerSecondaryColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
