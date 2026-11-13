package com.sawti.presentation.quran

import android.content.Context
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject

/** Fixed passages use the same cards/player as ayahs, with a distinct identity for each part. */
internal object QuranPassages {
    private const val MANIFEST = "quran_passages/catalog.json"
    private const val MAX_CLIP_BYTES = 2 * 1024 * 1024
    private data class Clip(val asset: String, val sha256: String)
    private data class Catalog(val verses: Map<Int, List<QuranVerse>>, val clips: Map<String, Clip>)

    // Cache data only, never an Activity or Context. Assets are immutable within an installed APK.
    @Volatile private var catalog: Catalog? = null

    private fun catalog(context: Context): Catalog = catalog ?: synchronized(this) {
        catalog ?: parse(context.assets.open(MANIFEST).bufferedReader(Charsets.UTF_8).use { it.readText() })
            .also { catalog = it }
    }

    private fun clipKey(verse: QuranVerse, reciter: QuranReciter) =
        "${reciter.editionId}/${requireNotNull(verse.segmentKey)}"

    fun load(context: Context, selectionId: Int): List<QuranVerse> =
        catalog(context).verses.getValue(selectionId)

    private fun clip(context: Context, verse: QuranVerse, reciter: QuranReciter): Clip {
        QuranSource.checkAyah(verse.globalNumber, verse.surahNumber, verse.verseNumber)
        val data = catalog(context)
        require(data.verses.values.any { verse in it }) { "Unknown Quran passage segment" }
        return data.clips.getValue(clipKey(verse, reciter))
    }

    fun hasRecording(context: Context, verse: QuranVerse, reciter: QuranReciter): Boolean {
        // These checked, packaged recordings are available without a first network download.
        val recording = clip(context, verse, reciter)
        return context.assets.open(recording.asset).use { it.read() >= 0 }
    }

    fun target(context: Context, verse: QuranVerse, reciter: QuranReciter): File {
        val recording = clip(context, verse, reciter)
        return File(context.applicationContext.filesDir,
            "quran_recitations/${reciter.editionId}/segment_${verse.segmentKey}_${recording.sha256.take(12)}.mp3")
    }

    /** Caller owns the same per-file lock used by ordinary ayah downloads and invalidation. */
    suspend fun obtain(context: Context, verse: QuranVerse, reciter: QuranReciter): File {
        val recording = clip(context, verse, reciter)
        val destination = target(context, verse, reciter)
        if (destination.isFile && destination.length() <= MAX_CLIP_BYTES &&
            digest(destination.readBytes()) == recording.sha256 && QuranMp3.isPlayable(destination)) return destination
        val directory = destination.parentFile ?: throw IOException("Missing Quran audio directory")
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create Quran audio directory")
        val temporary = File.createTempFile("segment_", ".part", directory)
        try {
            context.assets.open(recording.asset).use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_CLIP_BYTES) throw IOException("Quran segment too large")
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }
            if (digest(temporary.readBytes()) != recording.sha256 || !QuranMp3.isPlayable(temporary)) {
                throw IOException("Invalid packaged Quran segment")
            }
            currentCoroutineContext().ensureActive()
            // Keep the previous recording intact if publishing fails.
            if (!temporary.renameTo(destination)) throw IOException("Cannot save Quran segment")
            return destination
        } finally {
            temporary.delete()
        }
    }

    private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private fun parse(raw: String): Catalog {
        val root = JSONObject(raw)
        require(root.getInt("version") == 1)
        val sourceVerses = root.getJSONArray("verses")
        require(sourceVerses.length() == 3)
        val selectionVerses = mutableMapOf<Int, MutableList<QuranVerse>>()
        val recordings = mutableMapOf<String, Clip>()
        val seen = mutableSetOf<Int>()
        for (index in 0 until sourceVerses.length()) {
            val entry = sourceVerses.getJSONObject(index)
            val number = entry.getInt("ayah")
            require(seen.add(number)) { "Duplicate Quran passage ayah" }
            val global = entry.getInt("global")
            QuranSource.checkAyah(global, 2, number)
            val selection = if (number == 255) 2001 else 2002
            val parts = entry.getJSONArray("parts")
            val fragments = mutableListOf<String>()
            for (part in 0 until parts.length()) {
                val item = parts.getJSONObject(part)
                val text = item.getString("text")
                require(text.isNotBlank())
                fragments += text
                val verse = QuranVerse(2, number, global, text, part + 1, parts.length())
                selectionVerses.getOrPut(selection) { mutableListOf() }.add(verse)
                val audio = item.getJSONObject("audio")
                for (reciter in QuranReciter.entries) {
                    val metadata = audio.getJSONObject(reciter.editionId)
                    val expectedAsset = "quran_passages/${reciter.editionId}/${verse.segmentKey}.mp3"
                    require(metadata.getString("asset") == expectedAsset)
                    val sha = metadata.getString("sha256")
                    require(sha.matches(Regex("[a-f0-9]{64}")))
                    recordings[clipKey(verse, reciter)] = Clip(expectedAsset, sha)
                }
            }
            require(fragments.joinToString(" ") == entry.getString("text")) {
                "Quran passage text must preserve every character in order"
            }
        }
        require(seen == setOf(255, 285, 286))
        require(selectionVerses.getValue(2001).size == 9 && selectionVerses.getValue(2002).size == 13)
        require(selectionVerses.getValue(2002).map { it.verseNumber }.distinct() == listOf(285, 286))
        return Catalog(selectionVerses.mapValues { it.value.toList() }, recordings.toMap())
    }
}
