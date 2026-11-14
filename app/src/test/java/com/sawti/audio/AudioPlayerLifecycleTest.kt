package com.sawti.audio

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Looper
import com.sawti.domain.model.WordItem
import com.sawti.speech.SpeechManager
import java.io.File
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AudioPlayerLifecycleTest {
    private val created = mutableListOf<ShadowMediaPlayer>()
    private lateinit var app: Application
    private lateinit var audio: AudioPlayer
    private lateinit var focus: AudioFocusController
    private lateinit var file: File

    @Before fun setup() {
        app = RuntimeEnvironment.getApplication()
        focus = AudioFocusController(app)
        audio = AudioPlayer(focus)
        file = File(app.filesDir, "clip.m4a").apply { writeText("test recording") }
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(file.path), ShadowMediaPlayer.MediaInfo(10_000, -1))
        ShadowMediaPlayer.setCreateListener { _, shadow -> created.add(shadow) }
    }

    @After fun close() {
        audio.stop()
        ShadowMediaPlayer.setCreateListener(null)
        file.delete()
    }

    @Test fun permanentFocusLossClearsTheCardPlaybackIndicator() {
        val dispatcher = AudioDispatcher(SpeechManager(app, focus), audio)
        val card = WordItem(1, "transport", "", "سيارة", "سَيَّارَة", "سَـ...", null,
            file.path, null, "", "", semanticKey = "transport.car")
        dispatcher.play(card)
        assertTrue(dispatcher.playback.value.isSpeaking)
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        shadowOf(manager).lastAudioFocusRequest.listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(dispatcher.playback.value.isSpeaking)
        assertEquals(ShadowMediaPlayer.State.END, created.single().state)
    }

    @Test fun notificationsKeepPersonalRecordingsPlayingAndPhoneCallsStillPause() {
        assertTrue(audio.play(file.path))
        val current = created.single()
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val request = shadowOf(manager).lastAudioFocusRequest
        val listener = request.listener
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(ShadowMediaPlayer.State.PAUSED, current.state)
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        audio.stop()
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(ShadowMediaPlayer.State.END, current.state)
    }

    @Test fun cancellationAndCompletionEachFireOnceAndStaleCallbacksAreIgnored() {
        var completed = 0
        var canceled = 0
        assertTrue(audio.play(file.path, onDone = { completed++ }, onCanceled = { canceled++ }))
        val old = created.single()
        audio.stop()
        audio.stop()
        assertEquals(1, canceled)
        assertEquals(0, completed)
        assertTrue(audio.play(file.path, onDone = { completed++ }, onCanceled = { canceled++ }))
        // MediaInfo(10_000, -1) deliberately disables automatic completion of
        // prepareAsync in Robolectric: explicitly prepare the new recording.
        // This verifies the new player starts while the old completion is stale.
        created.last().invokePreparedListener()
        old.invokeCompletionListener()
        assertEquals(0, completed)
        assertEquals(ShadowMediaPlayer.State.STARTED, created.last().state)
        created.last().invokeCompletionListener()
        audio.stop()
        assertEquals(1, completed)
        assertEquals(1, canceled)
    }
}
