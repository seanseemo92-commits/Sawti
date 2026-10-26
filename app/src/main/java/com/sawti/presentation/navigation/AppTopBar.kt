package com.sawti.presentation.navigation

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.presentation.theme.AppBarBlue
import kotlinx.coroutines.launch

private const val HOLD_MS = 3000

/** Blue app bar: bold white title, practice icon and the discreet settings gear. */
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean,
    caregiverMode: Boolean,
    onBack: () -> Unit,
    onPractice: () -> Unit,
    onDialects: () -> Unit,
    onToggleCaregiver: () -> Unit,
) {
    Surface(
        color = AppBarBlue,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
    ) {
        Row(
            Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                    )
                }
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            IconButton(onClick = onPractice) {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = stringResource(R.string.practice_icon_desc),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onDialects) {
                Icon(Icons.Filled.Language, contentDescription = "اختيار اللهجة والصوت", tint = Color.White)
            }
            SettingsGear(caregiverMode, onToggleCaregiver)
        }
    }
}

/**
 * Gear that only reacts to a 3-second hold (a ring fills while holding), so it can't be
 * triggered by accident. A normal tap just shows a hint. Holding again leaves Caregiver Mode.
 */
@Composable
private fun SettingsGear(caregiverMode: Boolean, onToggle: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val currentToggle by rememberUpdatedState(onToggle)
    val description = stringResource(R.string.settings_gear_desc)

    Box(
        Modifier
            .size(48.dp)
            .semantics {
                contentDescription = description
                onLongClick(label = description) {
                    currentToggle()
                    true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        Toast.makeText(context, R.string.settings_hint, Toast.LENGTH_SHORT).show()
                    },
                    onPress = {
                        val hold = scope.launch {
                            progress.animateTo(1f, tween(HOLD_MS, easing = LinearEasing))
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentToggle()
                        }
                        tryAwaitRelease()
                        hold.cancel()
                        scope.launch { progress.snapTo(0f) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (progress.value > 0f) {
            CircularProgressIndicator(
                progress = { progress.value },
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp),
            )
        }
        Icon(
            if (caregiverMode) Icons.Filled.LockOpen else Icons.Filled.Settings,
            contentDescription = null,
            tint = if (caregiverMode) Color(0xFFFFE082) else Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp),
        )
    }
}
