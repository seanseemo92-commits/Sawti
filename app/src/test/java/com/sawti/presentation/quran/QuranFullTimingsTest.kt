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

    @Test fun fatihaAlafasyIntroRemainsUntrackedUntilBasmalaAt6420ms() {
        val fatiha = QuranSource.shortSurahs.first { it.number == 1 }
        val rows = listOf(Triple(1,6420,11120),Triple(2,11120,17640),
            Triple(3,17640,21280),Triple(4,21280,26800),
            Triple(5,26800,33280),Triple(6,33280,38960),Triple(7,38960,51200))
        val cues = QuranFullTimings.shortSurahCues(fatiha,rows)
        assertEquals("ayah-1",cues.first().itemKey)
        assertEquals(6420,cues.first().startMs)
        assertEquals(11120,cues.first().endMs)
        assertEquals(11120,cues[1].startMs)
        // Before basmala (including the opening ta'awwudh), NO verse is active.
        for (time in listOf(0,1000,4000,6419)) {
            assertNull(cues.firstOrNull { time >= it.startMs && time < it.endMs })
        }
        assertEquals("ayah-1",
            cues.firstOrNull { 6420 >= it.startMs && 6420 < it.endMs }?.itemKey)
        assertEquals("ayah-2",
            cues.firstOrNull { 11120 >= it.startMs && 11120 < it.endMs }?.itemKey)
    }

    @Test fun fatihaSalahIntroRemainsUntrackedUntilBasmalaAt3860ms() {
        val fatiha = QuranSource.shortSurahs.first { it.number == 1 }
        val rows = listOf(Triple(1,3860,7080),Triple(2,7080,12360),
            Triple(3,12360,16640),Triple(4,16640,20380),
            Triple(5,20380,26480),Triple(6,26480,31120),Triple(7,31120,45540))
        val cues = QuranFullTimings.shortSurahCues(fatiha,rows)
        assertEquals(3860,cues.first().startMs)
        assertNull(cues.firstOrNull { 3859 >= it.startMs && 3859 < it.endMs })
        assertEquals("ayah-1",
            cues.firstOrNull { 3860 >= it.startMs && 3860 < it.endMs }?.itemKey)
        assertEquals("ayah-2",
            cues.firstOrNull { 7080 >= it.startMs && 7080 < it.endMs }?.itemKey)
    }

    @Test fun shortSurahTimingsDoNotShiftExistingNonFatihaVerseOnsets() {
        val ikhlas = QuranSource.shortSurahs.first { it.number == 112 }
        val cues = QuranFullTimings.shortSurahCues(ikhlas,
            listOf(Triple(1,500,2800),Triple(2,2800,5200),
                Triple(3,5200,7600),Triple(4,7600,9900)))
        assertEquals(500,cues.first().startMs)
        assertEquals(2800,cues[1].startMs)
        assertEquals(9900,cues.last().endMs)
        assertEquals("ayah-6222",cues.first().itemKey)
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
        val missing = """[{"ayah":1,"start_time":0,"end_time":2000},{"ayah":2,"start_time":2000,"end_time":4000},{"ayah":3,"start_time":4000,"end_time":6000}]"""
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
