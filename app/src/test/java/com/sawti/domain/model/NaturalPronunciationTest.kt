package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** No Microsoft-era phonetic rewrite or artificial sukun in Habibi speech. */
class NaturalPronunciationTest {
    private fun seed(key: String) =
        WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun cleanSpeechRemovesMarksWithoutChangingLetters() {
        val fatha = 0x064E.toChar()
        val sukun = 0x0652.toChar()
        assertEquals("بقرة", HabibiSpeechText.prepare("ب" + fatha + "ق" + sukun + "رة"))
        assertEquals("نبي الحمام", HabibiSpeechText.prepare("نبي  الحمام"))
    }

    @Test fun allNineDialectsKeepTheRoomSourceAndMediaIntact() {
        val source = seed("request.water").copy(audioPath = "/my/voice.m4a",
            photoPath = "/my/photo.jpg")
        for (dialect in ArabicDialect.entries) {
            val view = DialectCatalog.localize(source, dialect)
            assertEquals(source, view.forEditing())
            assertEquals(source.audioPath, view.audioPath)
            assertEquals(source.photoPath, view.photoPath)
            assertEquals(view.displayText, view.speechText)
        }
    }

    @Test fun caregiverEditsAreNotNormalizedByHabibi() {
        val value = seed("request.water").copy(
            ttsPrompt = "صوت خاص", customTextFields = CardDetails.SPEECH)
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolveForHabibi(value, dialect)
            assertEquals(value.ttsPrompt, form.speechText)
            assertNull(DialectSpeechCatalog.habibiTextFor(value, dialect))
        }
    }

    @Test fun distinctRegionalVocabularyIsNotLost() {
        assertEquals("بيض", DialectCatalog.localize(seed("food.eggs"),
            ArabicDialect.LIBYAN).displayText)
        assertEquals("عظم", DialectCatalog.localize(seed("food.eggs"),
            ArabicDialect.TUNISIAN).displayText)
        assertEquals("ديك", DialectCatalog.localize(seed("animal.rooster"),
            ArabicDialect.LIBYAN).displayText)
    }
}
