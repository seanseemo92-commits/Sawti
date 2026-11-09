package com.sawti.presentation.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

enum class RecitationPhase { IDLE, BUFFERING, PLAYING, ERROR }

data class RecitationStatus(
    val globalVerseNumber: Int? = null,
    val phase: RecitationPhase = RecitationPhase.IDLE,
    val remaining: Int = 0,
)

/**
 * Ayah playback uses the persistently downloaded local MP3 rather than
 * a streaming URL. The first play saves the ayah before playing it; repeats
 * and subsequent offline plays never require network access.
 */
class QuranAyahPlayer(
    context: Context,
    private val cache: QuranAudioCache,
    private val update: (RecitationStatus) -> Unit,
) {
    private val audioManager = context.applicationContext
        .getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS ||
            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
        ) stop()
    }
    private var player: MediaPlayer? = null
    private var downloadJob: Job? = null
    private var focusHeld = false
    private var verseNumber: Int? = null
    private var playsLeft = 0
    private var released = false

    fun play(verse: QuranVerse, repeatCount: Int) {
        val globalVerseNumber = verse.globalNumber
        require(repeatCount in setOf(1, 3, 5))
        if (released) return
        stop()
        verseNumber = globalVerseNumber
        playsLeft = repeatCount
        update(RecitationStatus(globalVerseNumber, RecitationPhase.BUFFERING, repeatCount))
        downloadJob = scope.launch {
            try {
                val file = cache.obtain(globalVerseNumber, verse.surahNumber, verse.verseNumber)
                ensureActive()
                if (!released && verseNumber == globalVerseNumber) {
                    prepareLocalPlayer(globalVerseNumber, file)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (!released && verseNumber == globalVerseNumber) fail(globalVerseNumber)
            }
        }
    }

    private fun prepareLocalPlayer(number: Int, file: File) {
        val candidate = MediaPlayer()
        player = candidate
        try {
            candidate.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            candidate.setDataSource(file.absolutePath)
            candidate.setOnPreparedListener { mp ->
                if (mp !== player || released || verseNumber != number) return@setOnPreparedListener
                try {
                    if (audioManager.requestAudioFocus(
                            focusListener, AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                        ) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                    ) {
                        fail(number)
                        return@setOnPreparedListener
                    }
                    focusHeld = true
                    mp.start()
                    update(RecitationStatus(number, RecitationPhase.PLAYING, playsLeft))
                } catch (_: Exception) {
                    fail(number)
                }
            }
            candidate.setOnCompletionListener { mp ->
                if (mp !== player) return@setOnCompletionListener
                playsLeft -= 1
                if (playsLeft > 0) {
                    try {
                        mp.seekTo(0)
                        mp.start()
                        update(RecitationStatus(number, RecitationPhase.PLAYING, playsLeft))
                    } catch (_: Exception) {
                        fail(number)
                    }
                } else {
                    stop()
                }
            }
            candidate.setOnErrorListener { mp, _, _ ->
                if (mp === player) fail(number)
                true
            }
            candidate.prepareAsync()
        } catch (_: Exception) {
            fail(number)
        }
    }

    private fun fail(number: Int) {
        stop()
        if (!released) update(RecitationStatus(number, RecitationPhase.ERROR))
    }

    fun stop() {
        downloadJob?.cancel()
        downloadJob = null
        val active = player
        player = null
        verseNumber = null
        playsLeft = 0
        if (active != null) {
            try {
                active.reset()
            } catch (_: Exception) {
                // The async player might already have entered its error state.
            }
            active.release()
        }
        if (focusHeld) {
            focusHeld = false
            audioManager.abandonAudioFocus(focusListener)
        }
        update(RecitationStatus())
    }

    fun release() {
        if (released) return
        released = true
        stop()
        scope.cancel()
    }
}
