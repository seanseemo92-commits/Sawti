package com.sawti.audio

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer

/** Plays one AAC recording or bundled neural MP3, preserving focus ducking and lifecycle. */
class AudioPlayer(private val focus: AudioFocusController) {
    private var player: MediaPlayer? = null
    private var focusToken: Long? = null
    private var onCanceled: (() -> Unit)? = null
    private var pausedForFocus = false

    /** Returns false for immediate setup failures; asynchronous failures call onFailed. */
    fun play(path: String, onDone: () -> Unit = {}, onCanceled: () -> Unit = onDone,
        onFailed: () -> Unit = {}): Boolean =
        playSource(onDone, onCanceled, onFailed) { it.setDataSource(path) }

    /** Closing the descriptor after setDataSource is safe: MediaPlayer retains its own FD. */
    fun playAsset(afd: AssetFileDescriptor, onDone: () -> Unit = {},
        onCanceled: () -> Unit = onDone, onFailed: () -> Unit = {}): Boolean =
        try {
            playSource(onDone, onCanceled, onFailed) {
                it.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
        } finally {
            runCatching { afd.close() }
        }

    private fun playSource(onDone: () -> Unit, onCanceled: () -> Unit,
        onFailed: () -> Unit, source: (MediaPlayer) -> Unit): Boolean {
        stop()
        val mp = try { MediaPlayer() } catch (_: Exception) { return false }
        val token = focus.acquire(
            onLost = { if (player === mp) stop() },
            onTransientLoss = {
                if (player === mp && !pausedForFocus) {
                    runCatching { mp.pause() }
                        .onSuccess { pausedForFocus = true }
                        .onFailure { stop() }
                }
            },
            onDuck = { if (player === mp) runCatching { mp.setVolume(0.25f, 0.25f) } },
            onGain = {
                if (player === mp) {
                    runCatching { mp.setVolume(1.0f, 1.0f) }
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
            source(mp)
            mp.setOnCompletionListener {
                if (player === mp) {
                    this.onCanceled = null
                    stop()
                    onDone()
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                if (player === mp) {
                    this.onCanceled = null
                    stop()
                    onFailed()
                }
                true
            }
            mp.setOnPreparedListener {
                if (player === mp) {
                    runCatching { mp.start() }.onFailure {
                        this.onCanceled = null
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
            runCatching { current.release() }
        }
        if (token != null) focus.release(token)
        canceled?.invoke()
    }
}
