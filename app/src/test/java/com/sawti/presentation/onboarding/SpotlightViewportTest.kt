package com.sawti.presentation.onboarding

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpotlightViewportTest {
    @Test fun rotationAndMultiWindowUseVisibleViewport() {
        val original = Rect(250f, 100f, 430f, 240f)
        assertEquals(Rect(250f, 100f, 320f, 240f),
            spotlightVisibleBounds(original, 320f, 640f))
        assertEquals(original, spotlightVisibleBounds(original, 840f, 600f))
    }
    @Test fun offscreenAnchorsAreHidden() {
        assertNull(spotlightVisibleBounds(Rect(500f, 20f, 700f, 90f), 320f, 640f))
        assertNull(spotlightVisibleBounds(Rect(10f, -80f, 100f, -1f), 320f, 640f))
        assertNull(spotlightVisibleBounds(null, 320f, 640f))
    }
}
