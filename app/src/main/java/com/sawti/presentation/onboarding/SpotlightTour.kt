package com.sawti.presentation.onboarding

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The new cinematic guide deliberately has no dependency on the retired onboarding gate. */
object TourGuide {
    const val HOME = "home"
    const val QURAN = "quran"
    const val VERSE = "verse"
    const val FAMILY = "family"

    private val purple = Color(0xFFD3BCFF)
    private val gold = Color(0xFFE8CE91)
    private val blue = Color(0xFF9CD7FF)

    fun steps(kind: String, availableCategories: Set<String> = emptySet()): List<SpotlightStep> = when (kind) {
        // Keep first-use guidance intentionally short. Additional tours are available on demand.
        HOME -> buildList {
            add(SpotlightStep("top_caregiver", "وضع المرافق", "اضغط على الترس ٣ ثوانٍ لتفعيل وضع المرافق.", purple))
            if ("daily" in availableCategories) {
                add(SpotlightStep("home_daily", "تواصل بسهولة", "اضغط على الطلب أو الصورة حتى يعبّر صوتي عن احتياجك.", purple))
            }
            add(SpotlightStep("home_quran", "القرآن الكريم", "افتح القرآن للاستماع إلى الآيات وتكرارها.", gold))
        }
        QURAN -> listOf(
            SpotlightStep("quran_surah", "اختر سورة", "اضغط على أي سورة لعرض آياتها والاستماع للتلاوة.", gold),
        )
        VERSE -> listOf(
            SpotlightStep("quran_reciter", "خيارات التلاوة", "اختر القارئ وعدد مرات تكرار كل آية من هنا.", gold),
            SpotlightStep("quran_verse", "استمع آية بآية", "اضغط على الآية لسماعها، واضغط عليها مرة ثانية لإعادة تلاوتها.", gold),
        )
        FAMILY -> listOf(
            SpotlightStep("family_relatives", "الأقرباء", "هذا القسم يعرض صلات القرابة مثل أبي وأمي وابني.", blue),
            SpotlightStep("family_people", "أسماء العائلة", "اختر أحد الأقسام مثل الزوج أو الأبناء. في وضع المرافق تقدر تضيف الأسماء والصور بالزر +.", blue),
        )
        else -> emptyList()
    }
}

data class SpotlightStep(val target: String, val title: String, val description: String, val accent: Color)

/** Versioned and separate from Room, app content and the retired four-step onboarding. */
class TourProgress(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sawti_spotlight_v1", Context.MODE_PRIVATE)
    fun completed(kind: String): Boolean = prefs.getBoolean("completed_$kind", false)
    fun complete(kind: String) { prefs.edit().putBoolean("completed_$kind", true).apply() }
}

/** Tracks real Compose bounds; no screen-dependent hard-coded coordinates. */
class SpotlightRegistry {
    private val positions = mutableStateMapOf<String, Rect>()
    fun update(key: String, rect: Rect) { positions[key] = rect }
    fun remove(key: String) { positions.remove(key) }
    fun rect(key: String): Rect? = positions[key]
}
val LocalSpotlightRegistry = staticCompositionLocalOf<SpotlightRegistry?> { null }

@Composable
fun Modifier.spotlightAnchor(key: String): Modifier {
    val registry = LocalSpotlightRegistry.current ?: return this
    DisposableEffect(registry, key) {
        onDispose { registry.remove(key) }
    }
    return this.onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) registry.update(key, coordinates.boundsInRoot())
    }
}

/**
 * Modal, non-click-through focus layer. Only explicit guide controls are active.
 * The spotlight cuts a real transparent hole rather than copying content or intercepting
 * a caregiver long press. Device performance mode disables motion/pulsing.
 */
@Composable
fun SpotlightOverlay(
    step: SpotlightStep,
    index: Int,
    total: Int,
    registry: SpotlightRegistry,
    reducedMotion: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val target = registry.rect(step.target)
    val scrim = remember { Animatable(0f) }
    val cardFade = remember(step.target) { Animatable(0f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) scrim.snapTo(0.79f)
        else scrim.animateTo(0.79f, tween(350))
    }
    LaunchedEffect(step.target, reducedMotion) {
        if (reducedMotion) cardFade.snapTo(1f)
        else cardFade.animateTo(1f, tween(250))
    }
    val duration = if (reducedMotion) 0 else 560
    val left by animateFloatAsState(target?.left ?: 0f, tween(duration), label = "spotlight-left")
    val top by animateFloatAsState(target?.top ?: 0f, tween(duration), label = "spotlight-top")
    val right by animateFloatAsState(target?.right ?: 0f, tween(duration), label = "spotlight-right")
    val bottom by animateFloatAsState(target?.bottom ?: 0f, tween(duration), label = "spotlight-bottom")
    val glow = if (reducedMotion) 0.45f else {
        val transition = rememberInfiniteTransition(label = "spotlight-breath")
        val opacity by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
            label = "spotlight-halo",
        )
        opacity
    }
    val focus = if (target != null) Rect(left, top, right, bottom) else null
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = {}) },
    ) {
        Canvas(Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
            drawRect(Color(0xFF0A1020).copy(alpha = scrim.value))
            if (focus != null) {
                val p = 5.dp.toPx()
                val origin = Offset((focus.left - p).coerceAtLeast(0f), (focus.top - p).coerceAtLeast(0f))
                val extent = Size(
                    (focus.width + p * 2).coerceAtMost(size.width - origin.x).coerceAtLeast(1f),
                    (focus.height + p * 2).coerceAtMost(size.height - origin.y).coerceAtLeast(1f),
                )
                val radius = CornerRadius(20.dp.toPx())
                drawRoundRect(
                    color = Color.Transparent, topLeft = origin, size = extent,
                    cornerRadius = radius, blendMode = BlendMode.Clear,
                )
                drawRoundRect(
                    color = step.accent.copy(alpha = glow), topLeft = origin, size = extent,
                    cornerRadius = radius, style = Stroke(width = 7.dp.toPx()),
                )
                drawRoundRect(
                    color = step.accent, topLeft = origin, size = extent,
                    cornerRadius = radius, style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        val focusMidY = focus?.center?.y ?: 0f
        val cardAtBottom = focus == null || focusMidY < constraints.maxHeight * 0.53f
        Surface(
            modifier = Modifier
                .align(if (cardAtBottom) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .alpha(cardFade.value)
                .semantics { paneTitle = "دليل استخدام صوتي" },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, step.accent.copy(alpha = 0.8f)),
            shadowElevation = if (reducedMotion) 0.dp else 8.dp,
        ) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✦ دليل صوتي", color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${index + 1} / $total", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(step.title, fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge)
                Text(step.description, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    TextButton(onClick = onSkip, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("تخطي")
                    }
                    Spacer(Modifier.weight(1f))
                    if (index > 0) OutlinedButton(
                        onClick = onPrevious, modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("السابق") }
                    Button(onClick = onNext, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(if (index == total - 1) "إنهاء" else "التالي")
                    }
                }
            }
        }
    }
}
