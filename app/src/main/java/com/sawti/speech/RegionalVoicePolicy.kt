package com.sawti.speech

import android.speech.tts.Voice
import com.sawti.domain.model.ArabicDialect
import java.util.Locale

/** Ranks installed Arabic voices without pretending that each device has regional voices. */
internal object RegionalVoicePolicy {
    fun candidates(dialect: ArabicDialect): List<Locale> =
        listOf(
            Locale.forLanguageTag(if (dialect == ArabicDialect.STANDARD) "ar-AE" else dialect.localeTag),
            Locale.forLanguageTag("ar-SA"),
            Locale.forLanguageTag("ar"),
        ).distinct()

    fun score(voice: Voice, dialect: ArabicDialect, preferOffline: Boolean = true): Int {
        if (voice.locale.language != "ar") return Int.MIN_VALUE
        val country = candidates(dialect).first().country
        val region = when {
            voice.locale.country.equals(country, ignoreCase = true) -> 1000
            voice.locale.country.equals("SA", ignoreCase = true) -> 250
            voice.locale.country.isBlank() -> 200
            else -> 75
        }
        val quality = when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> 190
            voice.quality >= Voice.QUALITY_HIGH -> 130
            voice.quality >= Voice.QUALITY_NORMAL -> 50
            else -> 0
        }
        val offline = if (preferOffline && !voice.isNetworkConnectionRequired) 160 else 0
        val fast = if (voice.latency <= Voice.LATENCY_LOW) 25 else 0
        val named = if (listOf("neural", "wavenet").any { it in voice.name.lowercase(Locale.ROOT) }) 30 else 0
        return region + quality + offline + fast + named
    }

    fun best(voices: Collection<Voice>, dialect: ArabicDialect, preferOffline: Boolean = true): Voice? =
        voices.asSequence().filter { it.locale.language == "ar" }
            .sortedWith(compareByDescending<Voice> { score(it, dialect, preferOffline) }
                .thenBy { it.name })
            .firstOrNull()
}
