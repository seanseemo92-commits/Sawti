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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.sawti.domain.model.SentencePiece
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.cardElevation
import java.io.File

/** A softly floating AAC composition dock. Mutations and spoken text are unchanged. */
@Composable
fun SentenceStrip(
    pieces: List<SentencePiece>,
    speakingPieceIndex: Int?,
    onPlay: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    val dark = LocalIsDark.current
    val performance = LocalPerformanceMode.current
    val dock = if (dark) Color(0xFF161D2A) else Color.White
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = dock,
        shadowElevation = cardElevation(9.dp),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = if (dark) 0.19f else 0.93f),
                    Color(0xFF38BDF8).copy(alpha = 0.13f),
                    Color(0xFF64748B).copy(alpha = 0.16f))
            )
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Column(
            Modifier.then(if (performance) Modifier else Modifier.animateContentSize())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("جملتي", color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onBackspace, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.Backspace, contentDescription = "حذف آخر كلمة")
                }
                IconButton(onClick = onClear, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "مسح الجملة بالكامل")
                }
                Button(
                    onClick = onPlay,
                    enabled = pieces.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0369A1), Color(0xFF38BDF8))
                            )
                        ),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("انطق", fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pieces.isEmpty()) {
                    Text("المس الكلمات لبناء جملتك",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                }
                pieces.forEachIndexed { index, piece ->
                    key(index, piece.sourceId) {
                        var visible by remember(index, piece.sourceId) { mutableStateOf(false) }
                        LaunchedEffect(index, piece.sourceId) { visible = true }
                        @Composable fun Chip() {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.57f),
                                border = BorderStroke(
                                    1.dp,
                                    if (speakingPieceIndex == index) Color(0xFF38BDF8).copy(alpha = 0.70f)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f),
                                ),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                        VoiceAura(active = speakingPieceIndex == index)
                                        if (piece.photoPath != null) {
                                            AsyncImage(
                                                model = File(piece.photoPath),
                                                contentDescription = null,
                                                modifier = Modifier.size(27.dp).clip(RoundedCornerShape(7.dp)),
                                                contentScale = ContentScale.Crop,
                                            )
                                        } else {
                                            Text(piece.emoji.ifBlank { piece.text.take(1) })
                                        }
                                    }
                                    Text(piece.text, maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        if (performance) Chip()
                        else AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                scaleIn(initialScale = 0.86f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow,
                                )),
                        ) { Chip() }
                    }
                }
            }
        }
    }
}
