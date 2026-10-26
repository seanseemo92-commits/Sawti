package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionalDishesCatalogTest {
    @Test fun everyDialectHasCompleteAndDistinctCuisine() {
        assertEquals(9, ArabicDialect.entries.size)
        assertTrue(RegionalDishesCatalog.allDialectsCovered())
        for (dialect in ArabicDialect.entries) {
            val cuisine = RegionalDishesCatalog.forDialect(dialect)
            assertTrue("Missing title: $dialect", cuisine.title.isNotBlank())
            assertTrue("No food for $dialect", cuisine.dishes.size >= 5)
            assertEquals(cuisine.dishes.size, cuisine.dishes.map { it.displayText }.distinct().size)
            for (dish in cuisine.dishes) {
                assertTrue(dish.displayText.isNotBlank())
                assertTrue(dish.ttsPrompt.isNotBlank())
                assertTrue(dish.phonemicCue.isNotBlank())
                assertTrue(dish.sentencePrompt.isNotBlank())
                assertTrue(dish.emoji.isNotBlank() || !dish.photoPath.isNullOrBlank())
            }
        }
    }

    @Test fun titlesAndSubgroupsChangeWithoutReplacingExistingLibyanCards() {
        assertEquals("أكلات ليبية", RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).title)
        assertEquals("أكلات تونسية", RegionalDishesCatalog.forDialect(ArabicDialect.TUNISIAN).title)
        assertEquals("أكلات مصرية", RegionalDishesCatalog.forDialect(ArabicDialect.EGYPTIAN).title)
        assertEquals("أكلات سعودية", RegionalDishesCatalog.forDialect(ArabicDialect.SAUDI).title)
        assertEquals("أكلات عربية", RegionalDishesCatalog.forDialect(ArabicDialect.STANDARD).title)
        assertTrue(RegionalDishesCatalog.visibleForDialect("", ArabicDialect.LIBYAN))
        assertFalse(RegionalDishesCatalog.visibleForDialect("", ArabicDialect.EGYPTIAN))
        for (dialect in ArabicDialect.entries.filter { it != ArabicDialect.LIBYAN }) {
            val tag = RegionalDishesCatalog.subgroupFor(dialect)
            assertTrue(RegionalDishesCatalog.visibleForDialect(tag, dialect))
            assertFalse(RegionalDishesCatalog.visibleForDialect(tag, ArabicDialect.LIBYAN))
            assertFalse(RegionalDishesCatalog.visibleForDialect(tag, ArabicDialect.STANDARD.takeUnless { it == dialect } ?: ArabicDialect.EGYPTIAN))
        }
        assertEquals(40, RegionalDishesCatalog.additionalDialects().size)
    }

    @Test fun fiveUserRequestedLibyanDishesHaveExactSpeechAndCue() {
        val expected = mapOf(
            "مبطن" to ("إمْبَطَّنْ" to "مْبَـ..."),
            "حرايمي" to ("حَرَايْمِي" to "حَـ..."),
            "سفنز" to ("إِسْفَنْزْ" to "إِسْـ..."),
            "زميطة" to ("زَمِّيطَهْ" to "زَمْـ..."),
            "عيش باللبن" to ("عَيْشْ بِاللَّبَنْ" to "عَيْـ..."),
        )
        val dishes = RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).dishes
        assertEquals(expected.keys, dishes.map { it.displayText }.toSet())
        dishes.forEach {
            assertEquals(expected.getValue(it.displayText).first, it.ttsPrompt)
            assertEquals(expected.getValue(it.displayText).second, it.phonemicCue)
        }
        val saved = WordSeed.itemEntities().filter {
            it.categoryId == "libyan" && it.displayText in expected.keys
        }
        assertEquals(5, saved.size)
        assertTrue(saved.all { it.subgroup.isBlank() && !it.sentencePrompt.isNullOrBlank() })
        saved.forEach { dish ->
            assertEquals(expected.getValue(dish.displayText).first, dish.ttsPrompt)
            assertEquals(expected.getValue(dish.displayText).second, dish.phonemicCue)
        }
        assertEquals(16, WordSeed.VERSION)
    }
}
