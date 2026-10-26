@file:OptIn(ExperimentalFoundationApi::class)

package com.mywords.presentation.common

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.mywords.R
import com.mywords.domain.model.WordItem
import com.mywords.presentation.theme.AppBarBlue
import com.mywords.presentation.theme.HintAmber
import com.mywords.presentation.theme.LocalPerformanceMode
import com.mywords.presentation.theme.cardElevation
import com.mywords.presentation.theme.RecordRed
import java.io.File

/** Spring "bounce" on press + haptic tick; the card squeezes in and springs back. */
fun Modifier.bounceClick(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val performance = LocalPerformanceMode.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !performance) 0.93f else 1f,
        animationSpec = if (performance) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bounce",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.combinedClickable(
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
            onClick()
        },
    )
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

/** Small, subtle 💡 button: speaks only the phonemic cue. */
@Composable
fun HintButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 44) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
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
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
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
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
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
