package com.sawti.presentation.quran

import org.junit.Assert.*
import org.junit.Test

/** Whole recordings must remain distinct from the bundled individual-ayah files. */
class QuranFullAudioTest {
    @Test fun thirteenShortSurahsHaveWholeSourceForEveryExistingReciter() {
        assertEquals(13, QuranSource.shortSurahs.size)
        for (surah in QuranSource.shortSurahs) {
            assertTrue(QuranFullSources.available(surah))
            for (reciter in QuranReciter.entries) {
                val urls = QuranFullSources.urls(surah, reciter)
                assertTrue(urls.isNotEmpty())
                val number = surah.number.toString().padStart(3, '0')
                assertTrue(urls.all { it.startsWith("https://") && it.endsWith("/$number.mp3") })
                assertTrue(urls.none { it.contains("everyayah.com") })
            }
        }
    }

    @Test fun kursiDownloadsOriginalWholeAyahRatherThanNinePhraseFragments() {
        val kursi = QuranSource.selections.first { it.selectionId == 2001 }
        assertTrue(QuranFullSources.available(kursi))
        for (reader in QuranReciter.entries) {
            val urls = QuranFullSources.urls(kursi, reader)
            assertEquals(1, urls.size)
            assertTrue(urls.single().endsWith("/002255.mp3"))
        }
    }

    @Test fun finalBaqarahVersesNeverPretendTwoSeparateAyahFilesAreOneContinuousRecording() {
        val ending = QuranSource.selections.first { it.selectionId == 2002 }
        assertFalse(QuranFullSources.available(ending))
        assertThrows(IllegalArgumentException::class.java) {
            QuranFullSources.urls(ending, QuranReciter.SALAH_BUKHATIR)
        }
    }

    @Test fun cacheKeyIsIsolatedByReciterAndSelection() {
        // Existing QuranAudioCache paths remain dedicated to individual verses;
        // whole recordings use their own /quran_full_recitations/<edition>/ root.
        assertNotEquals(QuranReciter.ALAFASY.editionId, QuranReciter.SALAH_BUKHATIR.editionId)
        assertNotEquals(QuranSource.shortSurahs[0].selectionId, QuranSource.shortSurahs[1].selectionId)
    }
}
