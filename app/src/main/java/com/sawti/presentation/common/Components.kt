@file:OptIn(ExperimentalFoundationApi::class)

package com.sawti.presentation.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.sawti.presentation.theme.LocalIsDark
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sawti.R
import com.sawti.domain.model.WordItem
import com.sawti.domain.model.TouchDwell
import com.sawti.presentation.theme.AppBarBlue
import com.sawti.presentation.theme.HintAmber
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardElevation
import com.sawti.presentation.theme.RecordRed
import java.io.File

/**
 * Understated adult AAC identity: a three-dp accent rather than painting the whole card.
 * Cards remain semantic touch targets; this is purely decorative.
 */
@Composable
fun AdultAccentBar(color: Color) {
    Box(Modifier.fillMaxWidth().height(3.dp).background(color))
}

/** Default cards retain bounce/long-click. With touch dwell, scroll consumes pointer events and
 * cancels the tap; lifting a stationary finger after the chosen interval confirms it.
 * A longer hold still opens the existing focus/editor action. */
fun Modifier.bounceClick(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val dwell = LocalTouchDwell.current
    val currentClick by rememberUpdatedState(onClick)
    val currentLongClick by rememberUpdatedState(onLongClick)
    val source = remember { MutableInteractionSource() }
    var dwellPressed by remember { mutableStateOf(false) }
    val pressed by source.collectIsPressedAsState()
    val performance = LocalPerformanceMode.current
    val scale by animateFloatAsState(
        targetValue = if ((pressed || dwellPressed) && !performance) 0.94f else 1f,
        animationSpec = if (performance) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "bounce",
    )
    val base = graphicsLayer { scaleX = scale; scaleY = scale }
    if (dwell == TouchDwell.OFF) {
        base.combinedClickable(
            interactionSource = source,
            indication = null,
            onLongClick = onLongClick?.let { long ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    long()
                }
            },
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentClick()
            },
        )
    } else {
        base
            .semantics {
                onClick {
                    currentClick() // Switch Access and TalkBack don't use physical dwell.
                    true
                }
                if (onLongClick != null) onLongClick {
                    currentLongClick?.invoke()
                    true
                }
            }
            .pointerInput(dwell) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val id = down.id
                    var drift = Offset.Zero
                    dwellPressed = true
                    try {
                        while (true) {
                        // Observe after the LazyGrid scroll handler has had its chance to consume.
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        val change = event.changes.firstOrNull { it.id == id } ?: break
                        drift += change.positionChange()
                        if (change.isConsumed || drift.getDistance() > viewConfiguration.touchSlop ||
                            event.changes.any { it.id != id && it.pressed }) break
                        if (!change.pressed) {
                            val elapsed = change.uptimeMillis - down.uptimeMillis
                            if (elapsed >= dwell.millis) {
                                // Nested hint/favorite buttons own this release; don't also trigger
                                // the containing AAC card's speech action.
                                change.consume()
                                val longAction = currentLongClick
                                if (longAction != null && elapsed >= maxOf(900L, dwell.millis + 500L)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    longAction()
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentClick()
                                }
                            }
                            break
                        }
                        }
                    } finally {
                        // Release even when scrolling, multi-touch or cancellation consumes us.
                        dwellPressed = false
                    }
                }
            }
    }
}

/**
 * Picture for a word: attached photo → built-in emoji → first letter.
 * [crop] fills the frame (portraits); otherwise the image is fitted (cut-out style).
 */
@Composable
fun ItemImage(
    item: WordItem,
    accent: Color,
    modifier: Modifier = Modifier,
    crop: Boolean = false,
    glyphScale: Float = 0.55f,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        val photo = item.photoPath
        val performance = LocalPerformanceMode.current
        val density = LocalDensity.current
        val glyphSize = with(density) { (side * glyphScale).toSp() }
        when {
            photo != null -> {
                val context = LocalContext.current
                // Decode to the size actually shown (smaller + RGB565 + no crossfade in Performance Mode).
                val sidePx = with(density) { side.toPx() }.let { if (it.isFinite() && it > 0f) it.toInt() else 512 }
                val target = sidePx.coerceIn(96, if (performance) 320 else 1600)
                val request = remember(photo, target, performance) {
                    ImageRequest.Builder(context)
                        .data(File(photo))
                        .size(target, target)
                        .allowRgb565(performance)
                        .crossfade(!performance)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = item.displayText,
                    contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            item.emoji.isNotBlank() -> Text(item.emoji, fontSize = glyphSize)
            else -> Text(
                item.displayText.take(1),
                fontSize = glyphSize,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
            )
        }
    }
}

/**
 * A dedicated image stage for high-quality cutout JPEG/WebP and personal photographs.
 * Coil already loads source images from the app-private photo path with size-aware decoding.
 * The emoji remains only as a meaningful AAC fallback when no image has been attached.
 * The aura is behind the smaller image so it stays visible instead of being occluded.
 */
@Composable
fun CardImageContent(
    item: WordItem,
    accent: Color,
    isCurrentlyPlaying: Boolean,
    modifier: Modifier = Modifier,
    crop: Boolean = false,
) {
    val dark = LocalIsDark.current
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val diameter = (minOf(maxWidth, maxHeight) - 8.dp).coerceAtLeast(88.dp)
        val pictureSize = (diameter * 0.82f).coerceAtLeast(80.dp)
        Box(
            Modifier.size(diameter).clip(CircleShape).background(
                if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(
                    accent.copy(alpha = if (dark) 0.12f else 0.055f),
                    Color.Transparent,
                ))
            ))
            VoiceAura(active = isCurrentlyPlaying)
            ItemImage(
                item = item,
                accent = accent,
                modifier = Modifier.size(pictureSize),
                crop = crop,
                glyphScale = 0.66f,
            )
        }
    }
}

/** Small, subtle 💡 button: speaks only the phonemic cue. */
@Composable
fun HintButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Lightbulb,
            contentDescription = stringResource(R.string.hint_button),
            tint = HintAmber,
            modifier = Modifier.size((size * 0.55f).dp),
        )
    }
}

/** Small, subtle 💬 button: speaks the cloze carrier phrase ("نَأْكُلُ الـ...") up to the missing word. */
@Composable
fun PhraseHintButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = stringResource(R.string.sentence_hint),
            tint = AppBarBlue,
            modifier = Modifier.size((size * 0.52f).dp),
        )
    }
}

/** Caregiver Mode only: pins / unpins a card to the quick-access bar. */
@Composable
fun FavoriteStar(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(if (isFavorite) R.string.favorite_unpin else R.string.favorite_pin),
            tint = HintAmber,
            modifier = Modifier.size(26.dp),
        )
    }
}

/** Marks cards that can be edited while Caregiver Mode is on. */
@Composable
fun EditBadge(modifier: Modifier = Modifier) {
    Icon(
        Icons.Filled.Edit,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
            .size(30.dp)
            .background(RecordRed.copy(alpha = 0.9f), CircleShape)
            .padding(6.dp),
    )
}

/** Large rounded action with icon + label (listen, call, next…). */
@Composable
fun ActionPill(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (enabled) color else color.copy(alpha = 0.35f),
        contentColor = Color.White,
        shadowElevation = cardElevation(if (enabled) 4.dp else 0.dp),
        modifier = modifier
            .heightIn(min = 56.dp)
            .then(if (enabled) Modifier.bounceClick(onClick = onClick) else Modifier),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
