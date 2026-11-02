package com.sawti.presentation.common

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Source guards complement, but do not replace, runtime/UI regression tests. */
class RetiredHintSourceTest {
    private fun sourceRoot(): File = listOf(File("src/main/java"), File("app/src/main/java"))
        .firstOrNull { it.isDirectory } ?: error("Cannot locate main Kotlin sources")

    @Test fun retiredHintSymbolsAreAbsentFromRuntimeSources() {
        val retired = Regex("\\b(PhraseHintButton|onSentenceHint|sentenceHint|ChatBubbleOutline)\\b")
        val violations = sourceRoot().walkTopDown().filter { it.isFile && it.extension == "kt" }
            .filter { retired.containsMatchIn(it.readText()) }.map { it.path }.toList()
        assertTrue("Retired hint symbols remain in: $violations", violations.isEmpty())
    }
    @Test fun phonemicHintAndSentenceStripRemain() {
        val root = sourceRoot()
        val components = root.resolve("com/sawti/presentation/common/Components.kt").readText()
        val audio = root.resolve("com/sawti/audio/AudioDispatcher.kt").readText()
        assertTrue(components.contains("fun HintButton("))
        assertTrue(audio.contains("fun hint(item: WordItem)"))
        assertTrue(audio.contains("fun speakSentence("))
    }
}
