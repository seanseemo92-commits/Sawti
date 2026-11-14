package com.sawti.presentation.home

/**
 * The measured Compose window is the source of truth, including split-screen,
 * tablet resizing and foldables. Preserve two columns on normal compact phones.
 * A single column is allowed only for exceptionally narrow multi-window panes.
 */
/** Allow complete Arabic captions without sacrificing the photograph. */
internal fun homeCardHeightDp(cellWidthDp: Float, captionHeightDp: Float): Float {
    require(cellWidthDp.isFinite() && cellWidthDp > 0f)
    require(captionHeightDp.isFinite() && captionHeightDp >= 0f)
    return maxOf(cellWidthDp / 0.85f, 86f + captionHeightDp + 10f)
}

internal fun homeGridColumnCount(windowWidthDp: Float): Int {
    require(windowWidthDp.isFinite() && windowWidthDp > 0f)
    if (windowWidthDp < 270f) return 1
    // 20dp horizontal padding on each side, 18dp between cards, 160dp ideal cells.
    val ideal = ((windowWidthDp - 40f + 18f) / 178f).toInt()
    return ideal.coerceIn(2, 6)
}
