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
        assertEquals("نَبِّيْ إِمِّيَّهْ", view.ttsPrompt)
        assertEquals("إِمْـ...", view.phonemicCue)
        assertEquals("إِمِّيَّهْ", DialectCatalog.localize(item("food.water"), ArabicDialect.LIBYAN).ttsPrompt)
    }

    @Test fun canonicalFushaBypassesAutomaticVocalization() {
        for (entity in WordSeed.itemEntities()) {
            val source = entity.toDomain()
            val view = DialectCatalog.localize(source, ArabicDialect.STANDARD)
            assertEquals(source.ttsPrompt, view.ttsPrompt)
            assertEquals(source.phonemicCue, view.phonemicCue)
            assertEquals(source.sentencePrompt, view.sentencePrompt)
        }
        assertEquals("أُرِيدُ مَاءً", item("request.water").ttsPrompt)
    }

    @Test fun preservedLocalLexemesUseSemanticKeys() {
        assertEquals("دَحْيْ", DialectCatalog.localize(item("food.eggs"), ArabicDialect.LIBYAN).ttsPrompt)
        assertEquals("عْظَامْ", DialectCatalog.localize(item("food.eggs"), ArabicDialect.TUNISIAN).ttsPrompt)
        assertEquals("تْشَايْ", DialectCatalog.localize(item("food.tea"), ArabicDialect.IRAQI).ttsPrompt)
        assertEquals("نَبِّيْ إِدْوَايَا", DialectCatalog.localize(item("request.medicine"), ArabicDialect.LIBYAN).ttsPrompt)
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
}
