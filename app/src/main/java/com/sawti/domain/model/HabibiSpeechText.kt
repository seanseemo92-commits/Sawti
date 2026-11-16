package com.sawti.domain.model

/**
 * Text contract for Habibi-TTS generation and future dynamic inference.
 *
 * Habibi receives ordinary Arabic orthography plus an explicit dialect ID.
 * Do not encode dialect pronunciation as Microsoft/Edge-specific artificial
 * diacritics, Latin letters or phonetic spelling. Preserve the actual letters.
 *
 * Display text and teaching cues remain separate from generated speech input.
 */
object HabibiSpeechText {
    private val dialectTags = mapOf(
        ArabicDialect.STANDARD to "MSA",
        ArabicDialect.LIBYAN to "LBY",
        ArabicDialect.SAUDI to "SAU",
        ArabicDialect.EGYPTIAN to "EGY",
        ArabicDialect.SYRIAN to "LEV",
        ArabicDialect.IRAQI to "IRQ",
        ArabicDialect.TUNISIAN to "TUN",
        ArabicDialect.MOROCCAN to "MAR",
        ArabicDialect.ALGERIAN to "ALG",
    )

    private val arabicDiacritics =
        Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

    fun dialectTag(dialect: ArabicDialect): String = dialectTags.getValue(dialect)

    /** Remove diacritics only; never replace ق with g or change a word's identity. */
    fun prepare(text: String): String = text.replace(arabicDiacritics, "").trim()
}
