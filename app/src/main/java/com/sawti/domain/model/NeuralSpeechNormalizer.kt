package com.sawti.domain.model

/**
 * Neural synthesis must use a separate, dialect-preserving text channel.
 * This is NOT a replacement for Android TTS speechText or user-entered speech.
 * Keep internal lexical vowels and shadda; remove only robotic pronunciation aids.
 * Written pronunciation still needs native-speaker and real-device listening reviews.
 */
object NeuralSpeechNormalizer {
    private val vowelSukun = Regex("""(?:ِّ?ي|ُّ?و|َّ?ا)ْ""")
    private val initialSukun = Regex("""^([ء-ي])ْ""")
    private val trailingTa = Regex("""(?:ة[ٌٍُْ]?|هْ?|تْ)$""")
    private val caseEnding = Regex("""[ٌٍُ]$""")
    private val diacritics = Regex("""[\u064B-\u065F\u0670]""")
    private val whitespace = Regex("""\s+""")

    @Suppress("UNUSED_PARAMETER")
    fun normalize(
        displayText: String,
        baseSpeechText: String,
        dialect: ArabicDialect,
        semanticKey: String? = null,
    ): String {
        if (baseSpeechText.isBlank()) return baseSpeechText
        val spoken = baseSpeechText.trim().split(whitespace)
        val visible = displayText.trim().split(whitespace)
        // Never pair different words just because their token indexes happen to match.
        val aligned = visible.size == spoken.size
        return spoken.mapIndexed { index, token ->
            cleanWord(token, if (aligned) visible[index] else null,
                dialect, index < spoken.lastIndex)
        }.joinToString(" ")
    }

    private fun cleanWord(rawToken: String, displayToken: String?,
        dialect: ArabicDialect, connectsToNext: Boolean): String {
        val bare = rawToken.trimEnd('؟', '!', '،', '.', '؛', ':')
        val punctuation = rawToken.substring(bare.length)
        val visible = displayToken?.trimEnd('؟', '!', '،', '.', '؛', ':', ')', '(')
            ?.trimStart('(', ')')?.replace(diacritics, "")
        var word = bare

        word = when {
            word == "إِمِّيَّهْ" || word == "إِمِّيَّة" -> "مَيَّة"
            word == "إِبْنْ" && visible?.startsWith("ابن") == true -> "اِبْن"
            word == "إِرْتَاحْ" && visible == "ارتاح" -> "اِرْتَاح"
            word == "أَهْلَنْ" -> "أَهْلًا"
            word == "وَسَهْلَنْ" -> "وَسَهْلًا"
            else -> word
        }
        // Remove only an artificial initial hamza when the displayed dialect word
        // starts with a consonant. Preserve intentional hamza in Arabic words.
        if (word.startsWith("إِ") && visible != null && visible.isNotEmpty() &&
            visible.first() !in "اأإآٱ") {
            word = word.removePrefix("إِ")
        }

        word = word.replace("تْش", "تش")
            .replace(initialSukun, "$1")
            .replace("الْ", "ال")
            .replace(vowelSukun) { it.value.dropLast(1) }

        // Replace the robotic h/t or an inflectional ending ONLY when the
        // corresponding visible word actually ends in ta marbuta.
        if (visible?.endsWith('ة') == true) word = word.replace(trailingTa, "ة")
        word = word.removeSuffix("ْ")
        if (dialect != ArabicDialect.STANDARD && connectsToNext &&
            !word.endsWith("ًا") && !word.endsWith("اً")) {
            word = word.replace(caseEnding, "")
        }
        return word + punctuation
    }
}
