package com.sawti.audio

import org.junit.Assert.assertEquals
import org.junit.Test

/** Synthesis has already applied -18%; don't digitally stretch normal MP3 playback. */
class NeuralPlaybackSpeedTest {
    @Test fun ordinaryNeuralSpeechPlaysAtNativeRecordedRate() {
        assertEquals(1.0f, NeuralPlaybackSpeed.forTtsRate(0.85f), 0.00001f)
        assertEquals(1.0f, NeuralPlaybackSpeed.forTtsRate(1.0f), 0.00001f)
    }

    @Test fun slowAccessibilityModeCanBeSelectedExplicitly() {
        assertEquals(0.85f, NeuralPlaybackSpeed.forTtsRate(0.84f), 0.00001f)
        assertEquals(0.85f, NeuralPlaybackSpeed.forTtsRate(0.70f), 0.00001f)
    }

    @Test fun onlySupportedRatesCanBeReturned() {
        for (rate in listOf(0.5f, 0.84f, 0.85f, 1.0f, 1.2f)) {
            val speed = NeuralPlaybackSpeed.forTtsRate(rate)
            assert(speed == NeuralPlaybackSpeed.NORMAL || speed == NeuralPlaybackSpeed.SLOW)
        }
    }
}
