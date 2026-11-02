package com.sawti.presentation.category

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordCardSizingTest {
    @Test fun ordinaryTwoLineLabelKeepsBaselineRatio() {
        assertEquals(160f / 0.85f, wordCardHeightDp(160f, 48f), 0.01f)
    }
    @Test fun fourLineLabelUsesMeasuredSpaceNot360DpMinimum() {
        val height = wordCardHeightDp(160f, 96f)
        assertEquals(234f, height, 0.01f)
        assertTrue(height < 360f)
    }
    @Test fun largeFontCaptionGetsEnoughRoom() {
        assertTrue(wordCardHeightDp(160f, 192f) >= 56f + 72f + 10f + 192f)
    }
    @Test fun widerCellsKeepBaselineWhenItIsLarger() {
        assertEquals(320f / 0.85f, wordCardHeightDp(320f, 48f), 0.01f)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidWidthIsRejected() { wordCardHeightDp(0f, 48f) }
}
