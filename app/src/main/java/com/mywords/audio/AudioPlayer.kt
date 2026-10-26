package com.mywords.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

/** Plays caregiver recordings / imported audio files. One clip at a time; never throws. */
class AudioPlayer {
    private var player: MediaPlayer? = null

    /** Returns false when the file could not be played, so the caller can fall back to TTS. */
    fun play(path: String, onDone: () -> Unit = {}): Boolean {
        stop()
        val mp = MediaPlayer()
        return try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(path)
            mp.setOnCompletionListener { stop(); onDone() }
            mp.setOnErrorListener { _, _, _ -> stop(); onDone(); true }
            mp.prepare()
            mp.start()
            player = mp
            true
        } catch (e: Exception) {
            runCatching { mp.release() }
            false
        }
    }

    fun stop() {
        player?.runCatching { stop(); release() }
        player = null
    }
}
