package com.sawti.presentation.splash

import androidx.compose.runtime.Composable

/** Two-second branded startup; navigation is handled by MainActivity. */
const val SAWTI_SPLASH_DURATION_MS = 2_000L

/** Historical entry point retained for compatibility with older screens/tests. */
@Composable
fun SawtiWelcomeScreen() = SawtiSplashScreen()
