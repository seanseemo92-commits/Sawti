package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceComposerTest {
    private fun word(id: Long, text: String, spoken: String = text) = WordItem(
        id = id, categoryId = "daily", subgroup = "", displayText = text,
        ttsPrompt = spoken, phonemicCue = "", sentencePrompt = null,
        audioPath = null, photoPath = null, phone = "", emoji = "🍵",
    )

    @Test fun concatenatesSpokenPromptsWithPunctuation() {
        val strip = listOf(SentenceComposer.piece(word(1,"نبي", "نَبِّيْ")),
            SentenceComposer.piece(word(2,"شاهي","شَاهِي")))
        assertEquals("نَبِّيْ، شَاهِي", SentenceComposer.spokenSentence(strip))
    }

    @Test fun cappedAndSupportsRepeatedWords() {
        val initial = (1..24).map { SentenceComposer.piece(word(it.toLong(), "كلمة")) }
        assertEquals(24, SentenceComposer.add(initial, word(30, "زيادة")).size)
        assertEquals(2, SentenceComposer.add(listOf(SentenceComposer.piece(word(1, "نبي"))), word(1, "نبي")).size)
        assertTrue(SentenceComposer.spokenSentence(emptyList()).isEmpty())
    }
    @Test fun spokenPiecesKeepOrderAndIndexesWithoutEngineWordTimings() {
        val strip = listOf(
            SentenceComposer.piece(word(1, "نبي", "نَبِّيْ،")),
            SentenceComposer.piece(word(2, "مية", "مِيَّهْ")),
            SentenceComposer.piece(word(3, "توا", "تَوَّا")),
        )
        val pieces = SentenceComposer.spokenPieces(strip)
        assertEquals(3, pieces.size)
        assertEquals(listOf(0, 1, 2), pieces.map { it.pieceIndex })
        assertEquals(listOf(1L, 2L, 3L), pieces.map { it.sourceId })
        assertEquals(listOf("نَبِّيْ", "مِيَّهْ", "تَوَّا"), pieces.map { it.speech })
        assertEquals("نَبِّيْ، مِيَّهْ، تَوَّا", SentenceComposer.spokenSentence(strip))
    }

    @Test fun blankPartsStaySkippedWithoutShiftingChipIndexes() {
        val one = SentenceComposer.piece(word(1, "سلام"))
        val strip = listOf(one.copy(spokenText = " "), one)
        val spoken = SentenceComposer.spokenPieces(strip)
        assertEquals(1, spoken.size)
        assertEquals(1, spoken.single().pieceIndex)
        assertEquals(1L, spoken.single().sourceId)
        assertEquals("سلام", spoken.single().speech)
    }
}
