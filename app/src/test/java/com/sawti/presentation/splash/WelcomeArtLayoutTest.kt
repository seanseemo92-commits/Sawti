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
        // No stretching of the Arabic subtitle or luminous text baseline.
        assertEquals(350f * g.scale, g.y(420f) - g.y(70f), 0.001f)
        assertEquals(670f * g.scale, g.y(1090f) - g.y(420f), 0.001f)
        assertEquals(340f * g.scale, g.y(1540f) - g.y(1200f), 0.001f)
        assertTrue(g.y(1209f) >= 0f)
        assertTrue(g.y(1345f) <= 960f)
    }

    @Test fun sharedContinueHitboxMatchesTheLightReference() {
        val reference = WelcomeArtLayout.measure(941f, 1600f)
        val button = with(WelcomeArtLayout) { reference.buttonOnScreen() }
        assertEquals(119f, button.left, 0.001f)
        assertEquals(1236f, button.top, 0.001f)
        assertEquals(703f, button.width, 0.001f)
        assertEquals(117f, button.height, 0.001f)
    }

    @Test fun sharedContinueRemainsInsideEveryViewportWithoutStretching() {
        for ((width, height) in listOf(
            360f to 800f, 432f to 960f, 691f to 1536f,
            720f to 1280f, 1080f to 2400f, 960f to 540f
        )) {
            val g = WelcomeArtLayout.measure(width, height)
            val rect = with(WelcomeArtLayout) { g.buttonOnScreen() }
            assertTrue(rect.left >= 0f)
            assertTrue(rect.top >= 0f)
            assertTrue(rect.left + rect.width <= width)
            assertTrue(rect.top + rect.height <= height)
            assertEquals(703f / 117f, rect.width / rect.height, 0.001f)
        }
    }

    @Test fun fullCopyrightLineIncludingDescendersRemainsUniformOnTallPhones() {
        for ((width, height) in listOf(360f to 800f, 691f to 1536f, 1080f to 2400f)) {
            val g = WelcomeArtLayout.measure(width, height)
            // The old y=1540 boundary cut through the LIGHT glyphs. Check
            // every row across that boundary, not just the footer's top.
            for (row in 1500 until 1570) {
                assertEquals(g.scale, g.y(row + 1f) - g.y(row.toFloat()), 0.001f)
            }
            assertEquals(370f * g.scale, g.y(1570f) - g.y(1200f), 0.001f)
            assertEquals(height, g.y(1600f), 0.001f)
        }
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

