package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

class DialectCatalogTest {
    private fun item(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }
        .toDomain().copy(id = 8, photoPath = "/personal/photo.jpg", audioPath = "/personal/voice.m4a")

    @Test fun frequentRequestUsesIdentityAndFushaSource() {
        val original = item("request.water")
        assertEquals("أريد ماءً", original.displayText)
        assertEquals("أُرِيدُ مَاءً", original.ttsPrompt)
        assertEquals(original, DialectCatalog.localize(original, ArabicDialect.STANDARD))
        assertEquals("نبي مية", DialectCatalog.localize(original, ArabicDialect.LIBYAN).displayText)
        assertEquals("بدي مي", DialectCatalog.localize(original, ArabicDialect.SYRIAN).displayText)
        assertEquals("بغيت الما", DialectCatalog.localize(original, ArabicDialect.MOROCCAN).displayText)
    }

    @Test fun lookupUsesIdentityRatherThanSavedCaption() {
        val original = item("food.eggs").copy(displayText = "عنوان مختلف بلا علم تخصيص")
        assertEquals("دحي", DialectCatalog.localize(original, ArabicDialect.LIBYAN).displayText)
        val personal = original.copy(customTextFields = CardDetails.LABEL)
        val view = DialectCatalog.localize(personal, ArabicDialect.LIBYAN)
        assertEquals(personal.displayText, view.displayText)
        assertEquals(personal.ttsPrompt, view.ttsPrompt)
    }

    @Test fun customOrUnknownCardsAreNeverGuessedFromMatchingText() {
        val original = item("food.eggs").copy(semanticKey = null)
        val unknown = original.copy(semanticKey = "unknown.concept")
        val custom = item("food.eggs").copy(categoryId = "custom_example")
        for (dialect in ArabicDialect.entries) {
            assertEquals(original, DialectCatalog.localize(original, dialect))
            assertEquals(unknown, DialectCatalog.localize(unknown, dialect))
            assertEquals(custom, DialectCatalog.localize(custom, dialect))
        }
    }

    @Test fun projectionRetainsIdentityAndUserMediaForEveryConceptAndDialect() {
        for (entity in WordSeed.itemEntities()) {
            val source = entity.toDomain().copy(id = 99, photoPath = "/photo", audioPath = "/voice", phone = "123")
            for (dialect in ArabicDialect.entries) {
                val view = DialectCatalog.localize(source, dialect)
                assertEquals(source, view.forEditing())
                assertEquals(source.semanticKey, view.semanticKey)
                assertEquals(source.id, view.id)
                assertEquals(source.photoPath, view.photoPath)
                assertEquals(source.audioPath, view.audioPath)
                assertEquals(source.phone, view.phone)
                assertEquals(WordImageCatalog.imageFor(source), WordImageCatalog.imageFor(view))
            }
        }
    }

    @Test fun returningFromAnotherDialectRestoresCanonicalSource() {
        val source = item("food.bread")
        val egyptian = DialectCatalog.localize(source, ArabicDialect.EGYPTIAN)
        assertEquals("عيش", egyptian.displayText)
        assertEquals(source, DialectCatalog.localize(egyptian, ArabicDialect.STANDARD))
        assertEquals(source, egyptian.forEditing())
    }

    @Test fun sentenceHintsRemainAvailableInDefaultFusha() {
        val source = item("request.tea")
        assertFalse(source.sentencePrompt.isNullOrBlank())
        assertEquals(source.sentencePrompt, DialectCatalog.localize(source, ArabicDialect.STANDARD).sentencePrompt)
    }

    @Test fun relationshipAndPersonalNameAreDistinct() {
        val father = item("family.father")
        assertTrue(FamilyRelationships.isBuiltIn(father))
        assertFalse(FamilyRelationships.isBuiltIn(father.copy(semanticKey = null)))
        assertFalse(FamilyRelationships.isBuiltIn(father.copy(subgroup = "الوالد")))
    }
}
