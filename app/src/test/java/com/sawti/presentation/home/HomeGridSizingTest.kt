package com.sawti.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeGridSizingTest {
    @Test fun narrowSplitScreenAllowsOneUsableColumn() {
        assertEquals(1, homeGridColumnCount(240f))
        assertEquals(1, homeGridColumnCount(269f))
    }

    @Test fun compactPhonesKeepTwoColumns() {
        for (width in listOf(270f, 280f, 320f, 360f, 411f)) {
            assertEquals("width=$width", 2, homeGridColumnCount(width))
        }
    }

    @Test fun tabletsAndExpandedWindowsFitMoreCards() {
        assertEquals(3, homeGridColumnCount(600f))
        assertEquals(4, homeGridColumnCount(840f))
        assertEquals(5, homeGridColumnCount(1024f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidWindowSizeIsRejected() { homeGridColumnCount(0f) }
}
