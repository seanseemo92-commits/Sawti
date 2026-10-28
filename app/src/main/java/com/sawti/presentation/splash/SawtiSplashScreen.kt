package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * One continuous adaptive background plus the ORIGINAL bitmap containing both the
 * emblem and its "صوتي SAWTI" wordmark. No hand-drawn Canvas, paths or duplicate text.
 * The bitmap is always measured to fit INSIDE the screen without cropping or stretching.
 */
@Composable
fun SawtiSplashScreen(onNavigateToMain: () -> Unit) {
    val dark = LocalIsDark.current
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        delay(1350)
        onNavigateToMain()
    }

    val background = if (dark) {
        Brush.verticalGradient(
            0f to Color(0xFF071A30),
            0.50f to Color(0xFF0A203B),
            1f to Color(0xFF0F172A),
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0xFF4BAEEB),
            0.34f to Color(0xFF7F7CEB),
            0.64f to Color(0xFFD8E1FA),
            1f to Color(0xFFF1EAFB),
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(background)) {
        // Source logo bitmap: 410 x 430. Never use Crop or FillBounds, which made
        // the Arabic wordmark disappear offscreen on narrower phones.
        // Leave at least 170dp for the footer / navigation bars on short devices.
        val widthLimit = maxWidth * 0.84f
        val heightLimit = ((maxHeight - 175.dp).coerceAtLeast(100.dp)) * (410f / 430f)
        val imageWidth = minOf(widthLimit, heightLimit)
        Image(
            painter = painterResource(
                if (dark) R.drawable.ic_splash_logo_dark else R.drawable.ic_splash_logo_light
            ),
            contentDescription = "صوتي SAWTI",
            modifier = Modifier
                .align(Alignment.Center)
                .width(imageWidth)
                .aspectRatio(410f / 430f)
                .alpha(alpha.value),
            contentScale = ContentScale.Fit,
        )

        // Only this footer is programmatic. It never overlaps the centered logo.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Developed by Ismail Bin Salem",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) Color.White else Color(0xFF162044),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = "Ismail92sa@gmail.com",
                fontSize = 12.sp,
                color = if (dark) Color(0xFFD5DFEB) else Color(0xFF475569),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = "v1.0.0 • © 2026 All Rights Reserved",
                fontSize = 11.sp,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
