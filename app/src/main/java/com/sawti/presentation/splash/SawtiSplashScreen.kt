package com.sawti.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Brand artwork comes only from the ORIGINAL supplied PNG artwork, never from
 * Canvas / Path / generated wave vectors or Compose text pretending to be a logo.
 * A full-viewport Image uses Fit so the original wordmark is never cropped or
 * geometrically stretched on tall or narrow Android screens.
 *
 * Footer is the only Compose text and is English-only, outside the supplied art.
 * The MainActivity owns the existing two-second transition directly to AppRoot.
 */
@Composable
fun SawtiSplashScreen() {
    val dark = LocalIsDark.current
    val background = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF081729), Color(0xFF0F172A)))
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF42BDF2), Color(0xFF7776E8), Color(0xFFD0DAFF), Color(0xFFCCEAFF)),
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(background)) {
        val compact = maxHeight < 600.dp
        // This source image includes the original gold/white emblem, wave and
        // "صوتي SAWTI" wordmark as ONE raster image, unchanged by the UI.
        Image(
            painter = painterResource(
                if (dark) R.drawable.ic_splash_logo_dark else R.drawable.ic_splash_logo_light,
            ),
            contentDescription = "Sawti",
            modifier = Modifier.fillMaxSize().offset(y = if (compact) (-30).dp else (-20).dp),
            contentScale = ContentScale.Fit,
        )

        // Readability scrim is confined to the footer; no overlay touches the logo.
        Box(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (compact) 150.dp else 196.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A).copy(alpha = 0f),
                            Color(0xFF0F172A).copy(alpha = 0.90f),
                            Color(0xFF0F172A),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(bottom = if (compact) 10.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 5.dp),
        ) {
            Text(
                text = "Developed by Ismail Bin Salem",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Text(
                text = "Ismail92sa@gmail.com",
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Text(
                text = "v1.0.0 • © 2026 All Rights Reserved",
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
