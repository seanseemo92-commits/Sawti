package com.sawti.presentation.quran

import android.app.Application
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QuranFullTimingsTest {
    @Test fun differentReciterEditionsNeverShareUnverifiedTimeline() {
        assertEquals(123, QuranFullTimings.matchingRead(QuranReciter.ALAFASY))
        assertEquals(46, QuranFullTimings.matchingRead(QuranReciter.SALAH_BUKHATIR))
        assertNull(QuranFullTimings.matchingRead(QuranReciter.HUSARY_MUALLIM))
    }

    @Test fun shortSurahRequiresCompleteMonotonicVerseTimings() {
        val surah = QuranSource.shortSurahs.first { it.number == 112 }
        val complete = (1..4).joinToString(prefix="[",postfix="]") { n ->
            "{\"ayah\":$n,\"start_time\":${(n-1)*2000},\"end_time\":${n*2000}}"
        }
        val rows = QuranFullTimings.parseTiming(complete,surah)
        assertEquals(4,rows.size)
        assertEquals(Triple(1,0,2000),rows.first())
        assertEquals(Triple(4,6000,8000),rows.last())
        val missing = complete.replace(Regex(""",\\{\"ayah\":4[^}]+}"""),"")
        assertTrue(QuranFullTimings.parseTiming(missing,surah).isEmpty())
    }

    @Test fun finalTwoVersesRequireActual285And286Not284() {
        val selection = QuranSource.selections.first { it.selectionId == 2002 }
        val raw = """[{"ayah":284,"start_time":10,"end_time":30},{"ayah":285,"start_time":3000,"end_time":33000},{"ayah":286,"start_time":33000,"end_time":81000}]"""
        assertEquals(listOf(Triple(285,3000,33000),Triple(286,33000,81000)),
            QuranFullTimings.parseTiming(raw,selection))
    }

    @Test fun fullAyatKursiHasNineOriginalPhrasePositionsPerReciter() = runBlocking {
        val app = RuntimeEnvironment.getApplication() as Application
        val selection = QuranSource.selections.first { it.selectionId == 2001 }
        for (reader in QuranReciter.entries) {
            val cues = QuranFullTimings.obtain(app,selection,reader)
            assertEquals(9,cues.size)
            assertEquals("segment-2_255_1",cues.first().itemKey)
            assertEquals("segment-2_255_9",cues.last().itemKey)
            assertTrue(cues.zipWithNext().all { (a,b) -> a.startMs <= b.startMs })
        }
    }
}
