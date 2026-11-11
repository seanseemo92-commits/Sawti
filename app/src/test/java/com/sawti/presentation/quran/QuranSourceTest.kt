package com.sawti.presentation.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.app.Application

// org.json is part of the Android framework; plain JVM tests would use an
// unimplemented android.jar stub. Robolectric provides the real JSON behavior.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class QuranSourceTest {
    @Test
    fun verseAudioUsesGlobalQuranIndex() {
        assertEquals(
            "https://cdn.islamic.network/quran/audio/64/ar.alafasy/1.mp3",
            QuranSource.audioUrl(1),
        )
        assertEquals(
            "https://cdn.islamic.network/quran/audio/64/ar.alafasy/6236.mp3",
            QuranSource.audioUrl(6236),
        )
        assertThrows(IllegalArgumentException::class.java) { QuranSource.audioUrl(0) }
        assertThrows(IllegalArgumentException::class.java) { QuranSource.audioUrl(6237) }
        assertThrows(IllegalArgumentException::class.java) { QuranSource.checkAyah(291, 2, 284) }
    }

    @Test
    fun reciterChoicesExcludeSudaisAndOldSelectionFallsBackToAlafasy() {
        assertEquals(3, QuranReciter.entries.size)
        assertEquals(QuranReciter.ALAFASY, QuranReciter.fromEdition(null))
        assertEquals(QuranReciter.ALAFASY, QuranReciter.fromEdition("ar.sudais"))
        assertEquals(QuranReciter.SALAH_BUKHATIR, QuranReciter.fromEdition("salah.bukhatir"))
        assertEquals(QuranReciter.HUSARY_MUALLIM, QuranReciter.fromEdition("husary.muallim"))
    }

    @Test
    fun salahBukhatirVerseAudioUsesSurahLocalIndexAndWorkingMirror() {
        val urls = QuranSource.audioUrls(6205, QuranReciter.SALAH_BUKHATIR, 108, 1)
        assertEquals(2, urls.size)
        assertEquals(
            "https://everyayah.com/data/Salaah_AbdulRahman_Bukhatir_128kbps/108001.mp3",
            urls[0],
        )
        assertEquals(
            "https://huggingface.co/datasets/maqra-project/salah-bukhatir-128kbps/resolve/main/108001.mp3",
            urls[1],
        )
    }

    @Test
    fun husaryMuallimVerseAudioUsesSurahLocalIndexAndWorkingMirror() {
        val urls = QuranSource.audioUrls(1, QuranReciter.HUSARY_MUALLIM, 1, 1)
        assertEquals(2, urls.size)
        assertEquals(
            "https://everyayah.com/data/Husary_Muallim_128kbps/001001.mp3",
            urls[0],
        )
        assertEquals(
            "https://huggingface.co/datasets/maqra-project/mahmoud-al-husary-muallim-128kbps/resolve/main/001001.mp3",
            urls[1],
        )
        assertThrows(IllegalArgumentException::class.java) {
            QuranSource.audioUrls(1, QuranReciter.HUSARY_MUALLIM)
        }
        assertEquals(
            "https://cdn.islamic.network/quran/audio/64/ar.alafasy/1.mp3",
            QuranSource.audioUrl(1, QuranReciter.ALAFASY),
        )
    }

    @Test
    fun shortSurahsHaveUniqueIds() {
        assertEquals(13, QuranSource.shortSurahs.size)
        assertEquals(13, QuranSource.shortSurahs.map { it.number }.distinct().size)
        assertEquals(1, QuranSource.shortSurahs.first().number)
        assertEquals(7, QuranSource.shortSurahs.first().verseCount)
    }

    @Test
    fun parsesAndKeepsVerseTextVerbatim() {
        val source = """
            {"status":"OK","data":{"number":108,"numberOfAyahs":3,"ayahs":[
              {"number":6205,"numberInSurah":1,"text":"إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ"},
              {"number":6206,"numberInSurah":2,"text":"فَصَلِّ لِرَبِّكَ وَانْحَرْ"},
              {"number":6207,"numberInSurah":3,"text":"إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ"}
            ]}}
        """.trimIndent()
        val result = QuranSource.parse(source, ShortSurah(108, "الكوثر", 3))
        assertEquals(3, result.size)
        assertEquals("إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", result.first().uthmaniText)
        assertEquals(1, result.first().verseNumber)
        assertEquals(6205, result.first().globalNumber)
        assertThrows(IllegalArgumentException::class.java) {
            QuranSource.parse(source, ShortSurah(112, "الإخلاص", 4))
        }
    }

    @Test
    fun rejectsGlobalIdsFromAnotherSurahAndDuplicateGlobalIds() {
        val chapter = ShortSurah(108, "الكوثر", 3)
        fun response(ids: List<Int>) = """{"status":"OK","data":{"number":108,"numberOfAyahs":3,"ayahs":[
            {"number":${ids[0]},"numberInSurah":1,"text":"آية"},
            {"number":${ids[1]},"numberInSurah":2,"text":"آية"},
            {"number":${ids[2]},"numberInSurah":3,"text":"آية"}]}}"""
        assertThrows(IllegalArgumentException::class.java) {
            QuranSource.parse(response(listOf(1, 2, 3)), chapter)
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuranSource.parse(response(listOf(6205, 6205, 6207)), chapter)
        }
    }

    @Test
    fun rejectsWrongLocalGlobalCombinationBeforeRequestingAnyReciter() {
        QuranReciter.entries.forEach { reciter ->
            assertThrows(IllegalArgumentException::class.java) {
                QuranSource.audioUrls(1, reciter, 108, 1)
            }
            assertThrows(IllegalArgumentException::class.java) {
                QuranSource.audioUrls(6208, reciter, 108, 4)
            }
        }
    }
}
