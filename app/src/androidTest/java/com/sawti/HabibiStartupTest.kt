package com.sawti

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.DialectSpeechCatalog
import com.sawti.domain.model.HabibiSpeechText
import com.sawti.domain.model.RegionalDishesCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on ART/Android ICU: host JVM regex tests cannot detect Android-only flags. */
@RunWith(AndroidJUnit4::class)
class HabibiStartupTest {
    @Test fun textPreparationAndAll2399EntriesWorkOnAndroid() {
        val whitespace = "\t\n\u000B\u000C\r \u0085\u00A0\u1680\u2000\u2001\u2002" +
            "\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u2028\u2029\u202F\u205F\u3000"
        for (space in whitespace) {
            assertEquals("U+${space.code.toString(16)}", "نبي مية",
                HabibiSpeechText.prepare("نِبِّي${space}${space}مَيَّة"))
        }
        val all = WordSeed.itemEntities().map { it.toDomain() }
        val identities = mutableSetOf<Pair<ArabicDialect, String>>()
        for (dialect in ArabicDialect.entries) for (seed in all) {
            if (seed.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                !RegionalDishesCatalog.visibleForDialect(seed.subgroup, dialect)) continue
            val form = DialectSpeechCatalog.resolveForHabibi(seed, dialect)
            assertEquals(form.displayText, form.speechText)
            assertFalse(HabibiSpeechText.hasForbiddenMarksOrNumbers(form.speechText))
            assertTrue(identities.add(dialect to requireNotNull(seed.semanticKey)))
        }
        assertEquals(2399, identities.size)
    }

    @Test fun previewOpensLoadsCategoriesAndRecreates() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val app = ApplicationProvider.getApplicationContext<SawtiApp>()
            val categories = runBlocking {
                withTimeout(30_000) {
                    app.container.wordRepository.observeCategories().first { it.isNotEmpty() }
                }
            }
            assertTrue(categories.any { it.items.isNotEmpty() })
            scenario.onActivity { assertEquals(StartupStatus.READY, app.startup.value) }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.recreate()
            scenario.onActivity { assertFalse(it.isFinishing) }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
