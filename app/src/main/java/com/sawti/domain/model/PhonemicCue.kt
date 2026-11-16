package com.sawti.domain.model

/** Conservative written cue proposal, not syllabification or a phonetic review. */
object PhonemicCue {
    private const val SUKUN = 'ْ'
    private const val SHADDA = 'ّ'
    private fun isMark(c: Char) = c in 'ً'..'ٟ' || c == 'ٰ'
    private fun hasVowel(text: String) = text.any { it in 'ً'..'ِ' || it == 'ٰ' }

    private fun clusters(text: String): List<String> {
        val out = mutableListOf<StringBuilder>()
        for (c in text) {
            when {
                isMark(c) && out.isNotEmpty() -> out.last().append(c)
                c.isLetter() -> out.add(StringBuilder().append(c))
            }
        }
        return out.map { it.toString() }
    }

    fun derive(tts: String): String {
        // Never borrow a consonant or a vowel from the next word in a phrase.
        val token = tts.trim().split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
        val cl = clusters(token)
        if (cl.isEmpty()) return ""
        val start = if (cl.size > 2 && cl[0][0] in "اٱ" && cl[1][0] == 'ل') 2 else 0
        val first = cl[start]
        // An unvowelled built-in word still needs a non-empty, conservative cue.
        if (!hasVowel(first) && SUKUN !in first && first[0] != 'آ')
            return first[0].toString() + "ـ..."
        val result = StringBuilder(first) // Preserve shadda and the existing vowel marks.
        val next = cl.getOrNull(start + 1)
        if (next != null) {
            val carrier = next.length == 1 && (
                (next[0] in "اى" && 'َ' in first) ||
                (next[0] == 'ي' && 'ِ' in first) ||
                (next[0] == 'و' && 'ُ' in first))
            when {
                carrier || SUKUN in next -> result.append(next)
                // Only the first half of a doubled following consonant closes this cue.
                SHADDA in next -> result.append(next[0]).append(SUKUN)
                !hasVowel(first) && hasVowel(next) -> result.append(next)
            }
        }
        return result.toString() + "ـ..."
    }

    /** Decoration is display-only; never rewrite the caregiver's vowels or consonants. */
    fun forSpeech(cue: String): String =
        cue.replace("ـ", "").replace(".", "").replace("…", "").trim()
}
