package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Active startup defaults; the deprecated welcome screen was removed. */
class StartupBrandingDefaultsTest {
    @Test fun defaultDialectIsStandardArabic() {
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
        assertTrue(AppSettings().isOnboardingCompleted)
    }
}
