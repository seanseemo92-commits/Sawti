package com.sawti.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Process
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
import com.sawti.R
import com.sawti.SawtiApp
import com.sawti.presentation.category.CategoryScreen
import com.sawti.presentation.category.CustomCategoryManager
import com.sawti.presentation.category.LibraryViewModel
import com.sawti.presentation.common.SentenceStrip
import com.sawti.presentation.common.LocalTouchDwell
import com.sawti.presentation.home.HomeScreen
import com.sawti.presentation.practice.PracticeRoute
import com.sawti.presentation.practice.PracticeViewModel
import com.sawti.presentation.progress.ProgressRoute
import com.sawti.presentation.progress.ProgressViewModel
import com.sawti.presentation.quran.QuranScreen
import com.sawti.presentation.settings.DialectSettingsRoute
import com.sawti.presentation.settings.SettingsRoute
import com.sawti.presentation.settings.SettingsViewModel
import com.sawti.presentation.theme.CaregiverInk
import com.sawti.presentation.theme.CaregiverTint
import com.sawti.presentation.theme.categoryBannerColor
import com.sawti.presentation.theme.paletteOf
import com.sawti.speech.SpeechIssue

private const val HOME = "home"
private const val QURAN = "quran"
private const val PRACTICE = "practice"
private const val PROGRESS = "progress"
private const val SETTINGS = "settings"
private const val DIALECT = "dialect"
private const val CATEGORY_PREFIX = "category:"

@Composable
fun AppRoot(library: LibraryViewModel, practice: PracticeViewModel, progress: ProgressViewModel, settingsVm: SettingsViewModel) {
    val state by library.uiState.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf(HOME) }
    var quranSurahId by rememberSaveable { mutableStateOf<Int?>(null) }
    var manageCategories by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val operations = (context.applicationContext as SawtiApp).container.backupOperations
    val backup by operations.state.collectAsStateWithLifecycle()
    val backupLocked = backup.busy || backup.restartRequired
    val navigationLocked = { operations.state.value.let { it.busy || it.restartRequired } }
    LaunchedEffect(Unit) {
        library.messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.caregiverMode, route) {
        if ((route == PROGRESS || route == SETTINGS) && !state.caregiverMode) route = HOME
    }
    val category = if (route.startsWith(CATEGORY_PREFIX)) {
        state.categories.firstOrNull { it.id == route.removePrefix(CATEGORY_PREFIX) }
    } else null
    LaunchedEffect(state.categories, route) {
        if (route.startsWith(CATEGORY_PREFIX) && state.categories.isNotEmpty() && category == null) route = HOME
    }
    val navigateBack: () -> Unit = {
        if (!navigationLocked()) {
            if (route == QURAN && quranSurahId != null) quranSurahId = null
            else route = HOME
        }
    }
    BackHandler(enabled = route != HOME || backupLocked, onBack = navigateBack)
    val title = when {
        route == QURAN -> "القرآن الكريم"
        route == PRACTICE -> stringResource(R.string.practice_title)
        route == PROGRESS -> stringResource(R.string.progress_title)
        route == SETTINGS -> stringResource(R.string.settings_title)
        route == DIALECT -> "اللغة والصوت"
        category != null -> category.title
        else -> stringResource(R.string.app_title)
    }
    val sectionColor = when {
        category != null -> categoryBannerColor(category.id, paletteOf(category.palette).banner)
        route == QURAN -> Color(0xFF176448)
        else -> null
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                AppTopBar(
                    title = title,
                    sectionColor = sectionColor,
                    showBack = route != HOME,
                    caregiverMode = state.caregiverMode,
                    onBack = navigateBack,
                    onPractice = { if (!navigationLocked()) route = PRACTICE },
                    onDialects = { if (!navigationLocked()) route = DIALECT },
                    onToggleCaregiver = { if (!navigationLocked()) library.toggleCaregiver() },
                )
                if (settings.sentenceStripEnabled && (route == HOME || category != null)) {
                    SentenceStrip(
                        pieces = state.sentencePieces,
                        speakingPieceIndex = state.playbackVisual.sentenceIndex.takeIf { state.playbackVisual.isSpeaking },
                        onPlay = library::playSentenceStrip,
                        onBackspace = library::deleteLastSentencePiece,
                        onClear = library::clearSentenceStrip,
                    )
                }
                if (route == HOME && state.caregiverMode) {
                    TextButton(onClick = { if (!navigationLocked()) manageCategories = true }) {
                        Text("إدارة الأقسام المخصصة +")
                    }
                }
                if (state.caregiverMode) {
                    CaregiverBanner(
                        onOpenProgress = { if (!navigationLocked()) route = PROGRESS },
                        onOpenSettings = { if (!navigationLocked()) route = SETTINGS },
                    )
                }
                if (state.categories.isNotEmpty() && state.categoryLoadError != null) {
                    TextButton(
                        enabled = !backupLocked && !state.isLoadingCategories,
                        onClick = { if (!navigationLocked()) library.retryLoad() },
                    ) { Text("تعذّر تحديث الأقسام — إعادة المحاولة") }
                }
                state.speechIssue?.let { issue ->
                    SpeechIssueBanner(issue) {
                        try { context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)) }
                        catch (_: ActivityNotFoundException) { }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, density.fontScale * settings.textSize.factor),
                LocalTouchDwell provides settings.touchDwell,
            ) {
                when {
                    route == QURAN -> QuranScreen(quranSurahId) { id ->
                        if (!navigationLocked()) quranSurahId = id
                    }
                    route == PRACTICE -> PracticeRoute(practice)
                    route == PROGRESS && state.caregiverMode -> ProgressRoute(progress)
                    route == SETTINGS && state.caregiverMode -> SettingsRoute(settingsVm)
                    route == DIALECT -> DialectSettingsRoute(
                        settingsVm,
                        (context.applicationContext as SawtiApp).container.speech,
                        (context.applicationContext as SawtiApp).container.audioDispatcher,
                    )
                    category != null -> CategoryScreen(category, state, library)
                    else -> HomeScreen(
                        categories = state.categories,
                        speakingItemId = state.playbackVisual.activeWordId.takeIf { state.playbackVisual.isSpeaking },
                        onOpen = { id -> if (!navigationLocked()) route = CATEGORY_PREFIX + id },
                        onOpenQuran = {
                            if (!navigationLocked()) {
                                library.onAppBackgrounded()
                                quranSurahId = null
                                route = QURAN
                            }
                        },
                        onRetry = { if (!navigationLocked()) library.retryLoad() },
                        isLoading = state.isLoadingCategories,
                        loadError = state.categoryLoadError,
                        retryEnabled = !backupLocked,
                    )
                }
            }
        }
    }
    if (backup.busy) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { Text(if (backup.restoring) "جارٍ استرجاع البيانات" else "جارٍ إنشاء النسخة الاحتياطية") },
            text = { Text("جاري المعالجة. لا تغلق التطبيق حتى تكتمل العملية.") },
            confirmButton = {},
        )
    }
    if (backup.restartRequired) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { Text(if (backup.restoreSucceeded) "تم الاسترجاع بنجاح" else "تعذّر الاسترجاع") },
            text = { Text(backup.message ?: "يلزم إغلاق التطبيق وفتحه مجدداً لتحميل البيانات بأمان.") },
            confirmButton = {
                TextButton(onClick = { Process.killProcess(Process.myPid()) }) { Text("إغلاق التطبيق") }
            },
        )
    }
    if (manageCategories && state.caregiverMode && !backupLocked) {
        CustomCategoryManager(
            categories = state.categories,
            onCreate = library::createCategory,
            onUpdate = library::editCategory,
            onDelete = library::deleteCategory,
            onDismiss = { manageCategories = false },
        )
    }
    if (!backupLocked) state.errorMessage?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = library::clearError,
            title = { Text("تعذّر إكمال العملية") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = library::clearError) { Text("موافق") } },
        )
    }
}

@Composable
private fun CaregiverBanner(onOpenProgress: () -> Unit, onOpenSettings: () -> Unit) {
    Surface(
        color = CaregiverTint,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 2.dp)) {
            Text(stringResource(R.string.caregiver_banner), color = CaregiverInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(when (issue) {
                    SpeechIssue.EngineUnavailable -> R.string.tts_unavailable
                    SpeechIssue.MissingArabicVoice -> R.string.voice_missing
                    SpeechIssue.PlaybackFailed -> R.string.tts_playback_failed
                }),
                color = Color(0xFF5D3A00), fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f),
            )
            if (issue is SpeechIssue.MissingArabicVoice) {
                Button(onClick = onInstall, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(stringResource(R.string.install_voice))
                }
            }
        }
    }
}
