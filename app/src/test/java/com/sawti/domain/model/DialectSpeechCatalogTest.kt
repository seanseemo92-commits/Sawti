package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Linguistic and reversible-projection contracts, not a substitute for native listening. */
class DialectSpeechCatalogTest {
    private fun item(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun correctedLibyanRequestsAreExactAndConsistentBetweenAudioAndDisplay() {
        for (key in listOf("request.water", "request.tea", "request.rest",
            "request.medicine", "request.toilet", "request.cold", "food.water")) {
            val expected = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            val view = DialectCatalog.localize(item(key), ArabicDialect.LIBYAN)
            assertEquals(key + " label", expected.displayText, view.displayText)
            assertEquals(key + " pronunciation", expected.speechText, view.ttsPrompt)
            assertEquals(key + " neural", expected.speechText, view.neuralSpeechText)
        }
    }

    @Test fun locallyRecognizableWordsAndActionsRetainDialects() {
        for (key in listOf("food.eggs", "home.window", "vegetable.pepper",
            "clothing.blouse", "action.drink", "action.sleep", "action.sit",
            "action.walk", "action.wash", "action.speak", "action.hear",
            "action.open", "action.close", "action.laugh", "action.see",
            "action.dress", "action.pray")) {
            val row = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            val form = DialectSpeechCatalog.resolve(item(key), ArabicDialect.LIBYAN)
            assertEquals(key, row.displayText, form.displayText)
            assertEquals(key, row.speechText, form.speechText)
        }
        assertEquals("سردوك", DialectCatalog.localize(item("animal.rooster"),
            ArabicDialect.TUNISIAN).displayText)
    }

    @Test fun familyTermsAreRegionalNotClassicArabicInAllEightSpokenDialects() {
        for (dialect in ArabicDialect.entries.filter { it != ArabicDialect.STANDARD })
            for (key in listOf("family.husband_father", "family.brother_wife",
                "family.paternal_uncle_son")) {
                val row = requireNotNull(NeuralLexicon162.get(dialect, key))
                val localized = DialectCatalog.localize(item(key), dialect)
                assertEquals("$"+"dialect/$"+"key label", row.displayText, localized.displayText)
                assertEquals("$"+"dialect/$"+"key voice", row.speechText, localized.ttsPrompt)
            }
    }

    @Test fun userRecordingPhotosAndSourceSurviveSwitches() {
        val original = item("request.water").copy(audioPath = "/my/voice.m4a", photoPath = "/my/photo.jpg")
        for (dialect in ArabicDialect.entries) {
            val projected = DialectCatalog.localize(original, dialect)
            assertEquals(original, projected.forEditing())
            assertEquals(original.audioPath, projected.audioPath)
            assertEquals(original.photoPath, projected.photoPath)
            assertEquals(DialectSpeechCatalog.resolve(original, dialect),
                DialectSpeechCatalog.resolveForNeural(original, dialect))
        }
        for (flag in listOf(CardDetails.LABEL, CardDetails.SPEECH)) {
            val custom = original.copy(ttsPrompt = "صَوْتِي الشخصي", customTextFields = flag)
            val view = DialectCatalog.localize(custom, ArabicDialect.LIBYAN)
            assertEquals(custom.ttsPrompt, view.ttsPrompt)
            assertNull(view.neuralSpeechText)
        }
    }


    private val regional = ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }
    private val allSeeds by lazy { WordSeed.itemEntities().map { it.toDomain() } }

    @Test fun allNineDialectsAndFourteenNamespacesHaveNoWordFinalSukun() {
        val namespaces = setOf("family", "request", "dish", "routine", "vegetable",
            "fruit", "food", "home", "clothing", "body", "action", "animal",
            "transport", "greeting")
        assertEquals(namespaces, allSeeds.mapNotNull { it.semanticKey?.substringBefore('.') }.toSet())
        var total = 0
        var finalSukun = 0
        for (dialect in ArabicDialect.entries) for (seed in allSeeds) {
            val key = requireNotNull(seed.semanticKey)
            val expected = NeuralLexicon162.get(dialect, key) ?: continue
            total++
            val actual = DialectSpeechCatalog.resolveForNeural(seed, dialect).speechText
            assertEquals("$"+"dialect/$"+"key", expected.speechText, actual)
            assertFalse("$"+"dialect/$"+"key forbidden shadda+sukun", "ّْ" in actual)
            if (actual.endsWith('ْ')) finalSukun++
        }
        assertEquals(2399, total)
        assertTrue("Dialectal final sukun must be preserved", finalSukun > 1000)
    }

    @Test fun eightRegionalDialectsHaveNoClassicalDiphthongsOrConstructEndings() {
        for (dialect in regional) for (key in listOf("food.eggs", "food.olives",
            "food.oil", "fruit.banana", "fruit.peach", "routine.sleep",
            "home.fork", "body.eye", "home.water_bottle", "routine.evening_coffee",
            "fruit.coconut")) {
            val row = requireNotNull(NeuralLexicon162.get(dialect, key))
            assertEquals("$"+"dialect/$"+"key", row.speechText,
                DialectSpeechCatalog.resolve(item(key), dialect).speechText)
        }
    }

    @Test fun everyRegionalInLawAndCousinHasExplicitLocalizedSpeech() {
        val suffixes = listOf("husband_father", "husband_mother", "wife_father",
            "wife_mother", "husband_brother", "husband_sister", "wife_brother",
            "wife_sister", "sister_husband", "daughter_husband", "brother_wife",
            "son_wife", "paternal_uncle_son", "paternal_aunt_son",
            "maternal_uncle_son", "maternal_aunt_son", "brother_son",
            "sister_son", "brother_daughter", "sister_daughter")
        for (dialect in regional) for (suffix in suffixes) {
            val key = "family.$suffix"
            val expected = requireNotNull(NeuralLexicon162.get(dialect, key))
            val speech = DialectSpeechCatalog.resolve(item(key), dialect)
            assertEquals("$"+"dialect/$"+"key label", expected.displayText, speech.displayText)
            assertEquals("$"+"dialect/$"+"key voice", expected.speechText, speech.speechText)
            assertEquals(SpeechSource.EXPLICIT_DIALECT, speech.source)
        }
    }

    @Test fun v161DirectSpeechAndEditedCaregiverLabelsRemainIndependent() {
        for ((dialect, key) in listOf(
            ArabicDialect.SYRIAN to "action.close",
            ArabicDialect.IRAQI to "action.sit",
            ArabicDialect.MOROCCAN to "request.headache",
            ArabicDialect.ALGERIAN to "request.toilet")) {
            val row = requireNotNull(NeuralLexicon162.get(dialect, key))
            val form = DialectSpeechCatalog.resolve(item(key), dialect)
            assertEquals(row.displayText, form.displayText)
            assertEquals(row.speechText, form.speechText)
        }
        val seed = item("family.husband_father")
        val custom = seed.copy(displayText = "تسمية خاصة",
            ttsPrompt = "نطقي الخاص",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH)
        for (dialect in regional) {
            val result = DialectSpeechCatalog.resolve(custom, dialect)
            assertEquals("تسمية خاصة", result.displayText)
            assertEquals("نطقي الخاص", result.speechText)
            assertNull(DialectSpeechCatalog.neuralTextFor(custom, dialect))
            assertEquals(seed, DialectCatalog.localize(seed, dialect).forEditing())
        }
    }

    @Test fun previouslySavedStandardDishesUseCorrectedSpeechWithoutDestructiveMigration() {
        for (dish in RegionalDishesCatalog.forDialect(ArabicDialect.STANDARD).dishes) {
            val seed = item(dish.semanticKey)
            val legacy = seed.copy(ttsPrompt = "أَرُزٌّ بِاللَّحْمْ")
            val expected = requireNotNull(NeuralLexicon162.get(ArabicDialect.STANDARD,
                dish.semanticKey))
            val form = DialectSpeechCatalog.resolve(legacy, ArabicDialect.STANDARD)
            assertEquals(dish.semanticKey, expected.speechText, form.speechText)
            assertEquals(dish.semanticKey, legacy, DialectCatalog.localize(legacy,
                ArabicDialect.STANDARD).forEditing())
            val caregiver = legacy.copy(ttsPrompt = "صوتي الشخصي",
                customTextFields = CardDetails.SPEECH)
            assertEquals("صوتي الشخصي",
                DialectSpeechCatalog.resolve(caregiver, ArabicDialect.STANDARD).speechText)
        }
    }

    @Test fun writtenReviewDoesNotReplaceRequiredNativeListening() {
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(item("request.water"), dialect)
            assertTrue(dialect.name, form.needsNativeReview)
            assertTrue(dialect.name, form.needsTtsListening)
        }
    }
}
