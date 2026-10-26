package com.mywords.presentation.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mywords.MyWordsApp
import com.mywords.data.SettingsRepository
import com.mywords.domain.model.AppSettings
import com.mywords.domain.model.ArabicDialect
import com.mywords.domain.model.SpeechRate
import com.mywords.domain.model.TextSize
import com.mywords.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.state

    fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)
    fun setTextSize(size: TextSize) = repository.setTextSize(size)
    fun setPerformanceMode(on: Boolean) = repository.setPerformanceMode(on)
    fun setSpeechRate(rate: SpeechRate) = repository.setSpeechRate(rate)
    fun setDialect(dialect: ArabicDialect) = repository.setDialect(dialect)
    fun setPreferredVoice(name: String?) = repository.setPreferredVoice(name)
    fun setHapticsEnabled(on: Boolean) = repository.setHapticsEnabled(on)

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel((app as MyWordsApp).container.settingsRepository) as T
    }
}
