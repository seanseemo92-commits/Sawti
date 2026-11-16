package com.sawti.domain.model

/**
 * A short, unvowelled prompt from the word's actual spelling.
 * No legacy manually synthesized vowels, shadda, sukun or consonant tricks.
 */
object PhonemicCue {
    fun derive(speech: String): String {
        val first = HabibiSpeechText.prepare(speech)
            .trim().split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
        if (first.isEmpty()) return ""
        return first.take(2) + "ـ..."
    }

    /** Strip display decoration only; never introduce phonetic substitutions. */
    fun forSpeech(cue: String): String =
        HabibiSpeechText.prepare(cue).replace("ـ", "").replace(".", "")
            .replace("…", "").trim()
}
