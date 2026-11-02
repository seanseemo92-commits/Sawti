package com.sawti

import android.app.Application
import com.sawti.audio.AudioFocusController
import com.sawti.audio.AudioPlayer
import com.sawti.audio.SoftChime
import com.sawti.audio.VoiceRecorder
import com.sawti.audio.AudioDispatcher
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
    val recorder by lazy { VoiceRecorder(app) }
    val audioDispatcher by lazy { AudioDispatcher(speech, player) }
    val caregiver = CaregiverMode()
    val wordRepository: WordRepository by lazy { RoomWordRepository(database, app, settingsRepository) }
    val progressRepository: ProgressRepository by lazy { RoomProgressRepository(database) }
    val chime by lazy { SoftChime() }
    val settingsRepository by lazy { SettingsRepository(app) }
}

class SawtiApp : Application() {
    val container by lazy { AppContainer(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        // Recovery must precede opening Room or preferences.
        BackupManager.recoverIfInterrupted(this)
        // LibraryViewModel owns vocabulary loading, failure reporting and retries.
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
