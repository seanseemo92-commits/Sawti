package com.sawti.domain.model

/** Hint onsets are derived directly from the actual Arabic label, without vowels. */
object PhonemicCue {
    fun derive(text: String): String {
        val firstWord = HabibiSpeechText.prepare(text)
            .split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
        return if (firstWord.isEmpty()) "" else firstWord.take(2) + "..."
    }

    fun forSpeech(cue: String): String = HabibiSpeechText.prepare(cue)
        .replace("…", "").replace(".", "").trim()
}
