package com.sawti.domain.model

/**
 * Natural spoken endings: a ttsPrompt never ends with I'rab (case endings / tanween).
 *  - final ة (taa marbuta) becomes هْ  (Ha' with sukun), e.g. كُوجِينَة → كُوجِينَهْ
 *  - final case marks (fatha / damma / kasra / tanween) are removed
 *  - a final consonant (including و / ي) gets a sukun (مُحَمَّدْ, نُورِيْ); words ending in alef (ا / ى) are left alone
 * Idempotent, so it can safely run on seed data, on top-ups and on caregiver edits.
 */
object TtsEndings {
    private const val SUKUN = '\u0652'
    private const val SHADDA = '\u0651'
    private val CASE_MARKS = setOf('\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650')
    private val LONG_VOWEL_LETTERS = setOf('ا', 'ى') // alef / alef-maqsura cannot carry a sukun

    private fun isMark(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670'

    fun normalize(tts: String): String {
        val original = tts.trim()
        if (original.isEmpty()) return original

        // keep trailing punctuation such as "؟" out of the way
        var body = original
        var tail = ""
        while (body.isNotEmpty() && !(body.last().isLetter() || isMark(body.last()))) {
            tail = body.last() + tail
            body = body.dropLast(1)
        }
        if (body.isEmpty()) return original

        // "…ًا" (tanween fatha on alif): drop it, the word ends on its last letter
        if (body.endsWith("\u064Bا")) body = body.dropLast(2)

        var i = body.length
        while (i > 0 && isMark(body[i - 1])) i--
        if (i == 0) return original

        val letter = body[i - 1]
        val base = body.substring(0, i - 1)
        val kept = body.substring(i).filter { it !in CASE_MARKS }

        val fixed = when {
            letter == 'ة' -> base + "ه" + SUKUN
            letter in LONG_VOWEL_LETTERS -> base + letter + kept
            SUKUN in kept || SHADDA in kept -> base + letter + kept
            else -> base + letter + kept + SUKUN
        }
        return fixed + tail
    }
}
