package com.sawti.presentation.navigation

import android.widget.Toast
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
import com.sawti.presentation.onboarding.spotlightAnchor
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.HeaderColors
import com.sawti.presentation.theme.headerColors
import com.sawti.presentation.theme.headerContentColor
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch

private const val HOLD_MS = 3000

/** Brand-adaptive app bar, with high-contrast text and controls in both modes. */
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean,
    caregiverMode: Boolean,
    onBack: () -> Unit,
    onPractice: () -> Unit,
    onDialects: () -> Unit,
    onToggleCaregiver: () -> Unit,
    sectionColor: Color? = null,
) {
    val dark = LocalIsDark.current
    val target = headerColors(sectionColor, dark)
    val duration = if (LocalPerformanceMode.current) 0 else 360
    val start by animateColorAsState(target.start, tween(duration, easing = FastOutSlowInEasing), label = "headerStart")
    val middle by animateColorAsState(target.middle, tween(duration, easing = FastOutSlowInEasing), label = "headerMiddle")
    val end by animateColorAsState(target.end, tween(duration, easing = FastOutSlowInEasing), label = "headerEnd")
    val ink = headerContentColor(HeaderColors(start, middle, end))
    val window = LocalContext.current.activityOrNull()?.window
    SideEffect {
        window?.let {
            WindowInsetsControllerCompat(it, it.decorView).isAppearanceLightStatusBars = ink == Color.Black
        }
    }
    val headerGradient = Brush.horizontalGradient(listOf(start, middle, end))
    Surface(
        color = Color.Transparent,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
    ) {
        Box(Modifier.background(headerGradient)) {
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
                        tint = ink,
                    )
                }
            }
            Text(
                text = title,
                color = ink,
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
                    tint = ink,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onDialects, modifier = Modifier.spotlightAnchor("top_language")) {
                Icon(Icons.Filled.Language, contentDescription = "اختيار اللهجة والصوت", tint = ink)
            }
            SettingsGear(caregiverMode, onToggleCaregiver, ink)
        }
        }
    }
}

/**
 * Gear that only reacts to a 3-second hold (a ring fills while holding), so it can't be
 * triggered by accident. A normal tap just shows a hint. Holding again leaves Caregiver Mode.
 */
@Composable
private fun SettingsGear(caregiverMode: Boolean, onToggle: () -> Unit, ink: Color) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val currentToggle by rememberUpdatedState(onToggle)
    val description = stringResource(R.string.settings_gear_desc)

    Box(
        Modifier
            .size(48.dp)
            .spotlightAnchor("top_caregiver")
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
                color = ink,
                trackColor = ink.copy(alpha = 0.25f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp),
            )
        }
        Icon(
            if (caregiverMode) Icons.Filled.LockOpen else Icons.Filled.Settings,
            contentDescription = null,
            tint = if (caregiverMode && ink == Color.White) Color(0xFFFFE082) else ink,
            modifier = Modifier.size(24.dp),
        )
    }
}

private tailrec fun Context.activityOrNull(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.activityOrNull()
    else -> null
}
