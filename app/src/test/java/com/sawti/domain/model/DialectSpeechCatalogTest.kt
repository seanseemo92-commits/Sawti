package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers every bundled shared card in each selectable dialect (including Standard Arabic).
 * Country-specific dishes and user-created family/custom cards are intentionally excluded.
 */
class DialectSpeechCatalogTest {
    @Test fun everyBundledSharedCardHasVocalizedSpeechInEveryDialect() {
        val defaults = WordSeed.itemEntities()
            .filter { it.categoryId != RegionalDishesCatalog.CATEGORY_ID &&
                it.categoryId != CategoryIds.FAMILY }
        assertTrue(defaults.size >= 175)
        var checked = 0
        for (entity in defaults) {
            val original = entity.toDomain()
            assertTrue("Missing stable key: " + original.displayText, original.semanticKey != null)
            for (dialect in ArabicDialect.entries) {
                val translated = DialectCatalog.localize(original, dialect)
                val tts = translated.speechText
                assertTrue("Empty TTS for " + original.displayText + "/" + dialect, tts.isNotBlank())
                assertTrue("Missing vowel or final pause for " + original.displayText +
                    "/" + dialect + ": " + tts, DialectSpeechCatalog.isFullyMarked(tts))
                assertEquals("Image changed for " + original.displayText + "/" + dialect,
                    original.semanticKey, translated.semanticKey)
                assertEquals(original.id, translated.id)
                assertEquals(original.photoPath, translated.photoPath)
                assertEquals(original.audioPath, translated.audioPath)
                checked++
            }
        }
        assertEquals(defaults.size * ArabicDialect.entries.size, checked)
    }

    @Test fun highRiskColloquialSoundsAreExplicitlyPronounced() {
        fun speech(label: String, dialect: ArabicDialect) =
            DialectSpeechCatalog.spoken(label, label, label, dialect)
        assertEquals("دَحْيْ", speech("بيض", ArabicDialect.LIBYAN))
        assertEquals("عْظَامْ", speech("بيض", ArabicDialect.TUNISIAN))
        assertEquals("وْلَادْ الْجَاجْ", speech("بيض", ArabicDialect.ALGERIAN))
        assertEquals("شْخَاشِيرْ", speech("شخاشير", ArabicDialect.LIBYAN))
        assertEquals("كَلْسِيطَهْ", speech("شخاشير", ArabicDialect.TUNISIAN))
        assertEquals("مَيَهْ", DialectSpeechCatalog.fullyMark("مية"))
        assertEquals("تْشَايْ", speech("شاهي", ArabicDialect.IRAQI))
        assertEquals("نَبِّيْ إِمِّيَهْ", speech("نبي مية", ArabicDialect.LIBYAN))
    }

    @Test fun speechIsDifferentFromVisibleTextAndRetainsCustomMedia() {
        val card = WordItem(1, "food", "بيض", "دحي", "🥚").copy(
            semanticKey = "word_eggs", photoPath = "/photos/a.jpg", audioPath = "/audio/a.m4a")
        val next = DialectCatalog.localize(card, ArabicDialect.LIBYAN)
        assertEquals("دحي", next.displayText)
        assertEquals("دَحْيْ", next.speechText)
        assertEquals(card.photoPath, next.photoPath)
        assertEquals(card.audioPath, next.audioPath)
        assertFalse(next.speechText == next.displayText)
    }

    @Test fun fallbackNeverLeavesAnUnmarkedWord() {
        for (dialect in ArabicDialect.entries) {
            val result = DialectSpeechCatalog.spoken("مجهول", "كلمة عادية", "كلمة عادية", dialect)
            assertTrue(DialectSpeechCatalog.isFullyMarked(result))
        }
    }
}
