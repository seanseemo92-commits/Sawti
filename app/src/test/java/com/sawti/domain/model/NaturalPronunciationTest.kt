package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Written/behavioral contracts. Android-engine and native listening remain separate checks. */
class NaturalPronunciationTest {
    private fun item(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()
    private fun spoken(key: String, dialect: ArabicDialect) = DialectCatalog.localize(item(key), dialect).ttsPrompt

    @Test fun femininePauseRetainsTheVowelAndDoesNotSpeakTaa() {
        assertEquals("سَيَّارَهْ", TtsEndings.normalize("سَيَّارَةٌ"))
        assertEquals("قَنِّينِهْ!", TtsEndings.normalize("قَنِّينِةْ!"))
        assertEquals("صَلَاهْ", TtsEndings.normalize("صَلَاةْ"))
        assertEquals("بَيْتْ", TtsEndings.normalize("بَيْتٌ"))
    }

    @Test fun pauseNeverRunsOnEveryWordInAConstructOrAdjectivePhrase() {
        assertEquals("قَهْوَةُ الْمَسَاءْ", TtsEndings.normalize("قَهْوَةُ الْمَسَاءِ"))
        assertEquals("سَيَّارَةٌ صَغِيرَهْ", TtsEndings.normalize("سَيَّارَةٌ صَغِيرَةٌ"))
        assertEquals("قَهْوِتْ الْعَشِيَّهْ", spoken("routine.evening_coffee", ArabicDialect.LIBYAN))
        assertEquals("قَهْوَهْ", spoken("food.coffee", ArabicDialect.LIBYAN))
        assertEquals("قَهْوَهْ عَرَبِيَّهْ", spoken("dish.arabic_coffee", ArabicDialect.LIBYAN))
        assertEquals("كِبِّهْ مَقْلِيِّهْ", spoken("dish.syrian.fried_kibbeh", ArabicDialect.SYRIAN))
        assertEquals("عُجَّهْ بِالْمَرْقَازْ", spoken("dish.tunisian.merguez_omelette", ArabicDialect.TUNISIAN))
    }

    @Test fun fathatanRetainsLongAaInBothUnicodeSpellingsAndAfterHamza() {
        assertEquals("مَرْحَبَا", TtsEndings.normalize("مَرْحَبًا"))
        assertEquals("مَرْحَبَا", TtsEndings.normalize("مَرْحَباً"))
        assertEquals("مَرْحَبَا", TtsEndings.normalize("مَرْحَبَاً"))
        assertEquals("هُدَى", TtsEndings.normalize("هُدًى"))
        assertEquals("مَاءَا", TtsEndings.normalize("مَاءً"))
        assertEquals("رَحْمَهْ", TtsEndings.normalize("رَحْمَةً"))
    }

    @Test fun longVowelsDiphthongsConsonantalGlidesAndShaddaAreDistinct() {
        assertEquals("نَبِّي إِمِّيَّهْ", TtsEndings.normalize("نَبِّيْ إِمِّيَّهْ"))
        assertEquals("يَدْعُو", TtsEndings.normalize("يَدْعُوْ"))
        for (word in listOf("مَايْ", "شَايْ", "خَوْخْ", "مَشْيْ", "دَحْيْ", "مَيّْ", "نْحِبْ", "عْظَامْ")) {
            assertEquals(word, TtsEndings.normalize(word))
        }
        assertEquals("حَقّْ", TtsEndings.normalize("حَقٌّ"))
    }

    @Test fun punctuationEmptyAndNonArabicTextArePreservedAndPauseIsIdempotent() {
        for (text in listOf("", "   ", "؟!", "hello!", "١٢٣", "قِطَّةٌ؟!  ", "شُكْرًا", "أُمِّي", "سَيَّارَة أُجْرَة")) {
            val once = TtsEndings.normalize(text)
            assertEquals(once, TtsEndings.normalize(once))
        }
        assertEquals("قِطَّهْ؟!  ", TtsEndings.normalize("قِطَّةٌ؟!  "))
        assertEquals("كَيْفَ حَالُكْ؟", spoken("greeting.how_are_you", ArabicDialect.STANDARD))
    }

    @Test fun greetingSpeechHasIndependentExplicitExamplesForEveryDialect() {
        val expected = mapOf(
            ArabicDialect.STANDARD to "مَعَ السَّلَامَهْ",
            ArabicDialect.LIBYAN to "مَعْ السَّلَامَهْ",
            ArabicDialect.SAUDI to "مَعْ السَّلَامَهْ",
            ArabicDialect.EGYPTIAN to "مَعْ السَّلَامَهْ",
            ArabicDialect.SYRIAN to "مَعْ السَّلَامِهْ",
            ArabicDialect.IRAQI to "مَعْ السَّلَامَهْ",
            ArabicDialect.TUNISIAN to "بِالسَّلَامَهْ",
            ArabicDialect.MOROCCAN to "بْالسَّلَامَهْ",
            ArabicDialect.ALGERIAN to "بْالسَّلَامَهْ",
        )
        expected.forEach { (dialect, text) -> assertEquals(dialect.name, text, spoken("greeting.goodbye", dialect)) }
        assertEquals("أَهْلَنْ وَسَهْلَنْ", spoken("greeting.welcome", ArabicDialect.LIBYAN))
        assertEquals("يِسْلَمُو", spoken("greeting.thanks", ArabicDialect.SYRIAN))
    }

    @Test fun formerlyMissingRegionalWordsHaveReviewedWrittenCandidates() {
        assertEquals("بَطَّانِيَّة", spoken("home.blanket", ArabicDialect.SYRIAN))
        assertEquals("قَبُّوطْ", spoken("clothing.coat", ArabicDialect.IRAQI))
        assertEquals("دَبُّوزَةُ مَاءْ", spoken("home.water_bottle", ArabicDialect.TUNISIAN))
        assertEquals("قِنِينِيَّة", spoken("animal.rabbit", ArabicDialect.MOROCCAN))
        assertEquals("نَظَّارَة", spoken("clothing.glasses", ArabicDialect.ALGERIAN))
    }

    @Test fun connectedConstructCandidatesDoNotLoseTheirTaaInDialectSpeech() {
        assertEquals("إِزَازَةُ مَيَّة", spoken("home.water_bottle", ArabicDialect.EGYPTIAN))
        assertEquals("حَلَاوِتْ الْجِبِنْ", spoken("dish.syrian.halawet_jibn", ArabicDialect.SYRIAN))
        assertEquals("كُبَّتْ تِمَنْ", spoken("dish.iraqi.rice_kibbeh", ArabicDialect.IRAQI))
        assertEquals("مَرْقِتْ جِلْبَانَهْ", spoken("dish.tunisian.pea_stew", ArabicDialect.TUNISIAN))
        assertEquals("قَرْعَةُ مَاءْ", spoken("home.water_bottle", ArabicDialect.MOROCCAN))
        assertEquals("سَيَّارَةُ أُجْرَهْ", spoken("transport.taxi", ArabicDialect.STANDARD))
    }

    @Test fun savedSourceMediaAndIndividualTextOverridesSurviveAllDialectSwitches() {
        val raw = item("greeting.goodbye").copy(audioPath = "/my/voice.m4a", photoPath = "/my/photo.jpg")
        for (dialect in ArabicDialect.entries) {
            val projected = DialectCatalog.localize(raw, dialect)
            assertEquals(raw, projected.forEditing())
            assertEquals(raw.audioPath, projected.audioPath)
            assertEquals(raw.photoPath, projected.photoPath)
            for (flag in listOf(CardDetails.SPEECH, CardDetails.LABEL)) {
                val personal = raw.copy(ttsPrompt = "قَهْوَةُ أُمِّيْ، چ گ", customTextFields = flag)
                assertEquals(personal.ttsPrompt, DialectCatalog.localize(personal, dialect).ttsPrompt)
            }
            val personalCue = raw.copy(phonemicCue = "مَـ...", customTextFields = CardDetails.CUE)
            assertEquals("مَـ...", DialectCatalog.localize(personalCue, dialect).phonemicCue)
        }
    }

    @Test fun unknownAndPersonalCardsNeverReceiveEndingsFromTheBuiltinPolicy() {
        val raw = item("greeting.goodbye").copy(ttsPrompt = "صَلَاةٌ")
        for (dialect in ArabicDialect.entries) for (personal in listOf(
            raw.copy(semanticKey = null), raw.copy(semanticKey = "unknown.concept"),
            raw.copy(categoryId = "custom_personal"))) {
            assertEquals(personal, DialectCatalog.localize(personal, dialect))
        }
    }

    @Test fun entireVisibleCorpusHasNoMissingRegionalCandidatesOrFinalSpokenTaaMarbuta() {
        for (dialect in ArabicDialect.entries) for (source in WordSeed.itemEntities().map { it.toDomain() }.filter {
            it.categoryId != CategoryIds.CUISINE || RegionalDishesCatalog.visibleForDialect(it.subgroup, dialect)
        }) {
            val form = DialectSpeechCatalog.resolve(source, dialect)
            assertNotEquals("$dialect/${source.semanticKey}", SpeechSource.UNVOCALIZED_FALLBACK, form.source)
            // The supplied 245 candidates are preserved verbatim, including their
            // explicit pausal endings; the older automatic-ending rule remains
            // relevant only to the untouched built-in entries.
            if (!DialectSpeechRevisions.hasCompletedCandidate(dialect, source.semanticKey.orEmpty())) {
                assertFalse("$dialect/${source.semanticKey}",
                    Regex("ة[ً-ٟ]*[؟!،.\\s]*$").containsMatchIn(form.speechText))
            }
            assertEquals(form.speechText, DialectCatalog.localize(source, dialect).ttsPrompt)
            assertTrue(form.needsNativeReview && form.needsTtsListening)
        }
    }
}
