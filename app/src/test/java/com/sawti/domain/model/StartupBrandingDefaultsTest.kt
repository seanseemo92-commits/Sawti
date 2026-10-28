package com.sawti.domain.model

import com.sawti.presentation.splash.SAWTI_AURORA_ENTRANCE_MS
import com.sawti.presentation.splash.SAWTI_AURORA_HOLD_MS
import com.sawti.presentation.splash.SAWTI_AURORA_REDUCED_MOTION_MS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupBrandingDefaultsTest {
    @Test fun defaultDialectIsStandardArabic() {
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
        assertTrue(AppSettings().isOnboardingCompleted)
    }
    @Test fun auroraSplashUsesBoundedEntranceAndHold() {
        assertEquals(2_320L, SAWTI_AURORA_ENTRANCE_MS.toLong() + SAWTI_AURORA_HOLD_MS)
        assertTrue(SAWTI_AURORA_REDUCED_MOTION_MS <= SAWTI_AURORA_ENTRANCE_MS + SAWTI_AURORA_HOLD_MS)
    }
}
