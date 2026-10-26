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
    const val MAX_SPEECH_CHARACTERS = 3500

    fun piece(word: WordItem) = SentencePiece(
        sourceId = word.id,
        text = word.displayText,
        spokenText = word.ttsPrompt.ifBlank { word.displayText }.trim(),
        photoPath = word.photoPath,
        emoji = word.emoji,
    )

    fun add(current: List<SentencePiece>, word: WordItem): List<SentencePiece> {
        if (current.size >= MAX_PIECES) return current
        val next = current + piece(word)
        return if (spokenSentence(next).length <= MAX_SPEECH_CHARACTERS) next else current
    }

    /** Offsets for TTS onRangeStart callbacks, using the exact same text as spokenSentence. */
    data class SpokenSegment(val pieceIndex: Int, val start: Int, val endExclusive: Int)

    private fun spokenWord(piece: SentencePiece): String =
        piece.spokenText.trim().trimEnd('،', '.', '!', '؟')

    fun spokenSegments(parts: List<SentencePiece>): List<SpokenSegment> {
        var cursor = 0
        return parts.mapIndexedNotNull { index, part ->
            val spoken = spokenWord(part)
            if (spoken.isBlank()) null else {
                val segment = SpokenSegment(index, cursor, cursor + spoken.length)
                cursor += spoken.length + 2 // the joining "، "
                segment
            }
        }
    }

    /** A single TTS request preserves sentence context and natural engine pacing. */
    fun spokenSentence(parts: List<SentencePiece>): String =
        parts.map(::spokenWord)
            .filter { it.isNotBlank() }
            .joinToString("، ")
}
