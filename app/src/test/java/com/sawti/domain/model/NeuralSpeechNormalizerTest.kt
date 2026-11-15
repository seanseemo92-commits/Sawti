package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Neural export and Android fallback share ONE speech form. */
class NeuralSpeechNormalizerTest {
    private fun seed(key: String) =
        WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun noSeparateNeuralOrLegacyPronunciationPathRemains() {
        for (dialect in ArabicDialect.entries) for (source in WordSeed.itemEntities().map { it.toDomain() }) {
            assertEquals(dialect.name + "/" + source.semanticKey,
                DialectSpeechCatalog.resolve(source, dialect),
                DialectSpeechCatalog.resolveForNeural(source, dialect))
        }
    }

    @Test fun representativeLibyanWordsHaveNaturalVowels() {
        val words = mapOf(
            "request.water" to "نِبِّي مَيَّة",
            "request.rest" to "نِبِّي نِرْتَاح",
            "request.medicine" to "نِبِّي دْوَايَا",
            "food.eggs" to "دَحِي", "food.water" to "مَيَّة",
            "home.window" to "رُوشِن",
            "vegetable.pepper" to "فِلْفِل",
            "body.nose" to "خَشِم",
            "body.finger" to "صْبَع",
            "action.drink" to "نِشْرَب",
            "action.walk" to "نِمْشِي",
            "action.wash" to "نِغْسِل",
            "action.hear" to "نِسْمَع",
            "action.close" to "نِسَكِّر",
        )
        for ((key, speech) in words) {
            assertEquals(key, speech, DialectSpeechCatalog.resolve(seed(key), ArabicDialect.LIBYAN).speechText)
        }
    }

    @Test fun caregiverTextIsNeverAutomaticallyRewritten() {
        val original = seed("request.water").copy(ttsPrompt = "نَبِّيْ إِمِّيَّهْ",
            customTextFields = CardDetails.SPEECH)
        assertEquals(original.ttsPrompt,
            DialectSpeechCatalog.resolve(original, ArabicDialect.LIBYAN).speechText)
        assertNull(DialectCatalog.localize(original, ArabicDialect.LIBYAN).neuralSpeechText)
    }

    @Test fun canonicalSavedRoomCardNeverChangesDuringDialectSwitches() {
        val original = seed("request.water")
        for (dialect in ArabicDialect.entries) {
            val view = DialectCatalog.localize(original, dialect)
            assertEquals(original, view.forEditing())
        }
    }
}
