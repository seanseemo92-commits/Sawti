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
    fun resolve(value: WordItem, dialect: ArabicDialect): SpeechForm {
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
        return PronunciationCorrections.apply(item, dialect, audited)
    }
}

