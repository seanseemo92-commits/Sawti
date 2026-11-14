package com.sawti.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sawti.domain.model.SentencePiece
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import java.io.File

internal val SentencePlayBlue = Color(0xFF0369A1)

/**
 * Clean island dock: no Material shadow and no loud coloured container.
 * Sentence composition and TTS sequencing remain in the original ViewModel.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun SentenceStrip(
    pieces: List<SentencePiece>,
    speakingPieceIndex: Int?,
    onPlay: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    val dark = LocalIsDark.current
    val performance = LocalPerformanceMode.current
    val stripScroll = rememberScrollState()
    LaunchedEffect(pieces.size) {
        // Horizontal scrolling follows the newest word, without fighting
        // the user when the active/speaking word changes.
        if (pieces.isNotEmpty()) {
            // Wait until the new chip has participated in layout.
            withFrameNanos { }
            stripScroll.animateScrollTo(stripScroll.maxValue)
        }
    }
    val foreground = MaterialTheme.colorScheme.onSurface
    val surface = if (dark) Color(0xFF131924) else Color.White
    val hairline = if (dark) Color.White.copy(alpha = 0.08f)
        else Color.Black.copy(alpha = 0.05f)
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, hairline),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(
            Modifier.then(if (performance) Modifier else Modifier.animateContentSize())
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("جملتي", modifier = Modifier.align(Alignment.CenterVertically), color = foreground,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onBackspace, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "حذف آخر كلمة",
                        modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onClear, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "مسح الجملة بالكامل",
                        modifier = Modifier.size(20.dp))
                }
                Button(
                    onClick = onPlay,
                    enabled = pieces.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SentencePlayBlue,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null,
                        modifier = Modifier.size(20.dp))
                    Text("انطق", fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(stripScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pieces.isEmpty()) {
                    Text("المس الكلمات لبناء جملتك",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                pieces.forEachIndexed { index, piece ->
                    key(index, piece.sourceId) {
                        var visible by remember(index, piece.sourceId) { mutableStateOf(false) }
                        LaunchedEffect(index, piece.sourceId) { visible = true }
                        @Composable fun Chip() {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (dark) Color(0xFF202A36) else Color(0xFFF8FAFC),
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp,
                                    if (speakingPieceIndex == index) Color(0xFF38BDF8).copy(alpha = 0.82f)
                                    else hairline),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        Modifier.size(30.dp).background(
                                            if (dark) Color(0xFF2A3442) else Color(0xFFEFF3F6),
                                            CircleShape,
                                        ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (piece.photoPath != null) {
                                            AsyncImage(
                                                model = File(piece.photoPath),
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp).clip(CircleShape),
                                                contentScale = ContentScale.Crop,
                                            )
                                        } else Text(piece.emoji.ifBlank { piece.text.take(1) },
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(piece.text, maxLines = 1, color = foreground,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        if (performance) Chip()
                        else AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.92f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )),
                        ) { Chip() }
                    }
                }
            }
        }
    }
}
