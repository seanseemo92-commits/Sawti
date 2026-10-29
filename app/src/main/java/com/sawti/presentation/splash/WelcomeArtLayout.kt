package com.sawti.presentation.splash

/**
 * Non-uniformly extends ONLY the atmospheric, text-free regions of the
 * approved 941x1600 artwork. Faces, Arabic wordmark, dialogue bubbles,
 * CTA and developer credits remain uniformly scaled at their source aspect.
 *
 * Why: a portrait phone is much taller relative to its width than the
 * approved artwork. A blanket ContentScale.Fit leaves dark bars and Crop
 * cuts off the people or footer; FillBounds stretches faces/letters.
 */
internal object WelcomeArtLayout {
    const val SOURCE_WIDTH = 941
    const val SOURCE_HEIGHT = 1600

    // Stretch ONLY the first decorative sky strip, lower aurora ribbon,
    // and bottom empty gradient. The phrase under SAWTI extends through
    // y=420: stretching any part of it visibly warps the Arabic lettering.
    // All title, faces, speech bubbles, button and footer stay uniform.
    val boundaries = intArrayOf(0, 70, 1090, 1200, 1540, 1600)
    val extraWeights = floatArrayOf(0.30f, 0f, 0.40f, 0f, 0.30f)

    data class Geometry(
        val scale: Float,
        val width: Float,
        val height: Float,
        val left: Float,
        val extraHeight: Float,
    ) {
        fun y(sourceY: Float): Float {
            var result = 0f
            for (i in extraWeights.indices) {
                val top = boundaries[i].toFloat()
                val bottom = boundaries[i + 1].toFloat()
                val fraction = ((sourceY - top) / (bottom - top)).coerceIn(0f, 1f)
                result += fraction * ((bottom - top) * scale + extraHeight * extraWeights[i])
            }
            return result
        }
    }

    fun measure(viewportWidth: Float, viewportHeight: Float): Geometry {
        require(viewportWidth > 0 && viewportHeight > 0)
        val scale = minOf(
            viewportWidth / SOURCE_WIDTH,
            viewportHeight / SOURCE_HEIGHT,
        )
        val width = SOURCE_WIDTH * scale
        val extra = (viewportHeight - SOURCE_HEIGHT * scale).coerceAtLeast(0f)
        return Geometry(scale, width, viewportHeight, (viewportWidth - width) / 2f, extra)
    }
}
