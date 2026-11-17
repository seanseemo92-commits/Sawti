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
     * The ONLY speech projection for built-in cards: keep the regional word,
     * remove all legacy engine-specific vowels and artificial sukun marks.
     * The visible spelling selects the word; Habibi selects the pronunciation.
     * Never normalize user's custom text or caregiver recordings.
     */
    fun resolve(value: WordItem, dialect: ArabicDialect): SpeechForm {
        val form = resolveUncorrected(value, dialect)
        if (form.source == SpeechSource.USER ||
            value.semanticKey?.substringBefore('.') !in builtinNamespaces) return form
        val plainDisplay = HabibiSpeechText.prepare(form.displayText)
        return form.copy(
            displayText = plainDisplay,
            speechText = plainDisplay,
            phonemicCue = HabibiSpeechText.prepare(form.phonemicCue),
        )
    }

    fun resolveForHabibi(value: WordItem, dialect: ArabicDialect): SpeechForm = resolve(value, dialect)

    fun habibiTextFor(item: WordItem, dialect: ArabicDialect): String? =
        neuralTextFor(item, dialect)

    /** Kept as an API alias for callers which export the voice catalog; no second pipeline. */
    fun resolveForNeural(value: WordItem, dialect: ArabicDialect): SpeechForm = resolve(value, dialect)

    /**
     * Eligibility lives next to the single projection, not in a competing playback policy.
     * Users' personal labels and speech are not rewritten.
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

    /** Compatibility name: no old vowel/sukun manipulation survives. */
    internal fun cleanSpeech(display: String, spoken: String, dialect: ArabicDialect): String =
        HabibiSpeechText.prepare(spoken)
}
