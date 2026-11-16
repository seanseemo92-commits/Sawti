package com.sawti.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.HabibiSpeechText
import java.security.MessageDigest
import java.util.Locale
import org.json.JSONObject

/** Habibi-only offline clips. Never loads cached/packaged Microsoft voice files. */
class HabibiAudioResolver(context: Context) {
    private val assets = context.applicationContext.assets
    private data class Entry(val path: String, val sha256: String)
    private val manifests = mutableMapOf<String, Map<String, Entry>>()
    private val invalid = mutableSetOf<String>()

    private fun readManifest(dialect: ArabicDialect): Map<String, Entry> = runCatching {
        val folder = dialect.name.lowercase(Locale.ROOT)
        val data = assets.open("habibi_tts/$folder/manifest.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        val tag = HabibiSpeechText.dialectTag(dialect)
        if (data.optInt("version") != 2 ||
            data.optString("engine") != "Habibi-TTS" ||
            data.optString("dialectId") != tag) return@runCatching emptyMap()
        val model = data.optString("model")
        val ap2 = setOf("MSA", "ALG", "EGY", "MAR", "IRQ")
        val requiredLicense = when {
            model == "Specialized" && tag in ap2 -> "Apache-2.0"
            model == "Unified" -> "CC-BY-NC-SA-4.0"
            else -> return@runCatching emptyMap()
        }
        if (data.optString("license") != requiredLicense)
            return@runCatching emptyMap()
        val entries = data.getJSONObject("entries")
        val result = mutableMapOf<String, Entry>()
        val iterator = entries.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (!key.matches(Regex("[a-z0-9][a-z0-9._-]*")) ||
                key.contains("..")) continue
            val value = entries.optJSONObject(key) ?: continue
            val filename = value.optString("file")
            val checksum = value.optString("sha256")
            if (filename == "$key.mp3" &&
                checksum.matches(Regex("[a-f0-9]{64}")) &&
                value.optString("speechText").isNotBlank())
                result[key] = Entry("habibi_tts/$folder/$filename", checksum)
        }
        result
    }.getOrDefault(emptyMap())

    /** Reject corrupt/stale clips rather than falling back to old MS audio. */
    fun open(dialect: ArabicDialect, semanticKey: String?): AssetFileDescriptor? {
        val key = semanticKey ?: return null
        val folder = dialect.name.lowercase(Locale.ROOT)
        val entry = manifests.getOrPut(folder) { readManifest(dialect) }[key] ?: return null
        if (entry.path in invalid) return null
        return runCatching {
            val md = MessageDigest.getInstance("SHA-256")
            assets.open(entry.path).use { input ->
                val buffer = ByteArray(16384)
                while (true) {
                    val size = input.read(buffer)
                    if (size < 0) break
                    md.update(buffer, 0, size)
                }
            }
            val actual = md.digest().joinToString("") { "%02x".format(it) }
            if (actual != entry.sha256) error("Habibi clip checksum mismatch")
            assets.openFd(entry.path)
        }.onFailure { invalid += entry.path }.getOrNull()
    }
}
