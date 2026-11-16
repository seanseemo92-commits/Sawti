package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** The complete actual embedded corpus, not a sample or a fixture. */
class HabibiSpeechTextTest {
    private val all by lazy { WordSeed.itemEntities().map { it.toDomain() } }

    @Test fun nineOfficialDialectTagsAreUnique() {
        val tags = ArabicDialect.entries.map(HabibiSpeechText::dialectTag)
        assertEquals(9, tags.size)
        assertEquals(9, tags.distinct().size)
        assertEquals("LBY", HabibiSpeechText.dialectTag(ArabicDialect.LIBYAN))
        assertEquals("EGY", HabibiSpeechText.dialectTag(ArabicDialect.EGYPTIAN))
        assertEquals("LEV", HabibiSpeechText.dialectTag(ArabicDialect.SYRIAN))
    }

    @Test fun all2399SpeechEntriesAreCleanAndCover391Keys() {
        val entries = mutableListOf<Pair<ArabicDialect, String>>()
        val keys = mutableSetOf<String>()
        for (dialect in ArabicDialect.entries) for (seed in all) {
            if (seed.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                !RegionalDishesCatalog.visibleForDialect(seed.subgroup, dialect)) continue
            val key = requireNotNull(seed.semanticKey)
            val spoken = DialectSpeechCatalog.resolveForHabibi(seed, dialect).speechText
            assertTrue("$dialect/$key is blank", spoken.isNotBlank())
            assertFalse("$dialect/$key has diacritics/tatweel/digits: $spoken",
                HabibiSpeechText.hasForbiddenMarksOrNumbers(spoken))
            assertEquals("$dialect/$key has normalization drift",
                HabibiSpeechText.prepare(spoken), spoken)
            entries.add(dialect to key)
            keys.add(key)
        }
        assertEquals(9, entries.map { it.first }.distinct().size)
        assertEquals("Duplicate key in dialect", entries.size, entries.distinct().size)
        assertEquals("Total enabled dialect entries", 2399, entries.size)
        assertEquals("Total distinct semantic keys", 391, keys.size)
    }

    @Test fun libyanSymbolsStayArabicAndPlain() {
        fun speech(key: String): String = DialectSpeechCatalog.resolveForHabibi(
            all.single { it.semanticKey == key }, ArabicDialect.LIBYAN).speechText
        assertEquals("نقعمز", speech("action.sit"))
        assertEquals("نرقد", speech("action.sleep"))
        assertEquals("بقرة", speech("animal.cow"))
        assertEquals("نبي الحمام", speech("request.toilet"))
    }

    @Test fun caregiverCustomTextAndMediaRemainUntouched() {
        val original = all.first().copy(
            ttsPrompt = "نُطقيْ الخاص", audioPath = "/my/voice.m4a",
            customTextFields = CardDetails.SPEECH)
        assertEquals(original.ttsPrompt,
            DialectSpeechCatalog.resolveForHabibi(original, ArabicDialect.LIBYAN).speechText)
        assertNull(DialectSpeechCatalog.habibiTextFor(original, ArabicDialect.LIBYAN))
        assertEquals(original, DialectCatalog.localize(original, ArabicDialect.LIBYAN).forEditing())
    }

    @Test fun flagsRejectDiacriticsTatweelAndDigits() {
        for (sample in listOf("نِبي", "نبيْ", "هــذا", "العدد 2", "عام ٢٠٢٦"))
            assertTrue(sample, HabibiSpeechText.hasForbiddenMarksOrNumbers(sample))
        assertEquals("نبي مية", HabibiSpeechText.prepare("نِبِّي  مَيَّة"))
    }
}
