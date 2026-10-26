package com.sawti.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawti.domain.model.ArabicDialect
import com.sawti.speech.SpeechManager
import java.util.Locale

/** Dialect selection is available to the user, independently of caregiver editing permissions. */
@Composable
fun DialectSettingsRoute(
    settingsVm: SettingsViewModel,
    speech: SpeechManager,
    modifier: Modifier = Modifier,
) {
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    val voiceStatus by speech.voiceStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val country = Locale.forLanguageTag(settings.dialect.localeTag).country
    // A Libyan voice is rarely installed; show every Arabic voice rather than hiding
    // Egyptian/Saudi/other offline options behind the selected dialect country.
    val voices = voiceStatus.voices.sortedWith(
        compareByDescending<com.sawti.speech.InstalledArabicVoice> {
            Locale.forLanguageTag(it.localeTag).country.equals(country, ignoreCase = true)
        }.thenByDescending { it.offline }.thenBy { it.name }
    )

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("اللهجة وطريقة الكلام", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "تتغيّر العبارات الأساسية المكتوبة والمنطوقة فوراً. الأسماء والكلمات المخصصة والتسجيلات التي أضفتها تبقى كما هي. بعض الكلمات لم تُترجم بعد.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ArabicDialect.entries.forEach { dialect ->
            val selected = settings.dialect == dialect
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { settingsVm.setDialect(dialect) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = { settingsVm.setDialect(dialect) })
                    Column(Modifier.weight(1f)) {
                        Text(dialect.label, fontWeight = FontWeight.SemiBold)
                        Text(dialect.sample, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الصوت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    when {
                        !voiceStatus.initialized -> "جارٍ فحص محرك النطق العربي على الجهاز…"
                        voiceStatus.regionalVoiceAvailable -> "يتوفر صوت عربي بإعدادات المنطقة المحددة. قد يختلف النطق الفعلي باختلاف محرك الجهاز."
                        voiceStatus.actualTag != null -> "لا يوجد صوت مثبت مؤكد لهذه اللهجة. يجري استعمال أقرب صوت عربي متاح (${voiceStatus.actualTag})."
                        else -> "محرك النطق العربي غير متاح. ثبّت بيانات اللغة العربية أو جرّب صوتاً مسجلاً."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (voices.isNotEmpty()) {
                    Text("الأصوات المتاحة على جهازك (${voices.size})", fontWeight = FontWeight.SemiBold)
                    voices.forEach { voice ->
                        val chosen = settings.preferredVoice == voice.name ||
                            (settings.preferredVoice == null && voice.name == voiceStatus.actualVoice)
                        Row(
                            Modifier.fillMaxWidth().clickable { settingsVm.setPreferredVoice(voice.name) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = chosen, onClick = { settingsVm.setPreferredVoice(voice.name) })
                            Column(Modifier.weight(1f)) {
                                Text(voice.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${voice.localeTag} · ${if (Locale.forLanguageTag(voice.localeTag).country.equals(country, ignoreCase = true)) "مطابق للمنطقة · " else "صوت عربي بديل · "}${if (voice.offline) "لا يحتاج اتصالاً بالإنترنت" else "قد يتطلب اتصالاً بالإنترنت"}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    OutlinedButton(onClick = { settingsVm.setPreferredVoice(null) }) {
                        Text("اختيار الصوت تلقائياً")
                    }
                }
                Button(onClick = { speech.speak(settings.dialect.sample) }, modifier = Modifier.fillMaxWidth()) {
                    Text("استمع إلى تجربة هذه اللهجة")
                }
                OutlinedButton(onClick = {
                    try {
                        context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "افتح إعدادات محرك النطق لتثبيت الأصوات العربية", Toast.LENGTH_LONG).show()
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("إدارة الأصوات في Android")
                }
            }
        }
        Text(
            "ملاحظة: اللهجات تختلف من منطقة لأخرى. هذه مجموعة أولية من العبارات المدققة، وليست ترجمة آلية شاملة. لا يمكن للتطبيق توفير صوت أصلي لكل لهجة إذا لم يكن مثبتاً في جهازك.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
