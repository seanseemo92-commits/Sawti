package com.sawti.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Gentle two-note "ding-ding" synthesised in code (no audio asset needed): soft attack,
 * smooth decay, low level. Played for a correct answer in the receptive game.
 */
class SoftChime {
    private val sampleRate = 22_050
    private val main = Handler(Looper.getMainLooper())
    private var active: AudioTrack? = null

    @Synchronized private fun release(track: AudioTrack) {
        if (active === track) active = null
        runCatching { track.stop() }
        runCatching { track.release() }
    }

    private val pcm: ShortArray by lazy {
        val total = (sampleRate * 0.9).toInt()
        val mix = FloatArray(total)
        // C6 then E6, the second note slightly later and softer
        addNote(mix, startSec = 0.00, freq = 1046.5, gain = 0.34f)
        addNote(mix, startSec = 0.17, freq = 1318.5, gain = 0.28f)
        ShortArray(total) { (mix[it].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort() }
    }

    private fun addNote(mix: FloatArray, startSec: Double, freq: Double, gain: Float) {
        val start = (startSec * sampleRate).toInt()
        val length = (0.6 * sampleRate).toInt()
        for (i in 0 until length) {
            val idx = start + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val attack = min(1.0, i / (0.006 * sampleRate))
            val envelope = attack * exp(-5.5 * t)
            val tone = sin(2 * PI * freq * t) + 0.25 * sin(2 * PI * freq * 2 * t)
            mix[idx] += (tone * envelope * gain).toFloat()
        }
    }

    @Synchronized
    fun play() {
        // Prevent rapid correct taps from occupying multiple old-device audio tracks.
        active?.let(::release)
        var opened: AudioTrack? = null
        try {
            val data = pcm
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            opened = track
            if (track.state != AudioTrack.STATE_INITIALIZED ||
                track.write(data, 0, data.size) != data.size) {
                release(track)
                return
            }
            active = track
            track.setNotificationMarkerPosition(data.size - 1)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { release(t) }
                override fun onPeriodicNotification(t: AudioTrack) = Unit
            }, main)
            track.play()
            // Some OEMs never deliver the marker callback; still release promptly.
            main.postDelayed({ release(track) }, 1_300L)
        } catch (_: Exception) {
            opened?.let(::release)
        }
    }
}
