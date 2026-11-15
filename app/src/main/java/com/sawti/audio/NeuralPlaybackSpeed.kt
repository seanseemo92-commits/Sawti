package com.sawti.audio

/**
 * Neural MP3s are normally a little slower than their synthesized native pace
 * to make individual words easier to follow for an aphasia communication app.
 *
 * Do not apply this policy to recorded caregiver audio or Android TTS.
 * A user-selected slow TTS setting still requests the slower neural option.
 */
object NeuralPlaybackSpeed {
    const val NORMAL = 0.90f
    const val SLOW = 0.80f
    private const val SLOW_TTS_THRESHOLD = 0.85f

    fun forTtsRate(rate: Float): Float =
        if (rate < SLOW_TTS_THRESHOLD) SLOW else NORMAL
}
