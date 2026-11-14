package com.sawti.presentation.category

/** Use one column only in exceptionally narrow split-screen panes; keep 2+ on phones. */
internal fun wordGridColumnCount(contentWidthDp: Float): Int {
    require(contentWidthDp.isFinite() && contentWidthDp > 0f)
    if (contentWidthDp < 238f) return 1
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
