package com.sawti.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

/** Plays caregiver recordings / imported audio files. One clip at a time; never throws. */
class AudioPlayer(private val focus: AudioFocusController) {
    private var player: MediaPlayer? = null
    private var focusToken: Long? = null
    private var onCanceled: (() -> Unit)? = null

    /** Returns false when the file could not be played, so the caller can fall back to TTS. */
    fun play(path: String, onDone: () -> Unit = {}, onCanceled: () -> Unit = onDone): Boolean {
        stop()
        val token = focus.acquire { stop() } ?: return false
        focusToken = token
        val mp = try {
            MediaPlayer()
        } catch (_: Exception) {
            focusToken = null
            focus.release(token)
            return false
        }
        return try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(path)
            mp.setOnCompletionListener {
                if (player === mp) {
                    this.onCanceled = null
                    stop()
                    onDone()
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                if (player === mp) stop()
                true
            }
            mp.prepare()
            player = mp
            this.onCanceled = onCanceled
            mp.start()
            true
        } catch (_: Exception) {
            if (player === mp) player = null
            this.onCanceled = null
            runCatching { mp.release() }
            if (focusToken == token) {
                focusToken = null
                focus.release(token)
            }
            false
        }
    }

    fun stop() {
        val current = player
        val canceled = onCanceled
        onCanceled = null
        player = null
        val token = focusToken
        focusToken = null
        if (current != null) {
            runCatching { current.stop() }
            // Always release, even if stop() throws IllegalStateException.
            runCatching { current.release() }
        }
        if (token != null) focus.release(token)
        // Focus loss and explicit stops are terminal too: clear the AAC playing indicator.
        // Clear ownership before invoking user code, which may start another clip.
        canceled?.invoke()
    }
}
