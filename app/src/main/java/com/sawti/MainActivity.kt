package com.sawti

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawti.presentation.category.LibraryViewModel
import com.sawti.presentation.navigation.AppRoot
import com.sawti.presentation.practice.PracticeViewModel
import com.sawti.presentation.progress.ProgressViewModel
import com.sawti.presentation.settings.SettingsViewModel
import com.sawti.presentation.theme.SawtiTheme
import com.sawti.presentation.splash.SawtiAuroraSplash

class MainActivity : ComponentActivity() {

    private val libraryVm: LibraryViewModel by viewModels { LibraryViewModel.Factory(application) }
    private val practiceVm: PracticeViewModel by viewModels { PracticeViewModel.Factory(application) }
    private val progressVm: ProgressViewModel by viewModels { ProgressViewModel.Factory(application) }
    private val settingsVm: SettingsViewModel by viewModels { SettingsViewModel.Factory(application) }

    override fun onStop() {
        // Leaving Sawti must never leave a caregiver's microphone recording in background.
        libraryVm.onAppBackgrounded()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aurora paints below transparent system bars during launch.
        // Let Android choose dark/light system icons against the current device theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        // A communication aid should not dim or lock mid-conversation.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val settings by settingsVm.settings.collectAsStateWithLifecycle()
            var continueRequested by rememberSaveable { mutableStateOf(false) }
            val libraryState by libraryVm.uiState.collectAsStateWithLifecycle()
            SawtiTheme(
                themeMode = settings.themeMode,
                performanceMode = settings.performanceMode,
                hapticsEnabled = settings.hapticsEnabled,
            ) {
                // Contrast follows Sawti's chosen theme, not just the phone system theme.
                // The new welcome is dark across its top edge in both themes; use light
                // status icons over it and the dark app bar to retain contrast.
                SideEffect {
                    WindowInsetsControllerCompat(window, window.decorView)
                        .isAppearanceLightStatusBars = false
                }
                // Prepare Home underneath the welcome only after Continue.
                // Keep the illustration visible until the seeded categories
                // arrive, avoiding the dark loading screen from HomeScreen.
                val readyToEnter = libraryState.categories.isNotEmpty() ||
                    libraryState.errorMessage != null
                Box(Modifier.fillMaxSize()) {
                    if (continueRequested) {
                        AppRoot(libraryVm, practiceVm, progressVm, settingsVm)
                    }
                    AnimatedVisibility(
                        visible = !continueRequested || !readyToEnter,
                        modifier = Modifier.fillMaxSize(),
                        enter = EnterTransition.None,
                        exit = fadeOut(animationSpec = tween(durationMillis = 460)),
                    ) {
                        SawtiAuroraSplash(
                            onFinished = { continueRequested = true },
                            preparingMain = continueRequested && !readyToEnter,
                        )
                    }
                }
            }
        }
    }
}
