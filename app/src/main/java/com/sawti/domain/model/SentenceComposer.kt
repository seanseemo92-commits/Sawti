package com.sawti.domain.model

/** A strip stores a small immutable snapshot, so later changes to a category don't erase its sentence. */
data class SentencePiece(
    val sourceId: Long,
    val text: String,
    val spokenText: String,
    val photoPath: String?,
    val emoji: String,
    val audioPath: String? = null,
    val semanticKey: String? = null,
)

object SentenceComposer {
    const val MAX_PIECES = 24
    const val MAX_SPEECH_CHARACTERS = 3500

    fun piece(word: WordItem) = SentencePiece(
        sourceId = word.id,
        text = word.displayText,
        spokenText = word.speechText.ifBlank { word.ttsPrompt.ifBlank { word.displayText } }.trim(),
        photoPath = word.photoPath,
        emoji = word.emoji,
        audioPath = word.audioPath,
        semanticKey = word.semanticKey,
    )

    fun add(current: List<SentencePiece>, word: WordItem): List<SentencePiece> {
        if (current.size >= MAX_PIECES) return current
        val next = current + piece(word)
        return if (spokenSentence(next).length <= MAX_SPEECH_CHARACTERS) next else current
    }

    /** Plain spoken pieces: stable indexes, no engine-specific word-range or time offsets. */
    data class SpokenPiece(val pieceIndex: Int, val sourceId: Long, val speech: String, val audioPath: String? = null, val semanticKey: String? = null, val sourceSpeech: String = speech)

    private fun spokenWord(piece: SentencePiece): String =
        piece.spokenText.trim().trimEnd('،', '.', '!', '؟')

    fun spokenPieces(parts: List<SentencePiece>): List<SpokenPiece> =
        parts.mapIndexedNotNull { index, part ->
            val text = spokenWord(part)
            if (text.isBlank()) null
            else SpokenPiece(pieceIndex = index, sourceId = part.sourceId, speech = text, audioPath = part.audioPath, semanticKey = part.semanticKey, sourceSpeech = part.spokenText.trim())
        }

    /** A single TTS request preserves sentence context and natural engine pacing. */
    fun spokenSentence(parts: List<SentencePiece>): String =
        parts.map(::spokenWord)
            .filter { it.isNotBlank() }
            .joinToString("، ")
}
