package com.sawti.domain.model

import java.util.Locale

/** Country is only an initial hint, not proof of nationality or a replacement for user choice. */
object InitialDialectPolicy {
    fun choose(simCountry: String?, networkCountry: String?, deviceCountries: List<String>): ArabicDialect {
        val countries = listOf(simCountry, networkCountry) + deviceCountries
        val country = countries.asSequence().mapNotNull { it?.trim()?.uppercase(Locale.ROOT) }
            .firstOrNull { it.matches(Regex("[A-Z]{2}")) }
        return ArabicDialect.entries.firstOrNull { it.countryCode == country && country != null }
            ?: ArabicDialect.STANDARD
    }
}
