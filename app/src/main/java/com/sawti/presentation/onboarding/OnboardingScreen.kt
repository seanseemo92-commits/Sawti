package com.sawti.presentation.onboarding

import android.app.Activity
import androidx.core.view.WindowCompat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.painterResource
import com.sawti.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardElevation
import kotlinx.coroutines.launch

private val PaleSky = Color(0xFF38BDF8)
private val DeepNavy = Color(0xFF1E293B)

/** Calm concentric acoustic ripples; vector-like and drawn locally, never network dependent. */
@Composable
private fun AcousticRipples(page: Int) {
    val reducedMotion = LocalPerformanceMode.current
    if (reducedMotion) {
        RippleArtwork(page, 0f)
    } else {
        val transition = rememberInfiniteTransition(label = "onboarding-acoustic-breath")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2800), repeatMode = RepeatMode.Reverse),
            label = "ripple-phase",
        )
        RippleArtwork(page, progress)
    }
}

@Composable
private fun RippleArtwork(page: Int, progress: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.78f
        // The wave origin moves gently as the user swipes between onboarding pages.
        val origin = Offset(size.width * (0.5f + page * 0.012f), size.height * 0.32f)
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
                radius = size.minDimension * (0.115f + index * 0.115f + progress * 0.010f),
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
    val context = LocalContext.current
    val dark = LocalIsDark.current
    // The guide uses a light canvas instead of the navy app bar. Keep system icons legible.
    DisposableEffect(context, dark) {
        val activity = context as? Activity
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
        val previous = controller?.isAppearanceLightStatusBars
        if (controller != null) controller.isAppearanceLightStatusBars = !dark
        onDispose { if (controller != null && previous != null) controller.isAppearanceLightStatusBars = previous }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AcousticRipples(pager.currentPage)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
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
                Surface(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 9.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.88f else 0.81f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = if (dark) 0.18f else 0.85f)),
                    shadowElevation = cardElevation(6.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                    Box(
                        modifier = Modifier.size(120.dp).background(
                            Brush.radialGradient(
                                if (page == 0) listOf(DeepNavy, Color(0xFF0F172A))
                                else listOf(PaleSky.copy(alpha = 0.18f), Color.Transparent),
                            ),
                            CircleShape,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (page) {
                            0 -> Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = "شعار صوتي",
                                modifier = Modifier.size(108.dp),
                            )
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
                            0 -> "صوتي SAWTI"
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
                        color = if (page == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = if (page == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (page == 0) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp,
                    )
                    if (page == 0) {
                        Spacer(Modifier.height(20.dp))
                        Text("تواصل بثقة وخصوصية، خطوة بعد خطوة.",
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(18.dp))
                        Text("تطوير: إسماعيل بن سالم",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, textAlign = TextAlign.Center)
                        Text("Ismail92sa@gmail.com",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp, textAlign = TextAlign.Center)
                        Text("الإصدار 1.0.0 • صُنع بحب في ليبيا 🇱🇾",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp, textAlign = TextAlign.Center)
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
