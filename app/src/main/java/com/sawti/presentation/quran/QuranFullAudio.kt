package com.sawti.presentation.quran

import android.content.Context
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
 * The two final verses of Al-Baqarah require a separately verified contiguous
 * two-ayah source. Do not silently substitute the ~70 MB entire chapter or
 * concatenate the existing 002285/002286 files.
 */
internal object QuranFullSources {
    fun available(surah: ShortSurah): Boolean = surah.selectionId != 2002 &&
        (surah.selectionId == 2001 || QuranSource.shortSurahs.any { it.number == surah.number && it.selectionId == surah.selectionId })

    fun urls(surah: ShortSurah, reciter: QuranReciter): List<String> {
        require(available(surah)) { "No verified uninterrupted recording for this selection" }
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
                listOf(
                    "https://archive.org/download/Mushaf_Moalim_3/$number.mp3",
                    "https://archive.org/download/Islam47a/$number.mp3",
                )
        }
    }
}

internal class QuranFullAudioCache(
    context: Context,
    private val reciter: QuranReciter,
    private val connectionFactory: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
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

    private fun target(surah: ShortSurah): File {
        require(QuranFullSources.available(surah))
        return File(audioDir, "surah_${surah.selectionId}.mp3")
    }

    private companion object {
        // Keep each whole-surah download bounded; the full Al-Baqarah must never be downloaded.
        const val MAX_BYTES = 12L * 1024L * 1024L
        val locks = ConcurrentHashMap<String, Mutex>()
    }
}
