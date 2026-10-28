package com.sawti.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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

/** Two seconds from app launch to the main screen, with no onboarding gate. */
const val SAWTI_SPLASH_DURATION_MS = 2_000L

/** Brand-aligned startup. Uses app day/night setting and respects compact screen insets.
 * The full source artwork and density icons are in the user-supplied branding archive;
 * the existing source-control vector mark is retained as a safe no-binary fallback.
 */
@Composable
fun SawtiWelcomeScreen() {
    val dark = LocalIsDark.current
    val background = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF070D18), Color(0xFF0F172A), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF8B5CF6)))
    }
    val textColor = if (dark) Color(0xFFF8FAFC) else Color.White
    val footerColor = if (dark) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
    BoxWithConstraints(Modifier.fillMaxSize().background(background)) {
        val compact = maxHeight < 540.dp
        val markSize = (maxHeight * if (compact) 0.22f else 0.27f).coerceIn(90.dp, 184.dp)
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = if (compact) 8.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "شعار صوتي",
                        modifier = Modifier.size(markSize),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        "صوتي", color = textColor,
                        fontSize = if (compact) 31.sp else 40.sp,
                        fontWeight = FontWeight.Black, maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "SAWTI", color = footerColor,
                        fontSize = if (compact) 15.sp else 19.sp,
                        letterSpacing = 3.sp, maxLines = 1,
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = if (compact) 0.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Developed by Ismail Bin Salem",
                    fontSize = if (compact) 11.sp else 13.sp,
                    color = textColor, fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center, maxLines = 2)
                Text("Ismail92sa@gmail.com",
                    fontSize = if (compact) 10.sp else 12.sp,
                    color = footerColor, textAlign = TextAlign.Center, maxLines = 1)
                Text("v1.0.0 • © 2026 All Rights Reserved",
                    fontSize = if (compact) 9.sp else 11.sp,
                    color = footerColor.copy(alpha = 0.84f),
                    textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}
