package com.sawti.data

import android.content.Context
import com.sawti.domain.model.AppSettings
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.SpeechRate
import com.sawti.domain.model.TextSize
import com.sawti.domain.model.ThemeMode
import com.sawti.domain.model.TouchDwell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Small SharedPreferences-backed store for display settings. */
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Old four-step onboarding is retired. Mark existing installs completed without
    // touching dialect choices, caregiver media, or the database.
    init {
        if (!prefs.getBoolean("onboarding_completed", false)) {
            prefs.edit().putBoolean("onboarding_completed", true).apply()
        }
    }

    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    private inline fun <reified E : Enum<E>> read(key: String, default: E): E =
        runCatching { enumValueOf<E>(prefs.getString(key, null) ?: "") }.getOrDefault(default)

    private fun load() = AppSettings(
        themeMode = read("theme_mode", ThemeMode.SYSTEM),
        textSize = read("text_size", TextSize.NORMAL),
        speechRate = read("speech_rate", SpeechRate.NORMAL),
        dialect = read("dialect", ArabicDialect.STANDARD),
        preferredVoice = prefs.getString("preferred_voice", null),
        hapticsEnabled = prefs.getBoolean("haptics", true),
        performanceMode = prefs.getBoolean("performance_mode", false),
        touchDwell = read("touch_dwell", TouchDwell.OFF),
        sentenceStripEnabled = prefs.getBoolean("sentence_strip", false),
        isOnboardingCompleted = true,
    )

    fun completeOnboarding() {
        // Persist before notifying the UI; interrupted sessions will show it again safely.
        if (prefs.edit().putBoolean("onboarding_completed", true).commit()) {
            _state.update { it.copy(isOnboardingCompleted = true) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _state.update { it.copy(themeMode = mode) }
    }

    fun setTextSize(size: TextSize) {
        prefs.edit().putString("text_size", size.name).apply()
        _state.update { it.copy(textSize = size) }
    }

    fun setPerformanceMode(on: Boolean) {
        prefs.edit().putBoolean("performance_mode", on).apply()
        _state.update { it.copy(performanceMode = on) }
    }

    fun setSentenceStripEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sentence_strip", enabled).apply()
        _state.update { it.copy(sentenceStripEnabled = enabled) }
    }

    fun setTouchDwell(value: TouchDwell) {
        prefs.edit().putString("touch_dwell", value.name).apply()
        _state.update { it.copy(touchDwell = value) }
    }

    fun setSpeechRate(rate: SpeechRate) {
        prefs.edit().putString("speech_rate", rate.name).apply()
        _state.update { it.copy(speechRate = rate) }
    }

    fun setDialect(dialect: ArabicDialect) {
        prefs.edit().putString("dialect", dialect.name).remove("preferred_voice").apply()
        _state.update { it.copy(dialect = dialect, preferredVoice = null) }
    }

    fun setPreferredVoice(name: String?) {
        prefs.edit().apply {
            if (name == null) remove("preferred_voice") else putString("preferred_voice", name)
        }.apply()
        _state.update { it.copy(preferredVoice = name) }
    }

    fun setHapticsEnabled(on: Boolean) {
        prefs.edit().putBoolean("haptics", on).apply()
        _state.update { it.copy(hapticsEnabled = on) }
    }
}
