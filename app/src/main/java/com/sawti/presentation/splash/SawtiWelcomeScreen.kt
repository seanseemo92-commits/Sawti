package com.sawti.presentation.splash

import android.app.Activity
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.sawti.R
import com.sawti.presentation.theme.LocalIsDark

/**
 * Reference-matched startup artwork. The central symbol is always proportional.
 * Developer credits remain bottom-aligned and inside system insets, without
 * colliding with the centre logo on compact handsets or landscape screens.
 */
@Composable
fun SawtiWelcomeScreen() {
    val dark = LocalIsDark.current
    val context = LocalContext.current
    DisposableEffect(context, dark) {
        val activity = context as? Activity
        val controller = activity?.let {
            WindowCompat.getInsetsController(it.window, it.window.decorView)
        }
        val prior = controller?.isAppearanceLightStatusBars
        if (controller != null) controller.isAppearanceLightStatusBars = !dark
        onDispose {
            if (controller != null && prior != null) {
                controller.isAppearanceLightStatusBars = prior
            }
        }
    }

    val background = if (dark) {
        Brush.linearGradient(
            listOf(Color(0xFF06152A), Color(0xFF0D2440), Color(0xFF0F172A)),
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF43C8F2), Color(0xFF7C79EA), Color(0xFFE0E6FF)),
        )
    }
    val foreground = if (dark) Color(0xFFF7F4EE) else Color(0xFF142350)
    val secondary = foreground.copy(alpha = if (dark) 0.83f else 0.85f)

    BoxWithConstraints(Modifier.fillMaxSize().background(background)) {
        Canvas(Modifier.fillMaxSize()) {
            if (dark) {
                val centre = Offset(size.width * 0.51f, size.height * 0.48f)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF244264).copy(alpha = 0.30f), Color.Transparent),
                        center = centre,
                        radius = size.width * 0.75f,
                    ),
                    center = centre,
                    radius = size.width * 0.75f,
                )
            } else {
                // Fluid violet/sky bands from the supplied light-mode screenshot.
                val topWave = Path().apply {
                    moveTo(0f, size.height * 0.13f)
                    cubicTo(
                        size.width * 0.33f, size.height * 0.04f,
                        size.width * 0.39f, size.height * 0.38f,
                        size.width, size.height * 0.27f,
                    )
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(topWave, Color(0xFF65D5F6).copy(alpha = 0.36f))
                val bottomWave = Path().apply {
                    moveTo(0f, size.height * 0.73f)
                    cubicTo(
                        size.width * 0.27f, size.height * 0.68f,
                        size.width * 0.51f, size.height * 0.89f,
                        size.width, size.height * 0.78f,
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(bottomWave, Color(0xFFE5ECFF).copy(alpha = 0.74f))
            }
        }

        val markSize = (maxHeight * 0.28f).coerceIn(100.dp, 210.dp)
        val titleSize = if (maxHeight < 520.dp) 31.sp else 43.sp
        Column(
            modifier = Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "شعار صوتي: حرف ص وموجات صوتية",
                        modifier = Modifier.size(markSize),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        text = "صوتي",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = foreground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    Text(
                        text = "SAWTI",
                        fontSize = if (maxHeight < 520.dp) 15.sp else 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                        color = secondary,
                        maxLines = 1,
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "تطوير: إسماعيل بن سالم",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Text(
                    text = "Ismail92sa@gmail.com",
                    fontSize = 13.sp,
                    color = secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = "الإصدار 1.0.0 • صُنع بحب في ليبيا 🇱🇾",
                    fontSize = 11.sp,
                    color = secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
