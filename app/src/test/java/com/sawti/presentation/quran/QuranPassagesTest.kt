package com.sawti.presentation.quran

import android.app.Application
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class QuranPassagesTest {
    private val app: Application get() = RuntimeEnvironment.getApplication()

    @Test fun twoSelectionsFromTheSameSurahRemainDistinctAndKeepExistingSurahs() {
        assertEquals(13, QuranSource.shortSurahs.size)
        assertEquals(15, QuranSource.selections.size)
        assertEquals(15, QuranSource.selections.map { it.selectionId }.distinct().size)
        assertEquals(listOf("آية الكرسي", "خواتيم البقرة"), QuranSource.selections.takeLast(2).map { it.title })
        assertEquals(listOf(2, 2), QuranSource.selections.takeLast(2).map { it.number })
    }

    @Test fun fragmentsPreserveFullUthmaniTextAndOnlyTheFourApprovedAyahs() {
        val verses = QuranPassages.load(app, 2001) + QuranPassages.load(app, 2002)
        assertEquals(26, verses.size)
        assertEquals(26, verses.map { it.itemKey }.distinct().size)
        assertEquals(listOf(255, 284, 285, 286), verses.map { it.verseNumber }.distinct())
        val catalog = JSONObject(app.assets.open("quran_passages/catalog.json").bufferedReader().readText())
            .getJSONArray("verses")
        for (i in 0 until catalog.length()) {
            val entry = catalog.getJSONObject(i)
            val parts = verses.filter { it.verseNumber == entry.getInt("ayah") }
            assertEquals(entry.getString("text"), parts.joinToString(" ") { it.uthmaniText })
            assertEquals((1..parts.size).toList(), parts.map { it.segmentIndex })
            assertTrue(parts.all { it.segmentCount == parts.size && it.globalNumber == it.verseNumber + 7 })
        }
        assertThrows(IllegalArgumentException::class.java) { QuranSource.checkAyah(262, 2, 254) }
        assertThrows(IllegalArgumentException::class.java) { QuranSource.checkAyah(292, 2, 284) }
    }

    @Test fun allThreeRecitersHaveCompleteOfflineAudioWithIndependentFiles() = runBlocking {
        val verses = QuranPassages.load(app, 2001) + QuranPassages.load(app, 2002)
        val paths = mutableSetOf<String>()
        for (reciter in QuranReciter.entries) {
            val cache = QuranAudioCache(app, reciter) { error("Packaged passages must not use the network") }
            assertEquals(26, cache.cachedCount(verses))
            for (verse in verses) {
                val file = cache.obtain(verse)
                assertTrue(paths.add(file.absolutePath))
                assertTrue(QuranMp3.isPlayable(file))
            }
        }
        assertEquals(78, paths.size)
    }

    @Test fun damagedSegmentRestoresFromVerifiedAssetWithoutTouchingWholeAyah() = runBlocking {
        val verse = QuranPassages.load(app, 2001).first()
        val cache = QuranAudioCache(app) { error("Unexpected network request") }
        val whole = File(app.filesDir, "quran_recitations/ar.alafasy/ayah_262.mp3")
        whole.parentFile!!.mkdirs()
        whole.writeText("existing-whole-ayah-sentinel")
        val segment = cache.obtain(verse)
        val original = segment.readBytes()
        segment.writeText("partial")
        assertArrayEquals(original, cache.obtain(verse).readBytes())
        assertEquals("existing-whole-ayah-sentinel", whole.readText())
        cache.invalidate(verse)
        assertFalse(segment.exists())
        assertArrayEquals(original, cache.obtain(verse).readBytes())
        assertEquals("existing-whole-ayah-sentinel", whole.readText())
    }

    @Test fun forgedSegmentCannotResolveAnAssetOrEscapeItsPrivateDirectory() {
        val original = QuranPassages.load(app, 2001).first()
        assertThrows(IllegalArgumentException::class.java) {
            QuranPassages.target(app, original.copy(segmentIndex = 99), QuranReciter.ALAFASY)
        }
    }
}
