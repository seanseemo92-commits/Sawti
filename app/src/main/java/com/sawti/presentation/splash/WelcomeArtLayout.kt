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
    // Light copyright glyphs cross y=1540. Keep the entire footer, including
    // descenders and the copyright symbol, above the final stretch boundary.
    const val FOOTER_END = 1570
    val boundaries = intArrayOf(0, 70, 1090, 1200, FOOTER_END, 1600)
    val extraWeights = floatArrayOf(0.30f, 0f, 0.40f, 0f, 0.30f)

    /**
     * Actual PAINTED bounds of Continue, measured in the approved 941x1600
     * LIGHT reference artwork. Both themes use this one geometry for drawing
     * and hit testing. The old dark pill is replaced when rendering.
     */
    internal data class SourceButton(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
    )

    internal val referenceButton = SourceButton(119f, 1236f, 822f, 1353f)

    internal data class ButtonOnScreen(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
    )

    internal fun Geometry.buttonOnScreen(): ButtonOnScreen {
        val r = referenceButton
        return ButtonOnScreen(
            left = left + r.left * scale,
            top = y(r.top),
            width = (r.right - r.left) * scale,
            height = y(r.bottom) - y(r.top),
        )
    }

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


