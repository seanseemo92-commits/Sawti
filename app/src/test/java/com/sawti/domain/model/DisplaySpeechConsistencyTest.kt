package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** v1.6.2 display and neural audio are validated against the authoritative table. */
class DisplaySpeechConsistencyTest {
    private val seeds by lazy {
        WordSeed.itemEntities().map { it.toDomain() }
            .associateBy { requireNotNull(it.semanticKey) }
    }

    @Test fun theEightySixAuditedLabelsHaveTheirExactExpectedVisibleForms() {
        // All built-in displays are now sourced from the same v1.6.2 table.
        var verified = 0
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            assertEquals("$"+"dialect/$"+"key display", row.displayText,
                DialectSpeechCatalog.resolve(seed, dialect).displayText)
            verified++
        }
        assertEquals(2399, verified)
    }

    @Test fun LibyanWrittenCorrectionsHaveTheNaturalNeuralSpeechForms() {
        for (key in listOf("family.son_wife", "dish.mbakbaka", "vegetable.peas",
            "dish.zmeeta", "dish.sfinz", "clothing.blouse",
            "request.toilet", "dish.mbattan", "home.water_bottle")) {
            val row = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            val view = DialectSpeechCatalog.resolve(requireNotNull(seeds[key]), ArabicDialect.LIBYAN)
            assertEquals(key, row.displayText, view.displayText)
            assertEquals(key, row.speechText, view.speechText)
        }
    }

    @Test fun reviewedOtherDialectDishesHaveMatchingRuntimeSpeechAndSource() {
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            if (!key.startsWith("dish.")) continue
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            val view = DialectCatalog.localize(seed, dialect)
            assertEquals("$"+"dialect/$"+"key display", row.displayText, view.displayText)
            assertEquals("$"+"dialect/$"+"key speech", row.speechText, view.ttsPrompt)
            assertEquals("$"+"dialect/$"+"key neural", row.speechText, view.neuralSpeechText)
        }
    }

    @Test fun allBuiltInCardsAvoidUnsupportedTtsConsonantGlyphs() {
        val allowedG = setOf(ArabicDialect.LIBYAN, ArabicDialect.SAUDI,
            ArabicDialect.IRAQI, ArabicDialect.TUNISIAN,
            ArabicDialect.MOROCCAN, ArabicDialect.ALGERIAN)
        var countG = 0
        for (dialect in ArabicDialect.entries) for ((key, _) in seeds) {
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            assertFalse("$"+"dialect/$"+"key visible g", row.displayText.any { it == 'گ' || it == 'ڨ' })
            if (row.speechText.any { it == 'گ' || it == 'ڨ' }) {
                assertTrue("$"+"dialect/$"+"key unexpected g", dialect in allowedG)
                countG++
            }
            assertFalse("$"+"dialect/$"+"key invalid shadda+sukun", "ّْ" in row.speechText)
        }
        assertTrue("Intentional g consonants must not be stripped", countG >= 79)
    }

    @Test fun allBuiltInCardLabelsMatchSpeechExceptDocumentedPhoneticSpellings() {
        // Spoken forms may differ deliberately from written Arabic (e.g. Iraqi چ -> تْش).
        var coverage = 0
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            assertEquals("$"+"dialect/$"+"key display", row.displayText, form.displayText)
            assertEquals("$"+"dialect/$"+"key neural", row.speechText, form.speechText)
            coverage++
        }
        assertEquals(2399, coverage)
    }

    @Test fun phonemicCueMatchesItsSpokenTargetWord() {
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            assertEquals("$"+"dialect/$"+"key cue", PhonemicCue.derive(row.speechText), form.phonemicCue)
        }
    }

    @Test fun all199ConfirmedAuditItemsMatchExactDisplayAndSpeech() {
        assertEquals(199, Audit161Confirmed.corrections.size)
        for ((dialectKey, _) in Audit161Confirmed.corrections) {
            val (dialect, key) = dialectKey
            val row = requireNotNull(NeuralLexicon162.get(dialect, key)) {
                "Previously audited built-in missing in v1.6.2: $"+"dialect/$"+"key"
            }
            val form = DialectSpeechCatalog.resolve(requireNotNull(seeds[key]), dialect)
            assertEquals(row.displayText, form.displayText)
            assertEquals(row.speechText, form.speechText)
        }
    }

    @Test fun builtinCuesAreNeverBlankOrDerivedFromStaleSpeech() {
        var finalSukun = 0
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val row = NeuralLexicon162.get(dialect, key) ?: continue
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            assertTrue("$"+"dialect/$"+"key blank cue", form.phonemicCue.isNotBlank())
            assertEquals(PhonemicCue.derive(row.speechText), form.phonemicCue)
            if (form.speechText.endsWith('ْ')) finalSukun++
        }
        assertTrue("Final sukun was removed unexpectedly", finalSukun > 1000)
    }

    @Test fun personalLabelAndSpeechOverridesAreNeverReplaced() {
        val sample = requireNotNull(seeds["family.son"]).copy(
            displayText = "اسم مخصص", ttsPrompt = "نُطْقِي الْخَاصّ",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH,
        )
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(sample, dialect)
            assertEquals(sample.displayText, form.displayText)
            assertEquals(sample.ttsPrompt, form.speechText)
            assertNull(DialectSpeechCatalog.neuralTextFor(sample, dialect))
            assertFalse(DialectSpeechCatalog.isNeuralOnly(sample))
        }
    }
}
