package com.mywords.domain.model

/** Builds the "first syllable" prompt used for phonemic cueing, e.g. إِسْمَاعِيلْ → إِسْـ... */
object PhonemicCue {
    private const val SHADDA = "\u0651"
    private const val SUKUN = '\u0652'

    /** fatha, damma, kasra and the tanween marks: a real vowel (sukun and shadda are not). */
    private fun isVowelMark(c: Char) = c in '\u064B'..'\u0650' || c == '\u0670'

    private fun isMark(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670'

    /** Splits text into letter clusters (a letter plus its diacritics). */
    private fun clusters(text: String): List<String> {
        val out = mutableListOf<StringBuilder>()
        for (c in text.trim()) {
            when {
                isMark(c) && out.isNotEmpty() -> out.last().append(c)
                c.isLetter() -> out.add(StringBuilder().append(c))
            }
        }
        return out.map { it.toString() }
    }

    fun derive(tts: String): String {
        val cl = clusters(tts)
        if (cl.isEmpty()) return ""
        var i = 0
        // skip the definite article "ال"
        if (cl.size > 2 && cl[0] == "ا" && cl[1].startsWith("ل")) i = 2

        val first = cl[i]
        val sb = StringBuilder(first.replace(SHADDA, ""))
        val next = cl.getOrNull(i + 1)
        val startsWithAlef = first[0] in "أإآا"
        val hasVowel = sb.any { isVowelMark(it) }
        // a letter is always kept together with ALL of its marks (clusters), and a consonant that has
        // no vowel of its own (bare, or only a sukun) is never left alone: it takes the next syllable letter too
        if (next != null && (startsWithAlef || !hasVowel || next.contains(SUKUN))) {
            sb.append(next.replace(SHADDA, ""))
        }
        return sb.toString() + "ـ..."
    }

    /** TTS should say the sound only, without the tatweel and dots used for display. */
    fun forSpeech(cue: String): String =
        cue.replace("ـ", "").replace(".", "").replace("…", "").trim()
}
