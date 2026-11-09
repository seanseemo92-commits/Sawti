package com.sawti.presentation.quran

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.CardShape
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardBorder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val DeepGreen = Color(0xFF1B6652)
private val LightGold = Color(0xFFE8CE91)
private val AyahShape = RoundedCornerShape(20.dp)

@Composable
fun QuranScreen() {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val reciterPreferences = remember(context) {
        context.applicationContext.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE)
    }
    var selectedReciter by rememberSaveable {
        mutableStateOf(QuranReciter.fromEdition(reciterPreferences.getString("selected_edition", null)))
    }
    var reciterMenuExpanded by remember { mutableStateOf(false) }
    var chosenSurahId by rememberSaveable { mutableStateOf<Int?>(null) }
    var repeatCount by rememberSaveable { mutableIntStateOf(1) }
    var retryAttempt by remember { mutableIntStateOf(0) }
    var verses by remember { mutableStateOf<List<QuranVerse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var audioState by remember { mutableStateOf(RecitationStatus()) }
    var downloadedAyahs by remember { mutableIntStateOf(0) }
    var downloadInProgress by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val cache = remember(context, selectedReciter) {
        QuranAudioCache(context.applicationContext, selectedReciter)
    }
    val audio = remember(context, cache) {
        QuranAyahPlayer(context.applicationContext, cache) { status ->
            audioState = status
            if (status.phase == RecitationPhase.PLAYING) {
                downloadedAyahs = verses.count { cache.cachedFile(it.globalNumber) != null }
            }
        }
    }
    DisposableEffect(audio) {
        onDispose {
            downloadJob?.cancel()
            audio.release()
        }
    }
    LaunchedEffect(cache, verses) {
        downloadedAyahs = verses.count { cache.cachedFile(it.globalNumber) != null }
    }

    val chosenSurah = QuranSource.shortSurahs.firstOrNull { it.number == chosenSurahId }
    LaunchedEffect(chosenSurahId, retryAttempt) {
        audio.stop()
        downloadJob?.cancel()
        downloadJob = null
        downloadInProgress = false
        downloadError = null
        downloadedAyahs = 0
        verses = emptyList()
        loadError = null
        if (chosenSurah != null) {
            loading = true
            try {
                verses = QuranSource.load(context.applicationContext, chosenSurah)
                downloadedAyahs = verses.count { cache.cachedFile(it.globalNumber) != null }
            } catch (_: Exception) {
                loadError = "تعذّر تحميل الآيات. تأكّد من اتصال الإنترنت وحاول مرة ثانية."
            } finally {
                loading = false
            }
        }
    }
    BackHandler(enabled = chosenSurah != null) {
        downloadJob?.cancel()
        audio.stop()
        chosenSurahId = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 18.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            QuranHeading(
                title = if (chosenSurah == null) "القرآن الكريم" else "سورة ${chosenSurah.name}",
                subtitle = if (chosenSurah == null) {
                    "سور قصيرة للتلاوة والتكرار، مناسبة للتدريب والاستماع"
                } else {
                    "اضغط على أي آية لسماعها، واضغط عليها مجدداً لإعادتها من البداية"
                },
                dark = isDark,
            )
        }
        if (chosenSurah == null) {
            item {
                Text(
                    "اختر السورة",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
            }
            itemsIndexed(QuranSource.shortSurahs, key = { _, surah -> surah.number }) { index, surah ->
                StaggeredCascade(row = index, modifier = Modifier.fillMaxWidth()) {
                    val dark = LocalIsDark.current
                    val performance = LocalPerformanceMode.current
                    val accent = if (dark) Color(0xFF68C9A8) else DeepGreen
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val glow by animateFloatAsState(
                        targetValue = if (pressed && !performance) 1f else 0f,
                        animationSpec = if (performance) snap() else tween(170),
                        label = "QuranSurahGlow",
                    )
                    val cardColor by animateColorAsState(
                        targetValue = when {
                            performance -> MaterialTheme.colorScheme.surface
                            pressed && dark -> Color(0xFF213C37)
                            pressed -> Color(0xFFE8F5EF)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        animationSpec = if (performance) snap() else tween(170),
                        label = "QuranSurahCardColor",
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
                            .drawBehind {
                                if (glow > 0f) {
                                    drawRoundRect(
                                        color = accent.copy(alpha = 0.32f * glow),
                                        cornerRadius = CornerRadius(24.dp.toPx()),
                                        style = Stroke(width = 7.dp.toPx()),
                                    )
                                }
                            },
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(2.dp)
                                .heightIn(min = 84.dp).clip(CardShape)
                                .bounceClick(
                                    onClick = { audio.stop(); chosenSurahId = surah.number },
                                    accent = accent,
                                    interactionSource = interaction,
                                ),
                            shape = CardShape,
                            color = cardColor,
                            border = if (pressed && !performance) {
                                BorderStroke(2.dp, accent.copy(alpha = 0.85f))
                            } else {
                                cardBorder()
                            },
                            shadowElevation = if (performance) 0.dp else if (dark) 4.dp else 8.dp,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(
                                        "سورة ${surah.name}",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        "${surah.verseCount} آيات",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                Icon(
                                    Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = accent,
                                )
                            }
                        }
                    }
                }
            }
            item {
                AttributionText(
                    "الآيات بالرسم العثماني من AlQuran.cloud، والتلاوة بصوت الشيخ مشاري العفاسي عبر Islamic Network. يمكن تنزيل صوت السورة للاستماع دون إنترنت."
                )
            }
        } else {
            item {
                TextButton(onClick = {
                    downloadJob?.cancel()
                    audio.stop()
                    chosenSurahId = null
                }) {
                    Text("العودة إلى قائمة السور")
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "القارئ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Box {
                        TextButton(onClick = { reciterMenuExpanded = true }) {
                            Text(selectedReciter.displayName)
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "اختيار القارئ")
                        }
                        DropdownMenu(
                            expanded = reciterMenuExpanded,
                            onDismissRequest = { reciterMenuExpanded = false },
                        ) {
                            QuranReciter.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        reciterMenuExpanded = false
                                        if (option != selectedReciter) {
                                            audio.stop()
                                            downloadJob?.cancel()
                                            downloadJob = null
                                            downloadInProgress = false
                                            downloadError = null
                                            downloadedAyahs = 0
                                            selectedReciter = option
                                            reciterPreferences.edit()
                                                .putString("selected_edition", option.editionId)
                                                .apply()
                                        }
                                    },
                                    trailingIcon = {
                                        if (option == selectedReciter) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
            if (verses.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        if (downloadedAyahs == verses.size) {
                            Text(
                                "تلاوة السورة بصوت " + selectedReciter.displayName + " محفوظة على الهاتف وتشتغل بدون إنترنت",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            TextButton(onClick = {
                                if (downloadInProgress) {
                                    downloadJob?.cancel()
                                    downloadInProgress = false
                                } else {
                                    val numbers = verses.map { it.globalNumber }
                                    downloadError = null
                                    downloadInProgress = true
                                    downloadJob = scope.launch {
                                        try {
                                            for (number in numbers) {
                                                cache.obtain(number)
                                                downloadedAyahs = numbers.count {
                                                    cache.cachedFile(it) != null
                                                }
                                            }
                                        } catch (cancelled: CancellationException) {
                                            throw cancelled
                                        } catch (_: Exception) {
                                            downloadError = "تعذّر تنزيل بعض التلاوات. تأكّد من الإنترنت وأعد المحاولة."
                                        } finally {
                                            downloadInProgress = false
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Download, contentDescription = null)
                                Text(if (downloadInProgress) "إلغاء التنزيل" else "تحميل السورة بصوت " + selectedReciter.displayName)
                            }
                            if (downloadInProgress) {
                                Text(
                                    "تم حفظ " + downloadedAyahs + " من " + verses.size + " آيات",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            downloadError?.let { message ->
                                Text(message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "عدد مرات تكرار الآية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(1, 3, 5).forEach { count ->
                            FilterChip(
                                selected = repeatCount == count,
                                onClick = { repeatCount = count },
                                label = { Text("${count} ×", fontSize = 16.sp) },
                            )
                        }
                    }
                }
            }
            if (audioState.phase == RecitationPhase.ERROR) {
                item {
                    Text(
                        "تعذّر تشغيل التلاوة. الآية غير المحفوظة تحتاج إنترنت أول مرة، ثم تشتغل دون اتصال.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (audioState.phase == RecitationPhase.PLAYING ||
                audioState.phase == RecitationPhase.BUFFERING
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (audioState.phase == RecitationPhase.BUFFERING)
                                "جارٍ تجهيز التلاوة..."
                            else "التكرارات المتبقية: ${audioState.remaining}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { audio.stop() }) {
                            Icon(Icons.Filled.Stop, contentDescription = null)
                            Text("إيقاف")
                        }
                    }
                }
            }
            when {
                loading -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text("جارٍ تحميل الآيات...")
                    }
                }
                loadError != null -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            loadError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = { retryAttempt++ }) { Text("إعادة المحاولة") }
                    }
                }
                else -> {
                    items(verses, key = { it.globalNumber }) { verse ->
                        VerseCard(
                            verse = verse,
                            isActive = audioState.globalVerseNumber == verse.globalNumber &&
                                (audioState.phase == RecitationPhase.PLAYING ||
                                    audioState.phase == RecitationPhase.BUFFERING),
                            isBuffering = audioState.globalVerseNumber == verse.globalNumber &&
                                audioState.phase == RecitationPhase.BUFFERING,
                            onClick = { audio.play(verse.globalNumber, repeatCount) },
                        )
                    }
                    item {
                        AttributionText(
                            "الآيات من AlQuran.cloud والتلاوة من Islamic Network. يتم حفظ صوت الآية للقارئ المختار بعد أول تشغيل؛ لتفعيل الاستماع دون إنترنت لقارئ آخر نزّل تلاوته بشكل مستقل."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranHeading(title: String, subtitle: String, dark: Boolean) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        color = if (dark) Color(0xFF123C34) else DeepGreen,
        border = BorderStroke(1.dp, LightGold.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = LightGold)
            Text(
                title, color = Color.White, fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp, lineHeight = 34.sp, textAlign = TextAlign.Center,
            )
            Text(
                subtitle, color = Color(0xFFF2E4C6),
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun VerseCard(
    verse: QuranVerse,
    isActive: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = AyahShape,
        border = BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) DeepGreen else MaterialTheme.colorScheme.outlineVariant,
        ),
        tonalElevation = if (isActive) 4.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "الآية ${verse.verseNumber}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isBuffering) {
                    CircularProgressIndicator(modifier = Modifier.padding(5.dp))
                } else {
                    Icon(
                        Icons.Filled.PlayArrow, contentDescription = null,
                        tint = if (isActive) DeepGreen else MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = verse.uthmaniText,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 29.sp,
                lineHeight = 49.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )
            if (isActive && !isBuffering) {
                Text(
                    "اضغط لإعادة التلاوة من البداية",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun AttributionText(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
