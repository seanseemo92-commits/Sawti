package com.sawti.speech

import com.sawti.domain.model.ArabicDialect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ArabicTtsLocalePolicyTest {
    @Test fun dialectSelectionNeverIntroducesRegionalEngineLocale() {
        val expected = listOf(
            Locale.forLanguageTag("ar"),
            Locale.forLanguageTag("ar-SA"),
            Locale.forLanguageTag("en-US"),
        )
        // No dialect is used as input to this policy: all eight colloquial
        // options and standard Arabic share exactly these TTS fallbacks.
        ArabicDialect.entries.forEach { dialect ->
            assertEquals("Engine locales must stay fixed for $dialect",
                expected, ArabicTtsLocalePolicy.candidates(Locale.forLanguageTag("en-US")))
        }
        assertFalse(expected.any { it.toLanguageTag() in listOf("ar-TN","ar-LY","ar-DZ","ar-MA","ar-IQ") })
    }

    @Test fun deviceDefaultIsOnlyLastFallbackAndDuplicatesAreRemoved() {
        assertEquals(
            listOf(Locale.forLanguageTag("ar"), Locale.forLanguageTag("ar-SA")),
            ArabicTtsLocalePolicy.candidates(Locale.forLanguageTag("ar-SA")),
        )
        assertEquals(
            listOf(Locale.forLanguageTag("ar"), Locale.forLanguageTag("ar-SA"), Locale.FRANCE),
            ArabicTtsLocalePolicy.candidates(Locale.FRANCE),
        )
    }

    @Test fun manuallySelectedVoiceCannotForceCountrySpecificDialect() {
        assertTrue(ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag("ar")))
        assertTrue(ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag("ar-SA")))
        listOf("ar-LY","ar-TN","ar-MA","ar-IQ","ar-EG","en-US").forEach {
            assertFalse("A regional voice should not change the base engine language: $it",
                ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag(it)))
        }
    }
}
