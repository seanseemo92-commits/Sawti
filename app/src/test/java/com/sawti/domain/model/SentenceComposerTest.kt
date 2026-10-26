package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceComposerTest {
    private fun word(id: Long, text: String, spoken: String = text) = WordItem(
        id = id, categoryId = "daily", subgroup = "", displayText = text,
        ttsPrompt = spoken, phonemicCue = "", sentencePrompt = null,
        isFavorite = false, audioPath = null, photoPath = null, phone = "", emoji = "🍵",
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
    @Test fun speechRangeOffsetsFollowExactNormalizedArabicPhrase() {
        val strip = listOf(
            SentenceComposer.piece(word(1, "نبي", "نَبِّيْ،")),
            SentenceComposer.piece(word(2, "مية", "مِيَّهْ")),
            SentenceComposer.piece(word(3, "توا", "تَوَّا")),
        )
        val whole = SentenceComposer.spokenSentence(strip)
        val segments = SentenceComposer.spokenSegments(strip)
        assertEquals(3, segments.size)
        assertEquals(0, segments[0].start)
        assertEquals(1, segments[1].pieceIndex)
        assertEquals(2, segments[2].pieceIndex)
        assertEquals("نَبِّيْ، مِيَّهْ، تَوَّا", whole)
        assertEquals("نَبِّيْ", whole.substring(segments[0].start, segments[0].endExclusive))
        assertEquals("مِيَّهْ", whole.substring(segments[1].start, segments[1].endExclusive))
        assertEquals("تَوَّا", whole.substring(segments[2].start, segments[2].endExclusive))
    }

    @Test fun blankSentencePartsDoNotShiftSpokenWordIndexes() {
        val original = SentenceComposer.piece(word(1, "سلام"))
        val blank = original.copy(spokenText = " ")
        val strip = listOf(blank, original)
        assertEquals("سلام", SentenceComposer.spokenSentence(strip))
        assertEquals(1, SentenceComposer.spokenSegments(strip).single().pieceIndex)
        assertEquals(0, SentenceComposer.spokenSegments(strip).single().start)
    }

}
