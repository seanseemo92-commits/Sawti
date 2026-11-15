package com.sawti

import android.app.Application
import com.sawti.audio.AudioFocusController
import com.sawti.audio.AudioPlayer
import com.sawti.audio.SoftChime
import com.sawti.audio.VoiceRecorder
import com.sawti.audio.AudioDispatcher
import com.sawti.audio.NeuralAudioResolver
import com.sawti.data.BackupManager
import com.sawti.data.BackupOperations
import com.sawti.data.RoomProgressRepository
import com.sawti.data.SettingsRepository
import com.sawti.data.RoomWordRepository
import com.sawti.data.db.AppDatabase
import com.sawti.domain.repository.ProgressRepository
import com.sawti.domain.repository.WordRepository
import com.sawti.speech.SpeechManager
import com.sawti.state.CaregiverMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppContainer(private val app: Application) {
    val database by lazy { AppDatabase.build(app) }
    val backupOperations by lazy { BackupOperations(app, database) }
    val audioFocus by lazy { AudioFocusController(app) }
    val speech by lazy { SpeechManager(app, audioFocus) }
    val player by lazy { AudioPlayer(audioFocus) }
    val recorder by lazy { VoiceRecorder(app, audioFocus) }
    val neuralAudioResolver by lazy { NeuralAudioResolver(app) }
    val audioDispatcher by lazy { AudioDispatcher(speech, player, neuralAudioResolver) }
    val caregiver = CaregiverMode()
    val wordRepository: WordRepository by lazy { RoomWordRepository(database, app, settingsRepository) }
    val progressRepository: ProgressRepository by lazy { RoomProgressRepository(database) }
    val chime by lazy { SoftChime() }
    val settingsRepository by lazy { SettingsRepository(app) }
}

enum class StartupStatus { RECOVERING, READY, FAILED }

class SawtiApp : Application() {
    val startup = kotlinx.coroutines.flow.MutableStateFlow(StartupStatus.RECOVERING)
    val container by lazy { AppContainer(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        if (!BackupManager.hasPendingRecovery(this)) {
            startup.value = StartupStatus.READY
            observeSettings()
        } else retryRecovery()
    }

    private var recoveryJob: kotlinx.coroutines.Job? = null

    @Synchronized
    fun retryRecovery() {
        if (recoveryJob?.isActive == true) return
        startup.value = StartupStatus.RECOVERING
        recoveryJob = appScope.launch {
            try {
                BackupManager.recoverIfInterrupted(this@SawtiApp)
                startup.value = StartupStatus.READY
                observeSettings()
            } catch (error: Exception) {
                android.util.Log.e("SawtiRecovery", "Startup recovery failed", error)
                startup.value = StartupStatus.FAILED
            }
        }
    }

    private fun observeSettings() {
        appScope.launch {
            container.settingsRepository.state.collect { settings ->
                withContext(Dispatchers.Main.immediate) {
                    container.speech.setSpeechRate(settings.speechRate.factor)
                    container.speech.setDialect(settings.dialect, settings.preferredVoice)
                }
            }
        }
    }
}

