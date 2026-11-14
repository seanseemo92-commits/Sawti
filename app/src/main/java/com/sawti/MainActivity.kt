package com.sawti

import androidx.compose.foundation.layout.fillMaxSize
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawti.presentation.category.LibraryViewModel
import com.sawti.presentation.navigation.AppRoot
import com.sawti.presentation.practice.PracticeViewModel
import com.sawti.presentation.progress.ProgressViewModel
import com.sawti.presentation.settings.SettingsViewModel
import com.sawti.presentation.theme.SawtiTheme

class MainActivity : ComponentActivity() {
    private val libraryVm: LibraryViewModel by viewModels { LibraryViewModel.Factory(application) }
    private val practiceVm: PracticeViewModel by viewModels { PracticeViewModel.Factory(application) }
    private val progressVm: ProgressViewModel by viewModels { ProgressViewModel.Factory(application) }
    private val settingsVm: SettingsViewModel by viewModels { SettingsViewModel.Factory(application) }
    override fun onStop() {
        if ((application as SawtiApp).startup.value == StartupStatus.READY) libraryVm.onAppBackgrounded()
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val app = application as SawtiApp
            val startup by app.startup.collectAsStateWithLifecycle()
            if (startup != StartupStatus.READY) {
                SawtiTheme {
                    androidx.compose.material3.Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        ) {
                            if (startup == StartupStatus.RECOVERING) {
                                androidx.compose.material3.CircularProgressIndicator()
                                androidx.compose.material3.Text("جارٍ التحقق من البيانات المسترجعة…")
                            } else {
                                androidx.compose.material3.Text("تعذّر إكمال استرداد البيانات")
                                androidx.compose.material3.Button(onClick = app::retryRecovery) {
                                    androidx.compose.material3.Text("إعادة المحاولة")
                                }
                            }
                        }
                    }
                }
                return@setContent
            }
            val settings by settingsVm.settings.collectAsStateWithLifecycle()
            SawtiTheme(
                themeMode = settings.themeMode,
                performanceMode = settings.performanceMode,
                hapticsEnabled = settings.hapticsEnabled,
            ) {
                AppRoot(libraryVm, practiceVm, progressVm, settingsVm)
            }
        }
    }
}

