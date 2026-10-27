@file:OptIn(ExperimentalFoundationApi::class)

package com.sawti.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sawti.R
import com.sawti.domain.model.TouchDwell
import com.sawti.domain.model.WordItem
import com.sawti.presentation.theme.AppBarBlue
import com.sawti.presentation.theme.CardShape
import com.sawti.presentation.theme.CardSurfaceDark
import com.sawti.presentation.theme.CardSurfaceLight
import com.sawti.presentation.theme.HintAmber
import com.sawti.presentation.theme.Ink
import com.sawti.presentation.theme.InkOnDark
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.RecordRed
import com.sawti.presentation.theme.VoiceAura
import com.sawti.presentation.theme.cardBorder
import com.sawti.presentation.theme.cardElevation
import java.io.File
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Local providers & constants
// ---------------------------------------------------------------------------

val LocalTouchDwell = staticCompositionLocalOf { TouchDwell.OFF }

/**
 * Share of the card's vertical height reserved for the image stage
 * (the spec asks for 60%-65%). Use it from the card layout, e.g.
 * `Modifier.weight(CardImageStageFraction)` against `1f - CardImageStageFraction`
 * for the label area.
 */
const val CardImageStageFraction = 0.62f

private const val CascadeStepMillis = 35L
private const val CascadeMaxRows = 10

// ---------------------------------------------------------------------------
// Basic decorations
// ---------------------------------------------------------------------------

@Composable
fun AdultAccentBar(color: Color) {
    Box(Modifier.fillMaxWidth().height(3.dp).background(color))
}

// ---------------------------------------------------------------------------
// Touch handling: spring compression + haptics + Touch-Dwell anti-tremor filter
// ---------------------------------------------------------------------------

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
                    currentClick()
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
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            val change = event.changes.firstOrNull { it.id == id } ?: break
                            drift += change.positionChange()
                            if (change.isConsumed || drift.getDistance() > viewConfiguration.touchSlop ||
                                event.changes.any { it.id != id && it.pressed }) break
                            if (!change.pressed) {
                                val elapsed = change.uptimeMillis - down.uptimeMillis
                                if (elapsed >= dwell.millis) {
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
                        dwellPressed = false
                    }
                }
            }
    }
}

// ---------------------------------------------------------------------------
// Item imagery (photo via Coil, emoji, or first-letter fallback)
// ---------------------------------------------------------------------------

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
        val resId = drawableResourceId(wordDrawableName(item))
        val performance = LocalPerformanceMode.current
        val density = LocalDensity.current
        val glyphSize = with(density) { (side * glyphScale).toSp() }
        when {
            photo != null -> {
                val context = LocalContext.current
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
            resId != null -> Image(
                painter = painterResource(resId),
                contentDescription = item.displayText,
                contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
            )
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

// ---------------------------------------------------------------------------
// Breathing voice aura
// ---------------------------------------------------------------------------

/**
 * Soft azure radial wash that breathes while a word/phrase is being spoken.
 * In performance mode it renders as a static wash (no infinite animation).
 */
@Composable
fun VoiceAuraHalo(modifier: Modifier = Modifier) {
    val performance = LocalPerformanceMode.current
    val brush = Brush.radialGradient(
        colors = listOf(
            VoiceAura.copy(alpha = 0.42f),
            VoiceAura.copy(alpha = 0.16f),
            Color.Transparent,
        ),
    )
    if (performance) {
        Box(modifier.background(brush))
    } else {
        val transition = rememberInfiniteTransition(label = "voiceAura")
        val pulse by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "voiceAuraAlpha",
        )
        val breathe by transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "voiceAuraScale",
        )
        Box(
            modifier
                .graphicsLayer {
                    scaleX = breathe
                    scaleY = breathe
                    alpha = pulse
                }
                .background(brush),
        )
    }
}

// ---------------------------------------------------------------------------
// Image stage: large visual, soft disc for emoji, aura when playing
// ---------------------------------------------------------------------------

@Composable
fun CardImageContent(
    item: WordItem,
    accent: Color,
    isCurrentlyPlaying: Boolean,
    modifier: Modifier = Modifier,
    crop: Boolean = false,
) {
    val dark = LocalIsDark.current
    val resId = drawableResourceId(wordDrawableName(item))
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        if (item.photoPath != null || resId != null) {
            val photoShape = RoundedCornerShape(16.dp)
            if (isCurrentlyPlaying) {
                VoiceAuraHalo(Modifier.matchParentSize())
            }
            ItemImage(
                item = item,
                accent = accent,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isCurrentlyPlaying) {
                            Modifier.border(2.dp, VoiceAura.copy(alpha = 0.85f), photoShape)
                        } else {
                            Modifier
                        },
                    ),
                crop = crop,
                glyphScale = 0.45f,
            )
        } else {
            // Disc is never smaller than 84dp so the glyph stays >= 52sp.
            val circle = minOf(maxWidth, maxHeight, 132.dp).coerceAtLeast(84.dp)
            val surface = if (dark) CardSurfaceDark else CardSurfaceLight
            val disc = accent.copy(alpha = if (dark) 0.16f else 0.08f).compositeOver(surface)
            if (isCurrentlyPlaying) {
                VoiceAuraHalo(Modifier.requiredSize(circle * 1.5f))
            }
            Box(
                modifier = Modifier
                    .requiredSize(circle)
                    .background(disc, CircleShape)
                    .then(
                        if (isCurrentlyPlaying) {
                            Modifier.border(2.dp, VoiceAura.copy(alpha = 0.85f), CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ItemImage(
                    item = item,
                    accent = if (dark) Color(0xFFCBD5E1) else Color(0xFF64748B),
                    modifier = Modifier.fillMaxSize(),
                    glyphScale = 0.66f,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ceramic card container
// ---------------------------------------------------------------------------

/**
 * Crisp porcelain / titanium surface with a 1dp translucent micro-border
 * instead of a muddy drop shadow. [isActive] tints the border with the
 * category accent; [isSpeaking] lights it with the azure voice aura.
 */
@Composable
fun CeramicCard(
    accent: Color,
    isActive: Boolean = false,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dark = LocalIsDark.current
    val resting = cardBorder()
    val border = when {
        isSpeaking -> BorderStroke(2.dp, VoiceAura.copy(alpha = 0.85f))
        isActive -> BorderStroke(2.dp, accent.copy(alpha = if (dark) 0.75f else 0.60f))
        else -> resting
    }
    Surface(
        modifier = modifier,
        shape = CardShape,
        color = if (dark) CardSurfaceDark else CardSurfaceLight,
        contentColor = if (dark) InkOnDark else Ink,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
    ) {
        Box {
            if (isSpeaking) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(VoiceAura.copy(alpha = if (dark) 0.10f else 0.07f)),
                )
            }
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Typography helper for Arabic labels (diacritics-safe)
// ---------------------------------------------------------------------------

@Composable
fun CardLabel(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    maxLines: Int = 2,
) {
    val dark = LocalIsDark.current
    Text(
        text = text,
        modifier = modifier.padding(vertical = 4.dp),
        color = if (dark) InkOnDark else Ink,
        fontSize = fontSize,
        lineHeight = fontSize * 1.5f,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Whisper-soft, non-interactive corner cue (alpha 0.35) for hint availability. */
@Composable
fun WhisperCue(icon: ImageVector, modifier: Modifier = Modifier) {
    val dark = LocalIsDark.current
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = (if (dark) InkOnDark else Ink).copy(alpha = 0.35f),
        modifier = modifier.size(14.dp),
    )
}

// ---------------------------------------------------------------------------
// Staggered entrance
// ---------------------------------------------------------------------------

/**
 * Slide-up + fade entrance with a 35ms delay per row (capped so rows that
 * scroll into view late never wait long). Plays once per item, and is skipped
 * entirely in performance mode.
 */
@Composable
fun StaggeredCascade(
    row: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val performance = LocalPerformanceMode.current
    var played by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (played || performance) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!played && !performance) {
            delay(CascadeStepMillis * row.coerceIn(0, CascadeMaxRows))
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            )
        }
        played = true
    }
    Box(
        modifier.graphicsLayer {
            val p = progress.value
            alpha = p
            translationY = (1f - p) * 24.dp.toPx()
        },
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Small action buttons
// ---------------------------------------------------------------------------

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
