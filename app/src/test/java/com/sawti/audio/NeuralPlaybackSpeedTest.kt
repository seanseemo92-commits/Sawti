package com.sawti.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class NeuralPlaybackSpeedTest {
    @Test fun normalVoicePlaysTenPercentSlowerThanOriginalClip() {
        assertEquals(0.90f, NeuralPlaybackSpeed.forTtsRate(0.85f), 0.00001f)
        assertEquals(0.90f, NeuralPlaybackSpeed.forTtsRate(1.0f), 0.00001f)
    }

    @Test fun slowModeRetainsPreviousAccessibleSpeed() {
        assertEquals(0.80f, NeuralPlaybackSpeed.forTtsRate(0.84f), 0.00001f)
        assertEquals(0.80f, NeuralPlaybackSpeed.forTtsRate(0.70f), 0.00001f)
    }

    @Test fun neuralPlaybackSpeedNeverSpeedsUpTheClip() {
        for (rate in listOf(0.5f, 0.84f, 0.85f, 1.0f, 1.2f)) {
            val speed = NeuralPlaybackSpeed.forTtsRate(rate)
            assert(speed > 0.0f && speed <= 1.0f)
        }
    }
}
