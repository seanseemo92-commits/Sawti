package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Content contracts only; these tests do not constitute a listening/native-speaker review. */
class DialectSpeechCatalogTest {
    private fun item(key: String) = WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun mandatoryLibyanWaterRequestSurvivesTheCompleteProjection() {
        val view = DialectCatalog.localize(item("request.water"), ArabicDialect.LIBYAN)
        assertEquals("نبي مية", view.displayText)
        assertEquals("نَبِّي إِمِّيَّهْ", view.ttsPrompt)
        assertEquals("إِمْـ...", view.phonemicCue)
        assertEquals("إِمِّيَّهْ", DialectCatalog.localize(item("food.water"), ArabicDialect.LIBYAN).ttsPrompt)
    }

    @Test fun canonicalFushaSourceRemainsReversibleAfterPausalProjection() {
        for (entity in WordSeed.itemEntities()) {
            val source = entity.toDomain()
            val view = DialectCatalog.localize(source, ArabicDialect.STANDARD)
            // The saved canonical seed is never rewritten by the preview projection.
            assertEquals(source, view.forEditing())

        }
        assertEquals("أُرِيدُ مَاءً", item("request.water").ttsPrompt)
    }

    @Test fun preservedLocalLexemesUseSemanticKeys() {
        assertEquals("دَحْيْ", DialectCatalog.localize(item("food.eggs"), ArabicDialect.LIBYAN).ttsPrompt)
        assertEquals("عْظَامْ", DialectCatalog.localize(item("food.eggs"), ArabicDialect.TUNISIAN).ttsPrompt)
        assertEquals("تْشَايْ", DialectCatalog.localize(item("food.tea"), ArabicDialect.IRAQI).ttsPrompt)
        assertEquals("نَبِّي إِدْوَايَا", DialectCatalog.localize(item("request.medicine"), ArabicDialect.LIBYAN).ttsPrompt)
    }

    @Test fun personalSpeechAndCueOverrideEveryProjectionVerbatim() {
        val source = item("request.water").copy(ttsPrompt = "نطقي الخاص", phonemicCue = "تلميحي الخاص",
            customTextFields = CardDetails.SPEECH or CardDetails.CUE)
        for (dialect in ArabicDialect.entries) {
            val view = DialectCatalog.localize(source, dialect)
            assertEquals(source.ttsPrompt, view.ttsPrompt)
            assertEquals(source.phonemicCue, view.phonemicCue)
        }
    }

    @Test fun eggsDoNotLeakTheLibyanLexemeIntoOtherDialects() {
        for (dialect in listOf(ArabicDialect.SAUDI, ArabicDialect.EGYPTIAN,
            ArabicDialect.SYRIAN, ArabicDialect.IRAQI, ArabicDialect.MOROCCAN)) {
            val form = DialectSpeechCatalog.resolve(item("food.eggs"), dialect)
            assertEquals("بيض", form.displayText)
            assertFalse(form.speechText.contains("دَحْ"))
            assertTrue(form.needsNativeReview)
            assertTrue(form.needsTtsListening)
        }
    }

    @Test fun preservedLocalCueMustBelongToTheSpokenWord() {
        val eggs = DialectSpeechCatalog.resolve(item("food.eggs"), ArabicDialect.LIBYAN)
        assertEquals("دَحْـ...", eggs.phonemicCue)
        val trousers = DialectSpeechCatalog.resolve(item("clothing.trousers"), ArabicDialect.LIBYAN)
        assertEquals("سروال", trousers.displayText)
        assertEquals("سِرْوَالْ", trousers.speechText)
        assertEquals("سِرْ...", trousers.phonemicCue)
    }

    @Test fun missingWrittenSpeechIsExplicitAndDoesNotFabricateVowelsOrForeignCue() {
        val form = DialectSpeechCatalog.resolve(item("home.water_bottle"), ArabicDialect.MOROCCAN)
        assertEquals(SpeechSource.EXPLICIT_DIALECT, form.source)
        assertEquals("قَرْعَتْ مَا", form.speechText)
        assertEquals("قَرْ...", form.phonemicCue)
        assertTrue(form.needsNativeReview)
        assertTrue(form.needsTtsListening)
    }

    @Test fun regionalUserSpeechDoesNotReceiveAnUnrelatedRegionalCue() {
        val source = item("dish.tunisian.lablabi").copy(
            ttsPrompt = "صَوْتِي", phonemicCue = "صَوْ...", customTextFields = CardDetails.SPEECH)
        val view = DialectCatalog.localize(source, ArabicDialect.TUNISIAN)
        assertEquals(source.ttsPrompt, view.ttsPrompt)
        assertEquals(source.phonemicCue, view.phonemicCue)
        assertEquals(source, view.forEditing())
    }

    @Test fun noPhoneticRewritingOfUserLongVowelsShaddaOrConnectedWords() {
        val source = item("request.water").copy(ttsPrompt = "نَبِّيْ إِمِّيَّهْ، چ گ",
            phonemicCue = "مِّيـ...", customTextFields = CardDetails.SPEECH or CardDetails.CUE)
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(source, dialect)
            assertEquals(source.ttsPrompt, form.speechText)
            assertEquals(source.phonemicCue, form.phonemicCue)
        }
    }

    @Test fun familyFushaHasWrittenSpeechSeparateFromDisplay() {
        assertEquals("أمي", item("family.mother").displayText)
        assertEquals("أُمِّي", item("family.mother").ttsPrompt)
        assertEquals("زَوْجُ ابْنَتِي", item("family.daughter_husband").ttsPrompt)
        WordSeed.itemEntities().filter { it.categoryId == CategoryIds.FAMILY }.forEach {
            assertTrue(it.ttsPrompt.any { c -> c in 'ً'..'ٟ' })
        }
    }

    @Test fun allPreservedLibyanSpeechStringsRemainExactNotOnlyTheWaterExample() {
        val rows = requireNotNull(javaClass.classLoader?.getResourceAsStream("libyan-preserved-speech.tsv"))
            .bufferedReader().use { it.readLines().drop(1) }
        assertEquals(194, rows.size)
        for (line in rows) {
            val fields = line.split('\t')
            val source = item(fields[0])
            val form = DialectSpeechCatalog.resolve(source, ArabicDialect.LIBYAN)
            val card = DialectCatalog.localize(source, ArabicDialect.LIBYAN)
            // Historical Libyan strings remain represented by the unchanged seed.
            assertEquals(fields[0], source, card.forEditing())
            val constructRevisions = setOf("dish.reshda_kaskas", "dish.reshda_borma", "dish.potato_stew",
                "dish.khobz_tajin", "routine.evening_coffee", "clothing.watch", "clothing.high_heels")
            if (!ArgonAuditOverrides.hasPatch(ArabicDialect.LIBYAN, fields[0]) && fields[0] !in constructRevisions) {
                assertEquals(fields[0], TtsEndings.normalize(fields[1]), form.speechText)
            }
        }
    }
}

