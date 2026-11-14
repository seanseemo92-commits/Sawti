package com.sawti.presentation.category

import org.junit.Assert.assertEquals
import org.junit.Test

class NarrowWindowRegressionTest {
    @Test fun ultraNarrowSplitScreenUsesOneColumn() {
        // CategoryScreen subtracts 32 dp from the actual available window width.
        assertEquals("240dp split-screen window", 1, wordGridColumnCount(240f - 32f))
        assertEquals("269dp split-screen window", 1, wordGridColumnCount(269f - 32f))
    }

    @Test fun compactPhoneStillUsesTwoColumns() {
        assertEquals("280dp phone", 2, wordGridColumnCount(280f - 32f))
        assertEquals("320dp phone", 2, wordGridColumnCount(320f - 32f))
        assertEquals("360dp phone", 2, wordGridColumnCount(360f - 32f))
    }
}
