package com.sawti.presentation.category

/** Uniform grid geometry: baseline photo ratio plus room for controls and caption. */
internal fun wordCardHeightDp(widthDp: Float, captionHeightDp: Float): Float {
    require(widthDp.isFinite() && widthDp > 0f)
    require(captionHeightDp.isFinite() && captionHeightDp >= 0f)
    val baseline = widthDp / 0.85f
    val controls = 56f
    val visibleImage = 72f
    val captionPadding = 10f
    return maxOf(baseline, controls + visibleImage + captionPadding + captionHeightDp)
}
