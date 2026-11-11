package com.sawti.presentation.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.onboarding.spotlightAnchor
import com.sawti.presentation.theme.CardShape
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardBorder

@Composable
fun QuranScreen(chosenSurahId: Int?, onChooseSurah: (Int?) -> Unit, focusFirstVerse: Boolean = false) {
    val vm: QuranViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(chosenSurahId) { vm.openSurah(chosenSurahId) }
    DisposableEffect(vm, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.stopAudio()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); vm.leaveScreen() }
    }
    QuranContent(state, chosenSurahId, onChooseSurah, vm::selectReciter,
        vm::selectRepeats, vm::play, vm::stopAudio, vm::toggleDownload,
        onRetry = { vm.openSurah(chosenSurahId, retry = true) },
        focusFirstVerse = focusFirstVerse, onPlayFull = vm::playFull)
}

/** Content is separate from network/player ownership so layouts and taps can be tested offline. */
@Composable
internal fun QuranContent(
    state: QuranUiState,
    chosenSurahId: Int?,
    onChooseSurah: (Int?) -> Unit,
    onReciter: (QuranReciter) -> Unit,
    onRepeats: (Int) -> Unit,
    onPlay: (QuranVerse) -> Unit,
    onStop: () -> Unit,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
    focusFirstVerse: Boolean = false,
    onPlayFull: () -> Unit = {},
) {
    val surah = QuranSource.selections.firstOrNull { it.selectionId == chosenSurahId }
    val colors = quranColors()
    val listState = rememberLazyListState()
    LaunchedEffect(chosenSurahId) { listState.scrollToItem(0) }
    LaunchedEffect(focusFirstVerse, chosenSurahId, state.verses.size, state.loading) {
        if (focusFirstVerse && !state.loading && state.surahId == chosenSurahId && state.verses.isNotEmpty()) {
            listState.animateScrollToItem(2)
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("quran-content"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "heading") {
            QuranHeading(surah?.title ?: "القرآن الكريم",
                if (surah == null) "سور قصيرة وآيات مختارة للاستماع والتكرار" else
                    if (surah.segmented) "اضغط المقطع للاستماع، واضغط مجددًا لتعيده مع القارئ"
                    else "اضغط الآية للاستماع، واضغط مجددًا لتعيدها مع القارئ",
                surah?.countLabel)
        }
        if (surah == null) {
            item(key = "choose") {
                Text("اختر السورة أو الآيات", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            itemsIndexed(QuranSource.selections, key = { _, chapter -> "surah-${chapter.selectionId}" }) { index, chapter ->
                StaggeredCascade(row = index, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = if (index == 0) Modifier.spotlightAnchor("quran_surah") else Modifier) {
                        QuranSurahCard(chapter) { onChooseSurah(chapter.selectionId) }
                    }
                }
            }
        } else {
            item(key = "settings") {
                Box(Modifier.spotlightAnchor("quran_reciter")) {
                    QuranSettings(state, onReciter, onRepeats, onDownload, surah, onPlayFull, onStop)
                }
            }
            if (state.downloadError != null) item(key = "download-error") { ErrorText(state.downloadError) }
            if (state.audio.phase == RecitationPhase.ERROR) item(key = "audio-error") {
                ErrorText(when (state.audio.failure) {
                    RecitationFailure.FOCUS -> "الصوت مشغول الآن. أعد الضغط على الآية بعد انتهاء المكالمة أو الصوت الآخر."
                    RecitationFailure.DECODER -> "تعذّر قراءة التسجيل. أعد الضغط على الآية لإعادة تنزيلها وتشغيلها."
                    else -> "تعذّر تحميل أو حفظ التلاوة. تحقّق من الاتصال والمساحة المتاحة، ثم اضغط الآية للمحاولة."
                })
            }
            when {
                state.loading || state.surahId != chosenSurahId -> item(key = "loading") {
                    Column(Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = colors.accent)
                        Text("جارٍ تحميل الآيات…", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                state.loadError != null -> item(key = "load-error") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErrorText(state.loadError)
                        QuranAction("إعادة المحاولة", onClick = onRetry)
                    }
                }
                else -> itemsIndexed(state.verses, key = { _, verse -> verse.itemKey }) { index, verse ->
                    StaggeredCascade(row = index, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = if (index == 0) Modifier.spotlightAnchor("quran_verse") else Modifier) {
                            QuranVerseCard(verse, state.audio,
                                onClick = { onPlay(verse) }, onStop = onStop)
                        }
                    }
                }
            }
        }
        item(key = "source") {
            Text("النص: AlQuran.cloud • الآيات: EveryAyah • التسجيل الكامل: MP3Quran ومصادر المصحف المعلّم",
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun QuranHeading(title: String, subtitle: String, countLabel: String?) {
    val colors = quranColors()
    Surface(shape = CardShape, color = colors.header,
        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.4f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = colors.gold)
                Text(title, fontSize = 25.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
            }
            if (countLabel != null) Text(countLabel, color = colors.gold,
                style = MaterialTheme.typography.labelLarge)
            Text(subtitle, color = Color(0xFFF2E4C6), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuranSurahCard(surah: ShortSurah, onClick: () -> Unit) {
    val colors = quranColors()
    QuranInteractiveSurface(onClick = onClick, tag = "surah-${surah.selectionId}") {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(46.dp).background(colors.activeSurface, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = colors.accent)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(surah.title, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(surah.countLabel, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                tint = colors.accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuranSettings(
    state: QuranUiState, onReciter: (QuranReciter) -> Unit,
    onRepeats: (Int) -> Unit, onDownload: () -> Unit,
    surah: ShortSurah, onPlayFull: () -> Unit, onStop: () -> Unit,
) {
    val colors = quranColors()
    var expanded by remember { mutableStateOf(false) }
    Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface, border = cardBorder()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("القارئ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Box {
                QuranAction(state.reciter.displayName, icon = Icons.Filled.KeyboardArrowDown,
                    onClick = { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    QuranReciter.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.displayName) },
                            onClick = { expanded = false; onReciter(option) },
                            trailingIcon = { if (option == state.reciter) Icon(Icons.Filled.Check, null, tint = colors.accent) })
                    }
                }
            }
            Text(if (surah.segmented) "عدد مرات تكرار المقطع" else "عدد مرات تكرار الآية", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(1, 3, 5).forEach { count ->
                    val selected = count == state.repeats
                    Surface(color = if (selected) colors.activeSurface else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp,
                            if (selected) colors.accent else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp).clip(RoundedCornerShape(14.dp))
                            .semantics { stateDescription = if (selected) "محدد" else "غير محدد" }
                            .bounceClick(onClick = { onRepeats(count) }, accent = colors.accent)) {
                        Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Text("$count ×", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                }
            }
            if (state.verses.isNotEmpty() && QuranFullSources.available(surah)) {
                val fullStatus = state.audio
                val isFullActive = fullStatus.fullSelectionId == surah.selectionId &&
                    fullStatus.phase in setOf(RecitationPhase.BUFFERING, RecitationPhase.PLAYING, RecitationPhase.PAUSED)
                val title = when (surah.selectionId) {
                    2001 -> "تشغيل الآية كاملة"
                    2002 -> "تشغيل خواتيم البقرة"
                    else -> "تشغيل السورة كاملة"
                }
                Surface(shape = RoundedCornerShape(16.dp), color = colors.header,
                    border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(16.dp)).testTag("quran-full-play")
                        .bounceClick(onClick = if (isFullActive) onStop else onPlayFull, accent = colors.gold)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        when {
                            isFullActive && fullStatus.phase == RecitationPhase.BUFFERING ->
                                CircularProgressIndicator(Modifier.size(24.dp), color = colors.gold, strokeWidth = 2.dp)
                            else -> Icon(if (isFullActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null, tint = colors.gold, modifier = Modifier.size(27.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(when {
                            isFullActive && fullStatus.phase == RecitationPhase.BUFFERING -> "جارٍ تحميل التلاوة… · إلغاء"
                            isFullActive && fullStatus.phase == RecitationPhase.PAUSED -> "إيقاف التلاوة المؤقتة"
                            isFullActive -> "إيقاف التلاوة · المتبقي ${fullStatus.remaining}"
                            else -> title
                        }, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    }
                }
                Text("التسجيل الكامل يُحمّل أول مرة ويُحفظ للاستماع دون إنترنت",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            } else if (surah.selectionId == 2002) {
                Text("تشغيل خواتيم البقرة متواصلة سيتوفر بعد التحقق من تسجيل واحد أصلي للآيتين.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
            if (state.verses.isNotEmpty()) {
                if (state.savedAyahs == state.verses.size && !state.downloading) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, null, tint = colors.accent)
                        Text("الآيات منفردة محفوظة للاستماع دون إنترنت", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                } else {
                    QuranAction(if (state.downloading) "إلغاء التنزيل · ${state.savedAyahs} من ${state.verses.size}" else if (surah.segmented) "تحميل تلاوة المقاطع" else "تحميل تلاوة السورة",
                        icon = if (state.downloading) Icons.Filled.Stop else Icons.Filled.Download, onClick = onDownload)
                }
            }
        }
    }
}

@Composable
internal fun QuranVerseCard(verse: QuranVerse, status: RecitationStatus, onClick: () -> Unit, onStop: () -> Unit) {
    val colors = quranColors()
    val active = status.globalVerseNumber == verse.globalNumber && status.segmentKey == verse.segmentKey &&
        status.fullSelectionId == null && status.phase in setOf(RecitationPhase.PLAYING, RecitationPhase.BUFFERING, RecitationPhase.PAUSED)
    val buffering = active && status.phase == RecitationPhase.BUFFERING
    val paused = active && status.phase == RecitationPhase.PAUSED
    val unit = if (verse.segmentIndex == null) "الآية" else "المقطع"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuranInteractiveSurface(onClick = onClick, active = active, tag = verse.itemKey,
            description = when {
                buffering -> "جارٍ تجهيز $unit"
                paused -> "تلاوة $unit متوقفة مؤقتًا بسبب صوت آخر، وستستأنف بعد انتهائه"
                active -> "$unit قيد التشغيل؛ اضغط للإعادة من البداية"
                else -> "اضغط للاستماع إلى $unit"
            }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = colors.activeSurface, shape = CircleShape,
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f))) {
                        Text("${verse.verseNumber}", modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                            color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                    Text(if (buffering) "جارٍ التجهيز…" else if (paused) "توقف مؤقت · ${verse.label}" else if (active) "تلاوة $unit · ${verse.label}" else verse.label,
                        modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge)
                    if (buffering) CircularProgressIndicator(Modifier.size(24.dp), color = colors.accent, strokeWidth = 2.dp)
                    else Icon(if (active && !paused) Icons.Filled.Equalizer else Icons.Filled.PlayArrow,
                        contentDescription = null, tint = colors.accent, modifier = Modifier.size(27.dp))
                }
                Text(verse.uthmaniText, modifier = Modifier.fillMaxWidth().testTag(if (verse.segmentIndex == null) "quran-text-${verse.globalNumber}" else "quran-text-${verse.itemKey}"),
                    style = TextStyle(fontFamily = QuranFont, fontSynthesis = FontSynthesis.None,
                        fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 62.sp,
                        textAlign = TextAlign.Center, textDirection = TextDirection.ContentOrRtl,
                        platformStyle = PlatformTextStyle(includeFontPadding = true)),
                    color = MaterialTheme.colorScheme.onSurface)
                if (active) Text(if (buffering) "يتم تجهيز $unit قبل التشغيل" else if (paused) "ستُستأنف التلاوة بعد انتهاء المقاطعة" else "اضغط $unit مجددًا للإعادة مع القارئ",
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
        // A local stop action follows only this active ayah; there is no fixed player.
        if (active) QuranAction(if (buffering) "إلغاء تجهيز $unit" else "إيقاف التلاوة · المتبقي ${status.remaining}",
            icon = Icons.Filled.Stop, onClick = onStop)
    }
}

@Composable
private fun QuranAction(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    val colors = quranColors()
    Surface(shape = RoundedCornerShape(16.dp), color = colors.activeSurface,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp))
            .bounceClick(onClick = onClick, accent = colors.accent)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = colors.accent)
            Text(text, color = colors.accent, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
private fun QuranInteractiveSurface(
    onClick: () -> Unit, tag: String, active: Boolean = false, description: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = quranColors()
    val performance = LocalPerformanceMode.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val color by animateColorAsState(
        when { active -> colors.activeSurface; pressed -> colors.pressedSurface; else -> MaterialTheme.colorScheme.surface },
        animationSpec = if (performance) snap() else tween(190), label = "quran-card-color")
    val glow by animateFloatAsState(if ((active || pressed) && !performance) 1f else 0f,
        animationSpec = if (performance) snap() else tween(190), label = "quran-card-glow")
    Box(Modifier.fillMaxWidth().drawBehind {
        if (glow > 0f) drawRoundRect(colors.accent.copy(alpha = 0.16f * glow),
            cornerRadius = CornerRadius(25.dp.toPx()), style = Stroke(width = 6.dp.toPx()))
    }) {
        Surface(shape = CardShape, color = color,
            border = if (active || pressed) BorderStroke(2.dp, colors.accent) else cardBorder(),
            shadowElevation = if (performance) 0.dp else if (LocalIsDark.current) 4.dp else 8.dp,
            modifier = Modifier.fillMaxWidth().padding(2.dp).heightIn(min = 88.dp).clip(CardShape)
                .testTag(tag).semantics { if (description != null) stateDescription = description }
                .bounceClick(onClick = onClick, accent = colors.accent, interactionSource = interaction),
            content = content)
    }
}
