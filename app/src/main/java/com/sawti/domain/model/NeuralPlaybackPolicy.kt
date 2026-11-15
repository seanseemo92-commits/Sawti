package com.sawti.domain.model

/**
 * Pure, fail-closed selection for bundled neural speech.
 *
 * Caregiver recordings are handled before this policy by AudioDispatcher.
 * Android TTS remains the only fallback for custom or mismatched text.
 * The exact speech form is checked against the manifest by NeuralAudioResolver.
 */
object NeuralPlaybackPolicy {
    fun forCard(item: WordItem, dialect: ArabicDialect): String? {
        if (item.semanticKey.isNullOrBlank() || item.categoryId.startsWith("custom_")) return null
        if (item.customTextFields and (CardDetails.SPEECH or CardDetails.LABEL) != 0) return null
        if (item.neuralDialect != null && item.neuralDialect != dialect) return null

        // The persisted source is used only to verify this projection, never
        // as an input to the neural normalizer's legacy pronunciation channel.
        val original = item.forEditing()
        val legacy = DialectSpeechCatalog.resolve(original, dialect)
        if (legacy.source == SpeechSource.USER) return null
        if (item.displayText != legacy.displayText || item.speechText != legacy.speechText) return null
        val clean = DialectSpeechCatalog.resolveForNeural(original, dialect)
        if (clean.source == SpeechSource.USER || clean.speechText.isBlank()) return null
        if (item.neuralSpeechText != null && item.neuralSpeechText != clean.speechText) return null
        return clean.speechText
    }
}
