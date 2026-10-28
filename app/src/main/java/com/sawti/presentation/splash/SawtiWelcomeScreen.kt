package com.sawti.presentation.splash

import androidx.compose.runtime.Composable

/** Splash timing: 700ms fade-in + 1900ms hold (2.6 seconds). */
const val SAWTI_SPLASH_DURATION_MS = 2_600L

/** Legacy entry point; Splash handles navigation on its own. */
@Composable
fun SawtiWelcomeScreen(onNavigateToMain: () -> Unit) =
    SawtiSplashScreen(onNavigateToMain)
