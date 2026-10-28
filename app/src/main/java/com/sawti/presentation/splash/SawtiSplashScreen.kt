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
    val footerAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        footerAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(1900)
        onNavigateToMain()
    }

    // نصوص كحلية ناعمة في الوضع الفاتح لتقرأ بوضوح تام فوق التموجات الفاتحة، وفاتحة في الوضع الداكن
    val primaryTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // صورة الشاشة الأصلية بالكامل تمتد من الحافة إلى الحافة
        Image(
            painter = painterResource(
                id = if (isDark) {
                    com.sawti.R.drawable.ic_splash_logo_dark
                } else {
                    com.sawti.R.drawable.ic_splash_logo_light
                }
            ),
            contentDescription = "Sawti Splash",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // الفوتر بنصوص برمجية نظيفة بالإنجليزية، بدون أي طبقة سوداء أو تدرج لوني خلفه
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .alpha(footerAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed by Ismail Bin Salem",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Ismail92sa@gmail.com",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "v1.0.0 • © 2026 All Rights Reserved",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryTextColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
