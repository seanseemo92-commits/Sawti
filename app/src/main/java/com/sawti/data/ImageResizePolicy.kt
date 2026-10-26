package com.sawti.data

import kotlin.math.max
import kotlin.math.roundToInt

/** Pure sizing rules; independent of Android bitmap decoding and covered by JVM tests. */
internal object ImageResizePolicy {
    const val MAX_EDGE = 1024
    private const val MAX_INPUT_EDGE = 32_768
    private const val MAX_INPUT_PIXELS = 100_000_000L

    fun validateSource(width: Int, height: Int) {
        require(width in 1..MAX_INPUT_EDGE && height in 1..MAX_INPUT_EDGE &&
            width.toLong() * height.toLong() <= MAX_INPUT_PIXELS) {
            "الصورة غير صالحة أو أبعادها كبيرة جداً"
        }
    }

    /** Keep decoder allocations under roughly a 2048px edge, then resample to 1024px. */
    fun sampleSize(width: Int, height: Int): Int {
        validateSource(width, height)
        var sample = 1
        while (max(width, height) / sample > MAX_EDGE * 2) sample *= 2
        return sample
    }

    fun outputSize(width: Int, height: Int): Pair<Int, Int> {
        require(width > 0 && height > 0)
        val scale = minOf(1.0, MAX_EDGE.toDouble() / max(width, height))
        return max(1, (width * scale).roundToInt()) to
            max(1, (height * scale).roundToInt())
    }
}
