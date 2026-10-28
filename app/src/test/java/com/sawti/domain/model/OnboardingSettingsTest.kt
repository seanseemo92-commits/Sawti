package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Legacy onboarding is retired; startup defaults do not change private Room data. */
class OnboardingSettingsTest {
    @Test fun firstLaunchStartsOnMainAfterBrandSplash() {
        assertTrue(AppSettings().isOnboardingCompleted)
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
    }

    @Test fun completingGuideCannotResetOtherPreferences() {
        val personalized = AppSettings(
            dialect = ArabicDialect.TUNISIAN,
            textSize = TextSize.EXTRA_LARGE,
            touchDwell = TouchDwell.LONG,
            sentenceStripEnabled = true,
        )
        val completed = personalized.copy(isOnboardingCompleted = true)
        assertEquals(ArabicDialect.TUNISIAN, completed.dialect)
        assertEquals(TextSize.EXTRA_LARGE, completed.textSize)
        assertEquals(TouchDwell.LONG, completed.touchDwell)
        assertEquals(true, completed.sentenceStripEnabled)
        assertEquals(true, completed.isOnboardingCompleted)
    }
}
