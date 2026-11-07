package com.sawti.presentation.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
