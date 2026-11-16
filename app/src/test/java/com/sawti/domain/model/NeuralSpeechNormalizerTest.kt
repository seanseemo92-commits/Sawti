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

        for (key in listOf("request.water", "request.rest", "request.medicine",
            "food.eggs", "food.water", "home.window", "vegetable.pepper",
            "body.nose", "body.finger", "action.drink", "action.walk",
            "action.wash", "action.hear", "action.close")) {
            val expected = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            assertEquals(key, expected.speechText,
                DialectSpeechCatalog.resolve(seed(key), ArabicDialect.LIBYAN).speechText)
        }
    }

    @Test fun auditedMeaningAndInflectionCasesAreExact() {

        val keys = listOf(
            ArabicDialect.SYRIAN to "family.husband",
            ArabicDialect.EGYPTIAN to "action.stand",
            ArabicDialect.IRAQI to "action.close",
            ArabicDialect.ALGERIAN to "dish.algerian.douara",
            ArabicDialect.STANDARD to "clothing.high_heels",
        )
        for ((dialect, key) in keys) {
            val expected = requireNotNull(NeuralLexicon162.get(dialect, key))
            assertEquals("$dialect/$key", expected.speechText,
                DialectSpeechCatalog.resolve(seed(key), dialect).speechText)
        }
        assertEquals("حِذَاءٌ بِكَعْبٍ عَالٍ", DialectSpeechCatalog.resolve(
            seed("clothing.high_heels"), ArabicDialect.STANDARD).speechText)
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
