package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Exports the fully resolved, exact speech strings used by the Android app. */
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
                val localized = DialectCatalog.localize(seed, dialect)
                require(localized.ttsPrompt.isNotBlank()) { "No speech for $dialect/$key" }
                result += Entry(dialect.name, key, localized.ttsPrompt)
            }
        }
        assertEquals(9, result.map { it.dialect }.distinct().size)
        assertEquals(result.size, result.map { it.dialect to it.key }.toSet().size)
        assertTrue(result.all { it.key.isNotBlank() && it.speech.isNotBlank() })
        val jsonData = result.joinToString(",\n", prefix = "{\n  \"version\": 1,\n  \"entries\": [\n", postfix = "\n  ]\n}\n") {
            "    {\"dialect\":${json(it.dialect)},\"semanticKey\":${json(it.key)},\"speechText\":${json(it.speech)}}"
        }
        File("build/speech_catalog.json").apply {
            parentFile.mkdirs()
            writeText(jsonData, Charsets.UTF_8)
        }
    }
}
