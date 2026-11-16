package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Nine dialects are exported without forced vowels or Latin substitutions. */
class HabibiSpeechTextTest {
    private val cards by lazy { WordSeed.itemEntities().map { it.toDomain() } }
    @Test fun dialectTagsAreExplicit() {
        val tags = ArabicDialect.entries.map(HabibiSpeechText::dialectTag)
        assertEquals(9, tags.size)
        assertEquals(9, tags.toSet().size)
        assertEquals("LBY", HabibiSpeechText.dialectTag(ArabicDialect.LIBYAN))
        assertEquals("EGY", HabibiSpeechText.dialectTag(ArabicDialect.EGYPTIAN))
    }
    @Test fun everyBuiltinDialectInputIsUnvowelled() {
        var count = 0
        for (dialect in ArabicDialect.entries) for (seed in cards) {
            if (seed.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                !RegionalDishesCatalog.visibleForDialect(seed.subgroup, dialect)) continue
            val speech = DialectSpeechCatalog.resolveForHabibi(seed, dialect)
            assertTrue(speech.speechText.isNotBlank())
            assertEquals("$dialect/"+seed.semanticKey, speech.speechText,
                HabibiSpeechText.prepare(speech.speechText))
            count++
        }
        assertEquals(2399, count)
    }
    @Test fun libyanWordsRemainArabicWithoutManualG() {
        fun forKey(key: String) = DialectSpeechCatalog.resolveForHabibi(
            cards.single { it.semanticKey == key }, ArabicDialect.LIBYAN).speechText
        assertEquals("نقعمز", forKey("action.sit"))
        assertEquals("نرقد", forKey("action.sleep"))
        assertEquals("بقرة", forKey("animal.cow"))
        assertEquals("نبي الحمام", forKey("request.toilet"))
    }
    @Test fun personalSpeechRemainsByteForByteUntouched() {
        val source = cards.first().copy(ttsPrompt = "صوتي الخاص",
            customTextFields = CardDetails.SPEECH)
        assertEquals("صوتي الخاص",
            DialectSpeechCatalog.resolveForHabibi(source, ArabicDialect.LIBYAN).speechText)
        assertNull(DialectSpeechCatalog.habibiTextFor(source, ArabicDialect.LIBYAN))
    }
}
