package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** One canonical projection, clean speech, no additional neural dialect hacks. */
class NeuralSpeechNormalizerTest {
    private val words by lazy { WordSeed.itemEntities().map { it.toDomain() } }

    @Test fun everyVoiceExportMatchesTheActualRuntimeForm() {
        for (dialect in ArabicDialect.entries) for (word in words) {
            assertEquals(dialect.name + "/" + word.semanticKey,
                DialectSpeechCatalog.resolve(word, dialect),
                DialectSpeechCatalog.resolveForHabibi(word, dialect))
            assertEquals(DialectSpeechCatalog.resolveForHabibi(word, dialect).speechText,
                DialectCatalog.localize(word, dialect).ttsPrompt)
        }
    }

    @Test fun libyanWordsHaveNormalOrthography() {
        val cases = mapOf(
            "request.water" to "نبي مية",
            "request.medicine" to "نبي دوايا",
            "food.water" to "مية",
            "home.water_bottle" to "شيشة مية",
            "action.drink" to "نشرب",
            "action.walk" to "نمشي",
            "action.close" to "نسكر",
        )
        for ((key, expected) in cases) {
            val seed = words.single { it.semanticKey == key }
            val form = DialectSpeechCatalog.resolve(seed, ArabicDialect.LIBYAN)
            assertEquals(key, expected, form.speechText)
        }
    }

    @Test fun personalSpeechIsNeverChanged() {
        val seed = words.single { it.semanticKey == "request.water" }
        val personal = seed.copy(ttsPrompt = "عبارتي الخاصة",
            customTextFields = CardDetails.SPEECH)
        assertEquals(personal.ttsPrompt,
            DialectSpeechCatalog.resolve(personal, ArabicDialect.LIBYAN).speechText)
        assertNull(DialectCatalog.localize(personal, ArabicDialect.LIBYAN).neuralSpeechText)
    }

    @Test fun switchingDialectsCannotModifySavedRoomRows() {
        val source = words.single { it.semanticKey == "request.water" }
        for (dialect in ArabicDialect.entries) {
            assertEquals(source, DialectCatalog.localize(source, dialect).forEditing())
        }
    }
}
