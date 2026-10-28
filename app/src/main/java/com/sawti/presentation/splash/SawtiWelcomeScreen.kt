package com.sawti.presentation.splash

import androidx.compose.runtime.Composable

/** Legacy entry point; the splash itself owns navigation timing. */
@Composable
fun SawtiWelcomeScreen(onNavigateToMain: () -> Unit) =
    SawtiSplashScreen(onNavigateToMain)
