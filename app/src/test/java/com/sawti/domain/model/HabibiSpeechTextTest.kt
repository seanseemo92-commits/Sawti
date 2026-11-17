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
            assertEquals("$dialect/$key must use its plain display text",
                DialectSpeechCatalog.resolveForHabibi(seed, dialect).displayText, spoken)
            assertTrue("$dialect/$key is blank", spoken.isNotBlank())
            assertFalse("$dialect/$key has diacritics/tatweel/digits: $spoken",
                HabibiSpeechText.hasForbiddenMarksOrNumbers(spoken))
            assertEquals("$dialect/$key has normalization drift",
                HabibiSpeechText.prepare(spoken), spoken)
            entries.add(dialect to key)
            keys.add(key)
        }
        val identityKeys = requireNotNull(javaClass.getResourceAsStream("/vocabulary-identities.tsv"))
            .bufferedReader().useLines { lines -> lines.map { it.substringBefore('\t') }.toSet() }
        assertEquals("Every approved semantic identity remains covered", identityKeys, keys)
        for (dialect in ArabicDialect.entries) {
            assertEquals("$dialect cardinality", if (dialect == ArabicDialect.LIBYAN) 271 else 266,
                entries.count { it.first == dialect })
        }
        val sharedKeys = keys.filterNot { it.startsWith("dish.") }.toSet()
        assertEquals("Shared concepts", 251, sharedKeys.size)
        for (dialect in ArabicDialect.entries) {
            val dialectKeys = entries.filter { it.first == dialect }.map { it.second }.toSet()
            assertTrue("$dialect is missing shared concepts", dialectKeys.containsAll(sharedKeys))
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

    @Test fun existingUneditedRoomTextIsProjectedCleanWithoutChangingStoredValues() {
        val stored = all.single { it.semanticKey == "request.water" }.copy(
            displayText = "أريد ماءً", ttsPrompt = "أُرِيدُ مَاءً")
        val form = DialectSpeechCatalog.resolveForHabibi(stored, ArabicDialect.STANDARD)
        assertEquals("أريد ماء", form.displayText)
        assertEquals(form.displayText, form.speechText)
        assertEquals("أريد ماءً", stored.displayText)
        assertEquals("أُرِيدُ مَاءً", stored.ttsPrompt)
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


    @Test fun allDictionaryAndSeedSourcesHaveNoHistoricalPronunciationMarks() {
        val sources = listOf(
            "domain/model/DialectCatalog.kt", "domain/model/DialectSpeechRevisions.kt",
            "domain/model/NeuralLexicon161.kt", "domain/model/RegionalDishesCatalog.kt",
            "domain/model/GreetingsCatalog.kt", "domain/model/TransportCatalog.kt",
            "data/db/WordSeed.kt", "data/db/SeedExtras.kt",
        )
        for (source in sources) {
            val file = java.io.File("src/main/java/com/sawti/$source")
            assertTrue("Missing dictionary source $source", file.isFile)
            assertFalse("Historical pronunciation marks in $source",
                Regex("[\u064B-\u0652\u0670\u0640]").containsMatchIn(file.readText()))
        }
    }

    @Test fun flagsRejectDiacriticsTatweelAndDigits() {
        for (sample in listOf("نِبي", "نبيْ", "هــذا", "العدد 2", "عام ٢٠٢٦", "العدد ４"))
            assertTrue(sample, HabibiSpeechText.hasForbiddenMarksOrNumbers(sample))
        assertEquals("نبي مية", HabibiSpeechText.prepare("نِبِّي  مَيَّة"))
        assertEquals("أمي", HabibiSpeechText.prepare("ا\u0654مي"))
        assertEquals("نبي مية", HabibiSpeechText.prepare("نبي\u00a0\u00a0مية"))
    }
}
