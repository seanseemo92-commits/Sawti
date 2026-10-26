package com.sawti.presentation.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.domain.model.ArabicDialect
import com.sawti.presentation.theme.AppBarBlue
import kotlinx.coroutines.launch

private val PaleSky = Color(0xFF38BDF8)
private val DeepNavy = Color(0xFF1E293B)

/** Calm concentric acoustic ripples; vector-like and drawn locally, never network dependent. */
@Composable
private fun AcousticRipples() {
    Canvas(Modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.75f
        val origin = Offset(size.width * 0.5f, size.height * 0.32f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(PaleSky.copy(alpha = 0.19f), DeepNavy.copy(alpha = 0.045f), Color.Transparent),
                center = origin,
                radius = radius,
            ),
            center = origin,
            radius = radius,
        )
        for (index in 1..5) {
            drawCircle(
                color = PaleSky.copy(alpha = 0.19f - index * 0.023f),
                center = origin,
                radius = size.minDimension * (0.115f + index * 0.115f),
                style = Stroke(width = (2.8f - index * 0.33f).coerceAtLeast(1f), cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * A four-step, Arabic-first first-launch guide. Reopening from caregiver settings
 * never resets the persisted completion flag or any user data.
 */
@Composable
fun OnboardingScreen(
    selectedDialect: ArabicDialect,
    onSelectDialect: (ArabicDialect) -> Unit,
    onFinish: () -> Unit,
) {
    val pager = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AcousticRipples()
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "صوتي",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onFinish) { Text("تخطي") }
            }
            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                beyondViewportPageCount = 1,
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).background(
                            Brush.radialGradient(
                                listOf(PaleSky.copy(alpha = 0.18f), Color.Transparent),
                            ),
                            CircleShape,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (page) {
                            0 -> Text("ص", fontSize = 76.sp, color = PaleSky, fontWeight = FontWeight.Bold)
                            1 -> Icon(Icons.Filled.RecordVoiceOver, contentDescription = null,
                                modifier = Modifier.size(74.dp), tint = PaleSky)
                            2 -> Icon(Icons.Filled.Lock, contentDescription = null,
                                modifier = Modifier.size(74.dp), tint = PaleSky)
                            else -> Icon(Icons.Filled.Language, contentDescription = null,
                                modifier = Modifier.size(74.dp), tint = PaleSky)
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(
                        when (page) {
                            0 -> "صوتي"
                            1 -> "عبّر عن جملتك"
                            2 -> "إعدادات محمية"
                            else -> "لهجتك أقرب لك"
                        },
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        when (page) {
                            0 -> "صوتك حاضر.. في كل كلمة"
                            1 -> "اضغط على الكلمة لتسمعها. وعند تفعيل شريط الجمل من الإعدادات، تجمع عدة كلمات وتنطقها كجملة واحدة."
                            2 -> "أيقونة الترس ⚙️ محمية: اضغط عليها باستمرار 3 ثوانٍ حتى يفتح وضع المرافق، لحماية إعدادات المستخدم من اللمس العرضي."
                            else -> "اختر لهجتك المفضلة. تتبدل الأكلات الشعبية وبعض المفردات مباشرة، وتبقى صورك وتسجيلاتك محفوظة."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                    )
                    if (page == 0) {
                        Spacer(Modifier.height(20.dp))
                        Text("تواصل بثقة وخصوصية، خطوة بعد خطوة.",
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (page == 1) {
                        Spacer(Modifier.height(18.dp))
                        Text("الضغط المطوّل يتيح نطق الكلمة وحدها، دون تغيير الجملة.",
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (page == 3) {
                        Spacer(Modifier.height(12.dp))
                        ArabicDialect.entries.chunked(2).forEach { group ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            ) {
                                group.forEach { dialect ->
                                    FilterChip(
                                        selected = dialect == selectedDialect,
                                        onClick = { onSelectDialect(dialect) },
                                        label = { Text(dialect.label, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pager.currentPage > 0) {
                    OutlinedButton(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) {
                        Text("السابق")
                    }
                } else Spacer(Modifier.size(72.dp, 1.dp))
                Text("${pager.currentPage + 1} / 4", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = {
                    if (pager.currentPage == 3) onFinish()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }) { Text(if (pager.currentPage == 3) "ابدأ الآن" else "التالي") }
            }
        }
    }
}
