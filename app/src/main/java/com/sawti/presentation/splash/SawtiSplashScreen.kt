package com.sawti.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SawtiSplashScreen(
    onNavigateToMain: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        delay(1900)
        onNavigateToMain()
    }

    val brandTitleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF162044)
    val brandSubtitleColor = if (isDark) Color(0xFFD4AF6A) else Color(0xFF162044)
    val footerPrimaryColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val footerSecondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. الخلفية التكيفية: تتبدل تلقائياً عبر نظام الأندرويد وتتمدد للهاتف والتابلت
        Image(
            painter = painterResource(id = com.sawti.R.drawable.bg_splash_waves),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. كتلة الشعار والاسم في المنتصف بتناسب مثالي
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(
                    id = if (isDark) {
                        com.sawti.R.drawable.ic_splash_logo_dark
                    } else {
                        com.sawti.R.drawable.ic_splash_logo_light
                    }
                ),
                contentDescription = "Sawti Emblem",
                modifier = Modifier
                    .width(195.dp)
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

        // 3. الفوتر باللغة الإنجليزية في الأسفل
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp)
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
                fontWeight = FontWeight.Medium,
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
