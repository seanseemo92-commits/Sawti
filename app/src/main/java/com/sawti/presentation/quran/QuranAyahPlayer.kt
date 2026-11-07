package com.sawti.presentation.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer

enum class RecitationPhase { IDLE, BUFFERING, PLAYING, ERROR }

data class RecitationStatus(
    val globalVerseNumber: Int? = null,
    val phase: RecitationPhase = RecitationPhase.IDLE,
    val remaining: Int = 0,
)

/**
 * Single active ayah playback with explicit finite repeats.
 * Never uses TTS for Quran recitation. Releasing stops streaming on exit.
 */
class QuranAyahPlayer(
    context: Context,
    private val update: (RecitationStatus) -> Unit,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS ||
            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
        ) stop()
    }
    private var player: MediaPlayer? = null
    private var focusHeld = false
    private var verseNumber: Int? = null
    private var playsLeft = 0

    fun play(globalVerseNumber: Int, repeatCount: Int) {
        require(repeatCount in setOf(1, 3, 5))
        stop()
        if (audioManager.requestAudioFocus(
                focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        ) {
            update(RecitationStatus(globalVerseNumber, RecitationPhase.ERROR))
            return
        }
        focusHeld = true
        verseNumber = globalVerseNumber
        playsLeft = repeatCount
        val candidate = MediaPlayer()
        player = candidate
        update(RecitationStatus(globalVerseNumber, RecitationPhase.BUFFERING, repeatCount))
        try {
            candidate.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            candidate.setDataSource(QuranSource.audioUrl(globalVerseNumber))
            candidate.setOnPreparedListener { mp ->
                if (mp === player) {
                    try {
                        mp.start()
                        update(RecitationStatus(globalVerseNumber, RecitationPhase.PLAYING, playsLeft))
                    } catch (_: IllegalStateException) {
                        fail(globalVerseNumber)
                    }
                }
            }
            candidate.setOnCompletionListener { mp ->
                if (mp !== player) return@setOnCompletionListener
                playsLeft -= 1
                if (playsLeft > 0) {
                    try {
                        mp.seekTo(0)
                        mp.start()
                        update(RecitationStatus(globalVerseNumber, RecitationPhase.PLAYING, playsLeft))
                    } catch (_: IllegalStateException) {
                        fail(globalVerseNumber)
                    }
                } else {
                    stop()
                }
            }
            candidate.setOnErrorListener { mp, _, _ ->
                if (mp === player) fail(globalVerseNumber)
                true
            }
            candidate.prepareAsync()
        } catch (_: Exception) {
            fail(globalVerseNumber)
        }
    }

    private fun fail(number: Int) {
        stop()
        update(RecitationStatus(number, RecitationPhase.ERROR))
    }

    fun stop() {
        val active = player
        player = null
        verseNumber = null
        playsLeft = 0
        if (active != null) {
            try {
                active.reset()
            } catch (_: Exception) {
                // An async player might already have entered an error state.
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
        stop()
    }
}
