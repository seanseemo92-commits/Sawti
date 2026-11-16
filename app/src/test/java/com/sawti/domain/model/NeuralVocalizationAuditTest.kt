package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Textual audit only. Naturalness/clinical quality needs real native-speaker listening. */
class NeuralVocalizationAuditTest {
    private val cards by lazy { WordSeed.itemEntities().map { it.toDomain() } }
    private fun form(key: String, dialect: ArabicDialect): SpeechForm =
        DialectSpeechCatalog.resolve(cards.single { it.semanticKey == key }, dialect)

    @Test fun noUnvocalizedBuiltinSpeechAmong2399RegionalAndStandardCards() {
        var count = 0
        for (dialect in ArabicDialect.entries) for (card in cards) {
            if (card.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                !RegionalDishesCatalog.visibleForDialect(card.subgroup, dialect)) continue
            val speech = DialectSpeechCatalog.resolve(card, dialect)
            val label = "$dialect/${card.semanticKey}"
            assertNotEquals(label, SpeechSource.UNVOCALIZED_FALLBACK, speech.source)
            assertTrue(label + " has empty speech", speech.speechText.isNotBlank())
            assertTrue(label + " has no explicit Arabic vowel", speech.speechText.any {
                it in '\u064b'..'\u0652'
            })
            count++
        }
        assertEquals(2399, count)
    }

    @Test fun all112RegionalGreetingPhrasesCarryVowels() {

        assertEquals(14, GreetingsCatalog.greetings.size)
        var count = 0
        for (dialect in ArabicDialect.entries.filterNot { it == ArabicDialect.STANDARD })
            for (greeting in GreetingsCatalog.greetings) {
                val current = form(greeting.key, dialect)
                val expected = requireNotNull(NeuralLexicon162.get(dialect, greeting.key))
                assertEquals("$dialect/${greeting.key}", expected.speechText, current.speechText)
                assertTrue("$dialect/${greeting.key}", current.speechText.any {
                    it in '\u064b'..'\u0652'
                })
                count++
            }
        assertEquals(112, count)
    }

    @Test fun divineNameConnectedVowelsAndLibyanPauseRemainIntact() {

        for (dialect in listOf(ArabicDialect.LIBYAN, ArabicDialect.STANDARD,
            ArabicDialect.SAUDI)) for (key in listOf("greeting.bless",
            "greeting.fine", "greeting.safe")) {
            val expected = requireNotNull(NeuralLexicon162.get(dialect, key))
            assertEquals("$dialect/$key", expected.speechText, form(key, dialect).speechText)
        }
    }

    @Test fun keyLibyanAndOtherDialectWordsAvoidWrongHomographs() {

        val keys = listOf("fruit.banana", "fruit.peach", "fruit.coconut",
            "food.olives", "food.oil", "routine.sleep", "home.fork", "body.eye",
            "routine.prayer_beads", "clothing.belt", "home.pillow", "home.fan",
            "home.key", "body.neck", "body.shoulder", "animal.cow", "animal.chicken",
            "food.chicken", "animal.camel", "animal.horse", "dish.couscous_fish",
            "dish.libyan_soup", "dish.aysh_laban")
        for (key in keys) {
            val expected = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            assertEquals(key, expected.speechText, form(key, ArabicDialect.LIBYAN).speechText)
        }
        for ((dialect, key) in listOf(
            ArabicDialect.SYRIAN to "home.blanket",
            ArabicDialect.IRAQI to "dish.mint_tea",
            ArabicDialect.MOROCCAN to "animal.rabbit")) {
            val expected = requireNotNull(NeuralLexicon162.get(dialect, key))
            assertEquals("$dialect/$key", expected.speechText, form(key, dialect).speechText)
        }
    }

    @Test fun keyFamilyCorrectionsAreNaturalWithoutAlteringPersistentSeeds() {

        val selected = listOf(
            ArabicDialect.LIBYAN to "family.father",
            ArabicDialect.LIBYAN to "family.husband",
            ArabicDialect.LIBYAN to "family.wife",
            ArabicDialect.LIBYAN to "family.brother",
            ArabicDialect.LIBYAN to "family.grandmother",
            ArabicDialect.MOROCCAN to "family.father",
            ArabicDialect.MOROCCAN to "family.mother",
            ArabicDialect.IRAQI to "family.wife",
            ArabicDialect.SAUDI to "family.son",
        )
        for ((dialect, key) in selected) {
            val original = cards.single { it.semanticKey == key }
            val expected = requireNotNull(NeuralLexicon162.get(dialect, key))
            assertEquals(key, expected.speechText, form(key, dialect).speechText)
            assertEquals(key, original, DialectCatalog.localize(original, dialect).forEditing())
        }
    }

    @Test fun personalSpeechAndLabelsNeverUseBundledClipOrAreRetouched() {
        val orig = cards.single { it.semanticKey == "greeting.bless" }
        for (field in listOf(CardDetails.SPEECH, CardDetails.LABEL)) {
            val source = orig.copy(ttsPrompt = "صوتي الخاص", customTextFields = field)
            val localized = DialectCatalog.localize(source, ArabicDialect.LIBYAN)
            assertEquals(source.ttsPrompt, localized.speechText)
            assertNull(DialectSpeechCatalog.neuralTextFor(localized, ArabicDialect.LIBYAN))
            assertEquals(source, localized.forEditing())
        }
    }
}
