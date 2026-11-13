package com.sawti.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceRecorderDurationTest {
    @Test fun shortTapIsDiscardedWithoutStoppingAnUninitializedEncoder() {
        assertFalse(recordingDurationValid(0L))
        assertFalse(recordingDurationValid(100L))
        assertFalse(recordingDurationValid(399L))
        assertTrue(recordingDurationValid(400L))
        assertTrue(recordingDurationValid(1_000L))
    }
}
