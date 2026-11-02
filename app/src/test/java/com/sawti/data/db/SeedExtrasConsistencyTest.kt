package com.sawti.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class SeedExtrasConsistencyTest {
    @Test fun everyConfiguredPromptMatchesCurrentSeed() {
        val keys = WordSeed.itemEntities().mapNotNull { it.semanticKey }.toSet()
        val stale = SeedExtras.configuredKeys() - keys
        assertTrue("Old key(s) found in SeedExtras: $stale", stale.isEmpty())
    }

    @Test fun freshInstallKeepsFushaSentenceHint() {
        val shahi = WordSeed.itemEntities().single {
            it.semanticKey == "request.tea"
        }
        assertEquals("نَشْرَبُ الـ...", shahi.sentencePrompt)
    }

    @Test fun noUngrammaticalActionPromptAndMelonGlyph() {
        for (action in WordSeed.itemEntities().filter { it.categoryId == "actions" }) {
            assertNull("Conjugated verb should not have a noun-cloze hint", action.sentencePrompt)
        }
        val melon = WordSeed.itemEntities().single {
            it.semanticKey == "fruit.melon"
        }
        assertEquals("🍈", melon.emoji)
        assertFalse(melon.emoji.contains("?"))
    }
}
