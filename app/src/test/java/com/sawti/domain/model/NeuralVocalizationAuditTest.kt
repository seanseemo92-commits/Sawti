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
        for (dialect in ArabicDialect.entries.filterNot { it == ArabicDialect.STANDARD }) {
            for (greeting in GreetingsCatalog.greetings) {
                val candidate = GreetingsCatalog.dialectForms.getValue(greeting.key).getValue(dialect)
                assertTrue("$dialect/${greeting.key}", candidate.speechText.orEmpty().any {
                    it in '\u064b'..'\u0652'
                })
                assertEquals(greeting.key, candidate.speechText,
                    form(greeting.key, dialect).speechText)
                count++
            }
        }
        assertEquals(112, count)
    }

    @Test fun divineNameConnectedVowelsAndLibyanPauseRemainIntact() {
        assertEquals("بَارَكَ اللَّهُ فِيك", form("greeting.bless", ArabicDialect.LIBYAN).speechText)
        assertEquals("بَارَكَ اللَّهُ فِيك", form("greeting.bless", ArabicDialect.STANDARD).speechText)
        assertEquals("اللَّهُ يبَارِك فِيك", form("greeting.bless", ArabicDialect.SAUDI).speechText)
        assertEquals("الحَمْدُ لِلَّه", form("greeting.fine", ArabicDialect.LIBYAN).speechText)
        assertEquals("فِي أَمَانِ اللَّه", form("greeting.safe", ArabicDialect.LIBYAN).speechText)
    }

    @Test fun keyLibyanAndOtherDialectWordsAvoidWrongHomographs() {
        val libyan = mapOf(
            "fruit.banana" to "مُوز", "fruit.peach" to "خُوخ",
            "fruit.coconut" to "جُوز الهِنْد",
            "food.olives" to "زِيتُون", "food.oil" to "زِيت",
            "routine.sleep" to "نُوم", "home.fork" to "شُوكَة",
            "body.eye" to "عِين", "routine.prayer_beads" to "سِبْحَة",
            "clothing.belt" to "سِبْتَة", "home.pillow" to "مَخَدَّة",
            "home.fan" to "مَرْوَحَة", "home.key" to "مِفْتَاح",
            "body.neck" to "رَقْبَة", "body.shoulder" to "كِتْف",
            "animal.cow" to "بَقْرَة", "animal.chicken" to "دجَاجَة",
            "food.chicken" to "دجَاج", "animal.camel" to "جمَل",
            "animal.horse" to "حصَان", "dish.couscous_fish" to "كُسْكْسِي بِالحُوت",
            "dish.libyan_soup" to "شَرْبَة لِيبِيَّة",
            "dish.aysh_laban" to "عِيش بِاللَّبَن",
        )
        for ((key, expected) in libyan)
            assertEquals(key, expected, form(key, ArabicDialect.LIBYAN).speechText)
        assertEquals("حْرَام", form("home.blanket", ArabicDialect.SYRIAN).speechText)
        assertEquals("تشَاي بِالنَّعْنَاع",
            form("dish.mint_tea", ArabicDialect.IRAQI).speechText)
        assertEquals("قْنِيَّة", form("animal.rabbit", ArabicDialect.MOROCCAN).speechText)
    }

    @Test fun keyFamilyCorrectionsAreNaturalWithoutAlteringPersistentSeeds() {
        val selected = listOf(
            Triple(ArabicDialect.LIBYAN, "family.father", "بُوي"),
            Triple(ArabicDialect.LIBYAN, "family.husband", "رَاجْلِي"),
            Triple(ArabicDialect.LIBYAN, "family.wife", "مَرْتِي"),
            Triple(ArabicDialect.LIBYAN, "family.brother", "خُوي"),
            Triple(ArabicDialect.LIBYAN, "family.grandmother", "حَنَّاي"),
            Triple(ArabicDialect.MOROCCAN, "family.father", "بَّا"),
            Triple(ArabicDialect.MOROCCAN, "family.mother", "مِّي"),
            Triple(ArabicDialect.IRAQI, "family.wife", "مَرْتِي"),
            Triple(ArabicDialect.SAUDI, "family.son", "وَلَدِي"),
        )
        for ((dialect, key, speech) in selected) {
            val original = cards.single { it.semanticKey == key }
            assertEquals(key, speech, form(key, dialect).speechText)
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
