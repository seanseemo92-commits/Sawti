package com.sawti.presentation.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sawti.domain.model.SentencePiece
import java.io.File

/** AAC sentence composition: short chips, one continuous utterance, and explicit undo/clear. */
@Composable
fun SentenceStrip(
    pieces: List<SentencePiece>,
    onPlay: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("جملتي", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onBackspace, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.Backspace, contentDescription = "حذف آخر كلمة")
                }
                IconButton(onClick = onClear, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "مسح الجملة")
                }
                Button(onClick = onPlay, enabled = pieces.isNotEmpty()) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("انطق")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pieces.isEmpty()) Text("المس الكلمات لتكوين جملة", style = MaterialTheme.typography.bodyMedium)
                pieces.forEach { piece ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            if (piece.photoPath != null) {
                                AsyncImage(
                                    model = File(piece.photoPath),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else Text(piece.emoji.ifBlank { piece.text.take(1) })
                            Text(piece.text, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
