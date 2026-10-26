package com.mywords

import android.app.Application
import com.mywords.audio.AudioPlayer
import com.mywords.audio.SoftChime
import com.mywords.audio.VoiceRecorder
import com.mywords.audio.AudioDispatcher
import com.mywords.data.BackupManager
import com.mywords.data.RoomProgressRepository
import com.mywords.data.SettingsRepository
import com.mywords.data.RoomWordRepository
import com.mywords.data.db.AppDatabase
import com.mywords.domain.repository.ProgressRepository
import com.mywords.domain.repository.WordRepository
import com.mywords.speech.SpeechManager
import com.mywords.state.CaregiverMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Hand-rolled DI: one shared instance of everything the ViewModels need. Swap for Hilt if you prefer. */
class AppContainer(private val app: Application) {
    val database by lazy { AppDatabase.build(app) }
    val speech by lazy { SpeechManager(app) }
    val player by lazy { AudioPlayer() }
    val recorder by lazy { VoiceRecorder(app) }
    val audioDispatcher by lazy { AudioDispatcher(speech, player) }
    val caregiver = CaregiverMode()
    val wordRepository: WordRepository by lazy { RoomWordRepository(database, app, settingsRepository) }
    val progressRepository: ProgressRepository by lazy { RoomProgressRepository(database.progressDao()) }
    val chime by lazy { SoftChime() }
    val settingsRepository by lazy { SettingsRepository(app) }
}

class MyWordsApp : Application() {
    val container by lazy { AppContainer(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Restore interrupted file swaps before Room or preferences are opened.
        BackupManager.recoverIfInterrupted(this)
        // On startup add public relationship labels and built-in vocabulary, never sample people.
        appScope.launch { container.wordRepository.seedOrUpdateDefaults() }
        // Keep voice, region and rate reactive without recreating the TTS engine.
        appScope.launch {
            container.settingsRepository.state.collect { settings ->
                container.speech.setSpeechRate(settings.speechRate.factor)
                container.speech.setDialect(settings.dialect, settings.preferredVoice)
            }
        }
    }
}
