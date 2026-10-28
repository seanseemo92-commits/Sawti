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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF070D18) else Color(0xFF38BDF8))
    ) {
        // 1. رسم التموجات الانسيابية بتدرجات ناعمة ومواقع دقيقة
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (!isDark) {
                // تدرج الخلفية السماوية العلوية
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFF818CF8)),
                        startY = 0f,
                        endY = h * 0.45f
                    )
                )

                // التموج البنفسجي الأوسط (ينتهي أعلى منتصف الشاشة ليحتضن الرمز فقط)
                val purpleWave = Path().apply {
                    moveTo(0f, h * 0.20f)
                    cubicTo(w * 0.35f, h * 0.12f, w * 0.65f, h * 0.36f, w, h * 0.26f)
                    lineTo(w, h * 0.48f)
                    cubicTo(w * 0.70f, h * 0.44f, w * 0.30f, h * 0.52f, 0f, h * 0.44f)
                    close()
                }
                drawPath(
                    path = purpleWave,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7)),
                        start = Offset(0f, h * 0.2f),
                        end = Offset(w, h * 0.5f)
                    )
                )

                // المساحة الانسيابية السفلية الفاتحة بتدرج اللافندر الناعم
                val lightWave = Path().apply {
                    moveTo(0f, h * 0.44f)
                    cubicTo(w * 0.30f, h * 0.52f, w * 0.70f, h * 0.44f, w, h * 0.48f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = lightWave,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEBEBFF), Color(0xFFF5F3FF), Color(0xFFFFFFFF)),
                        startY = h * 0.44f,
                        endY = h
                    )
                )
            } else {
                // تدرجات الوضع الليلي
                val darkWave = Path().apply {
                    moveTo(0f, h * 0.25f)
                    cubicTo(w * 0.35f, h * 0.18f, w * 0.65f, h * 0.42f, w, h * 0.32f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = darkWave,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
            }
        }

        // 2. المحتوى المركزي: الرمز على حافة التموج والنصوص بالكامل في المساحة الفاتحة
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 10.dp)
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
                    .width(170.dp)
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(18.dp))

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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 5.sp,
                color = brandSubtitleColor,
                textAlign = TextAlign.Center
            )
        }

        // 3. الفوتر الإنجليزي في الأسفل
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
