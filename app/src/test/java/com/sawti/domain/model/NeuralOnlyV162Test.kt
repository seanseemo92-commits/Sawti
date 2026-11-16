package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source-level parity, glyph preservation and explicit TTS eligibility. */
class NeuralOnlyV162Test {
    private val seeds by lazy {
        WordSeed.itemEntities().map { it.toDomain() }
            .associateBy { requireNotNull(it.semanticKey) }
    }

    data class Row(val dialect: ArabicDialect, val key: String, val display: String,
        val speech: String, val voice: String)

    private val rows by lazy {
        File("../scripts/neural_v162_overrides.csv").readLines(Charsets.UTF_8)
            .drop(1).filter { it.isNotBlank() }.map { line ->
                val fields = line.split(',', limit = 6)
                require(fields.size == 6) { "Invalid CSV: $"+"line" }
                Row(ArabicDialect.valueOf(fields[0]), fields[2], fields[4], fields[5], fields[3])
            }
    }

    @Test fun all2399NeuralRowsMatchRuntimeExactlyWithNoNormalization() {
        assertEquals(2399, rows.size)
        assertEquals(2399, rows.map { it.dialect to it.key }.toSet().size)
        assertEquals(271, rows.count { it.dialect == ArabicDialect.LIBYAN })
        for (dialect in ArabicDialect.entries.filter { it != ArabicDialect.LIBYAN })
            assertEquals(dialect.name, 266, rows.count { it.dialect == dialect })
        for (row in rows) {
            val seed = requireNotNull(seeds[row.key]) { "Missing seed $"+"{row.key}" }
            val expected = requireNotNull(NeuralLexicon162.get(row.dialect, row.key))
            assertEquals("$"+"{row.dialect}/$"+"{row.key} display table", row.display, expected.displayText)
            assertEquals("$"+"{row.dialect}/$"+"{row.key} speech table", row.speech, expected.speechText)
            val form = DialectSpeechCatalog.resolve(seed, row.dialect)
            assertEquals("$"+"{row.dialect}/$"+"{row.key} display runtime", row.display, form.displayText)
            assertEquals("$"+"{row.dialect}/$"+"{row.key} speech runtime", row.speech, form.speechText)
            assertEquals(row.speech, DialectSpeechCatalog.neuralTextFor(seed, row.dialect))
            assertFalse("Invalid shadda+sukun", "ّْ" in row.speech)
        }
    }

    @Test fun dialectalSukunConsonantsAndStandardInflectionsStayExact() {
        val examples = listOf(
            ArabicDialect.LIBYAN to "action.sit",
            ArabicDialect.LIBYAN to "action.sleep",
            ArabicDialect.IRAQI to "food.tea",
            ArabicDialect.IRAQI to "food.fish",
            ArabicDialect.STANDARD to "clothing.high_heels",
            ArabicDialect.LIBYAN to "dish.osban",
        )
        for ((dialect, key) in examples) {
            val row = requireNotNull(rows.singleOrNull { it.dialect == dialect && it.key == key })
            val seed = requireNotNull(seeds[key])
            assertEquals(row.speech, DialectSpeechCatalog.neuralTextFor(seed, dialect))
        }
        assertEquals("نْجَعْمِزْ", NeuralLexicon162.get(ArabicDialect.LIBYAN, "action.sit")?.speechText)
        assertTrue(rows.any { it.speech.startsWith("تْش") })
        assertTrue(rows.any { it.speech.endsWith('ْ') })
        assertTrue(rows.any { it.speech.contains("حِذَاءٌ بِكَعْبٍ عَالٍ") })
    }


    @Test fun libyanHardGVariantsAreExplicitAndDoNotAffectOtherDialectsOrFormalQaf() {
        val variants = File("../scripts/libyan_shakir_hard_g_review.csv")
            .readLines(Charsets.UTF_8).drop(1).filter { it.isNotBlank() }
            .map { line ->
                val fields = line.split(',', limit = 8)
                require(fields.size == 8) { "Bad hard-g row: $"+ "line" }
                fields
            }
        assertEquals(21, variants.size)
        assertEquals(21, variants.map { it[0] }.toSet().size)
        for (fields in variants) {
            val key = fields[0]
            val display = fields[1]
            val source = fields[2]
            val target = fields[3]
            val voice = fields[4]
            val rate = fields[5]
            val pitch = fields[6]
            val status = fields[7]
            assertEquals("ar-EG-ShakirNeural", voice)
            assertEquals("-8%", rate)
            assertEquals("+12Hz", pitch)
            assertEquals("needs_native_listening", status)
            assertTrue(source.contains('ق'))
            assertEquals(source.replace('ق', 'ج'), target)
            assertFalse(target.contains('ق'))
            val item = requireNotNull(seeds[key])
            val entry = requireNotNull(NeuralLexicon162.get(ArabicDialect.LIBYAN, key))
            assertEquals(display, entry.displayText)
            assertEquals(target, entry.speechText)
            assertEquals(target, DialectSpeechCatalog.neuralTextFor(item, ArabicDialect.LIBYAN))
            assertEquals(target, DialectSpeechCatalog.resolve(item, ArabicDialect.LIBYAN).speechText)
        }
        // Formal vocabulary may genuinely retain /q/; the change is not global.
        assertEquals("قِطَارْ", NeuralLexicon162.get(ArabicDialect.LIBYAN, "transport.train")?.speechText)
        assertEquals("قَارِبْ", NeuralLexicon162.get(ArabicDialect.LIBYAN, "transport.boat")?.speechText)
        // The one listener-validated word retains its exact approved TTS input.
        assertEquals("بُجْرَهْ", NeuralLexicon162.get(ArabicDialect.LIBYAN, "animal.cow")?.speechText)
        assertEquals("بقرة", NeuralLexicon162.get(ArabicDialect.LIBYAN, "animal.cow")?.displayText)
        // Standard Arabic unaffected by dialect-specific voice spellings.
        assertEquals("بَقَرَةٌ", NeuralLexicon162.get(ArabicDialect.STANDARD, "animal.cow")?.speechText)
    }

    @Test fun originalCardsAreNeuralOnlyAndPersonalEditsRemainTtsEligible() {
        val source = requireNotNull(seeds["request.toilet"])
        assertTrue(DialectSpeechCatalog.isNeuralOnly(source))
        val localized = DialectCatalog.localize(source, ArabicDialect.LIBYAN)
        val piece = SentenceComposer.piece(localized)
        assertTrue(piece.neuralOnly)
        assertNotNull(piece.neuralSpeechText)
        assertEquals(piece.neuralSpeechText, SentenceComposer.spokenPieces(listOf(piece))
            .single().neuralSpeechText)
        for (flag in listOf(CardDetails.LABEL, CardDetails.SPEECH,
            CardDetails.LABEL or CardDetails.SPEECH)) {
            val edited = source.copy(customTextFields = flag, ttsPrompt = "نطقي الخاص")
            assertFalse(DialectSpeechCatalog.isNeuralOnly(edited))
            assertNull(DialectSpeechCatalog.neuralTextFor(edited, ArabicDialect.LIBYAN))
            assertFalse(SentenceComposer.piece(edited).neuralOnly)
        }
        val custom = source.copy(categoryId = "custom_family",
            semanticKey = "family.test")
        assertFalse(DialectSpeechCatalog.isNeuralOnly(custom))
        assertNull(DialectSpeechCatalog.neuralTextFor(custom, ArabicDialect.LIBYAN))
    }
}
