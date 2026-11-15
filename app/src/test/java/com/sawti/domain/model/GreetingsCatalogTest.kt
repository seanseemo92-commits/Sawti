package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.presentation.common.categoryDrawableName
import com.sawti.presentation.theme.categoryBannerColor
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class GreetingsCatalogTest {
    @Test fun everyGreetingHasAStableUniqueConceptAndNineExplicitForms() {
        val words = WordSeed.itemEntities().filter { it.categoryId == GreetingsCatalog.CATEGORY_ID }
        val keys = GreetingsCatalog.greetings.map { it.key }
        assertEquals(14, keys.size)
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(keys, words.map { it.semanticKey })
        assertEquals(keys.toSet(), GreetingsCatalog.dialectForms.keys)
        assertEquals(8, GreetingsCatalog.dialectForms.values.first().size)
        for (entry in GreetingsCatalog.dialectForms.values) {
            assertEquals(ArabicDialect.entries.filterNot { it == ArabicDialect.STANDARD }.toSet(), entry.keys)
            assertTrue(entry.values.all { !it.displayText.isBlank() && !it.speechText.isNullOrBlank() })
        }
    }

    @Test fun localizationChangesDisplayAndSpeechWithoutChangingStableIdentity() {
        val words = WordSeed.itemEntities().filter { it.categoryId == GreetingsCatalog.CATEGORY_ID }
        words.forEach { entity ->
            val original = entity.toDomain()
            assertEquals(original, DialectCatalog.localize(original, ArabicDialect.STANDARD).forEditing())
            ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }.forEach { dialect ->
                val expected = GreetingsCatalog.dialectForms.getValue(requireNotNull(original.semanticKey)).getValue(dialect)
                val actual = DialectCatalog.localize(original, dialect)
                assertEquals(expected.displayText, actual.displayText)
                assertEquals(original, actual.forEditing())
                assertTrue("Natural speech can be unvoweled but cannot be empty",
                    actual.ttsPrompt.isNotBlank())
                assertEquals(DialectSpeechCatalog.resolve(original, dialect).speechText, actual.ttsPrompt)
                assertEquals(original.id, actual.id)
                assertEquals(original.semanticKey, actual.semanticKey)
            }
        }
    }

    @Test fun greetingCardUsesDedicatedPhotoAndCoralAccent() {
        assertEquals("img_cat_greetings", categoryDrawableName("greetings"))
        assertEquals(com.sawti.presentation.theme.GreetingsCoral, categoryBannerColor("greetings", Color.Black))
        assertEquals("عائلتي", WordSeed.categories.first().title)
        assertEquals("التحيات", WordSeed.categories.last().title)
    }
}


