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
            // Historical rows remain as independent 245-case coverage; updated
            // v1.6.1 forms intentionally supersede their old expected spellings.
            val upgrade = NeuralLexicon161.forms[row.key]?.get(row.dialect)
            val expectedDisplay = upgrade?.displayText ?: row.display
            assertEquals("$context display", expectedDisplay, form.displayText)
            val clean = HabibiSpeechText.prepare(expectedDisplay)
            assertEquals("$context speech", clean, form.speechText)
            assertTrue("$context did not use a built-in regional word", form.source != SpeechSource.USER)
            assertTrue("$context cue is empty", form.phonemicCue.isNotBlank())
            val derivedCue = PhonemicCue.derive(clean)
            if (derivedCue.isNotBlank())
                assertEquals("$context cue", derivedCue, form.phonemicCue)
            val projected = DialectCatalog.localize(seed, row.dialect)
            assertEquals("$context localized display", expectedDisplay, projected.displayText)
            assertEquals("$context localized speech", clean, projected.ttsPrompt)
        }
    }

    @Test fun userChangesNeverGetReplacedByThe245WrittenCandidates() {
        val seed = requireNotNull(seeds["body.nose"]).copy(
            displayText = "كلمة خاصة",
            ttsPrompt = "صوتي الخاص",
            phonemicCue = "ص...",
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
