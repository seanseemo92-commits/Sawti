package com.sawti.speech

import com.sawti.domain.model.AppSettings
import com.sawti.domain.model.ArabicDialect
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ArabicTtsLocalePolicyTest {
    @Test fun standardArabicIsTheSettingsAndEngineDefault() {
        assertEquals(ArabicDialect.STANDARD, AppSettings().dialect)
        assertEquals(ArabicDialect.STANDARD, DialectVoiceStatus().requested)
        assertEquals("ar", ArabicTtsLocalePolicy.candidates().first().toLanguageTag())
    }

    @Test fun stopsAtFirstSupportedArabicLocale() {
        val calls = mutableListOf<String>()
        val chosen = ArabicTtsLocalePolicy.select { calls += it.toLanguageTag(); true }
        assertEquals(listOf("ar"), calls)
        assertEquals("ar", chosen?.toLanguageTag())
    }

    @Test fun missingGeneralDataTriesOnlyArabicFallback() {
        val calls = mutableListOf<String>()
        val chosen = ArabicTtsLocalePolicy.select {
            calls += it.toLanguageTag()
            it.country == "SA"
        }
        assertEquals(listOf("ar", "ar-SA"), calls)
        assertEquals("ar-SA", chosen?.toLanguageTag())
    }

    @Test fun unavailableArabicNeverUsesDeviceEnglishOrFrench() {
        val previous = Locale.getDefault()
        try {
            for (device in listOf(Locale.US, Locale.FRANCE, Locale.forLanguageTag("ar-TN"))) {
                Locale.setDefault(device)
                val calls = mutableListOf<String>()
                assertNull(ArabicTtsLocalePolicy.select { calls += it.toLanguageTag(); false })
                assertEquals(listOf("ar", "ar-SA"), calls)
            }
        } finally { Locale.setDefault(previous) }
    }

    @Test fun languageSuccessDoesNotApproveAnObservedForeignVoice() {
        val selected = Locale.forLanguageTag("ar")
        assertTrue(ArabicTtsLocalePolicy.canSpeak(selected, "ar-SA"))
        assertTrue(ArabicTtsLocalePolicy.canSpeak(selected, null))
        assertFalse(ArabicTtsLocalePolicy.canSpeak(selected, "en-US"))
        assertFalse(ArabicTtsLocalePolicy.canSpeak(null, "ar-SA"))
        assertFalse(ArabicTtsLocalePolicy.canSpeak(Locale.US, "ar-SA"))
    }

    @Test fun manualGenericVoiceFilterDoesNotClaimADialectVoice() {
        assertTrue(ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag("ar")))
        assertTrue(ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag("ar-SA")))
        listOf("ar-LY", "ar-TN", "ar-MA", "ar-IQ", "ar-EG", "en-US").forEach {
            assertFalse(ArabicTtsLocalePolicy.isGenericArabic(Locale.forLanguageTag(it)))
        }
    }
}
