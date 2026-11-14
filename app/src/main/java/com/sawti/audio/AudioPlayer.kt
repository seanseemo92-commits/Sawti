package com.sawti.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

/** Plays caregiver recordings / imported audio files. One clip at a time; never throws. */
class AudioPlayer(private val focus: AudioFocusController) {
    private var player: MediaPlayer? = null
    private var focusToken: Long? = null
    private var onCanceled: (() -> Unit)? = null
    private var pausedForFocus = false

    /** Returns false for immediate setup failures; asynchronous failures call onFailed. */
    fun play(path: String, onDone: () -> Unit = {}, onCanceled: () -> Unit = onDone,
        onFailed: () -> Unit = {}): Boolean {
        stop()
        val mp = try {
            MediaPlayer()
        } catch (_: Exception) {
            return false
        }
        val token = focus.acquire(
            onLost = { if (player === mp) stop() },
            onTransientLoss = {
                if (player === mp && !pausedForFocus) {
                    runCatching { mp.pause() }
                        .onSuccess { pausedForFocus = true }
                        .onFailure { stop() }
                }
            },
            // Short notifications must not pause recordings.
            onDuck = {},
            onGain = {
                if (player === mp) {
                    if (pausedForFocus) {
                        pausedForFocus = false
                        runCatching { mp.start() }.onFailure { stop() }
                    }
                }
            },
        ) ?: run {
            runCatching { mp.release() }
            return false
        }
        focusToken = token
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
                if (player === mp) {
                    stop()
                    onFailed()
                }
                true
            }
            mp.setOnPreparedListener {
                if (player === mp) {
                    runCatching { mp.start() }.onFailure {
                        stop()
                        onFailed()
                    }
                }
            }
            player = mp
            this.onCanceled = onCanceled
            mp.prepareAsync()
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
        pausedForFocus = false
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
