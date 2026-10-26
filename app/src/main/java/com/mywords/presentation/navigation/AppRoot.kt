package com.mywords.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mywords.R
import com.mywords.presentation.category.CategoryScreen
import com.mywords.presentation.category.LibraryViewModel
import com.mywords.presentation.home.HomeScreen
import com.mywords.presentation.practice.PracticeRoute
import com.mywords.presentation.practice.PracticeViewModel
import com.mywords.presentation.progress.ProgressRoute
import com.mywords.presentation.progress.ProgressViewModel
import com.mywords.MyWordsApp
import com.mywords.presentation.settings.DialectSettingsRoute
import com.mywords.presentation.settings.SettingsRoute
import com.mywords.presentation.settings.SettingsViewModel
import com.mywords.presentation.theme.CaregiverInk
import com.mywords.presentation.theme.CaregiverTint
import com.mywords.speech.SpeechIssue

private const val HOME = "home"
private const val PRACTICE = "practice"
private const val PROGRESS = "progress"
private const val SETTINGS = "settings"
private const val DIALECT = "dialect"
private const val CATEGORY_PREFIX = "category:"

/** State-based navigation: home → category (grid / flashcards), practice games, caregiver progress. */
@Composable
fun AppRoot(
    library: LibraryViewModel,
    practice: PracticeViewModel,
    progress: ProgressViewModel,
    settingsVm: SettingsViewModel,
) {
    val state by library.uiState.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf(HOME) }
    val context = LocalContext.current

    // one-shot messages, e.g. "favourites are full"
    LaunchedEffect(Unit) {
        library.messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    // the progress tab is private: leaving Caregiver Mode closes it
    LaunchedEffect(state.caregiverMode, route) {
        if ((route == PROGRESS || route == SETTINGS) && !state.caregiverMode) route = HOME
    }

    val category = if (route.startsWith(CATEGORY_PREFIX)) {
        state.categories.firstOrNull { it.id == route.removePrefix(CATEGORY_PREFIX) }
    } else null

    BackHandler(enabled = route != HOME) { route = HOME }

    val title = when {
        route == PRACTICE -> stringResource(R.string.practice_title)
        route == PROGRESS -> stringResource(R.string.progress_title)
        route == SETTINGS -> stringResource(R.string.settings_title)
        route == DIALECT -> "اللغة والصوت"
        category != null -> category.title
        else -> stringResource(R.string.app_title)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                AppTopBar(
                    title = title,
                    showBack = route != HOME,
                    caregiverMode = state.caregiverMode,
                    onBack = { route = HOME },
                    onPractice = { route = PRACTICE },
                    onDialects = { route = DIALECT },
                    onToggleCaregiver = library::toggleCaregiver,
                )
                if (state.caregiverMode) {
                    CaregiverBanner(
                        onOpenProgress = { route = PROGRESS },
                        onOpenSettings = { route = SETTINGS },
                    )
                }
                state.speechIssue?.let { issue ->
                    SpeechIssueBanner(issue) {
                        try {
                            context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                        } catch (_: ActivityNotFoundException) { /* no installer on this device */ }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            // card text scaling (Normal / Large / Extra Large) applies to everything below the app bar
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, density.fontScale * settings.textSize.factor),
            ) {
                when {
                    route == PRACTICE -> PracticeRoute(practice)
                    route == PROGRESS && state.caregiverMode -> ProgressRoute(progress)
                    route == SETTINGS && state.caregiverMode -> SettingsRoute(settingsVm)
                    route == DIALECT -> DialectSettingsRoute(settingsVm, (context.applicationContext as MyWordsApp).container.speech)
                    category != null -> CategoryScreen(category, state, library)
                    else -> HomeScreen(
                        categories = state.categories,
                        favorites = state.favorites,
                        activeItemId = state.activeItemId,
                        caregiverMode = state.caregiverMode,
                        onOpen = { id -> route = CATEGORY_PREFIX + id },
                        onQuickTap = library::onQuickTap,
                    )
                }
            }
        }
    }
}

/** Soft amber strip – the only hint that Caregiver Mode is on – with the private progress and settings tabs. */
@Composable
private fun CaregiverBanner(onOpenProgress: () -> Unit, onOpenSettings: () -> Unit) {
    Surface(
        color = CaregiverTint,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 2.dp)) {
            Text(
                stringResource(R.string.caregiver_banner),
                color = CaregiverInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                TextButton(onClick = onOpenProgress, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(stringResource(R.string.progress_tab), color = CaregiverInk, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(stringResource(R.string.settings_tab), color = CaregiverInk, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun SpeechIssueBanner(issue: SpeechIssue, onInstall: () -> Unit) {
    Surface(
        color = Color(0xFFFFE0B2),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(
                    if (issue is SpeechIssue.MissingArabicVoice) R.string.voice_missing else R.string.tts_unavailable
                ),
                color = Color(0xFF5D3A00),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (issue is SpeechIssue.MissingArabicVoice) {
                Button(onClick = onInstall, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(stringResource(R.string.install_voice))
                }
            }
        }
    }
}
