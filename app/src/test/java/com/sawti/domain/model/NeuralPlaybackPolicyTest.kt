package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** One catalog policy controls eligibility, without manifest diacritic equality. */
class NeuralPlaybackPolicyTest {
    private fun source(key: String) =
        WordSeed.itemEntities().single { it.semanticKey == key }.toDomain()

    @Test fun intactBuiltInCardsResolveInAllNineDialects() {
        val original = source("request.water")
        for (dialect in ArabicDialect.entries) {
            val view = DialectCatalog.localize(original, dialect)
            val expected = DialectSpeechCatalog.resolve(original, dialect).speechText
            assertEquals(dialect.name, expected, DialectSpeechCatalog.habibiTextFor(view, dialect))
            assertEquals(dialect.name, expected, view.neuralSpeechText)
            assertEquals(original, view.forEditing())
        }
    }

    @Test fun wrongDialectAndPersonalEditsAlwaysRejectBundledNeuralSound() {
        val original = source("request.water")
        val view = DialectCatalog.localize(original, ArabicDialect.LIBYAN)
        assertNull(DialectSpeechCatalog.habibiTextFor(view, ArabicDialect.EGYPTIAN))
        for (flag in listOf(CardDetails.LABEL, CardDetails.SPEECH,
                           CardDetails.LABEL or CardDetails.SPEECH)) {
            val edited = DialectCatalog.localize(
                original.copy(customTextFields = flag, ttsPrompt = "صوت خاص"),
                ArabicDialect.LIBYAN)
            assertNull(DialectSpeechCatalog.habibiTextFor(edited, ArabicDialect.LIBYAN))
            assertNull(SentenceComposer.piece(edited).neuralSpeechText)
        }
        assertNull(DialectSpeechCatalog.habibiTextFor(original.copy(categoryId = "custom_personal"),
            ArabicDialect.LIBYAN))
        assertNull(DialectSpeechCatalog.habibiTextFor(original.copy(semanticKey = null),
            ArabicDialect.LIBYAN))
    }

    @Test fun rebindingAnUnchangedBuiltinCardKeepsTheHabibiKey() {
        val view = DialectCatalog.localize(source("request.water"), ArabicDialect.LIBYAN)
        val altered = view.copy(ttsPrompt = "نبي ميه")
        assertEquals(DialectSpeechCatalog.habibiTextFor(view, ArabicDialect.LIBYAN),
            DialectSpeechCatalog.habibiTextFor(altered, ArabicDialect.LIBYAN))
    }

    @Test fun personalFallbackSpeechRemainsPersonalInSentence() {
        val original = source("request.water")
        val edited = DialectCatalog.localize(
            original.copy(ttsPrompt = "نطقي الخاص", customTextFields = CardDetails.SPEECH),
            ArabicDialect.LIBYAN)
        val part = SentenceComposer.spokenPieces(listOf(SentenceComposer.piece(edited))).single()
        assertEquals("نطقي الخاص", part.speech)
        assertNull(part.neuralSpeechText)
    }
}
