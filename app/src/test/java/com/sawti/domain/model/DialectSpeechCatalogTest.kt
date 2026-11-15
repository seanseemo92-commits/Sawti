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
            "food.eggs" to ("بيض" to "بِيض"),
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
        assertEquals("قنية", DialectCatalog.localize(item("animal.rabbit"),
            ArabicDialect.MOROCCAN).displayText)
    }

    @Test fun familyTermsAreRegionalNotClassicArabicInAllEightSpokenDialects() {
        val cases = mapOf(
            ArabicDialect.LIBYAN to ("بو راجلي" to "مرت خوي"),
            ArabicDialect.TUNISIAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.MOROCCAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.ALGERIAN to ("بو راجلي" to "مرت خويا"),
            ArabicDialect.EGYPTIAN to ("أبو جوزي" to "مرات أخويا"),
            ArabicDialect.SYRIAN to ("أبو جوزي" to "مرت أخوي"),
            ArabicDialect.SAUDI to ("أبو زوجي" to "زوجة أخوي"),
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


    private val regional = ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }
    private val allSeeds by lazy { WordSeed.itemEntities().map { it.toDomain() } }

    @Test fun allNineDialectsAndFourteenNamespacesHaveNoWordFinalSukun() {
        val namespaces = setOf("family", "request", "dish", "routine", "vegetable",
            "fruit", "food", "home", "clothing", "body", "action", "animal",
            "transport", "greeting")
        assertEquals(namespaces, allSeeds.mapNotNull { it.semanticKey?.substringBefore('.') }.toSet())
        val trailingSukun = Regex("""ْ(?=\s|$|[؟،؛.!])""")
        val trailingHaSukun = Regex("""هْ(?=\s|$|[؟،؛.!])""")
        for (dialect in ArabicDialect.entries) {
            for (seed in allSeeds) {
                if (seed.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                    !RegionalDishesCatalog.visibleForDialect(seed.subgroup, dialect)) continue
                val speech = DialectSpeechCatalog.resolveForNeural(seed, dialect).speechText
                assertFalse("$dialect/${seed.semanticKey}: $speech",
                    trailingSukun.containsMatchIn(speech))
                assertFalse("$dialect/${seed.semanticKey}: $speech",
                    trailingHaSukun.containsMatchIn(speech))
            }
            for (vehicle in TransportCatalog.vehicles) {
                val speech = requireNotNull(vehicle.forms.getValue(dialect).speechText)
                assertFalse("$dialect/${vehicle.semanticKey}: $speech",
                    trailingSukun.containsMatchIn(speech))
                assertFalse("$dialect/${vehicle.semanticKey}: $speech",
                    trailingHaSukun.containsMatchIn(speech))
            }
            for (dish in RegionalDishesCatalog.forDialect(dialect).dishes) {
                assertFalse("$dialect/${dish.semanticKey}: ${dish.ttsPrompt}",
                    trailingSukun.containsMatchIn(dish.ttsPrompt))
                assertFalse("$dialect/${dish.semanticKey}: ${dish.ttsPrompt}",
                    trailingHaSukun.containsMatchIn(dish.ttsPrompt))
            }
            for (greeting in GreetingsCatalog.greetings) {
                val speech = DialectSpeechCatalog.resolve(
                    allSeeds.single { it.semanticKey == greeting.key }, dialect).speechText
                assertFalse("$dialect/${greeting.key}: $speech",
                    trailingSukun.containsMatchIn(speech))
            }
        }
    }

    @Test fun eightRegionalDialectsHaveNoClassicalDiphthongsOrConstructEndings() {
        val forbidden = mapOf("food.eggs" to "بَيْض", "food.olives" to "زَيْت",
            "food.oil" to "زَيْت", "fruit.banana" to "مَوْز",
            "fruit.peach" to "خَوْخ", "routine.sleep" to "نَوْم",
            "home.fork" to "شَوْك", "body.eye" to "عَيْن")
        for (dialect in regional) {
            for ((key, classical) in forbidden) {
                val speech = DialectSpeechCatalog.resolve(item(key), dialect).speechText
                assertFalse("$dialect/$key: $speech", classical in speech)
            }
            for (key in listOf("home.water_bottle", "routine.evening_coffee")) {
                val speech = DialectSpeechCatalog.resolve(item(key), dialect).speechText
                assertFalse("$dialect/$key: $speech", "ةُ" in speech)
            }
            assertEquals("$dialect coconut", "جُوز الهِنْد",
                DialectSpeechCatalog.resolve(item("fruit.coconut"), dialect).speechText)
        }
        assertEquals("جَوْزُ الهِنْد",
            DialectSpeechCatalog.resolve(item("fruit.coconut"), ArabicDialect.STANDARD).speechText)
    }

    @Test fun everyRegionalInLawAndCousinHasExplicitLocalizedSpeech() {
        val inLaws = listOf("husband_father", "husband_mother", "wife_father", "wife_mother",
            "husband_brother", "husband_sister", "wife_brother", "wife_sister",
            "sister_husband", "daughter_husband", "brother_wife", "son_wife")
        val cousins = listOf("paternal_uncle_son", "paternal_aunt_son", "maternal_uncle_son",
            "maternal_aunt_son", "brother_son", "sister_son",
            "brother_daughter", "sister_daughter")
        for (dialect in regional) for (suffix in inLaws + cousins) {
            val key = "family.$suffix"
            val speech = DialectSpeechCatalog.resolve(item(key), dialect)
            val expected = requireNotNull(NeuralLexicon161.forms[key]?.get(dialect)) {
                "Missing explicit regional form: $dialect/$key"
            }
            assertEquals("$dialect/$key label", expected.displayText, speech.displayText)
            assertEquals("$dialect/$key speech", expected.speechText, speech.speechText)
            assertEquals("$dialect/$key source", SpeechSource.EXPLICIT_DIALECT, speech.source)
        }
        for (dialect in listOf(ArabicDialect.LIBYAN, ArabicDialect.TUNISIAN,
            ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN)) {
            assertEquals("بو راجلي",
                DialectSpeechCatalog.resolve(item("family.husband_father"), dialect).displayText)
            assertEquals("بو مرتي",
                DialectSpeechCatalog.resolve(item("family.wife_father"), dialect).displayText)
        }
    }

    @Test fun v161DirectSpeechAndEditedCaregiverLabelsRemainIndependent() {
        val exact = mapOf(
            ArabicDialect.SYRIAN to mapOf("action.close" to ("أسكر" to "أَسَكِّر"),
                "action.see" to ("أشوف" to "أَشُوف"),
                "action.speak" to ("أحكي" to "أَحْكِي")),
            ArabicDialect.IRAQI to mapOf("action.sit" to ("أقعد" to "أَقْعُد"),
                "action.read" to ("أقرا" to "أَقْرَا")),
            ArabicDialect.MOROCCAN to mapOf("action.sit" to ("نجلس" to "نْجْلِس"),
                "request.headache" to ("راسي كيوجعني" to "رَاسِي كَيُوجَعْنِي")),
            ArabicDialect.ALGERIAN to mapOf(
                "request.toilet" to ("بغيت نروح للتواليت" to "بْغِيت نْرُوح لِلتْوَالِيت"))
        )
        for ((dialect, cases) in exact) for ((key, expected) in cases) {
            val form = DialectSpeechCatalog.resolve(item(key), dialect)
            assertEquals("$dialect/$key display", expected.first, form.displayText)
            assertEquals("$dialect/$key speech", expected.second, form.speechText)
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

    @Test fun writtenReviewDoesNotReplaceRequiredNativeListening() {
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(item("request.water"), dialect)
            assertTrue(dialect.name, form.needsNativeReview)
            assertTrue(dialect.name, form.needsTtsListening)
        }
    }
}
