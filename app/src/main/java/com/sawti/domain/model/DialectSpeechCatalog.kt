package com.sawti.domain.model

/** A missing speech candidate is explicit; never fill it with guessed vowels. */
data class DialectForm(
    val displayText: String,
    val speechText: String? = null,
    val phonemicCue: String? = null,
)

enum class SpeechSource { CANONICAL, EXPLICIT_DIALECT, REGIONAL, UNVOCALIZED_FALLBACK, USER }

data class SpeechForm(
    val displayText: String,
    val speechText: String,
    val phonemicCue: String,
    val source: SpeechSource,
) {
    // There is no recorded native-speaker or device listening evidence for this release.
    // These flags must not be derived from diacritic coverage or unit-test success.
    val needsNativeReview: Boolean get() = source != SpeechSource.USER
    val needsTtsListening: Boolean get() = source != SpeechSource.USER
}

object DialectSpeechCatalog {
    /** Unknown semantic namespaces are personal/unregistered concepts, never normalized. */
    private val builtinNamespaces = setOf(
        "family", "request", "dish", "routine", "vegetable", "fruit", "food",
        "home", "clothing", "body", "action", "animal", "transport", "greeting"
    )

    private fun resolveUncorrected(value: WordItem, dialect: ArabicDialect): SpeechForm {
        val item = value.forEditing()
        val key = item.semanticKey
        fun custom(flag: Int) = item.customTextFields and flag != 0
        val protectedSpeech = custom(CardDetails.SPEECH) || custom(CardDetails.LABEL)
        val builtIn = key != null && !item.categoryId.startsWith("custom_")
        val regional = if (builtIn && dialect != ArabicDialect.STANDARD)
            RegionalDishesCatalog.variant(key!!, dialect) else null
        val variant = if (builtIn && dialect != ArabicDialect.STANDARD)
            DialectCatalog.words[key]?.get(dialect) else null
        val visible = if (custom(CardDetails.LABEL)) item.displayText
            else regional?.displayText ?: variant?.displayText ?: item.displayText
        val source = when {
            !builtIn || protectedSpeech -> SpeechSource.USER
            regional != null -> SpeechSource.REGIONAL
            variant?.speechText != null -> SpeechSource.EXPLICIT_DIALECT
            visible == item.displayText -> SpeechSource.CANONICAL
            else -> SpeechSource.UNVOCALIZED_FALLBACK
        }
        val speech = when (source) {
            SpeechSource.USER, SpeechSource.CANONICAL -> item.ttsPrompt
            SpeechSource.REGIONAL -> regional!!.ttsPrompt
            SpeechSource.EXPLICIT_DIALECT -> variant!!.speechText!!
            SpeechSource.UNVOCALIZED_FALLBACK -> visible
        }
        val cue = when {
            custom(CardDetails.CUE) || protectedSpeech || !builtIn -> item.phonemicCue
            source == SpeechSource.CANONICAL -> item.phonemicCue
            source == SpeechSource.REGIONAL -> regional!!.phonemicCue
            source == SpeechSource.UNVOCALIZED_FALLBACK -> ""
            else -> variant?.phonemicCue ?: PhonemicCue.derive(speech)
        }
        val audited = DialectSpeechRevisions.applySpeech(item, dialect, SpeechForm(visible, speech, cue, source))
        return audited
    }

    /**
     * One and only catalog projection for display, Android fallback and neural speech.
     * Never rewrite caregiver-supplied text. Published assets are keyed by concept/dialect,
     * rather than a fragile string comparison with this human-readable speech form.
     */
    fun resolve(value: WordItem, dialect: ArabicDialect): SpeechForm {
        val form = resolveUncorrected(value, dialect)
        if (form.source == SpeechSource.USER ||
            value.semanticKey?.substringBefore('.') !in builtinNamespaces) return form
        val clean = cleanSpeech(form.displayText, form.speechText, dialect)
        // The Gemini review is a conditional overlay, never a mutation of Room
        // seed rows, caregivers' recordings or editable user-supplied content.
        // Store the exact proposed speech after pausal cleanup so the neural export
        // and Android fallback resolve the same reviewed form.
        val proposed = GeminiPronunciationReview.apply(dialect, value.semanticKey, clean)
        val reviewedCue = if (proposed != clean || DialectSpeechRevisions.hasCompletedCandidate(
                dialect, value.semanticKey.orEmpty()
            )) PhonemicCue.derive(proposed) else ""
        val cue = reviewedCue.ifBlank { form.phonemicCue }
        return form.copy(speechText = proposed, phonemicCue = cue)
    }

    /** Kept as an API alias for callers which export the voice catalog; no second pipeline. */
    fun resolveForNeural(value: WordItem, dialect: ArabicDialect): SpeechForm = resolve(value, dialect)

    /**
     * Eligibility lives next to the single projection, not in a competing playback policy.
     * Users' personal labels/speech always use their recording or Android TTS.
     */
    fun neuralTextFor(item: WordItem, dialect: ArabicDialect): String? {
        if (item.semanticKey.isNullOrBlank() || item.categoryId.startsWith("custom_") ||
            item.semanticKey.substringBefore('.') !in builtinNamespaces)
            return null
        if (item.customTextFields and (CardDetails.LABEL or CardDetails.SPEECH) != 0)
            return null
        if (item.neuralDialect != null && item.neuralDialect != dialect) return null
        val form = resolve(item.forEditing(), dialect)
        return form.speechText.takeIf { form.source != SpeechSource.USER && it.isNotBlank() }
    }

    // Lexical diacritics stay intact; remove only pronunciation aids from built-in phrases.
    // This function is deliberately shared by export and Android fallback.
    internal fun cleanSpeech(display: String, spoken: String, dialect: ArabicDialect): String {
        if (spoken.isBlank()) return spoken
        val sounds = spoken.trim().split(Regex("""\s+"""))
        val labels = display.trim().split(Regex("""\s+"""))
        val aligned = sounds.size == labels.size
        val marks = Regex("""[\u064B-\u065F\u0670]""")
        val vowelSukun = Regex("""(?:ِّ?ي|ُّ?و|َّ?ا)ْ""")
        val terminalTa = Regex("""(?:ة[ٌٍُْ]?|هْ?|تْ)$""")
        return sounds.mapIndexed { i, sound ->
            val base = sound.trimEnd('؟', '!', '،', '.', '؛', ':')
            val punctuation = sound.substring(base.length)
            val visible = if (aligned) labels[i].trim('(', ')', '؟', '!', '،', '.', '؛', ':')
                .replace(marks, "") else null
            var word = when (base) {
                "إِمِّيَّهْ", "إِمِّيَّة" -> "مَيَّة"
                "إِبْنْ" -> "اِبْن"
                "أَهْلَنْ" -> "أَهْلًا"
                "وَسَهْلَنْ" -> "وَسَهْلًا"
                else -> base
            }
            if (word.startsWith("إِ") && !visible.isNullOrEmpty() &&
                visible.first() !in "اأإآٱ") word = word.removePrefix("إِ")
            word = word.replace("تْش", "تش")
            // Keep lexical clusters in خَالْتِي and similar regional words.
            if (word.startsWith("الْ")) word = word.replaceFirst("الْ", "ال")
            else if (word.startsWith("بِالْ")) word = word.replaceFirst("بِالْ", "بِال")
            else if (word.startsWith("لِلْ")) word = word.replaceFirst("لِلْ", "لِل")
            word = word
                .replace(vowelSukun) { it.value.dropLast(1) }
            // صْبَع is a deliberate Libyan lexical cluster, not a prosthetic sukun.
            // Explicit regional onset clusters (حْرَام، قْنِيَّة، دْجَاج)
            // are lexical, not synthetic prefixes; keep their initial sukun.
            if (visible?.endsWith('ة') == true) word = word.replace(terminalTa, "ة")
            word = word.removeSuffix("ْ")
            // Keep every deliberately specified medial vowel. In Arabic greetings
            // these vowels control liaison and the pronunciation of 'اللَّهُ'.
            word + punctuation
        }.joinToString(" ")
    }
}

