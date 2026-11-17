package com.sawti.domain.model

/** Central unvowelized Arabic input contract for all nine Habibi dialects. */
object HabibiSpeechText {
    private val dialectTags = mapOf(
        ArabicDialect.LIBYAN to "LBY",
        ArabicDialect.TUNISIAN to "TUN",
        ArabicDialect.ALGERIAN to "ALG",
        ArabicDialect.MOROCCAN to "MAR",
        ArabicDialect.EGYPTIAN to "EGY",
        ArabicDialect.SYRIAN to "LEV",
        ArabicDialect.IRAQI to "IRQ",
        ArabicDialect.SAUDI to "SAU",
        ArabicDialect.STANDARD to "MSA",
    )

    private val marks = Regex("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
    private val numbers = Regex("[\\p{Nd}]")
    // Explicit Unicode White_Space works with both Android ICU and JVM Pattern.
    // Android rejects the JVM-only embedded (?U) flag during object initialization.
    private val spaces = Regex("[\\p{Z}\\u0009-\\u000D\\u0085]+")

    fun dialectTag(dialect: ArabicDialect) = dialectTags.getValue(dialect)

    /** Remove engine-specific marks and tatweel; never replace Arabic ق with Latin G. */
    fun prepare(text: String): String = spaces.replace(
        marks.replace(java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFC), ""),
        " ",
    ).trim()

    fun hasForbiddenMarksOrNumbers(text: String): Boolean =
        marks.containsMatchIn(text) || numbers.containsMatchIn(text)
}
