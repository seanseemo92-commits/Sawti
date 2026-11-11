package com.sawti.presentation.quran

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Whole recordings, as delivered by the source, never assembled from verse MP3s.
 * They are downloaded only on first use and persisted under filesDir, outside the APK.
 *
 * The final two verses of Al-Baqarah are extracted without recompression
 * from the original continuous recording using a bounded HTTP byte range.
 * The entire ~100 MB chapter is never saved or downloaded to the device.
 */
internal data class BaqarahEndingSpec(
    val fullUrl: String,
    val auditedBytes: Long,
    val ayah285StartMs: Long,
    val ayah286StartMs: Long,
    val lastAyahEndMs: Long,
    /** Trim only verified bleed from the previous ayah; not a generic reader-wide offset. */
    val openingBleedTrimMs: Long = 0L,
)

internal object QuranFullSources {
    // These two source editions have recorded verse 285/286 timestamps in the
    // SAME MP3Quran read whose exact whole-MP3 URL is used below. Do not use
    // the normal Husary timings with the separate Muallim archive recording.
    fun baqarahSpec(reader: QuranReciter): BaqarahEndingSpec? = when (reader) {
        QuranReciter.ALAFASY -> BaqarahEndingSpec(
            "https://cdn.mp3quran.net/download/audio/mishary-alafasy/r1/002.mp3",
            120641555L, 7454920L, 7487820L, 7539060L)
        QuranReciter.SALAH_BUKHATIR -> BaqarahEndingSpec(
            "https://cdn.mp3quran.net/download/audio/salah-bukhatir/r1/002.mp3",
            109142897L, 6737280L, 6767360L, 6816680L,
            openingBleedTrimMs = 1_250L)
        QuranReciter.HUSARY_MUALLIM -> null
    }

    fun available(surah: ShortSurah, reader: QuranReciter? = null): Boolean =
        when (surah.selectionId) {
            2001 -> true
            2002 -> reader != null && baqarahSpec(reader) != null
            else -> QuranSource.shortSurahs.any {
                it.number == surah.number && it.selectionId == surah.selectionId
            }
        }

    fun urls(surah: ShortSurah, reciter: QuranReciter): List<String> {
        require(available(surah,reciter)) { "No verified uninterrupted recording for this selection" }
        if (surah.selectionId == 2002) return listOf(requireNotNull(baqarahSpec(reciter)).fullUrl)
        if (surah.selectionId == 2001) {
            val folder = when (reciter) {
                QuranReciter.ALAFASY -> "Alafasy_128kbps"
                QuranReciter.SALAH_BUKHATIR -> "Salaah_AbdulRahman_Bukhatir_128kbps"
                QuranReciter.HUSARY_MUALLIM -> "Husary_Muallim_128kbps"
            }
            // One original, uncut recording of Ayat al-Kursi (not the phrase clips).
            return listOf("https://everyayah.com/data/$folder/002255.mp3")
        }
        val number = surah.number.toString().padStart(3, '0')
        return when (reciter) {
            QuranReciter.ALAFASY ->
                listOf("https://cdn.mp3quran.net/download/audio/mishary-alafasy/r1/$number.mp3")
            QuranReciter.SALAH_BUKHATIR ->
                listOf("https://cdn.mp3quran.net/download/audio/salah-bukhatir/r1/$number.mp3")
            QuranReciter.HUSARY_MUALLIM ->
                // Dedicated whole-surah teaching recitation archives; never use a
                // different reciter or a regular (non-muallim) Husary recording.
                listOf("https://archive.org/download/Mushaf_Moalim_3/$number.mp3")
        }
    }
}

internal class QuranFullAudioCache(
    context: Context,
    private val reciter: QuranReciter,
    private val connectionFactory: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
    private val durationProvider: (String) -> Long = { address ->
        // MediaMetadataRetriever uses the HTTP source's seek metadata rather
        // than transferring the entire 100MB chapter into phone storage.
        val probe = MediaMetadataRetriever()
        try {
            probe.setDataSource(address, emptyMap<String, String>())
            probe.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: throw IOException("Cannot verify source MP3 duration")
        } finally {
            probe.release()
        }
    },
) {
    private val audioDir = File(context.applicationContext.filesDir, "quran_full_recitations/${reciter.editionId}")

    fun cachedFile(surah: ShortSurah): File? =
        target(surah).takeIf { QuranMp3.isPlayable(it) }

    suspend fun invalidate(surah: ShortSurah) = withContext(Dispatchers.IO) {
        val file = target(surah)
        locks.getOrPut(file.absolutePath) { Mutex() }.withLock {
            if (file.exists() && !file.delete()) throw IOException("Unable to remove damaged full recording")
        }
    }

    suspend fun obtain(surah: ShortSurah): File = withContext(Dispatchers.IO) {
        val sources = QuranFullSources.urls(surah, reciter)
        val file = target(surah)
        locks.getOrPut(file.absolutePath) { Mutex() }.withLock {
            cachedFile(surah)?.let { return@withLock it }
            if (!audioDir.exists() && !audioDir.mkdirs())
                throw IOException("Unable to create whole-surah recordings directory")
            if (surah.selectionId == 2002) {
                // Never publish a full 2:285–286 file from verse-by-verse
                // stitched clips or from an unverified alternate edition.
                return@withLock obtainBaqarahEnding(requireNotNull(QuranFullSources.baqarahSpec(reciter)),file)
            }
            val part = File.createTempFile("surah_${surah.selectionId}_", ".part", audioDir)
            try {
                var lastFailure: Exception? = null
                for (source in sources) {
                    currentCoroutineContext().ensureActive()
                    try {
                        val connection = connectionFactory(URL(source))
                        try {
                            connection.connectTimeout = 15_000
                            connection.readTimeout = 30_000
                            connection.instanceFollowRedirects = true
                            connection.setRequestProperty("Accept", "audio/mpeg, audio/*")
                            if (connection.responseCode != HttpURLConnection.HTTP_OK)
                                throw IOException("Full recording HTTP ${connection.responseCode}")
                            if (connection.contentLengthLong > MAX_BYTES)
                                throw IOException("Full recording exceeds cache limit")
                            connection.inputStream.use { input ->
                                FileOutputStream(part, false).use { output ->
                                    val buffer = ByteArray(8192)
                                    var total = 0L
                                    while (true) {
                                        currentCoroutineContext().ensureActive()
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        total += count
                                        if (total > MAX_BYTES) throw IOException("Full recording exceeds cache limit")
                                        output.write(buffer, 0, count)
                                    }
                                    if (connection.contentLengthLong >= 0 && total != connection.contentLengthLong)
                                        throw IOException("Incomplete full recording")
                                    output.fd.sync()
                                }
                            }
                        } finally {
                            connection.disconnect()
                        }
                        currentCoroutineContext().ensureActive()
                        if (!QuranMp3.isPlayable(part)) throw IOException("Invalid full recitation MP3")
                        if (file.exists() && !file.delete()) throw IOException("Unable to replace recording")
                        if (!part.renameTo(file)) throw IOException("Unable to persist full recording")
                        return@withLock file
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        lastFailure = error
                        part.delete()
                    }
                }
                throw IOException("Whole recitation unavailable for ${reciter.displayName}", lastFailure)
            } finally {
                part.delete()
            }
        }
    }

    private suspend fun obtainBaqarahEnding(spec: BaqarahEndingSpec, saved: File): File {
        // Checking the source byte length prevents silently applying old
        // timing metadata to a changed or replaced 002.mp3.
        currentCoroutineContext().ensureActive()
        val head = connectionFactory(URL(spec.fullUrl))
        val sourceLength = try {
            head.requestMethod = "HEAD"
            head.connectTimeout = 15_000
            head.readTimeout = 20_000
            if (head.responseCode != HttpURLConnection.HTTP_OK)
                throw IOException("Full Baqarah source HEAD failed")
            val bytes = head.contentLengthLong
            if (bytes != spec.auditedBytes)
                throw IOException("Source recording changed: reverify Baqarah timings")
            if (!head.getHeaderField("Accept-Ranges").orEmpty().contains("bytes",ignoreCase=true))
                throw IOException("Source does not advertise bounded-range downloading")
            bytes
        } finally { head.disconnect() }
        val duration = durationProvider(spec.fullUrl)
        if (duration !in (spec.lastAyahEndMs - 2500L)..(spec.lastAyahEndMs + 15_000L))
            throw IOException("Whole recording duration mismatches source timing")
        val start = (sourceLength - MAX_BYTES).coerceAtLeast(0)
        val suffix = File.createTempFile("baqarah_full_tail_", ".range", audioDir)
        val clipped = File.createTempFile("baqarah_285_286_", ".part", audioDir)
        try {
            val conn = connectionFactory(URL(spec.fullUrl))
            try {
                conn.connectTimeout = 15_000
                conn.readTimeout = 30_000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("Range","bytes=$start-${sourceLength-1}")
                conn.setRequestProperty("Accept","audio/mpeg,audio/*")
                if (conn.responseCode != HttpURLConnection.HTTP_PARTIAL)
                    throw IOException("Host refused byte range; avoiding full chapter download")
                val contentRange = conn.getHeaderField("Content-Range").orEmpty()
                val match = Regex("""bytes (\d+)-(\d+)/(\d+)""").matchEntire(contentRange)
                    ?: throw IOException("Invalid Baqarah byte-range headers")
                if (match.groupValues[1].toLong()!=start ||
                    match.groupValues[2].toLong()!=sourceLength-1 ||
                    match.groupValues[3].toLong()!=sourceLength)
                    throw IOException("Incorrect byte-range response")
                conn.inputStream.use { input ->
                    FileOutputStream(suffix).use { out ->
                        val buffer=ByteArray(8192)
                        var total=0L
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val n=input.read(buffer)
                            if (n<0) break
                            total+=n
                            if (total>MAX_BYTES) throw IOException("Bounded range exceeded")
                            out.write(buffer,0,n)
                        }
                        if (total!=sourceLength-start) throw IOException("Truncated Baqarah ending range")
                        out.fd.sync()
                    }
                }
            } finally { conn.disconnect() }
            currentCoroutineContext().ensureActive()
            // Salah's original source contains the trailing word of 2:284 at
            // the old cut boundary. The reviewed screen recording shows ~1.25 s
            // of leftover speech before "آمن الرسول" begins. Advance the
            // frame-aligned crop by that source-scoped amount, retaining MP3
            // decoder pre-roll and the full first word of 2:285.
            val clipDuration = QuranMp3Tail.cut(
                suffix, clipped, duration - spec.ayah285StartMs - spec.openingBleedTrimMs)
            val expected = spec.lastAyahEndMs -
                spec.ayah285StartMs - spec.openingBleedTrimMs
            if (clipDuration !in (expected-1500L)..(expected+16_000L))
                throw IOException("Cropped final verses mismatch timing")
            currentCoroutineContext().ensureActive()
            if (saved.exists() && !saved.delete()) throw IOException("Cannot overwrite damaged ending")
            if (!clipped.renameTo(saved)) throw IOException("Unable to cache ending MP3")
            // Now that the corrected MP3 is safely saved, reclaim the obsolete
            // cached clip containing the trailing word of 2:284.
            if (spec.openingBleedTrimMs > 0L) {
                File(audioDir, "surah_2002.mp3").delete()
            }
            return saved
        } finally {
            suffix.delete()
            clipped.delete()
        }
    }

    private fun target(surah: ShortSurah): File {
        require(QuranFullSources.available(surah, reciter))
        // A new file name prevents earlier, incorrectly cropped 2:285 audio
        // from being silently reused after this upgrade. Other saved surahs
        // (and Alafasy's unchanged Baqarah clip) retain their original cache.
        val name = if (surah.selectionId == 2002 &&
            reciter == QuranReciter.SALAH_BUKHATIR) "surah_2002_crop_v2.mp3"
            else "surah_${surah.selectionId}.mp3"
        return File(audioDir, name)
    }

    private companion object {
        // Keep each whole-surah download bounded; the full Al-Baqarah must never be downloaded.
        const val MAX_BYTES = 12L * 1024L * 1024L
        val locks = ConcurrentHashMap<String, Mutex>()
    }
}
