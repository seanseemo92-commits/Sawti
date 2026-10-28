package com.sawti.domain.model

import com.sawti.presentation.splash.SAWTI_WELCOME_BACKGROUND_ENTER_MS
import com.sawti.presentation.splash.SAWTI_WELCOME_SCENE_ENTER_MS
import com.sawti.presentation.splash.SAWTI_WELCOME_COPY_ENTER_MS
import com.sawti.presentation.splash.SAWTI_WELCOME_CTA_ENTER_MS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupBrandingDefaultsTest {
    @Test fun defaultDialectIsStandardArabic() {
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
        assertTrue(AppSettings().isOnboardingCompleted)
    }

    @Test fun conversationWelcomeHasBoundedEntranceStages() {
        assertTrue(SAWTI_WELCOME_BACKGROUND_ENTER_MS in 500..1500)
        assertTrue(SAWTI_WELCOME_SCENE_ENTER_MS in 500..1500)
        assertTrue(SAWTI_WELCOME_COPY_ENTER_MS in 300..1100)
        assertTrue(SAWTI_WELCOME_CTA_ENTER_MS in 300..1100)
        // The welcome screen only transitions through the explicit Continue button.
    }
}
