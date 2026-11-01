package com.sawti.speech

import java.util.Locale

/** Fixed general Arabic baseline. A non-Arabic device language is never a speech fallback. */
internal object ArabicTtsLocalePolicy {
    fun candidates(): List<Locale> = listOf(Locale.forLanguageTag("ar"), Locale.forLanguageTag("ar-SA"))

    fun select(tryLanguage: (Locale) -> Boolean): Locale? = candidates().firstOrNull(tryLanguage)

    fun isGenericArabic(locale: Locale): Boolean =
        locale.language == "ar" && (locale.country.isBlank() || locale.country == "SA")

    fun canSpeak(selected: Locale?, actualTag: String?): Boolean =
        selected?.language == "ar" &&
            (actualTag == null || Locale.forLanguageTag(actualTag).language == "ar")
}
