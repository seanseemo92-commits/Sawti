package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

class TransportCatalogTest {
    private val cards = WordSeed.itemEntities().filter { it.categoryId == CategoryIds.TRANSPORT }.map { it.toDomain() }

    @Test fun allTwelveConceptsHaveNineExplicitSpeechFormsAndStablePhotos() {
        val expected = setOf("car", "taxi", "bus", "minibus", "bicycle", "motorcycle", "train",
            "airplane", "helicopter", "ship", "boat", "truck").map { "transport.$it" }.toSet()
        assertEquals(expected, cards.map { it.semanticKey }.toSet())
        assertEquals(12, cards.size)
        assertEquals(CategoryIds.TRANSPORT, WordSeed.categories[WordSeed.categories.lastIndex - 1].id)
        for (vehicle in TransportCatalog.vehicles) {
            assertEquals(ArabicDialect.entries.toSet(), vehicle.forms.keys)
            val card = cards.single { it.semanticKey == vehicle.semanticKey }
            val photo = "word_" + vehicle.semanticKey.replace('.', '_')
            for (dialect in ArabicDialect.entries) {
                val written = vehicle.forms.getValue(dialect)
                assertTrue(written.displayText.isNotBlank())
                assertTrue(requireNotNull(written.speechText).any { it in '\u064B'..'\u0652' })
                val speech = DialectSpeechCatalog.resolve(card, dialect)
                val localized = DialectCatalog.localize(card, dialect)
                assertEquals(written.displayText, localized.displayText)
                assertEquals(card, localized.forEditing())
                assertEquals(speech.speechText, localized.ttsPrompt)
                assertNotEquals(SpeechSource.UNVOCALIZED_FALLBACK, speech.source)
                assertTrue(speech.phonemicCue.isNotBlank())
                assertTrue(speech.needsNativeReview && speech.needsTtsListening)
                assertEquals(card.id, localized.id)
                assertEquals(card.semanticKey, localized.semanticKey)
                assertEquals(photo, WordImageCatalog.imageFor(localized))
                assertEquals(TransportCatalog.sentencePrompt(vehicle.semanticKey, dialect), localized.sentencePrompt)
            }
        }
    }

    @Test fun dialectChangesNeverReplaceCaregiverTextPhotosOrRecordings() {
        val edited = cards.first().copy(displayText = "سيارتي", ttsPrompt = "نطقي الخاص", phonemicCue = "تلميحي",
            sentencePrompt = "جملتي", photoPath = "/photos/car.jpg", audioPath = "/custom_audio/car.m4a",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH or CardDetails.CUE or CardDetails.SENTENCE)
        for (dialect in ArabicDialect.entries) {
            val localized = DialectCatalog.localize(edited, dialect)
            assertEquals(edited, localized.forEditing())
            assertEquals(edited.displayText, localized.displayText)
            assertEquals(edited.ttsPrompt, localized.ttsPrompt)
            assertEquals(edited.phonemicCue, localized.phonemicCue)
            assertEquals(edited.sentencePrompt, localized.sentencePrompt)
            assertEquals(edited.photoPath, localized.photoPath)
            assertEquals(edited.audioPath, localized.audioPath)
        }
    }

    @Test fun localizationCanSwitchRepeatedlyWithoutSavingProjectedText() {
        val source = cards.first()
        var projected = source
        for (dialect in ArabicDialect.entries.reversed()) {
            projected = DialectCatalog.localize(projected, dialect)
            assertEquals(TransportCatalog.vehicles.first().forms.getValue(dialect).displayText, projected.displayText)
            assertEquals(source, projected.forEditing())
        }
        assertEquals(source, projected.forEditing())
    }
}

