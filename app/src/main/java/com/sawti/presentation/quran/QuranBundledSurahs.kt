package com.sawti.presentation.quran

import android.content.Context
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject

/**
 * Immutable 128kbps recordings of the thirteen short surahs.
 * Uses the same private-file playback path as downloaded ayahs and Quran passages.
 * No network work, decoding or bulk copying happens at application startup.
 */
internal object QuranBundledSurahs {
    private const val MANIFEST = "quran_surahs/catalog.json"
    private const val MAX_AYAH_BYTES = 12L * 1024 * 1024
    private data class Clip(val asset: String, val sha256: String)
    @Volatile private var clips: Map<String, Clip>? = null

    private fun key(global: Int, reciter: QuranReciter) = "${reciter.editionId}/$global"

    private fun lookup(context: Context): Map<String, Clip> = clips ?: synchronized(this) {
        clips ?: run {
            // No resources in narrow test fixtures: retain the legacy network cache behavior.
            val raw = try {
                context.assets.open(MANIFEST).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (_: java.io.FileNotFoundException) {
                return@synchronized emptyMap()
            }
            parse(raw).also { clips = it }
        }
    }

    private fun parse(raw: String): Map<String, Clip> {
        val json = JSONObject(raw)
        require(json.getInt("version") == 1)
        val entries = json.getJSONArray("verses")
        require(entries.length() == 67) { "Incomplete bundled Quran surah catalog" }
        val found = LinkedHashMap<String, Clip>()
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val chapter = entry.getInt("surah")
            val local = entry.getInt("ayah")
            val global = entry.getInt("global")
            QuranSource.checkAyah(global, chapter, local)
            val filename = chapter.toString().padStart(3, '0') +
                local.toString().padStart(3, '0') + ".mp3"
            val recordings = entry.getJSONObject("audio")
            QuranReciter.entries.forEach { reciter ->
                val data = recordings.getJSONObject(reciter.editionId)
                val expected = "quran_surahs/${reciter.editionId}/$filename"
                require(data.getString("asset") == expected)
                val sha = data.getString("sha256")
                require(sha.matches(Regex("[a-f0-9]{64}")))
                require(found.put(key(global, reciter), Clip(expected, sha)) == null)
            }
        }
        require(found.size == 67 * QuranReciter.entries.size)
        return found
    }

    fun hasRecording(context: Context, global: Int, reciter: QuranReciter): Boolean {
        val recording = lookup(context)[key(global, reciter)] ?: return false
        return runCatching { context.assets.open(recording.asset).use { it.read() >= 0 } }.getOrDefault(false)
    }

    /** A null result means this verse is not bundled, and normal online caching applies. */
    suspend fun obtain(context: Context, global: Int, reciter: QuranReciter, destination: File): File? {
        val recording = lookup(context)[key(global, reciter)] ?: return null
        if (destination.isFile && destination.length() in 1024L..MAX_AYAH_BYTES &&
            sha256(destination) == recording.sha256 && QuranMp3.isPlayable(destination)) {
            return destination
        }
        val parent = destination.parentFile ?: throw IOException("Missing Quran audio folder")
        if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Cannot create Quran audio folder")
        val temporary = File.createTempFile("ayah_bundled_", ".part", parent)
        try {
            context.assets.open(recording.asset).use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytes = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val amount = input.read(buffer)
                        if (amount < 0) break
                        bytes += amount
                        if (bytes > MAX_AYAH_BYTES) throw IOException("Bundled Quran ayah is too large")
                        output.write(buffer, 0, amount)
                    }
                    output.fd.sync()
                }
            }
            if (sha256(temporary) != recording.sha256 || !QuranMp3.isPlayable(temporary)) {
                throw IOException("Corrupted bundled Quran recording")
            }
            currentCoroutineContext().ensureActive()
            if (destination.exists() && !destination.delete()) throw IOException("Cannot replace Quran audio")
            if (!temporary.renameTo(destination)) throw IOException("Cannot save bundled Quran recording")
            return destination
        } finally {
            temporary.delete()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
