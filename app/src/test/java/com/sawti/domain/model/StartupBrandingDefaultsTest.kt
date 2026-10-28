package com.sawti.domain.model

import com.sawti.presentation.splash.SAWTI_SPLASH_DURATION_MS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupBrandingDefaultsTest {
    @Test fun defaultDialectIsStandardArabic() {
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
        assertTrue(AppSettings().isOnboardingCompleted)
    }
    @Test fun splashIsExactlyTwoSeconds() {
        assertEquals(2_000L, SAWTI_SPLASH_DURATION_MS)
    }
}
