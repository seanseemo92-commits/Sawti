package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Linguistic and reversible-projection contracts, not a substitute for native listening. */
class DialectSpeechCatalogTest {
    private fun item(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun correctedLibyanRequestsAreExactAndConsistentBetweenAudioAndDisplay() {
        val cases = mapOf(
            "request.water" to ("نبي مية" to "نِبِّي مَيَّة"),
            "request.tea" to ("نبي شاهي" to "نِبِّي شَاهِي"),
            "request.rest" to ("نبي نرتاح" to "نِبِّي نِرْتَاح"),
            "request.medicine" to ("نبي دوايا" to "نِبِّي دْوَايَا"),
            "request.toilet" to ("نبي الحمام" to "نِبِّي الحَمَّام"),
            "request.cold" to ("نحس بالبرد" to "نِحِسّ بِالبَرْد"),
            "food.water" to ("مية" to "مَيَّة"),
        )
        for ((key, expected) in cases) {
            val view = DialectCatalog.localize(item(key), ArabicDialect.LIBYAN)
            assertEquals(key + " label", expected.first, view.displayText)
            assertEquals(key + " pronunciation", expected.second, view.ttsPrompt)
        }
    }

    @Test fun locallyRecognizableWordsAndActionsRetainDialects() {
        val libyan = mapOf(
            "food.eggs" to ("دحي" to "دَحِي"),
            "home.window" to ("روشن" to "رُوشِن"),
            "vegetable.pepper" to ("فلفل" to "فِلْفِل"),
            "clothing.blouse" to ("سورية" to "سُورِيَّة"),
            "action.drink" to ("نشرب" to "نِشْرَب"),
            "action.sleep" to ("نرقد" to "نُرْقُد"),
            "action.sit" to ("نقعمز" to "نِقَعْمِز"),
            "action.walk" to ("نمشي" to "نِمْشِي"),
            "action.wash" to ("نغسل" to "نِغْسِل"),
            "action.speak" to ("ندوي" to "نِدْوِي"),
            "action.hear" to ("نسمع" to "نِسْمَع"),
            "action.open" to ("نفتح" to "نِفْتَح"),
            "action.close" to ("نسكر" to "نِسَكِّر"),
            "action.laugh" to ("نضحك" to "نِضْحَك"),
            "action.see" to ("نشوف" to "نِشُوف"),
            "action.dress" to ("نلبس" to "نِلْبِس"),
            "action.pray" to ("نصلي" to "نِصَلِّي"),
        )
        for ((key, expected) in libyan) {
            val form = DialectSpeechCatalog.resolve(item(key), ArabicDialect.LIBYAN)
            assertEquals(key, expected.first, form.displayText)
            assertEquals(key, expected.second, form.speechText)
        }
        assertEquals("سردوك", DialectCatalog.localize(item("animal.rooster"),
            ArabicDialect.TUNISIAN).displayText)
        assertEquals("قنينية", DialectCatalog.localize(item("animal.rabbit"),
            ArabicDialect.MOROCCAN).displayText)
    }

    @Test fun familyTermsAreRegionalNotClassicArabicInAllEightSpokenDialects() {
        val cases = mapOf(
            ArabicDialect.LIBYAN to ("بو راجلي" to "مرت خوي"),
            ArabicDialect.TUNISIAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.MOROCCAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.ALGERIAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.EGYPTIAN to ("أبو جوزي" to "مرات أخويا"),
            ArabicDialect.SYRIAN to ("أبو جوزي" to "مرت أخي"),
            ArabicDialect.SAUDI to ("أبو زوجي" to "مرت أخوي"),
            ArabicDialect.IRAQI to ("أبو زوجي" to "مرت أخوي"),
        )
        for ((dialect, expected) in cases) {
            assertEquals(dialect.name,
                expected.first, DialectCatalog.localize(item("family.husband_father"), dialect).displayText)
            assertEquals(dialect.name,
                expected.second, DialectCatalog.localize(item("family.brother_wife"), dialect).displayText)
        }
        for (dialect in listOf(ArabicDialect.TUNISIAN, ArabicDialect.MOROCCAN,
                               ArabicDialect.ALGERIAN, ArabicDialect.SAUDI, ArabicDialect.IRAQI)) {
            assertEquals(dialect.name, "ولد عمي",
                DialectCatalog.localize(item("family.paternal_uncle_son"), dialect).displayText)
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

    @Test fun writtenReviewDoesNotReplaceRequiredNativeListening() {
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(item("request.water"), dialect)
            assertTrue(dialect.name, form.needsNativeReview)
            assertTrue(dialect.name, form.needsTtsListening)
        }
    }
}
