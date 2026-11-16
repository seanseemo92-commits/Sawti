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
    private val numbers = Regex("[0-9\u0660-\u0669\u06F0-\u06F9]")
    private val spaces = Regex("\\s+")

    fun dialectTag(dialect: ArabicDialect) = dialectTags.getValue(dialect)

    /** Remove engine-specific marks and tatweel; never replace Arabic ق with Latin G. */
    fun prepare(text: String): String = spaces.replace(marks.replace(text, ""), " ").trim()

    fun hasForbiddenMarksOrNumbers(text: String): Boolean =
        marks.containsMatchIn(text) || numbers.containsMatchIn(text)
}
