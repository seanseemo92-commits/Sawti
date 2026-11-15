package com.sawti.presentation.quran

import org.junit.Assert.*
import org.junit.Test

class QuranPlaybackBridgeTest {
    @Test fun mediaSessionActionsControlTheCurrentAyahOwner() {
        val owner = Any()
        var played = 0
        var paused = 0
        var stopped = 0
        QuranPlaybackBridge.attach(owner, { played++ }, { paused++ }, { stopped++ })
        try {
            QuranPlaybackBridge.play()
            QuranPlaybackBridge.pause()
            QuranPlaybackBridge.stop()
            assertEquals(1, played)
            assertEquals(1, paused)
            assertEquals(1, stopped)
        } finally {
            QuranPlaybackBridge.detach(owner)
        }
        QuranPlaybackBridge.play()
        assertEquals(1, played)
    }

    @Test fun completionAndFailureEndForegroundPlaybackState() {
        val owner = Any()
        QuranPlaybackBridge.attach(owner, {}, {}, {})
        try {
            QuranPlaybackBridge.publish(RecitationStatus(1, RecitationPhase.BUFFERING), "الفاتحة")
            assertEquals(RecitationPhase.BUFFERING, QuranPlaybackBridge.snapshot.phase)
            QuranPlaybackBridge.publish(RecitationStatus(1, RecitationPhase.PLAYING), "الفاتحة")
            assertEquals(RecitationPhase.PLAYING, QuranPlaybackBridge.snapshot.phase)
            QuranPlaybackBridge.publish(RecitationStatus(), "الفاتحة")
            assertEquals(RecitationPhase.IDLE, QuranPlaybackBridge.snapshot.phase)
            QuranPlaybackBridge.publish(RecitationStatus(1, RecitationPhase.ERROR,
                failure = RecitationFailure.FOCUS), "الفاتحة")
            assertEquals(RecitationPhase.ERROR, QuranPlaybackBridge.snapshot.phase)
        } finally {
            QuranPlaybackBridge.detach(owner)
        }
        assertEquals(RecitationPhase.IDLE, QuranPlaybackBridge.snapshot.phase)
    }
}
