package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

class CardDetailsTest {
    private val word = WordSeed.itemEntities().first { it.semanticKey == "food.bread" }.toDomain()

    @Test fun editorReceivesSavedValuesInEveryDialect() {
        val original = word.copy(sentencePrompt = "جملة شخصية", phonemicCue = "تلميح شخصي")
        ArabicDialect.entries.forEach {
            val shown = DialectCatalog.localize(original, it)
            assertEquals(original, shown.forEditing())
            assertEquals(CardDetails.from(original), CardDetails.from(shown))
        }
    }

    @Test fun customSpeechIsExactEvenWhenCuratedPronunciationExists() {
        val original = word.copy(ttsPrompt = "نطق اختاره المستخدم", customTextFields = CardDetails.SPEECH)
        ArabicDialect.entries.forEach {
            assertEquals(original.ttsPrompt, DialectCatalog.localize(original, it).ttsPrompt)
        }
    }

    @Test fun customizedMappedLabelCueAndSentenceAreNeverReplaced() {
        val original = word.copy(displayText = "مية", phonemicCue = "مخصص",
            sentencePrompt = "جملة شخصية", customTextFields = 15)
        ArabicDialect.entries.forEach {
            val shown = DialectCatalog.localize(original, it)
            assertEquals(original.displayText, shown.displayText)
            assertEquals(original.phonemicCue, shown.phonemicCue)
            assertEquals(original.sentencePrompt, shown.sentencePrompt)
        }
    }

    @Test fun projectionCanBeRelocalizedWithoutBecomingTheSource() {
        val egyptian = DialectCatalog.localize(word, ArabicDialect.EGYPTIAN)
        assertEquals("عيش", egyptian.displayText)
        assertEquals(word, DialectCatalog.localize(egyptian, ArabicDialect.LIBYAN).forEditing())
    }

    @Test fun unchangedFieldsKeepConcurrentChanges() {
        val baseline = CardDetails.from(word)
        val latest = baseline.copy(phone = "123")
        assertEquals("123", latest.merge(baseline, baseline.copy(cue = "جديد")).phone)
        assertEquals("جديد", latest.merge(baseline, baseline.copy(cue = "جديد")).cue)
        assertEquals(latest, latest.merge(baseline, baseline))
    }

    @Test fun conflictingEditsAreRejectedButIdenticalChangesAreAllowed() {
        val baseline = CardDetails.from(word)
        val latest = baseline.copy(speech = "الأحدث")
        assertThrows(IllegalArgumentException::class.java) {
            latest.merge(baseline, baseline.copy(speech = "القديم"))
        }
        assertEquals(latest, latest.merge(baseline, latest))
    }

    @Test fun clearingSentenceIsAnExplicitCustomization() {
        val baseline = CardDetails.from(word).copy(sentence = "جملة")
        assertEquals(CardDetails.SENTENCE, baseline.changedTextFields(baseline.copy(sentence = null)))
    }

    @Test fun labelOnlyOverrideCannotRetargetUneditedSpeechToAnotherDictionaryWord() {
        val renamed = word.copy(displayText = "مية", customTextFields = CardDetails.LABEL)
        ArabicDialect.entries.forEach {
            assertEquals(word.ttsPrompt, DialectCatalog.localize(renamed, it).ttsPrompt)
        }
    }
}
