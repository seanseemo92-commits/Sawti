package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Independent 245-row exact-text snapshot; written coverage does not imply audible approval. */
class Complete245DialectSpeechTest {
    private val seeds by lazy {
        WordSeed.itemEntities().map { it.toDomain() }.associateBy { requireNotNull(it.semanticKey) }
    }

    private data class Expected(val dialect: ArabicDialect, val key: String,
                                val display: String, val speech: String)

    private fun rows(): List<Expected> {
        val stream = requireNotNull(javaClass.getResourceAsStream("/complete-245-dialect-speech.tsv"))
        val lines = stream.bufferedReader(Charsets.UTF_8).use { it.readLines() }
        assertEquals("dialect\tsemantic_key\tdisplay\tspeech", lines.first())
        return lines.drop(1).filter { it.isNotBlank() }.map { line ->
            val columns = line.split('\t')
            assertEquals("Malformed fixture: $line", 4, columns.size)
            Expected(ArabicDialect.valueOf(columns[0]), columns[1], columns[2], columns[3])
        }
    }

    @Test fun all245WrittenDisplaySpeechPairsAndCuesMatchTheRuntime() {
        val expected = rows()
        assertEquals(245, expected.size)
        assertEquals(245, expected.map { it.dialect to it.key }.toSet().size)
        assertEquals(
            mapOf(
                ArabicDialect.SAUDI to 20, ArabicDialect.IRAQI to 23,
                ArabicDialect.SYRIAN to 27, ArabicDialect.EGYPTIAN to 29,
                ArabicDialect.TUNISIAN to 45, ArabicDialect.ALGERIAN to 49,
                ArabicDialect.MOROCCAN to 52,
            ),
            expected.groupingBy { it.dialect }.eachCount(),
        )
        for (row in expected) {
            val seed = requireNotNull(seeds[row.key]) { "Missing seed: ${row.key}" }
            val form = DialectSpeechCatalog.resolve(seed, row.dialect)
            val context = "${row.dialect}/${row.key}"
            assertEquals("$context display", row.display, form.displayText)
            val clean = DialectSpeechCatalog.cleanSpeech(row.display, row.speech, row.dialect)
            assertEquals("$context speech", clean, form.speechText)
            assertEquals("$context source", SpeechSource.EXPLICIT_DIALECT, form.source)
            assertTrue("$context cue is empty", form.phonemicCue.isNotBlank())
            val derivedCue = PhonemicCue.derive(clean)
            if (derivedCue.isNotBlank())
                assertEquals("$context cue", derivedCue, form.phonemicCue)
            val projected = DialectCatalog.localize(seed, row.dialect)
            assertEquals("$context localized display", row.display, projected.displayText)
            assertEquals("$context localized speech", clean, projected.ttsPrompt)
        }
    }

    @Test fun userChangesNeverGetReplacedByThe245WrittenCandidates() {
        val seed = requireNotNull(seeds["body.nose"]).copy(
            displayText = "كلمة خاصة",
            ttsPrompt = "صَوْتِي الخَاصّ",
            phonemicCue = "صَـ...",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH or CardDetails.CUE,
        )
        for (dialect in listOf(ArabicDialect.SAUDI, ArabicDialect.IRAQI,
                              ArabicDialect.SYRIAN, ArabicDialect.EGYPTIAN,
                              ArabicDialect.TUNISIAN, ArabicDialect.ALGERIAN,
                              ArabicDialect.MOROCCAN)) {
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            assertEquals(seed.displayText, form.displayText)
            assertEquals(seed.ttsPrompt, form.speechText)
            assertEquals(seed.phonemicCue, form.phonemicCue)
            assertNotEquals(SpeechSource.UNVOCALIZED_FALLBACK, form.source)
        }
    }
}
