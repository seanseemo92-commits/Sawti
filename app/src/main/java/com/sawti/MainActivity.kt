package com.sawti

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
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
import com.sawti.presentation.splash.SawtiSplashScreen

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
        // The original splash artwork paints under the transparent status/navigation bars.
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
            var showBrandWelcome by rememberSaveable { mutableStateOf(true) }
            SawtiTheme(
                themeMode = settings.themeMode,
                performanceMode = settings.performanceMode,
                hapticsEnabled = settings.hapticsEnabled,
            ) {
                if (showBrandWelcome) SawtiSplashScreen(onNavigateToMain = { showBrandWelcome = false })
                else AppRoot(libraryVm, practiceVm, progressVm, settingsVm)
            }
        }
    }
}
