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
}
