package com.sawti.presentation.quran

import android.content.Context
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONArray
import org.json.JSONObject

/** Relative milliseconds of one displayed verse/phrase within the ORIGINAL full recording. */
internal data class QuranFullCue(
    val itemKey: String,
    val startMs: Int,
    val endMs: Int,
) {
    init { require(startMs >= 0 && endMs > startMs) }
}

/**
 * Source-matched timings. MP3Quran's read 123 and 46 are the exact editions
 * whose full-track CDN URLs the player uses; the same reader in another edition
 * must never borrow these timestamps. A complete original EveryAyah ayah uses
 * its own phrase timings bundled in the existing verified passage catalog.
 */
internal object QuranFullTimings {
    private const val MAX_JSON_BYTES = 800_000

    internal fun matchingRead(reciter: QuranReciter): Int? = when (reciter) {
        QuranReciter.ALAFASY -> 123
        QuranReciter.SALAH_BUKHATIR -> 46
        // MP3Quran's available Husary editions are Murattal/Mujawwad/other riwayat,
        // not the separately hosted Husary Muallim recording currently in Sawti.
        QuranReciter.HUSARY_MUALLIM -> null
    }

    /**
     * Keep highlighting one verse through any within-verse repeat or brief
     * silence. The *next verse's verified start*, not this verse's early end,
     * is the transition point. There is deliberately no highlight during the
     * opening ta'awwudh (before the first recorded verse onset).
     *
     * The final verse remains highlighted until playback actually stops.
     */
    internal fun activeCue(cues: List<QuranFullCue>, positionMs: Int): QuranFullCue? {
        if (positionMs < 0 || cues.isEmpty()) return null
        return cues.lastOrNull { positionMs >= it.startMs }
    }

    /** A single gentle progress cycle covers both readings of a repeated phrase. */
    internal fun progressFraction(cue: QuranFullCue, nextStartMs: Int?, positionMs: Int): Float {
        val completion = maxOf(cue.endMs, nextStartMs ?: cue.endMs)
        return ((positionMs - cue.startMs).toFloat() /
            (completion - cue.startMs)).coerceIn(0f, 1f)
    }

    suspend fun obtain(context: Context, surah: ShortSurah, reciter: QuranReciter): List<QuranFullCue> =
        withContext(Dispatchers.IO) {
            val app = context.applicationContext
            if (surah.selectionId == 2001) return@withContext fromPassagePlan(app, surah, reciter, null)
            val readId = matchingRead(reciter) ?: return@withContext emptyList()
            val root = File(app.filesDir, "quran_full_timing/${reciter.editionId}")
            val file = File(root, "selection_${surah.selectionId}_read_${readId}.json")
            if (file.isFile) {
                try {
                    if (file.length() > MAX_JSON_BYTES) throw IOException("Cached timing oversized")
                    val parsed = parseTiming(file.readText(Charsets.UTF_8), surah)
                    if (parsed.isNotEmpty()) return@withContext cues(app, surah, reciter, parsed)
                    file.delete()
                } catch (_: Exception) { file.delete() }
            }
            val conn = (URL("https://www.mp3quran.net/api/v3/ayat_timing?surah=${surah.number}&read=$readId")
                .openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 17_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK)
                    throw IOException("Unable to retrieve verified original-recitation timings")
                if (conn.contentLengthLong > MAX_JSON_BYTES)
                    throw IOException("Timing metadata oversized")
                val bytes = conn.inputStream.use { input ->
                    val buffer = ByteArray(4096)
                    val output = java.io.ByteArrayOutputStream()
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        if (output.size() + read > MAX_JSON_BYTES) throw IOException("Timing metadata oversized")
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
                val raw = bytes.toString(Charsets.UTF_8)
                val timing = parseTiming(raw, surah)
                if (timing.isEmpty()) throw IOException("Incomplete Quran timing metadata")
                currentCoroutineContext().ensureActive()
                if (!root.isDirectory && !root.mkdirs()) throw IOException("Cannot create timing directory")
                val tmp = File.createTempFile("timing_", ".part", root)
                try {
                    tmp.writeText(raw, Charsets.UTF_8)
                    currentCoroutineContext().ensureActive()
                    if (file.exists()) file.delete()
                    if (!tmp.renameTo(file)) throw IOException("Cannot persist Quran timings")
                } finally { tmp.delete() }
                cues(app, surah, reciter, timing)
            } finally { conn.disconnect() }
        }

    /** Each row: (verse number, start, end) in the source's original timeline. */
    internal fun parseTiming(raw: String, surah: ShortSurah): List<Triple<Int, Int, Int>> {
        val arr = JSONArray(raw)
        val wanted = if (surah.selectionId == 2002) listOf(285, 286)
            else (1..surah.verseCount).toList()
        val rows = mutableMapOf<Int, Pair<Int,Int>>()
        for (i in 0 until arr.length()) {
            val x = arr.getJSONObject(i)
            val verse = x.optInt("ayah", -1)
            if (verse !in wanted) continue
            val start = x.getInt("start_time")
            val end = x.getInt("end_time")
            require(start >= 0 && end > start && end < 1_000_000_000) { "Invalid verse timing" }
            require(rows.put(verse, start to end) == null) { "Duplicate timing" }
        }
        if (rows.size != wanted.size) return emptyList()
        val sorted = wanted.map { n -> Triple(n, rows.getValue(n).first, rows.getValue(n).second) }
        require(sorted.zipWithNext().all { (a,b) -> b.second >= a.third }) { "Overlapping verse timeline" }
        return sorted
    }

    private fun cues(
        context: Context, surah: ShortSurah, reader: QuranReciter,
        rows: List<Triple<Int,Int,Int>>,
    ): List<QuranFullCue> {
        if (surah.selectionId == 2002) return fromPassagePlan(context,surah,reader,rows)
        return shortSurahCues(surah,rows)
    }

    /** Verified source timestamps are absolute from playback position 0 (including any invocation). */
    internal fun shortSurahCues(
        surah: ShortSurah, rows: List<Triple<Int,Int,Int>>,
    ): List<QuranFullCue> {
        require(!surah.segmented && surah.number in 1..114)
        // MP3Quran's positions are absolute within the uninterrupted recording.
        // The opening ta'awwudh is not a Quran verse and must NOT be counted as
        // ayah 1. For Al-Fatiha MP3Quran reports: Alafasy 6420 ms and Salah
        // Bukhatir 3860 ms before the first verse (the basmala) begins.
        // Subtracting rows.first().second made the on-screen highlight jump
        // ahead of the audio by that amount, and could show two verses ahead.
        return rows.map { (verse,start,end) ->
            val key = "ayah-${QuranSource.firstAyahGlobal(surah.number) + verse - 1}"
            QuranFullCue(key, start, end)
        }
    }

    /**
     * The clipped per-phrase file for Salah 2:286/6 starts at its SECOND
     * reading (36,300 ms). In the original uncut source the first reading
     * starts at 31,650 ms, immediately after 2:286/5.
     *
     * The previous implementation assigned that 4,650 ms first reading to
     * segment 5, because the original catalog describes only the extracted
     * second-reading clip. The user-supplied playback recording confirms it:
     * highlight phrase 6 from the first \"واعف عنا\" through both repetitions.
     *
     * This is explicitly source+reader+phrase scoped. Do not shift arbitrary
     * catalog pauses, sources, or other reciters by guessing.
     */
    internal fun startOfWholePassagePhrase(
        reciter: QuranReciter, ayah: Int, part: Int,
        catalogStartMs: Int, precedingEndMs: Int?,
    ): Int {
        if (reciter == QuranReciter.SALAH_BUKHATIR &&
            ayah == 286 && part == 6 &&
            catalogStartMs == 36_300 && precedingEndMs == 31_650) {
            return precedingEndMs
        }
        return catalogStartMs
    }

    internal fun fromPassagePlan(
        context: Context, surah: ShortSurah, reciter: QuranReciter,
        sourceTimes: List<Triple<Int,Int,Int>>?,
    ): List<QuranFullCue> {
        val raw = context.assets.open("quran_passages/catalog.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val entries = JSONObject(raw).getJSONArray("verses")
        val output = mutableListOf<QuranFullCue>()
        val firstStart = sourceTimes?.firstOrNull()?.second ?: 0
        val openingTrimMs = if (surah.selectionId == 2002 && sourceTimes != null) {
            QuranFullSources.baqarahSpec(reciter)?.openingBleedTrimMs?.toInt() ?: 0
        } else 0
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val ayah = entry.getInt("ayah")
            if (surah.selectionId == 2001 && ayah != 255) continue
            if (surah.selectionId == 2002 && ayah !in 285..286) continue
            val row = sourceTimes?.firstOrNull { it.first == ayah }
            val parts = entry.getJSONArray("parts")
            var fullPartEnd = 0
            for (p in 0 until parts.length()) {
                val times = parts.getJSONObject(p).getJSONObject("audio").getJSONObject(reciter.editionId)
                fullPartEnd = maxOf(fullPartEnd, times.getInt("endMs"))
            }
            require(fullPartEnd > 0)
            for (p in 0 until parts.length()) {
                val times = parts.getJSONObject(p).getJSONObject("audio").getJSONObject(reciter.editionId)
                val catalogStart = times.getInt("startMs")
                val end = times.getInt("endMs")
                val previousEnd = if (p == 0) null else
                    parts.getJSONObject(p - 1).getJSONObject("audio")
                        .getJSONObject(reciter.editionId).getInt("endMs")
                val start = startOfWholePassagePhrase(
                    reciter, ayah, p + 1, catalogStart, previousEnd)
                val (mappedStart,mappedEnd) = if (row == null) start to end else {
                    // The whole Baqarah CDN recording and EveryAyah verse source have
                    // slightly different silence. Anchor the exact 285/286 boundary
                    // from the CDN timing, interpolate phrase cues within each verse.
                    // The clip now begins AFTER any verified 2:284 bleed.
                    // Keep first-verse and 286 boundaries at the same positions
                    // in the newly cropped MP3; otherwise visual tracking drifts
                    // even though the audio itself has been corrected.
                    val clipTrim = if (ayah == 285) openingTrimMs else 0
                    val sourceDuration = row.third - row.second - clipTrim
                    val base = row.second - firstStart -
                        (if (ayah > 285) openingTrimMs else 0)
                    require(sourceDuration > 0 && base >= 0)
                    (base + (start.toLong()*sourceDuration/fullPartEnd).toInt()) to
                        (base + (end.toLong()*sourceDuration/fullPartEnd).toInt())
                }
                output += QuranFullCue("segment-2_${ayah}_${p+1}",mappedStart,mappedEnd)
            }
        }
        return output.sortedBy { it.startMs }
    }
}
