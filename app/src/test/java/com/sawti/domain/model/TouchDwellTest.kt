package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TouchDwellTest {
    @Test fun defaultIsImmediateAndUserPreferencesHaveExplicitDurations() {
        assertEquals(TouchDwell.OFF, AppSettings().touchDwell)
        assertEquals(0L, TouchDwell.OFF.millis)
        assertEquals(300L, TouchDwell.NORMAL.millis)
        assertEquals(500L, TouchDwell.LONG.millis)
    }
}
