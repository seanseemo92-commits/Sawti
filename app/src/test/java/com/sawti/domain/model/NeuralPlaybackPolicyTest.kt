package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Regression contracts: neural assets are never used for changed or stale card text. */
class NeuralPlaybackPolicyTest {
    private fun source(key: String) = WordSeed.itemEntities()
        .single { it.semanticKey == key }.toDomain()

    @Test fun intactBuiltInCardsAreEligibleInEveryDialect() {
        val original = source("request.water")
        for (dialect in ArabicDialect.entries) {
            val view = DialectCatalog.localize(original, dialect)
            val expected = DialectSpeechCatalog.resolveForNeural(original, dialect).speechText
            assertEquals(dialect.name, expected, NeuralPlaybackPolicy.forCard(view, dialect))
            assertEquals(dialect, view.neuralDialect)
            assertEquals(expected, view.neuralSpeechText)
            assertEquals(original, view.forEditing())
        }
    }

    @Test fun staleDialectProjectionCannotSelectAnOtherDialectClip() {
        val view = DialectCatalog.localize(source("request.water"), ArabicDialect.LIBYAN)
        assertNull(NeuralPlaybackPolicy.forCard(view, ArabicDialect.EGYPTIAN))
        assertEquals(ArabicDialect.LIBYAN,
            SentenceComposer.piece(view).neuralDialect)
        val strip = SentenceComposer.spokenPieces(listOf(SentenceComposer.piece(view)))
        assertEquals(ArabicDialect.LIBYAN, strip.single().neuralDialect)
    }

    @Test fun editedSpeechOrLabelNeverMatchesNeuralEvenWithoutEditFlags() {
        val view = DialectCatalog.localize(source("request.water"), ArabicDialect.LIBYAN)
        val changes = listOf(
            view.copy(displayText = "غيرت العنوان"),
            view.copy(ttsPrompt = "النطق من عندي"),
            view.copy(displayText = view.displayText, ttsPrompt = "نطق مختلف")
        )
        for (changed in changes) {
            assertNull(NeuralPlaybackPolicy.forCard(changed, ArabicDialect.LIBYAN))
        }
    }

    @Test fun explicitPersonalTextAndCustomCategoriesAlwaysUseFallback() {
        val original = source("request.water")
        for (flag in listOf(CardDetails.SPEECH, CardDetails.LABEL)) {
            val personalized = original.copy(customTextFields = flag)
            val projected = DialectCatalog.localize(personalized, ArabicDialect.LIBYAN)
            assertNull(NeuralPlaybackPolicy.forCard(projected, ArabicDialect.LIBYAN))
            assertNull(SentenceComposer.piece(projected).neuralSpeechText)
        }
        val custom = original.copy(categoryId = "custom_personal")
        assertNull(NeuralPlaybackPolicy.forCard(custom, ArabicDialect.LIBYAN))
        assertNull(NeuralPlaybackPolicy.forCard(original.copy(semanticKey = null), ArabicDialect.LIBYAN))
    }

    @Test fun synthesizedTextCannotBeReplacedByAStaleCachedVoice() {
        val original = source("request.water")
        val view = DialectCatalog.localize(original, ArabicDialect.LIBYAN)
        view.neuralSpeechText = "صوت قديم"
        assertNull(NeuralPlaybackPolicy.forCard(view, ArabicDialect.LIBYAN))
        view.neuralSpeechText = DialectSpeechCatalog.resolveForNeural(
            original, ArabicDialect.LIBYAN).speechText
        assertNotNull(NeuralPlaybackPolicy.forCard(view, ArabicDialect.LIBYAN))
        view.neuralDialect = ArabicDialect.SAUDI
        assertNull(NeuralPlaybackPolicy.forCard(view, ArabicDialect.LIBYAN))
    }

    @Test fun sentenceRetainsFallbackSpeechForPersonalizedCards() {
        val original = source("request.water")
        val custom = original.copy(ttsPrompt = "نطقي الخاص", customTextFields = CardDetails.SPEECH)
        val view = DialectCatalog.localize(custom, ArabicDialect.LIBYAN)
        val part = SentenceComposer.spokenPieces(listOf(SentenceComposer.piece(view))).single()
        assertEquals("نطقي الخاص", part.speech)
        assertNull(part.neuralSpeechText)
    }
}
