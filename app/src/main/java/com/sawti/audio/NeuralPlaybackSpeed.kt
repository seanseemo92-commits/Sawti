package com.sawti.audio

/**
 * Habibi MP3s are synthesized at 0.75 (short words) or 0.88 (sentences).
 * Normal playback is unchanged; optional accessibility slow mode is independent.
 */
object NeuralPlaybackSpeed {
    const val NORMAL = 1.0f
    const val SLOW = 0.85f
    private const val SLOW_TTS_THRESHOLD = 0.85f

    fun forTtsRate(rate: Float): Float =
        if (rate < SLOW_TTS_THRESHOLD) SLOW else NORMAL
}
