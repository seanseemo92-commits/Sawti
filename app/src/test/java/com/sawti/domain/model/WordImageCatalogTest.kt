package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

class WordImageCatalogTest {
    private fun card(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun distinctConceptsShareAnAssetWithoutSharingIdentity() {
        val request = card("request.water")
        val water = card("food.water")
        assertNotEquals(request.semanticKey, water.semanticKey)
        assertEquals("word_water", WordImageCatalog.imageFor(request))
        assertEquals(WordImageCatalog.imageFor(request), WordImageCatalog.imageFor(water))
    }

    @Test fun changingCaptionOrSpeechNeverRetargetsImage() {
        val original = card("animal.cat")
        val renamed = original.copy(displayText = "ماء", ttsPrompt = "مَاء", customTextFields = 15)
        assertEquals("word_cat", WordImageCatalog.imageFor(renamed))
        for (dialect in ArabicDialect.entries)
            assertEquals("word_cat", WordImageCatalog.imageFor(DialectCatalog.localize(original, dialect)))
    }

    @Test fun matchingCaptionCannotBorrowAnUnrelatedImage() {
        val custom = card("animal.cat").copy(categoryId = "custom_example", semanticKey = null)
        assertNull(WordImageCatalog.imageFor(custom))
        assertNull(WordImageCatalog.imageFor(custom.copy(semanticKey = "word_cat")))
        assertNull(WordImageCatalog.imageFor(custom.copy(semanticKey = "unknown.concept")))
    }

    @Test fun regionalDishesNeverBorrowPhotosByTheirNames() {
        assertEquals("word_couscous_fish", WordImageCatalog.imageFor(card("dish.couscous_fish")))
        assertEquals("word_dish_tunisian_couscous_fish", WordImageCatalog.imageFor(card("dish.tunisian.couscous_fish")))
        assertNull(WordImageCatalog.imageFor(card("family.father")))
    }

    @Test fun everyRegionalDishKeepsItsDedicatedPhotoWhenTextOrDialectChanges() {
        val dishes = RegionalDishesCatalog.additionalDialects()
        assertEquals(120, dishes.size)
        val photos = dishes.map { (_, dish) ->
            val original = card(dish.semanticKey)
            val image = WordImageCatalog.imageFor(original)
            assertNotNull(dish.semanticKey, image)
            assertTrue(dish.semanticKey, image!!.startsWith("word_dish_"))
            val renamed = original.copy(displayText = "ماء", ttsPrompt = "ماء", customTextFields = 15)
            assertEquals(image, WordImageCatalog.imageFor(renamed))
            for (dialect in ArabicDialect.entries)
                assertEquals(image, WordImageCatalog.imageFor(DialectCatalog.localize(original, dialect)))
            image
        }
        assertEquals(120, photos.toSet().size)
    }

    @Test fun imageMapHasNoOrphanConceptReferences() {
        val keys = WordSeed.itemEntities().mapNotNull { it.semanticKey }.toSet()
        assertTrue(keys.containsAll(WordImageCatalog.configuredKeys()))
        assertEquals(347, WordImageCatalog.configuredKeys().size)
    }
}
