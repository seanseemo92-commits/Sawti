package com.sawti.presentation.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Private recordings downloaded on demand and retained between app launches.
 * Do not use Android cacheDir: the OS may evict it at any time.
 */
class QuranAudioCache(
    context: Context,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) {
    private val audioDir = File(context.applicationContext.filesDir, "quran_recitations/ar_alafasy")
    private val locks = ConcurrentHashMap<Int, Mutex>()

    fun cachedFile(globalVerseNumber: Int): File? {
        val target = targetFile(globalVerseNumber)
        return target.takeIf(::hasValidMp3Header)
    }

    suspend fun obtain(globalVerseNumber: Int): File = withContext(Dispatchers.IO) {
        val target = targetFile(globalVerseNumber)
        // Playback and whole-surah downloads may request the same ayah.
        val lock = locks.getOrPut(globalVerseNumber) { Mutex() }
        lock.withLock {
            cachedFile(globalVerseNumber)?.let { return@withLock it }
            if (!audioDir.isDirectory && !audioDir.mkdirs()) {
                throw IOException("Unable to create Quran recordings directory")
            }
            val temporary = File.createTempFile("ayah_" + globalVerseNumber + "_", ".part", audioDir)
            try {
                val connection = connectionFactory(URL(QuranSource.audioUrl(globalVerseNumber)))
                try {
                    connection.connectTimeout = 12_000
                    connection.readTimeout = 20_000
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("Accept", "audio/mpeg, audio/*")
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("Recording HTTP " + connection.responseCode)
                    }
                    if (connection.contentLengthLong > MAX_AUDIO_BYTES) {
                        throw IOException("Recording is too large")
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(temporary).use { output ->
                            val buffer = ByteArray(8192)
                            var total = 0L
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                if (total > MAX_AUDIO_BYTES) throw IOException("Recording is too large")
                                output.write(buffer, 0, count)
                            }
                            output.fd.sync()
                        }
                    }
                } finally {
                    connection.disconnect()
                }
                currentCoroutineContext().ensureActive()
                if (!hasValidMp3Header(temporary)) throw IOException("Invalid or incomplete MP3")
                if (target.exists() && !target.delete()) {
                    throw IOException("Cannot replace incomplete recording")
                }
                if (!temporary.renameTo(target)) {
                    throw IOException("Cannot save Quran recording")
                }
                target
            } finally {
                // Failed requests and cancelled downloads never leave partial mp3 files.
                temporary.delete()
            }
        }
    }

    private fun targetFile(number: Int): File {
        require(number in 1..6236) { "Invalid global Quran verse number" }
        return File(audioDir, "ayah_" + number + ".mp3")
    }

    private fun hasValidMp3Header(file: File): Boolean {
        if (!file.isFile || file.length() < MIN_AUDIO_BYTES || file.length() > MAX_AUDIO_BYTES) {
            return false
        }
        return try {
            file.inputStream().buffered().use { input ->
                val head = ByteArray(3)
                if (input.read(head) != 3) return@use false
                (head[0] == 'I'.code.toByte() &&
                    head[1] == 'D'.code.toByte() &&
                    head[2] == '3'.code.toByte()) ||
                    ((head[0].toInt() and 0xFF) == 0xFF &&
                        (head[1].toInt() and 0xE0) == 0xE0)
            }
        } catch (_: IOException) {
            false
        }
    }

    private companion object {
        const val MIN_AUDIO_BYTES = 1024L
        const val MAX_AUDIO_BYTES = 12L * 1024 * 1024
    }
}
