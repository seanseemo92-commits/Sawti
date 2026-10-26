package com.sawti.presentation.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sawti.SawtiApp
import com.sawti.data.SettingsRepository
import com.sawti.domain.model.AppSettings
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.SpeechRate
import com.sawti.domain.model.TextSize
import com.sawti.domain.model.ThemeMode
import com.sawti.domain.model.TouchDwell
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.state

    fun completeOnboarding() = repository.completeOnboarding()
    fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)
    fun setTextSize(size: TextSize) = repository.setTextSize(size)
    fun setPerformanceMode(on: Boolean) = repository.setPerformanceMode(on)
    fun setTouchDwell(value: TouchDwell) = repository.setTouchDwell(value)
    fun setSentenceStripEnabled(enabled: Boolean) = repository.setSentenceStripEnabled(enabled)
    fun setSpeechRate(rate: SpeechRate) = repository.setSpeechRate(rate)
    fun setDialect(dialect: ArabicDialect) = repository.setDialect(dialect)
    fun setPreferredVoice(name: String?) = repository.setPreferredVoice(name)
    fun setHapticsEnabled(on: Boolean) = repository.setHapticsEnabled(on)

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel((app as SawtiApp).container.settingsRepository) as T
    }
}
