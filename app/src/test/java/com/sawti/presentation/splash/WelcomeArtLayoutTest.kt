package com.sawti.presentation.splash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeArtLayoutTest {

    @Test fun approvedReferenceSizeIsPreservedWithoutStretching() {
        val g = WelcomeArtLayout.measure(941f, 1600f)
        assertEquals(1f, g.scale, 0.0001f)
        assertEquals(0f, g.left, 0.0001f)
        assertEquals(0f, g.extraHeight, 0.0001f)
        for (y in listOf(0f, 370f, 420f, 1090f, 1200f, 1600f)) {
            assertEquals(y, g.y(y), 0.001f)
        }
    }

    @Test fun narrowPhoneFillsHeightAndPreservesSourceContentRegions() {
        val g = WelcomeArtLayout.measure(432f, 960f)
        assertEquals(432f, g.width, 0.001f)
        assertEquals(0f, g.y(0f), 0.001f)
        assertEquals(960f, g.y(1600f), 0.001f)
        // Title, people, CTA and credits are all scaled at the same aspect.
        assertEquals(300f * g.scale, g.y(370f) - g.y(70f), 0.001f)
        assertEquals(670f * g.scale, g.y(1090f) - g.y(420f), 0.001f)
        assertEquals(340f * g.scale, g.y(1540f) - g.y(1200f), 0.001f)
        assertTrue(g.y(1209f) >= 0f)
        assertTrue(g.y(1345f) <= 960f)
    }

    @Test fun tabletLandscapeHasNoVerticalCropOrNegativeGeometry() {
        val g = WelcomeArtLayout.measure(960f, 540f)
        assertTrue(g.left >= 0f)
        assertTrue(g.width <= 960f)
        assertEquals(540f, g.y(1600f), 0.001f)
    }

    @Test fun sliceOffsetsAreStrictlyIncreasingForTypicalDevices() {
        for ((w, h) in listOf(432f to 960f, 720f to 1280f, 1024f to 768f)) {
            val g = WelcomeArtLayout.measure(w, h)
            val ys = WelcomeArtLayout.boundaries.map { g.y(it.toFloat()) }
            assertTrue(ys.zipWithNext().all { (a, b) -> b > a })
            assertEquals(h, ys.last(), 0.01f)
        }
    }
}
