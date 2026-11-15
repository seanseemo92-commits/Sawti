package com.sawti.audio

/**
 * Clips are synthesized at -18% by the neural service itself.
 * Normal playback must not stretch or distort this recording.
 * Slow mode is an explicit accessibility preference.
 */
object NeuralPlaybackSpeed {
    const val NORMAL = 1.0f
    const val SLOW = 0.85f
    private const val SLOW_TTS_THRESHOLD = 0.85f

    fun forTtsRate(rate: Float): Float =
        if (rate < SLOW_TTS_THRESHOLD) SLOW else NORMAL
}
