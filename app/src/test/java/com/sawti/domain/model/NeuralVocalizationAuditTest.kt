package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Habibi acoustic validation requires native listening; no forced vowel rules. */
class NeuralVocalizationAuditTest {
    private val cards by lazy { WordSeed.itemEntities().map { it.toDomain() } }
    private fun form(key: String, dialect: ArabicDialect): SpeechForm =
        DialectSpeechCatalog.resolve(cards.single { it.semanticKey == key }, dialect)

    @Test fun everyBuiltInCardUsesUnvowelizedArabicOrthography() {
        for (dialect in ArabicDialect.entries) for (card in cards) {
            val speech = DialectSpeechCatalog.resolve(card, dialect)
            assertFalse(dialect.name + "/" + card.semanticKey,
                HabibiSpeechText.hasForbiddenMarksOrNumbers(speech.speechText))
        }
    }

    @Test fun greetingsAreAvailableAcrossAllNineDialects() {
        assertEquals(14, GreetingsCatalog.greetings.size)
        for (dialect in ArabicDialect.entries) for (item in GreetingsCatalog.greetings) {
            val result = form(item.key, dialect)
            assertTrue(result.speechText.isNotBlank())
            assertEquals(result.displayText, result.speechText)
        }
    }

    @Test fun representativeLibyanWordsUseTheirOrdinaryWrittenForms() {
        val cases = mapOf(
            "animal.cow" to "بقرة",
            "animal.rooster" to "ديك",
            "animal.rabbit" to "أرنب",
            "action.sit" to "نقعمز",
            "action.sleep" to "نرقد",
            "request.toilet" to "نبي الحمام",
        )
        for ((key, expected) in cases) {
            assertEquals(key, expected, form(key, ArabicDialect.LIBYAN).speechText)
        }
    }

    @Test fun userVoiceAndTextStillHavePriority() {
        val original = cards.single { it.semanticKey == "request.water" }
        val custom = original.copy(ttsPrompt = "قراءتي الخاصة",
            audioPath = "/private/voice.m4a", customTextFields = CardDetails.SPEECH)
        val localized = DialectCatalog.localize(custom, ArabicDialect.LIBYAN)
        assertEquals(custom.ttsPrompt, localized.ttsPrompt)
        assertEquals(custom.audioPath, localized.audioPath)
        assertNull(localized.neuralSpeechText)
        assertEquals(custom, localized.forEditing())
    }
}
