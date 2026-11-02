package com.sawti.presentation.practice

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import com.sawti.presentation.theme.cardElevation
import com.sawti.R
import com.sawti.presentation.common.ActionPill
import com.sawti.presentation.common.HintButton
import com.sawti.presentation.common.ItemImage
import com.sawti.presentation.common.PhraseHintButton
import com.sawti.presentation.theme.AppBarBlue
import com.sawti.presentation.theme.HintAmber
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.paletteOf

private val Correct = Color(0xFF2E7D32)

@Composable
fun PracticeRoute(viewModel: PracticeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PracticeScreen(state, viewModel)
}

@Composable
fun PracticeScreen(state: PracticeUiState, actions: PracticeActions) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ExerciseSwitch(state.exercise, actions::setExercise)
        when {
            !state.ready -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.total == 0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.practice_empty), fontSize = 20.sp, textAlign = TextAlign.Center)
            }
            state.finished -> FinishedContent(state, actions::restart)
            state.exercise == Exercise.RECALL -> RecallContent(state, actions)
            else -> ChoiceContent(state, actions)
        }
    }
}

@Composable
private fun ExerciseSwitch(selected: Exercise, onSelect: (Exercise) -> Unit) {
    val dark = LocalIsDark.current
    val selectedContainer = if (dark) MaterialTheme.colorScheme.primaryContainer else AppBarBlue
    val selectedContent = if (dark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(Exercise.RECALL to R.string.practice_mode_recall, Exercise.CHOICE to R.string.practice_mode_choice)
            .forEach { (exercise, label) ->
                val on = exercise == selected
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (on) selectedContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (on) selectedContent else MaterialTheme.colorScheme.onSurface,
                    shadowElevation = cardElevation(if (on) 4.dp else 1.dp),
                    modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(24.dp)).clickable { onSelect(exercise) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stringResource(label), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
    }
}

@Composable
private fun ProgressHeader(state: PracticeUiState, color: Color, track: Color) {
    LinearProgressIndicator(
        progress = { (state.index + 1f) / state.total }, color = color, trackColor = track,
        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
    )
    Text("${state.index + 1} / ${state.total}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun FinishedContent(state: PracticeUiState, onRestart: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🌟", fontSize = 96.sp)
        Text(stringResource(R.string.practice_done), fontSize = 38.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground)
        Text(stringResource(R.string.practice_score, state.score, state.total), fontSize = 22.sp,
            modifier = Modifier.padding(vertical = 16.dp), textAlign = TextAlign.Center)
        ActionPill(label = stringResource(R.string.practice_restart), icon = Icons.Filled.Refresh,
            color = AppBarBlue, onClick = onRestart)
    }
}

@Composable
private fun RecallContent(state: PracticeUiState, actions: PracticeActions) {
    val card = state.card ?: return
    val palette = paletteOf(card.palette)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProgressHeader(state, palette.banner, palette.pastel)
        Surface(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surface,
            shadowElevation = cardElevation(8.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ItemImage(
                    item = card.item, accent = palette.banner,
                    modifier = Modifier.weight(1f).fillMaxWidth().background(palette.pastel).padding(20.dp),
                    crop = card.item.photoPath != null, glyphScale = 0.6f,
                )
                Text(
                    text = if (state.revealed) card.item.displayText else stringResource(R.string.practice_prompt),
                    color = palette.accent, fontSize = if (state.revealed) 38.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (!state.revealed) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HintButton(onClick = actions::hint, size = 64)
                if (!card.item.sentencePrompt.isNullOrBlank()) PhraseHintButton(onClick = actions::sentenceHint, size = 64)
                ActionPill(label = stringResource(R.string.practice_reveal), icon = Icons.Filled.Visibility,
                    color = palette.banner, onClick = actions::reveal)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionPill(label = stringResource(if (state.hintUsed) R.string.practice_known_cued else R.string.practice_known),
                    icon = Icons.Filled.Check, color = palette.banner, onClick = actions::known)
                ActionPill(label = stringResource(R.string.practice_next), icon = Icons.AutoMirrored.Filled.ArrowForward,
                    color = HintAmber, onClick = actions::skip)
            }
        }
    }
}

@Composable
private fun ChoiceContent(state: PracticeUiState, actions: PracticeActions) {
    val card = state.card ?: return
    val palette = paletteOf(card.palette)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProgressHeader(state, palette.banner, palette.pastel)
        ActionPill(label = stringResource(R.string.choice_replay), icon = Icons.AutoMirrored.Filled.VolumeUp,
            color = AppBarBlue, onClick = actions::replayQuestion)
        Text(stringResource(R.string.choice_instruction), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Row(Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            state.choices.forEach { choice ->
                ChoiceCard(choice = choice, isTarget = choice.item.id == card.item.id, state = state,
                    onChoose = actions::choose, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun ChoiceCard(choice: PracticeCard, isTarget: Boolean, state: PracticeUiState,
    onChoose: (Long) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val palette = paletteOf(choice.palette)
    val wrong = choice.item.id in state.wrongIds
    val solvedHere = state.solved && isTarget
    val scale by animateFloatAsState(
        targetValue = if (solvedHere) 1.05f else 1f,
        animationSpec = if (LocalPerformanceMode.current) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "choice-scale",
    )
    Surface(
        shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(8.dp),
        border = BorderStroke(if (solvedHere) 7.dp else 4.dp, if (solvedHere) Correct else MaterialTheme.colorScheme.onSurface),
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (wrong) 0.35f else 1f).clip(RoundedCornerShape(28.dp))
            .clickable(enabled = !state.solved && !wrong) {
                if (!isTarget) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChoose(choice.item.id)
            },
    ) {
        Box(Modifier.fillMaxSize().background(palette.pastel), contentAlignment = Alignment.Center) {
            ItemImage(item = choice.item, accent = palette.banner, modifier = Modifier.fillMaxSize().padding(14.dp),
                crop = choice.item.photoPath != null, glyphScale = 0.7f)
            if (solvedHere) {
                Box(Modifier.align(Alignment.TopEnd).padding(12.dp).size(52.dp).background(Correct, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Text(choice.item.displayText, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Correct).padding(vertical = 8.dp, horizontal = 6.dp))
            }
        }
    }
}
