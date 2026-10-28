package com.sawti.speech

import java.util.Locale

/** Pure policy: dialect selection must not alter the Android TTS engine Locale. */
internal object ArabicTtsLocalePolicy {
    private val generalArabic = Locale.forLanguageTag("ar")
    private val saudiArabic = Locale.forLanguageTag("ar-SA")

    fun candidates(defaultLocale: Locale): List<Locale> =
        listOf(generalArabic, saudiArabic, defaultLocale).distinct()

    fun isGenericArabic(locale: Locale): Boolean =
        locale.language == "ar" && (locale.country.isBlank() || locale.country == "SA")
}
