package com.sawti.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.presentation.theme.LocalIsDark

private val DeepGreen = Color(0xFF1B6652)
private val LightGold = Color(0xFFE8CE91)
private val AyahShape = RoundedCornerShape(20.dp)

@Composable
fun QuranScreen() {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    var chosenSurahId by rememberSaveable { mutableStateOf<Int?>(null) }
    var repeatCount by rememberSaveable { mutableIntStateOf(1) }
    var retryAttempt by remember { mutableIntStateOf(0) }
    var verses by remember { mutableStateOf<List<QuranVerse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var audioState by remember { mutableStateOf(RecitationStatus()) }
    val audio = remember(context) {
        QuranAyahPlayer(context.applicationContext) { audioState = it }
    }
    DisposableEffect(audio) { onDispose { audio.release() } }

    val chosenSurah = QuranSource.shortSurahs.firstOrNull { it.number == chosenSurahId }
    LaunchedEffect(chosenSurahId, retryAttempt) {
        audio.stop()
        verses = emptyList()
        loadError = null
        if (chosenSurah != null) {
            loading = true
            try {
                verses = QuranSource.load(context.applicationContext, chosenSurah)
            } catch (_: Exception) {
                loadError = "تعذّر تحميل الآيات. تأكّد من اتصال الإنترنت وحاول مرة ثانية."
            } finally {
                loading = false
            }
        }
    }
    BackHandler(enabled = chosenSurah != null) {
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
            items(QuranSource.shortSurahs, key = { it.number }) { surah ->
                Surface(
                    onClick = { audio.stop(); chosenSurahId = surah.number },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                    shape = AyahShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) LightGold.copy(alpha = 0.2f) else Color(0xFFD5E6DF)),
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
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
                        Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = DeepGreen)
                    }
                }
            }
            item {
                AttributionText(
                    "الآيات من AlQuran.cloud بالرسم العثماني. التلاوات بصوت الشيخ مشاري راشد العفاسي عبر Islamic Network؛ تشغيل الصوت يحتاج إلى الإنترنت."
                )
            }
        } else {
            item {
                TextButton(onClick = { audio.stop(); chosenSurahId = null }) {
                    Text("العودة إلى قائمة السور")
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
                        "تعذّر تشغيل التلاوة. تأكّد من الإنترنت والصوت، واضغط على الآية للمحاولة من جديد.",
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
                            "نص الآيات من AlQuran.cloud (الرسم العثماني)، وتلاوة الشيخ مشاري راشد العفاسي عبر Islamic Network. يمكن قراءة الآيات المحفوظة دون إنترنت بعد تحميلها أول مرة، أما التلاوة فتحتاج اتصالاً بالإنترنت."
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
