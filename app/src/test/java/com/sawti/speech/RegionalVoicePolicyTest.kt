package com.sawti.speech

import android.speech.tts.Voice
import com.sawti.domain.model.ArabicDialect
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RegionalVoicePolicyTest {
    private fun voice(tag: String, quality: Int, network: Boolean = false) =
        Voice("voice-$tag", Locale.forLanguageTag(tag), quality, Voice.LATENCY_LOW, network, emptySet())

    @Test fun allNineDialectsHaveRegionalAndArabicFallbacks() {
        assertEquals(9, ArabicDialect.entries.size)
        for (dialect in ArabicDialect.entries) {
            val locales = RegionalVoicePolicy.candidates(dialect)
            assertEquals("ar", locales.first().language)
            assertEquals("ar", locales.last().toLanguageTag())
            assertTrue(locales.contains(Locale.forLanguageTag("ar-SA")))
        }
        assertEquals("ar-LY", RegionalVoicePolicy.candidates(ArabicDialect.LIBYAN).first().toLanguageTag())
        assertEquals("ar-AE", RegionalVoicePolicy.candidates(ArabicDialect.STANDARD).first().toLanguageTag())
    }

    @Test fun exactArabicCountryOutranksOtherDialectsAndForeignVoicesNeverQualify() {
        val voices = listOf(
            voice("ar-EG", Voice.QUALITY_VERY_HIGH),
            voice("ar-LY", Voice.QUALITY_HIGH),
            voice("en-GB", Voice.QUALITY_VERY_HIGH),
        )
        assertEquals("ar-LY", RegionalVoicePolicy.best(voices, ArabicDialect.LIBYAN)?.locale?.toLanguageTag())
        assertNull(RegionalVoicePolicy.best(listOf(voices.last()), ArabicDialect.LIBYAN))
    }

    @Test fun offlineAndHigherQualityArePreferredWithinSameCountry() {
        val offline = voice("ar-TN", Voice.QUALITY_HIGH)
        val online = voice("ar-TN", Voice.QUALITY_HIGH, network = true)
        assertTrue(RegionalVoicePolicy.score(offline, ArabicDialect.TUNISIAN) >
            RegionalVoicePolicy.score(online, ArabicDialect.TUNISIAN))
    }
}
