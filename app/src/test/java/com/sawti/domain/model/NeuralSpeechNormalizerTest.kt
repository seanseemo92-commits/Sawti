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

    @Test fun everyNeuralSpeechEntryUsesSupportedCharacters() {
        val catalogs = NeuralLexicon162.allEntries()
        assertEquals(9, catalogs.size)
        assertEquals(2399, catalogs.values.sumOf { it.size })
        val unsupported = setOf('گ', 'ڨ', 'چ', 'پ', 'ڤ')
        for ((dialect, records) in catalogs) for ((key, entry) in records) {
            assertTrue("$dialect/$key has an unsupported speech character: ${entry.speechText}",
                entry.speechText.none { it in unsupported })
        }
        // The CSV explicitly retains six native Iraqi display spellings with چ.
        // Display labels are NOT sent to neural synthesis; their speech uses تْش.
        val iraqiCh = catalogs.getValue(ArabicDialect.IRAQI)
            .filterValues { 'چ' in it.displayText }.keys
        assertEquals(setOf("request.tea", "dish.iraqi.masgouf",
            "dish.iraqi.fish_mutabbaq", "food.fish", "food.tea",
            "action.speak"), iraqiCh)
        assertTrue(catalogs.filterKeys { it != ArabicDialect.IRAQI }.values
            .flatMap { it.values }.none { 'چ' in it.displayText })
    }

    @Test fun exactHotfixFormsAndFinalSukunSurvive() {
        val examples = listOf(
            Triple(ArabicDialect.LIBYAN, "action.sit", "نْقَعْمِزْ"),
            Triple(ArabicDialect.LIBYAN, "action.sleep", "نُرْقُدْ"),
            Triple(ArabicDialect.LIBYAN, "action.stand", "نُوقَفْ"),
            Triple(ArabicDialect.LIBYAN, "dish.osban", "عُصْبَانْ"),
            Triple(ArabicDialect.IRAQI, "fruit.mandarin", "لَالَنْكِي"),
            Triple(ArabicDialect.IRAQI, "body.mouth", "حَلِكْ"),
        )
        for ((dialect, key, expected) in examples) {
            assertEquals("$dialect/$key", expected,
                DialectSpeechCatalog.resolve(seed(key), dialect).speechText)
        }
        assertEquals("نْقَعْمِزْ", NeuralSpeechNormalizer.normalize("نْگَعْمِزْ"))
        assertEquals("رَجْ فْ بْ", NeuralSpeechNormalizer.normalize("رَچْ ڤْ پْ"))
    }
}
