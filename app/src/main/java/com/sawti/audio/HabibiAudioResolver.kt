package com.sawti.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.HabibiSpeechText
import java.security.MessageDigest
import java.util.Locale
import org.json.JSONObject

/**
 * Offline Habibi-TTS only. Old Microsoft v1 manifests are rejected, and
 * downloaded or cached edge-tts files are never opened by this class.
 *
 * Semantic key AND exact unvowelized speech text must match the v2 manifest.
 */
class HabibiAudioResolver(context: Context) {
    private val assets = context.applicationContext.assets
    private data class Entry(val assetPath: String, val text: String, val sha256: String)
    private val manifests = mutableMapOf<String, Map<String, Entry>>()
    private val rejected = mutableSetOf<String>()
    private val verified = mutableSetOf<String>()

    companion object {
        fun safeKey(key: String): Boolean =
            key.matches(Regex("[a-z0-9][a-z0-9._-]*")) && !key.contains("..")
    }

    private fun load(dialect: ArabicDialect): Map<String, Entry> = runCatching {
        val folder = dialect.name.lowercase(Locale.ROOT)
        val json = assets.open("neural_tts/$folder/manifest.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        val tag = HabibiSpeechText.dialectTag(dialect)
        if (json.optInt("version") != 2 ||
            json.optString("engine") != "Habibi-TTS" ||
            json.optString("model") != "Unified" ||
            json.optString("license") != "CC-BY-NC-SA-4.0" ||
            json.optString("dialectId") != tag ||
            json.optInt("sampleRate") != 24000 ||
            json.optString("bitrate") != "96k") {
            Log.e("HabibiAudio", "Reject old/invalid manifest for $folder")
            return@runCatching emptyMap()
        }
        val data = json.getJSONObject("entries")
        // Libyan pack contains 271 keys; the others contain 266 each.
        val expected = if (dialect == ArabicDialect.LIBYAN) 271 else 266
        if (data.length() != expected) return@runCatching emptyMap()
        val result = mutableMapOf<String, Entry>()
        val iterator = data.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (!safeKey(key)) return@runCatching emptyMap()
            val entry = data.optJSONObject(key) ?: return@runCatching emptyMap()
            val text = entry.optString("speechText")
            val hash = entry.optString("sha256")
            if (entry.optString("file") != "$key.mp3" ||
                text.isBlank() ||
                text != HabibiSpeechText.prepare(text) ||
                HabibiSpeechText.hasForbiddenMarksOrNumbers(text) ||
                !hash.matches(Regex("[0-9a-f]{64}"))) {
                return@runCatching emptyMap()
            }
            result[key] = Entry("neural_tts/$folder/$key.mp3", text, hash)
        }
        result
    }.onFailure { Log.e("HabibiAudio", "Manifest read failed", it) }
        .getOrDefault(emptyMap())

    /** Missing or outdated Habibi packs return null; NO Android TTS fallback. */
    fun open(
        dialect: ArabicDialect,
        semanticKey: String?,
        speechText: String,
    ): AssetFileDescriptor? {
        val key = semanticKey?.takeIf(::safeKey) ?: return null
        val folder = dialect.name.lowercase(Locale.ROOT)
        val entry = manifests.getOrPut(folder) { load(dialect) }[key] ?: return null
        if (entry.text != speechText || entry.assetPath in rejected) return null
        return runCatching {
            if (entry.assetPath !in verified) {
                val md = MessageDigest.getInstance("SHA-256")
                assets.open(entry.assetPath).use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        md.update(buffer, 0, count)
                    }
                }
                val digest = md.digest().joinToString("") { "%02x".format(it) }
                if (digest != entry.sha256) error("Audio checksum mismatch")
                verified += entry.assetPath
            }
            assets.openFd(entry.assetPath)
        }.onFailure {
            rejected += entry.assetPath
            Log.e("HabibiAudio", "Reject unverified Habibi audio: " + entry.assetPath, it)
        }.getOrNull()
    }
}
