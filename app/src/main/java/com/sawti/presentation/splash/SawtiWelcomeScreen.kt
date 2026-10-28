package com.sawti.presentation.splash

import androidx.compose.runtime.Composable

/** Full splash time: 600ms footer fade + 1900ms hold, per provided design. */
const val SAWTI_SPLASH_DURATION_MS = 2_500L

/** Legacy entry point; the splash itself owns navigation timing. */
@Composable
fun SawtiWelcomeScreen(onNavigateToMain: () -> Unit) =
    SawtiSplashScreen(onNavigateToMain)
