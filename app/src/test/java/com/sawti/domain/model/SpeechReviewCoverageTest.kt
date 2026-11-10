package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import java.io.File
import java.security.MessageDigest
import org.junit.Assert.*
import org.junit.Test

/** Generates the actual projected review queue, without treating text checks as auditory evidence. */
class SpeechReviewCoverageTest {
    @Test fun exportEveryVisibleConceptAndDialectWithIndependentReviewGates() {
        val seeds = WordSeed.itemEntities().map { it.toDomain() }
        val keys = seeds.mapNotNull { it.semanticKey }.toSet()
        assertTrue(DialectCatalog.words.keys.all { it in keys })
        assertTrue(DialectCatalog.words.values.all { ArabicDialect.STANDARD !in it })
        val rows = mutableListOf<String>()
        val seen = mutableSetOf<Pair<ArabicDialect, String>>()
        for (dialect in ArabicDialect.entries) {
            for (source in seeds.filter {
                it.categoryId != CategoryIds.CUISINE || RegionalDishesCatalog.visibleForDialect(it.subgroup, dialect)
            }) {
                val key = requireNotNull(source.semanticKey)
                assertTrue(seen.add(dialect to key))
                val form = DialectSpeechCatalog.resolve(source, dialect)
                val projected = DialectCatalog.localize(source, dialect)
                assertEquals(form.displayText, projected.displayText)
                assertEquals(form.speechText, projected.ttsPrompt)
                assertEquals(form.phonemicCue, projected.phonemicCue)
                assertTrue(form.speechText.isNotBlank())
                assertTrue("No native evidence may be inferred for $dialect/$key", form.needsNativeReview)
                assertTrue("No device evidence may be inferred for $dialect/$key", form.needsTtsListening)
                val payload = listOf(dialect.name, key, form.displayText, form.speechText, form.phonemicCue,
                    projected.sentencePrompt.orEmpty()).joinToString("\t")
                val fingerprint = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
                rows += listOf(payload, form.source.name, "PENDING", "PENDING", fingerprint).joinToString("\t")
            }
        }
        // 237 shared concepts x 9 dialects + 140 distinct regional dishes.
        assertEquals(2273, rows.size)
        assertEquals(9, seen.map { it.first }.distinct().size)
        val report = File("build/reports/speech-review.tsv")
        report.parentFile.mkdirs()
        report.writeText("dialect\tsemantic_key\tdisplay\tspeech\tcue\tsentence_hint\tsource\tnative_review\ttts_listening\ttext_sha256\n" +
            rows.joinToString("\n", postfix = "\n"))
    }

    @Test fun everyPreviewUsesTheSameWaterSpeechAsTheRealCard() {
        val sample = WordSeed.itemEntities().single { it.semanticKey == "request.water" }.toDomain()
        for (dialect in ArabicDialect.entries) {
            val preview = DialectSpeechCatalog.resolve(sample, dialect)
            val card = DialectCatalog.localize(sample, dialect)
            assertEquals(card.displayText, preview.displayText)
            assertEquals(card.ttsPrompt, preview.speechText)
            assertNotEquals(SpeechSource.UNVOCALIZED_FALLBACK, preview.source)
        }
    }

    @Test fun standardRegionalSpeechCannotDriftAwayFromCanonicalSeed() {
        val seeds = WordSeed.itemEntities().associateBy { it.semanticKey }
        RegionalDishesCatalog.forDialect(ArabicDialect.STANDARD).dishes.forEach {
            assertEquals(it.semanticKey, seeds.getValue(it.semanticKey).displayText, it.displayText)
            assertEquals(it.semanticKey, seeds.getValue(it.semanticKey).ttsPrompt, it.ttsPrompt)
        }
    }

    @Test fun dailyCommunicationAlwaysHasAnExplicitSpeechCandidateAndTargetCue() {
        val requests = WordSeed.itemEntities().filter { it.categoryId == CategoryIds.DAILY }
        assertEquals(8, requests.size)
        for (dialect in ArabicDialect.entries) for (request in requests) {
            val form = DialectSpeechCatalog.resolve(request.toDomain(), dialect)
            assertNotEquals("$dialect/${request.semanticKey}", SpeechSource.UNVOCALIZED_FALLBACK, form.source)
            assertTrue(form.phonemicCue.isNotBlank())
            assertTrue(form.needsNativeReview)
            assertTrue(form.needsTtsListening)
        }
    }
}
