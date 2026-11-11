package com.sawti.presentation.quran

import android.app.Application
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Offline behavior must not depend on either the audio CDN or the Uthmani text API. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class QuranBundledSurahsTest {
    @Test fun allShortSurahVersesAndReadersAreAvailableInsideApk() = runBlocking {
        val app = RuntimeEnvironment.getApplication() as Application
        assertEquals(13, QuranSource.shortSurahs.size)
        var verified = 0
        for (surah in QuranSource.shortSurahs) {
            val verses = QuranSource.load(app, surah)
            assertEquals(surah.verseCount, verses.size)
            for (verse in verses) {
                assertTrue(verse.uthmaniText.isNotBlank())
                QuranReciter.entries.forEach { reader ->
                    assertTrue("Missing bundled ${reader.editionId} verse ${verse.globalNumber}",
                        QuranBundledSurahs.hasRecording(app, verse.globalNumber, reader))
                    verified++
                }
            }
        }
        assertEquals(201, verified)
    }

    @Test fun canPlayFirstAyahOfflineAndRecoverDamagedPrivateCopy() = runBlocking {
        val app = RuntimeEnvironment.getApplication() as Application
        QuranReciter.entries.forEach { reader ->
            val cache = QuranAudioCache(app, reader, usePackagedSurahs = true) {
                throw IOException("Network must never be used for bundled surahs")
            }
            val file = cache.obtain(1, 1, 1)
            assertTrue(file.isFile)
            assertTrue(QuranMp3.isPlayable(file))
            assertTrue(cache.cachedCount(QuranSource.load(app, QuranSource.shortSurahs.first())) == 7)
            file.writeBytes(ByteArray(3000))
            val repaired = cache.obtain(1, 1, 1)
            assertEquals(file.path, repaired.path)
            assertTrue(QuranMp3.isPlayable(repaired))
            File(repaired.parentFile, repaired.name).delete()
        }
    }
}
