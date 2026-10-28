package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import kotlinx.coroutines.delay

/**
 * Original supplied Sawti artwork, shown as a bounded, un-cropped logo.
 * No Canvas, manually drawn waves, zooming, scrim or dark overlay.
 * Background responds to Android light/dark appearance.
 */
@Composable
fun SawtiSplashScreen(
    onNavigateToMain: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        contentAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        delay(1900)
        onNavigateToMain()
    }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF070D18), Color(0xFF0F172A), Color(0xFF1E293B))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF8B5CF6))
        )
    }

    val footerPrimaryColor = if (isDark) Color(0xFFF8FAFC) else Color.White
    val footerSecondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Image(
            painter = painterResource(
                id = if (isDark) {
                    R.drawable.ic_splash_logo_dark
                } else {
                    R.drawable.ic_splash_logo_light
                }
            ),
            contentDescription = "Sawti Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.62f)
                .alpha(contentAlpha.value),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
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
                text = "Ismail92sa@gmail.com",
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
