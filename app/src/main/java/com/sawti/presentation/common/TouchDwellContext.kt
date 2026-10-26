package com.sawti.presentation.common

import androidx.compose.runtime.compositionLocalOf
import com.sawti.domain.model.TouchDwell

/** Applies to communication cards, not ordinary navigation/settings controls. */
val LocalTouchDwell = compositionLocalOf { TouchDwell.OFF }
