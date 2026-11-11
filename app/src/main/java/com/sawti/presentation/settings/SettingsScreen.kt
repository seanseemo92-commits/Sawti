package com.sawti.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.sawti.SawtiApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawti.R
import com.sawti.domain.model.SpeechRate
import com.sawti.domain.model.TextSize
import com.sawti.domain.model.ThemeMode
import com.sawti.domain.model.TouchDwell
import com.sawti.presentation.theme.AppBarBlue
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.cardElevation

/** Caregiver-only display settings: theme, card text size and Performance Mode. */
@Composable
fun SettingsRoute(viewModel: SettingsViewModel, onStartTour: (String) -> Unit = {}, onOpenAbout: () -> Unit = {}) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val operations = remember { (context.applicationContext as SawtiApp).container.backupOperations }
    val backup by operations.state.collectAsStateWithLifecycle()
    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) operations.export(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingRestore = uri
    }
    if (pendingRestore != null) {
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("تأكيد استرجاع النسخة") },
            text = { Text("سيتم استبدال كل الكلمات والصور والتسجيلات الحالية بمحتوى النسخة المختارة. احفظ نسخة من بياناتك الحالية أولاً. بعد النجاح سيتطلب التطبيق إعادة فتحه لتحديث البيانات.") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingRestore ?: return@TextButton
                    pendingRestore = null
                    operations.restore(uri)
                }) { Text("استرجاع واستبدال") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("إلغاء") } },
        )
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingBlock(stringResource(R.string.about_title)) {
            Text(stringResource(R.string.about_settings_summary))
            Button(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_title))
            }
        }
        SettingBlock("النسخ الاحتياطي والاسترجاع") {
            Text("احفظ كلماتك وصورك وتسجيلاتك في ملف ZIP على جهازك. الملف غير مشفّر؛ احتفظ به في مكان آمن. لاسترجاع النسخ الجديدة على جهاز آخر، حدّث تطبيق صوتي عليه أولاً.")
            Button(enabled = !backup.busy && !backup.restartRequired, onClick = { exportLauncher.launch("Sawti-backup.zip") }) {
                Text("إنشاء نسخة احتياطية")
            }
            Button(enabled = !backup.busy && !backup.restartRequired, onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }) {
                Text("استرجاع نسخة احتياطية")
            }
            backup.message?.let { Text(it) }
        }
        SettingBlock("اللهجة والصوت") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DialectFlag(settings.dialect)
                Spacer(Modifier.width(12.dp))
                Text("اللهجة الحالية: ${settings.dialect.label}. يمكن تغيير اللهجة واختيار الصوت من أيقونة اللغة أعلى الشاشة.")
            }
        }
        SettingBlock("دليل استخدام صوتي") {
            Text("تقدر تعاود الجولة التعريفية، أو تختار شرحًا خاصًا بالقرآن أو العائلة.")
            Button(onClick = { onStartTour("home") }, modifier = Modifier.fillMaxWidth()) {
                Text("إعادة الجولة الرئيسية")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStartTour("quran") }, modifier = Modifier.weight(1f)) {
                    Text("شرح القرآن")
                }
                Button(onClick = { onStartTour("family") }, modifier = Modifier.weight(1f)) {
                    Text("شرح العائلة")
                }
            }
        }
        SettingBlock(stringResource(R.string.settings_theme)) {
            ChoiceRow(
                options = listOf(
                    ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                    ThemeMode.LIGHT to stringResource(R.string.theme_light),
                    ThemeMode.DARK to stringResource(R.string.theme_dark),
                ),
                selected = settings.themeMode,
                onSelect = viewModel::setThemeMode,
            )
        }
        SettingBlock(stringResource(R.string.settings_text_size)) {
            ChoiceRow(
                options = listOf(
                    TextSize.NORMAL to stringResource(R.string.size_normal),
                    TextSize.LARGE to stringResource(R.string.size_large),
                    TextSize.EXTRA_LARGE to stringResource(R.string.size_xlarge),
                ),
                selected = settings.textSize,
                onSelect = viewModel::setTextSize,
            )
        }
        SettingBlock("شريط تركيب الجملة") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("إظهار شريط لتجميع الكلمات ثم نطقها بالترتيب. الضغط المطوّل ينطق البطاقة مباشرةً.", modifier = Modifier.weight(1f))
                Switch(checked = settings.sentenceStripEnabled, onCheckedChange = viewModel::setSentenceStripEnabled)
            }
        }
        SettingBlock("زمن تثبيت اللمس") {
            Text("لتخفيف الضغطات غير المقصودة الناتجة عن ارتعاش اليد: اضغط مع الاستمرار على البطاقة قبل رفع الإصبع. السحب للتمرير يلغي الضغطة.")
            ChoiceRow(
                options = listOf(TouchDwell.OFF to "معطل", TouchDwell.NORMAL to "عادي 300ms", TouchDwell.LONG to "طويل 500ms"),
                selected = settings.touchDwell,
                onSelect = viewModel::setTouchDwell,
            )
        }
        SettingBlock(stringResource(R.string.settings_speech_rate)) {
            ChoiceRow(
                options = listOf(
                    SpeechRate.SLOW to stringResource(R.string.rate_slow),
                    SpeechRate.NORMAL to stringResource(R.string.rate_normal),
                    SpeechRate.FAST to stringResource(R.string.rate_fast),
                ),
                selected = settings.speechRate,
                onSelect = viewModel::setSpeechRate,
            )
        }
        SettingBlock(stringResource(R.string.settings_haptics)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings_haptics_desc), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Switch(checked = settings.hapticsEnabled, onCheckedChange = viewModel::setHapticsEnabled)
            }
        }
        SettingBlock(stringResource(R.string.settings_performance)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings_performance_desc), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Switch(checked = settings.performanceMode, onCheckedChange = viewModel::setPerformanceMode)
            }
        }
    }
}

@Composable
private fun SettingBlock(title: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val dark = LocalIsDark.current
    val selectedContainer = if (dark) MaterialTheme.colorScheme.primaryContainer else AppBarBlue
    val selectedContent = if (dark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
    FlowRow(Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            val on = value == selected
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (on) selectedContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (on) selectedContent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 92.dp).heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(value) }),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

