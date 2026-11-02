package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import org.junit.Assert.*
import org.junit.Test

class WordSeedFoundationTest {
    @Test fun identitiesAndParentsMatchTheFrozenContentContract() {
        val expected = requireNotNull(javaClass.getResourceAsStream("/vocabulary-identities.tsv"))
            .bufferedReader().use { it.readLines().filter(String::isNotBlank).toSet() }
        val words = WordSeed.itemEntities()
        val actual = words.map { "${it.semanticKey}\t${it.categoryId}" }
        assertEquals(365, words.size)
        assertEquals(words.size, actual.distinct().size)
        assertEquals(words.size, words.map { it.semanticKey }.distinct().size)
        assertEquals(expected, actual.toSet())
        val format = Regex("[a-z]+(?:[._][a-z0-9]+)+")
        assertTrue(words.all { it.semanticKey?.matches(format) == true })
        assertTrue(words.all { it.displayText.isNotBlank() && it.ttsPrompt.isNotBlank() })
        assertEquals(12, WordSeed.categories.size)
        assertEquals(WordSeed.categories.size, WordSeed.categories.map { it.id }.distinct().size)
    }

    @Test fun commonSourceWordsAreFusha() {
        val expected = mapOf("food.water" to "ماء", "food.bread" to "خبز", "home.pillow" to "وسادة",
            "home.window" to "نافذة", "body.nose" to "أنف", "clothing.socks" to "جوارب",
            "action.speak" to "أتكلم", "fruit.watermelon" to "بطيخ", "request.cold" to "أشعر بالبرد")
        val byKey = WordSeed.itemEntities().associateBy { it.semanticKey }
        expected.forEach { (key, text) -> assertEquals(text, byKey.getValue(key).displayText) }
    }

    @Test fun allRegionalVariantsHaveExactlyOneCanonicalSource() {
        val byKey = WordSeed.itemEntities().associateBy { it.semanticKey }
        for (dialect in ArabicDialect.entries) {
            val entries = RegionalDishesCatalog.forDialect(dialect).dishes
            assertEquals(entries.size, entries.map { it.semanticKey }.distinct().size)
            for (entry in entries) {
                val source = byKey.getValue(entry.semanticKey)
                assertEquals(CategoryIds.CUISINE, source.categoryId)
                assertTrue(RegionalDishesCatalog.visibleForDialect(source.subgroup, dialect))
            }
        }
    }
}
