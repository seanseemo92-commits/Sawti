package com.sawti.domain.model

/**
 * Pausal pronunciation for a built-in card spoken as one utterance. Never a persistence
 * migration or a filter on arbitrary TTS/user text. Internal case vowels and construct
 * endings belong to the phrase lexicon, not to an automatic word-by-word rule.
 */
object TtsEndings {
    private const val SUKUN = 'ْ'
    private const val FATHATAN = 'ً'
    private val caseMarks = setOf('ً', 'ٌ', 'ٍ', 'َ', 'ُ', 'ِ')
    private fun isMark(c: Char) = c in 'ً'..'ٟ' || c == 'ٰ'
    private fun isArabicLetter(c: Char) = c in 'ء'..'ي' || c in "ٱپچڤگ"

    /** Preserve ay/aw diphthongs, consonantal y/w, gemination and Maghrebi clusters. */
    private fun repairLongVowels(text: String): String = buildString {
        for (index in text.indices) {
            val c = text[index]
            if (c == SUKUN && index > 1 && text[index - 1] in "يو") {
                var start = index - 2
                while (start >= 0 && isMark(text[start])) start--
                val marks = text.substring(start + 1, index - 1)
                val vowel = if (text[index - 1] == 'ي') 'ِ' else 'ُ'
                if (vowel in marks && marks.none { it in caseMarks && it != vowel } && SUKUN !in marks) continue
            }
            append(c)
        }
    }

    fun normalize(tts: String): String {
        val original = repairLongVowels(tts)
        var end = original.length
        while (end > 0 && !(original[end - 1].isLetter() || isMark(original[end - 1]))) end--
        if (end == 0) return original
        val body = original.substring(0, end)
        val tail = original.substring(end)

        // Both common Unicode spellings of fathatan retain the supporting long aa.
        if (body.endsWith("ًا") || body.endsWith("ًى")) {
            return body.dropLast(2) + "َ" + body.last() + tail
        }
        if (body.endsWith("اً") || body.endsWith("ىً")) {
            val stem = body.dropLast(2)
            val marks = stem.takeLastWhile(::isMark)
            return stem + (if ('َ' in marks) "" else "َ") + body[body.lastIndex - 1] + tail
        }

        var index = body.length
        while (index > 0 && isMark(body[index - 1])) index--
        if (index == 0 || !isArabicLetter(body[index - 1])) return original
        val letter = body[index - 1]
        val base = body.substring(0, index - 1)
        val marks = body.substring(index)
        val kept = marks.filter { it !in caseMarks }
        val fixed = when {
            letter == 'ة' -> base + "هْ"
            letter in "اىوي" -> base + letter + kept
            FATHATAN in marks -> base + letter + kept + "َا"
            marks.any { it in caseMarks } -> base + letter + kept + if (SUKUN in kept) "" else "ْ"
            else -> body
        }
        return fixed + tail
    }
}
