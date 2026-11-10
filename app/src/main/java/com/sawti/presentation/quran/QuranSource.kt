package com.sawti.presentation.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ShortSurah(val number: Int, val name: String, val verseCount: Int)

data class QuranVerse(
    val surahNumber: Int,
    val verseNumber: Int,
    val globalNumber: Int,
    val uthmaniText: String,
)

object QuranSource {
    // First-release set of shorter chapters, with al-Fatihah first.
    val shortSurahs = listOf(
        ShortSurah(1, "الفاتحة", 7),
        ShortSurah(112, "الإخلاص", 4),
        ShortSurah(113, "الفلق", 5),
        ShortSurah(114, "الناس", 6),
        ShortSurah(108, "الكوثر", 3),
        ShortSurah(103, "العصر", 3),
        ShortSurah(109, "الكافرون", 6),
        ShortSurah(110, "النصر", 3),
        ShortSurah(111, "المسد", 5),
        ShortSurah(105, "الفيل", 5),
        ShortSurah(106, "قريش", 4),
        ShortSurah(107, "الماعون", 7),
        ShortSurah(104, "الهمزة", 9),
    )

    private const val API_BASE = "https://api.alquran.cloud/v1/surah"

    // Islamic Network's CDN uses the global Quran verse number, NOT the local surah index.
    fun audioUrl(globalVerseNumber: Int): String =
        audioUrl(globalVerseNumber, QuranReciter.ALAFASY)

    // Alafasy uses its global verse ID; the other reciters use surah + local ayah.
    fun audioUrl(globalVerseNumber: Int, reciter: QuranReciter = QuranReciter.ALAFASY): String {
        require(globalVerseNumber in 1..6236)
        require(reciter == QuranReciter.ALAFASY) {
            "Use audioUrls with the surah and local verse for this reciter"
        }
        return "https://cdn.islamic.network/quran/audio/64/ar.alafasy/$globalVerseNumber.mp3"
    }

    fun audioUrls(
        globalVerseNumber: Int,
        reciter: QuranReciter,
        surahNumber: Int? = null,
        verseNumber: Int? = null,
    ): List<String> {
        require(globalVerseNumber in 1..6236)
        if (reciter == QuranReciter.ALAFASY) return listOf(audioUrl(globalVerseNumber))
        require(surahNumber != null && surahNumber in 1..114)
        require(verseNumber != null && verseNumber in 1..286)
        val filename = surahNumber.toString().padStart(3, '0') +
            verseNumber.toString().padStart(3, '0') + ".mp3"
        val (folder, mirror) = when (reciter) {
            QuranReciter.SALAH_BUKHATIR ->
                "Salaah_AbdulRahman_Bukhatir_128kbps" to "salah-bukhatir-128kbps"
            QuranReciter.HUSARY_MUALLIM ->
                "Husary_Muallim_128kbps" to "mahmoud-al-husary-muallim-128kbps"
            QuranReciter.ALAFASY -> error("Handled above")
        }
        return listOf(
            "https://everyayah.com/data/$folder/$filename",
            "https://huggingface.co/datasets/maqra-project/$mirror/resolve/main/$filename",
        )
    }

    suspend fun load(context: Context, surah: ShortSurah): List<QuranVerse> =
        withContext(Dispatchers.IO) {
            // Download once; subsequent visits can display verified text without internet.
            // Recordings are cached separately in filesDir by QuranAudioCache. None are bundled in the APK.
            val dir = File(context.filesDir, "quran_uthmani_cache")
            val file = File(dir, "surah_${surah.number}.json")
            if (file.isFile) {
                try {
                    return@withContext parse(file.readText(Charsets.UTF_8), surah)
                } catch (_: Exception) {
                    file.delete()
                }
            }
            val connection = (URL("$API_BASE/${surah.number}/quran-uthmani")
                .openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    error("تعذّر تحميل السورة (HTTP ${connection.responseCode})")
                }
                val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val checked = parse(response, surah)
                dir.mkdirs()
                val tmp = File(dir, "surah_${surah.number}.tmp")
                tmp.writeText(response, Charsets.UTF_8)
                if (!tmp.renameTo(file)) {
                    file.writeText(response, Charsets.UTF_8)
                    tmp.delete()
                }
                checked
            } finally {
                connection.disconnect()
            }
        }

    // Text is displayed verbatim as received; the source isn't edited or re-diacritized.
    internal fun parse(response: String, surah: ShortSurah): List<QuranVerse> {
        val root = JSONObject(response)
        require(root.optString("status").equals("OK", ignoreCase = true)) { "Unexpected Quran response" }
        val data = root.getJSONObject("data")
        require(data.getInt("number") == surah.number) { "Mismatched surah" }
        require(data.getInt("numberOfAyahs") == surah.verseCount) { "Unexpected verse count" }
        val rawVerses = data.getJSONArray("ayahs")
        require(rawVerses.length() == surah.verseCount) { "Incomplete Quran response" }
        return (0 until rawVerses.length()).map { index ->
            val ayah = rawVerses.getJSONObject(index)
            val local = ayah.getInt("numberInSurah")
            val global = ayah.getInt("number")
            val text = ayah.getString("text")
            require(local == index + 1 && global in 1..6236 && text.isNotBlank()) {
                "Unexpected verse data"
            }
            QuranVerse(surah.number, local, global, text)
        }
    }
}
