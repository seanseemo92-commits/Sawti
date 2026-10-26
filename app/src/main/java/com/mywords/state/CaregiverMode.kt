package com.mywords.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide caregiver switch shared by the Dashboard and Family screens.
 * Deliberately in-memory only: the app always starts in patient mode.
 */
class CaregiverMode {
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()
    fun toggle() { _enabled.value = !_enabled.value }
}
