package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordImageCatalogTest {
    private fun card(category: String, label: String): WordItem = WordItem(
        id = 88L, categoryId = category, subgroup = "", displayText = label,
        ttsPrompt = label, phonemicCue = "", sentencePrompt = null,
        isFavorite = false, audioPath = null, photoPath = null, phone = "", emoji = "",
        semanticKey = WordImageCatalog.keyForSeed(category, label),
    )

    @Test fun localizedCatsAlwaysKeepTheirImageKey() {
        val original = card("animals", "قطوسة")
        assertEquals("word_cat", original.semanticKey)
        for (dialect in ArabicDialect.entries) {
            assertEquals("word_cat", WordImageCatalog.imageFor(DialectCatalog.localize(original, dialect)))
        }
    }

    @Test fun localizedSharedVocabularyKeepsItsPhotos() {
        for ((category, text, key) in listOf(
            Triple("food", "خبزة", "word_bread"),
            Triple("body", "خشم", "word_nose"),
            Triple("actions", "ندوي", "word_speak"),
            Triple("vegetables", "قرعة", "word_pumpkin"),
            Triple("fruits", "دلاع", "word_watermelon"),
        )) for (dialect in ArabicDialect.entries) {
            assertEquals(key, WordImageCatalog.imageFor(DialectCatalog.localize(card(category, text), dialect)))
        }
    }

    @Test fun regionalCuisineIsIsolated() {
        assertNull(WordImageCatalog.keyForSeed(
            "libyan", "كسكسي بالحوت", RegionalDishesCatalog.subgroupFor(ArabicDialect.TUNISIAN),
        ))
        assertEquals("word_couscous_fish", WordImageCatalog.keyForSeed("libyan", "كسكسي بالحوت"))
    }

    @Test fun customAndFamilyCardsDoNotBorrowBuiltInPictures() {
        assertNull(WordImageCatalog.keyForSeed("custom_test", "قطوسة"))
        assertNull(WordImageCatalog.keyForSeed("family", "قطوسة"))
    }
}
