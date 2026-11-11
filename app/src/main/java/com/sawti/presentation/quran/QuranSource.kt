package com.sawti.presentation.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException

data class ShortSurah(
    val number: Int,
    val name: String,
    val verseCount: Int,
    val selectionId: Int = number,
    val segmentCount: Int? = null,
) {
    val segmented: Boolean get() = segmentCount != null
    val title: String get() = if (segmented) name else "سورة $name"
    val countLabel: String get() = when (selectionId) {
        2001 -> "الآية 255 · $segmentCount مقاطع"
        2002 -> "الآيات 284–286 · $segmentCount مقطعًا"
        else -> "$verseCount آيات"
    }
}

data class QuranVerse(
    val surahNumber: Int,
    val verseNumber: Int,
    val globalNumber: Int,
    val uthmaniText: String,
    val segmentIndex: Int? = null,
    val segmentCount: Int? = null,
) {
    val segmentKey: String? get() = segmentIndex?.let { "${surahNumber}_${verseNumber}_$it" }
    val itemKey: String get() = segmentKey?.let { "segment-$it" } ?: "ayah-$globalNumber"
    val label: String get() = segmentIndex?.let { "الآية $verseNumber · المقطع $it من $segmentCount" }
        ?: "الآية $verseNumber"
}

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

    // Selection IDs are distinct from surah IDs: both passages belong to Al-Baqarah.
    val selections = shortSurahs + listOf(
        ShortSurah(2, "آية الكرسي", 1, selectionId = 2001, segmentCount = 9),
        ShortSurah(2, "خواتيم البقرة", 3, selectionId = 2002, segmentCount = 17),
    )

    private const val API_BASE = "https://api.alquran.cloud/v1/surah"
    private const val MAX_TEXT_BYTES = 256 * 1024
    // Canonical Hafs global ayah numbering for the supported first-release surahs.
    private val firstAyahs = mapOf(
        1 to 1, 103 to 6177, 104 to 6180, 105 to 6189, 106 to 6194,
        107 to 6198, 108 to 6205, 109 to 6208, 110 to 6214,
        111 to 6217, 112 to 6222, 113 to 6226, 114 to 6231,
    )

    internal fun checkAyah(global: Int, surah: Int, local: Int) {
        if (surah == 2) {
            require(local in setOf(255, 284, 285, 286) && global == local + 7) {
                "Ayah does not belong to the selected Al-Baqarah passages"
            }
            return
        }
        val chapter = shortSurahs.firstOrNull { it.number == surah }
            ?: throw IllegalArgumentException("Unsupported surah")
        require(local in 1..chapter.verseCount && global == firstAyahs.getValue(surah) + local - 1) {
            "Ayah does not belong to the requested surah"
        }
    }

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
        if (surahNumber != null && verseNumber != null) {
            checkAyah(globalVerseNumber, surahNumber, verseNumber)
        }
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
            if (surah.segmented) return@withContext QuranPassages.load(context, surah.selectionId)
            // Download once; subsequent visits can display verified text without internet.
            // Recordings are cached separately in filesDir by QuranAudioCache. None are bundled in the APK.
            val dir = File(context.filesDir, "quran_uthmani_cache")
            val file = File(dir, "surah_${surah.number}.json")
            if (file.isFile) {
                try {
                    require(file.length() <= MAX_TEXT_BYTES) { "Cached Quran response too large" }
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
                if (connection.contentLengthLong > MAX_TEXT_BYTES) throw IOException("Quran response too large")
                val response = connection.inputStream.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (output.size() + count > MAX_TEXT_BYTES) throw IOException("Quran response too large")
                        output.write(buffer, 0, count)
                    }
                    output.toString("UTF-8")
                }
                val checked = parse(response, surah)
                currentCoroutineContext().ensureActive()
                dir.mkdirs()
                val tmp = File.createTempFile("surah_${surah.number}_", ".tmp", dir)
                try {
                    tmp.writeText(response, Charsets.UTF_8)
                    currentCoroutineContext().ensureActive()
                    if (!tmp.renameTo(file)) throw IOException("Cannot save Quran text")
                } finally {
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
            checkAyah(global, surah.number, local)
            require(local == index + 1 && global in 1..6236 && text.isNotBlank()) {
                "Unexpected verse data"
            }
            QuranVerse(surah.number, local, global, text)
        }
    }
}
