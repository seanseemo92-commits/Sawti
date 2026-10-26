package com.mywords.presentation.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import com.mywords.presentation.theme.cardElevation
import com.mywords.R
import com.mywords.domain.model.ProgressSummary
import com.mywords.presentation.theme.AppBarBlue

private val Green = Color(0xFF2E7D32)
private val Orange = Color(0xFFE65100)
private val Slate = Color(0xFF546E7A)

@Composable
fun ProgressRoute(viewModel: ProgressViewModel) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    ProgressScreen(summary, viewModel::clear)
}

/** Private summary for the caregiver (only reachable while Caregiver Mode is unlocked). */
@Composable
fun ProgressScreen(summary: ProgressSummary, onClear: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    val hasData = summary.recallAttempts > 0 || summary.choiceAttempts > 0

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.progress_private), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        if (!hasData) {
            Text(stringResource(R.string.progress_empty), fontSize = 18.sp, modifier = Modifier.padding(vertical = 24.dp))
        } else {
            SectionTitle(stringResource(R.string.progress_naming_title))
            StatCard(stringResource(R.string.progress_attempts), summary.recallAttempts.toString(), AppBarBlue)
            val selfPercent = if (summary.recallAttempts > 0) summary.selfRecalled * 100 / summary.recallAttempts else 0
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    stringResource(R.string.progress_self),
                    "${summary.selfRecalled}  (${stringResource(R.string.progress_percent, selfPercent)})",
                    Green,
                    Modifier.weight(1f),
                )
                StatCard(stringResource(R.string.progress_cued), summary.cued.toString(), Orange, Modifier.weight(1f))
            }
            StatCard(stringResource(R.string.progress_shown), summary.shown.toString(), Slate)

            if (summary.cuedWords.isNotEmpty()) {
                SectionTitle(stringResource(R.string.progress_cued_words))
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = cardElevation(2.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.cuedWords.forEach { w ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(w.word, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("× ${w.count}", fontSize = 16.sp, color = Orange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (summary.choiceAttempts > 0) {
                SectionTitle(stringResource(R.string.progress_choice_title))
                StatCard(
                    stringResource(R.string.progress_choice_correct, summary.choiceCorrect, summary.choiceAttempts),
                    stringResource(R.string.progress_percent, summary.choiceCorrect * 100 / summary.choiceAttempts),
                    Green,
                )
            }

            OutlinedButton(
                onClick = { confirmReset = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) { Text(stringResource(R.string.progress_reset), fontSize = 16.sp) }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            text = { Text(stringResource(R.string.progress_reset_confirm), fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = { onClear(); confirmReset = false }) { Text(stringResource(R.string.progress_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AppBarBlue, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = cardElevation(2.dp), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
        }
    }
}
