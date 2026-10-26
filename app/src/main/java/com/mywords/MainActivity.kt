package com.mywords

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mywords.presentation.category.LibraryViewModel
import com.mywords.presentation.navigation.AppRoot
import com.mywords.presentation.practice.PracticeViewModel
import com.mywords.presentation.progress.ProgressViewModel
import com.mywords.presentation.settings.SettingsViewModel
import com.mywords.presentation.theme.MyWordsTheme

class MainActivity : ComponentActivity() {

    private val libraryVm: LibraryViewModel by viewModels { LibraryViewModel.Factory(application) }
    private val practiceVm: PracticeViewModel by viewModels { PracticeViewModel.Factory(application) }
    private val progressVm: ProgressViewModel by viewModels { ProgressViewModel.Factory(application) }
    private val settingsVm: SettingsViewModel by viewModels { SettingsViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light status-bar icons on top of the blue app bar.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        // A communication aid should not dim or lock mid-conversation.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val settings by settingsVm.settings.collectAsStateWithLifecycle()
            MyWordsTheme(
                themeMode = settings.themeMode,
                performanceMode = settings.performanceMode,
                hapticsEnabled = settings.hapticsEnabled,
            ) {
                AppRoot(libraryVm, practiceVm, progressVm, settingsVm)
            }
        }
    }
}
