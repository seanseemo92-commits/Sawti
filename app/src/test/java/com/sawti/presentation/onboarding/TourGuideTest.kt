package com.sawti.presentation.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TourGuideTest {
    @Test fun homeTargetsMatchRealScreen() {
        val steps = TourGuide.steps(TourGuide.HOME, setOf("family", "daily"))
        assertEquals(
            listOf("top_caregiver", "home_daily", "home_quran"),
            steps.map { it.target },
        )
    }

    @Test fun firstUseHasNoMoreThanThreeStops() {
        val withAllCategories = TourGuide.steps(TourGuide.HOME, setOf("daily", "family", "cuisine", "routine"))
        assertEquals(3, withAllCategories.size)
        assertFalse(withAllCategories.any { it.target == "top_language" || it.target == "home_family" })
    }

    @Test fun emptyCategoriesNeverTargetMissingCards() {
        val steps = TourGuide.steps(TourGuide.HOME, emptySet())
        assertFalse(steps.any { it.target == "home_family" || it.target == "home_daily" })
        assertTrue(steps.isNotEmpty())
    }

    @Test fun sectionToursHaveStableAnchors() {
        assertEquals(listOf("quran_surah"), TourGuide.steps(TourGuide.QURAN).map { it.target })
        assertEquals(listOf("quran_reciter", "quran_verse"), TourGuide.steps(TourGuide.VERSE).map { it.target })
        assertEquals(listOf("family_relatives", "family_people"), TourGuide.steps(TourGuide.FAMILY).map { it.target })
    }

    @Test fun unrecognizedTourHasNoSteps() {
        assertTrue(TourGuide.steps("unknown").isEmpty())
    }
}
