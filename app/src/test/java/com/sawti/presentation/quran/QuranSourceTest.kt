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
    }

    @Test
    fun sudaisUsesIndependentVerseAudioEdition() {
        assertEquals(
            "https://everyayah.com/data/Abdurrahmaan_As-Sudais_192kbps/001001.mp3",
            QuranSource.audioUrls(1, QuranReciter.SUDAIS, 1, 1).first(),
        )
        assertEquals(
            "https://verse.mp3quran.net/data/Abdurrahmaan_As-Sudais_192kbps/001001.mp3",
            QuranSource.audioUrls(1, QuranReciter.SUDAIS, 1, 1)[1],
        )
        assertEquals(
            "https://everyayah.com/data/Abdurrahmaan_As-Sudais_192kbps/108001.mp3",
            QuranSource.audioUrls(6205, QuranReciter.SUDAIS, 108, 1).first(),
        )
        assertThrows(IllegalArgumentException::class.java) {
            QuranSource.audioUrls(1, QuranReciter.SUDAIS)
        }
        assertEquals(
            "https://cdn.islamic.network/quran/audio/64/ar.alafasy/1.mp3",
            QuranSource.audioUrl(1, QuranReciter.ALAFASY),
        )
        assertEquals(QuranReciter.SUDAIS, QuranReciter.fromEdition("ar.sudais"))
        assertEquals(QuranReciter.ALAFASY, QuranReciter.fromEdition("unknown"))
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
}
