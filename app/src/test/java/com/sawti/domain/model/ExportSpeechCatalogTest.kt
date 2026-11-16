package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Exports Habibi-ready text (unvowelized) from the exact Kotlin dialect catalog. */
class ExportSpeechCatalogTest {
    data class Entry(val dialect: String, val key: String, val speech: String)
    private fun json(s: String) = buildString {
        append('"')
        for (char in s) when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (char.code < 32) append("\\u%04x".format(char.code)) else append(char)
        }
        append('"')
    }

    @Test fun exportAllNineDialectsWithUniqueKeysAndNonEmptyResolvedSpeech() {
        val seeded = WordSeed.itemEntities().map { it.toDomain() }
        val result = mutableListOf<Entry>()
        for (dialect in ArabicDialect.entries) {
            for (seed in seeded) {
                if (seed.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                    !RegionalDishesCatalog.visibleForDialect(seed.subgroup, dialect)) continue
                val key = requireNotNull(seed.semanticKey)
                val habibi = DialectSpeechCatalog.resolveForHabibi(seed, dialect)
                require(habibi.speechText.isNotBlank()) { "No Habibi speech for $dialect/$key" }
                result += Entry(dialect.name, key, habibi.speechText)
            }
        }
        assertEquals(9, result.map { it.dialect }.distinct().size)
        assertEquals(result.size, result.map { it.dialect to it.key }.toSet().size)
        assertTrue(result.all { it.key.isNotBlank() && it.speech.isNotBlank() })
        assertTrue("Habibi input must have no Arabic diacritics", result.all {
            it.speech == HabibiSpeechText.prepare(it.speech)
        })
        val jsonData = result.joinToString(",\n", prefix = "{\n  \"version\": 1,\n  \"entries\": [\n", postfix = "\n  ]\n}\n") {
            "    {\"dialect\":${json(it.dialect)},\"semanticKey\":${json(it.key)},\"speechText\":${json(it.speech)}}"
        }
        File("build/speech_catalog.json").apply {
            parentFile.mkdirs()
            writeText(jsonData, Charsets.UTF_8)
        }
    }
}
