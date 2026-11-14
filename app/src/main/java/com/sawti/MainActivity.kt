package com.sawti

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val app = application as SawtiApp
            val startup by app.startup.collectAsStateWithLifecycle()
            if (startup != StartupStatus.READY) {
                SawtiTheme {
                    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.fillMaxSize().systemBarsPadding()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        ) {
                            if (startup == StartupStatus.RECOVERING) {
                                androidx.compose.material3.CircularProgressIndicator()
                                androidx.compose.material3.Text(stringResource(R.string.recovery_checking))
                            } else {
                                androidx.compose.material3.Text(stringResource(R.string.recovery_failed))
                                androidx.compose.material3.Button(onClick = app::retryRecovery) {
                                    androidx.compose.material3.Text(stringResource(R.string.retry))
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

