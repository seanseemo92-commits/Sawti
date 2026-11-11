package com.sawti.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper

/**
 * Shared, app-scoped audio focus for TTS and caregiver recordings.
 * Acquires transient may-duck focus before speaking. Different interruptions
 * must not be conflated: notifications can duck speech, while phone calls
 * temporarily pause pausable MediaPlayer sessions. Pre-O is supported.
 */
class AudioFocusController(context: Context) {
    private val manager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private val leases = FocusLeaseTracker()
    private var activeRequest: AudioFocusRequest? = null
    private var activeListener: AudioManager.OnAudioFocusChangeListener? = null

    fun acquire(
        onLost: () -> Unit,
        onTransientLoss: () -> Unit = onLost,
        onDuck: () -> Unit = onTransientLoss,
        onGain: () -> Unit = {},
    ): Long? = synchronized(lock) {
        clearCurrent()
        val token = leases.next()
        val listener = AudioManager.OnAudioFocusChangeListener { state ->
            val callback = when (state) {
                AudioManager.AUDIOFOCUS_LOSS -> onLost
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onTransientLoss
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onDuck
                AudioManager.AUDIOFOCUS_GAIN -> onGain
                else -> null
            }
            if (callback != null) {
                handler.post {
                    // A late focus gain/loss must never alter a replacement clip.
                    val stillOwned = synchronized(lock) { leases.owns(token) }
                    if (stillOwned) runCatching { callback() }
                }
            }
        }
        activeListener = listener
        // Android may reject focus while a call or another exclusive session is active.
        // Some OEMs throw instead of returning FAILED: never crash or start without focus.
        val result = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(listener, handler)
                .build()
            activeRequest = request
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            }
        }.getOrDefault(AudioManager.AUDIOFOCUS_REQUEST_FAILED)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) token else {
            clearCurrent()
            null
        }
    }

    fun release(token: Long) = synchronized(lock) {
        if (leases.owns(token)) clearCurrent()
    }

    private fun clearCurrent() {
        leases.reset()
        val listener = activeListener
        val request = activeRequest
        activeListener = null
        activeRequest = null
        if (listener != null) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && request != null) {
                    manager.abandonAudioFocusRequest(request)
                } else {
                    @Suppress("DEPRECATION")
                    manager.abandonAudioFocus(listener)
                }
            }
        }
    }
}
