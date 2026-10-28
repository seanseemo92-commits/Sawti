package com.sawti.presentation.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark

/**
 * Fully native Compose splash. No PNG in the splash composition:
 * Canvas paths are resolution-independent, the emblem is an Android vector,
 * and Arabic/Latin branding and credits are real Text composables.
 */
@Composable
fun SawtiSplashScreen() {
    val dark = LocalIsDark.current
    val titleColor = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subColor = if (dark) Color(0xFFF0C98C) else Color(0xFF253459)
    val footerColor = if (dark) Color(0xFFCBD5E1) else Color(0xFF334155)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 580.dp
        val narrow = maxWidth < 345.dp
        Canvas(Modifier.matchParentSize()) {
            drawSawtiWaveBackground(dark)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = if (narrow) 14.dp else 24.dp,
                    vertical = if (compact) 8.dp else 18.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_splash_emblem),
                    contentDescription = "شعار صوتي، حرف ص مع موجات صوتية",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(if (compact || narrow) 110.dp else 118.dp),
                )
                Spacer(Modifier.height(if (compact) 8.dp else 16.dp))
                Text(
                    text = "صوتي",
                    color = titleColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact || narrow) 45.sp else 54.sp,
                    lineHeight = if (compact || narrow) 58.sp else 66.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = "SAWTI",
                    color = subColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact || narrow) 15.sp else 18.sp,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            // Footer uses measured native text and safe system-bar insets;
            // even short screens can allocate remaining height to the branding.
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = if (compact) 4.dp else 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 5.dp),
            ) {
                Text(
                    text = "تطوير: إسماعيل بن سالم",
                    color = titleColor,
                    fontSize = if (compact || narrow) 11.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Text(
                    text = "Ismail92sa@gmail.com",
                    color = footerColor,
                    fontSize = if (compact || narrow) 10.sp else 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Text(
                    text = "الإصدار 1.0.0 • صُنع بحب في ليبيا 🇱🇾",
                    color = footerColor,
                    fontSize = if (compact || narrow) 9.sp else 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

/** Curved, multi-layered brand waves in canvas coordinates (not a stretched bitmap). */
private fun DrawScope.drawSawtiWaveBackground(dark: Boolean) {
    val w = size.width
    val h = size.height
    drawRect(
        brush = Brush.verticalGradient(
            colors = if (dark) listOf(
                Color(0xFF070D18), Color(0xFF0B1830), Color(0xFF0F172A),
            ) else listOf(
                Color(0xFF4BC9F2), Color(0xFF7B8EF0), Color(0xFF8B5CF6),
                Color(0xFFCBDDFC), Color(0xFFE5F4FF),
            ),
            startY = 0f,
            endY = h,
        ),
    )

    // Upper-left sky-colored arc flowing into the violet band.
    val left = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.62f, 0f)
        cubicTo(w * 0.57f, h * 0.14f, w * 0.30f, h * 0.09f, w * 0.28f, h * 0.22f)
        cubicTo(w * 0.21f, h * 0.34f, w * 0.11f, h * 0.36f, 0f, h * 0.33f)
        close()
    }
    drawPath(
        path = left,
        brush = Brush.linearGradient(
            listOf(
                if (dark) Color(0xFF102C4C) else Color(0xFF7CE7FF),
                if (dark) Color(0xFF0E2140) else Color(0xFF52B7F5),
            ),
            start = Offset(0f, 0f), end = Offset(w * 0.66f, h * 0.34f),
        ),
    )

    // Upper-right rounded lavender / slate wave.
    val right = Path().apply {
        moveTo(w * 0.52f, 0f)
        lineTo(w, 0f)
        lineTo(w, h * 0.36f)
        cubicTo(w * 0.79f, h * 0.24f, w * 0.53f, h * 0.28f, w * 0.52f, 0f)
        close()
    }
    drawPath(
        path = right,
        brush = Brush.verticalGradient(
            if (dark) listOf(Color(0xFF152640), Color(0xFF1A2750))
            else listOf(Color(0xFFA2B8FF), Color(0xFF9875F7)),
            startY = 0f, endY = h * 0.36f,
        ),
    )

    // The pronounced organic transition from blue-violet into the pale lower surface.
    val lower = Path().apply {
        moveTo(0f, h * 0.56f)
        cubicTo(w * 0.20f, h * 0.43f, w * 0.40f, h * 0.65f, w * 0.65f, h * 0.54f)
        cubicTo(w * 0.80f, h * 0.48f, w * 0.95f, h * 0.49f, w, h * 0.52f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = lower,
        brush = Brush.verticalGradient(
            if (dark) listOf(Color(0xFF14263F), Color(0xFF0D1B31), Color(0xFF0F172A))
            else listOf(Color(0xFFC8E9FF), Color(0xFFE5E7FF), Color(0xFFDCF4FF)),
            startY = h * 0.5f, endY = h,
        ),
    )

    // Subtle lower lilac/indigo ripple; its exact shape follows screen aspect ratio.
    val ripple = Path().apply {
        moveTo(0f, h * 0.78f)
        cubicTo(w * 0.20f, h * 0.69f, w * 0.41f, h * 0.78f, w * 0.59f, h * 0.81f)
        cubicTo(w * 0.78f, h * 0.84f, w * 0.87f, h * 0.70f, w, h * 0.76f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = ripple,
        brush = Brush.verticalGradient(
            if (dark) listOf(Color(0xFF1B2D4D), Color(0xFF0B1529))
            else listOf(Color(0xFFD4CBFF), Color(0xFFE4F4FF)),
            startY = h * 0.76f, endY = h,
        ),
    )
}
