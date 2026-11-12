package com.sawti.presentation.category

/** Keep the phone grid in two columns, then add columns when 160dp cards fit. */
internal fun wordGridColumnCount(contentWidthDp: Float): Int {
    require(contentWidthDp.isFinite() && contentWidthDp > 0f)
    return ((contentWidthDp + 14f) / 174f).toInt().coerceAtLeast(2)
}

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
