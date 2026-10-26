package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** No Room schema mutation or private family data is required for onboarding state. */
class OnboardingSettingsTest {
    @Test fun firstLaunchShowsGuideByDefault() {
        assertFalse(AppSettings().isOnboardingCompleted)
        assertEquals(ArabicDialect.LIBYAN, AppSettings().dialect)
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
