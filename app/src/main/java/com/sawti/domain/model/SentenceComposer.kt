package com.sawti.domain.model

/** A strip stores a small immutable snapshot, so later changes to a category don't erase its sentence. */
data class SentencePiece(
    val sourceId: Long,
    val text: String,
    val spokenText: String,
    val photoPath: String?,
    val emoji: String,
)

object SentenceComposer {
    const val MAX_PIECES = 24

    fun piece(word: WordItem) = SentencePiece(
        sourceId = word.id,
        text = word.displayText,
        spokenText = word.ttsPrompt.ifBlank { word.displayText }.trim(),
        photoPath = word.photoPath,
        emoji = word.emoji,
    )

    fun add(current: List<SentencePiece>, word: WordItem): List<SentencePiece> =
        if (current.size >= MAX_PIECES) current else current + piece(word)

    /** A single TTS request preserves sentence context and natural engine pacing. */
    fun spokenSentence(parts: List<SentencePiece>): String =
        parts.map { it.spokenText.trim().trimEnd('،', '،', '.', '!', '؟') }
            .filter { it.isNotBlank() }
            .joinToString("، ")
}
