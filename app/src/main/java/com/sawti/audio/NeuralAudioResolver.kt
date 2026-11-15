package com.sawti.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import com.sawti.domain.model.ArabicDialect
import java.io.File
import java.util.Locale
import org.json.JSONObject

/** Bundle metadata checks clip integrity; semantic identity selects the voice. */
sealed interface NeuralClip {
    data class Downloaded(val file: File) : NeuralClip
    data class Bundled(val assetPath: String) : NeuralClip
}

class NeuralAudioResolver(context: Context) {
    private val app = context.applicationContext
    private val assets = app.assets
    private val root = File(app.filesDir, "neural_tts")
    private data class Entry(val file: String, val speechText: String)
    private val localManifests = mutableMapOf<String, Map<String, Entry>>()
    private val assetManifests = mutableMapOf<String, Map<String, Entry>>()
    private val invalidAssets = mutableSetOf<String>()

    companion object {
        /** Semantic IDs are stable, never generated from untrusted display text. */
        fun safeKey(key: String): Boolean =
            key.matches(Regex("[a-z0-9][a-z0-9._-]*")) && !key.contains("..")
    }

    private fun parse(json: String): Map<String, Entry> = runCatching {
        val root = JSONObject(json)
        if (root.optInt("version") != 1 || root.optString("rate") != "-18%")
            return@runCatching emptyMap()
        val entries = root.getJSONObject("entries")
        val result = mutableMapOf<String, Entry>()
        val iterator = entries.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (!safeKey(key)) continue
            val info = entries.optJSONObject(key) ?: continue
            val file = info.optString("file")
            val text = info.optString("speechText")
            if (file == "$key.mp3" && text.isNotBlank()) result[key] = Entry(file, text)
        }
        result
    }.getOrDefault(emptyMap())

    private fun localManifest(dialect: String): Map<String, Entry> =
        localManifests.getOrPut(dialect) {
            runCatching { parse(File(File(root, dialect), "manifest.json").readText(Charsets.UTF_8)) }
                .getOrDefault(emptyMap())
        }

    private fun bundledManifest(dialect: String): Map<String, Entry> =
        assetManifests.getOrPut(dialect) {
            runCatching {
                assets.open("neural_tts/$dialect/manifest.json").bufferedReader(Charsets.UTF_8)
                    .use { parse(it.readText()) }
            }.getOrDefault(emptyMap())
        }

    /** The caller has checked whether the card was customized; never use vowel-string equality. */
    fun resolve(dialect: ArabicDialect, semanticKey: String?, speechText: String): NeuralClip? {
        val key = semanticKey?.takeIf(::safeKey) ?: return null
        if (speechText.isBlank()) return null
        val folder = dialect.name.lowercase(Locale.ROOT)
        localManifest(folder)[key]?.let {
            val file = File(File(root, folder), it.file)
            if (file.isFile && file.length() >= 64L) return NeuralClip.Downloaded(file)
        }
        bundledManifest(folder)[key]?.let {
            val path = "neural_tts/$folder/${it.file}"
            if (path !in invalidAssets) return NeuralClip.Bundled(path)
        }
        return null
    }

    /** Assets are indexed once by manifest rather than repeatedly scanned on every card tap. */
    fun openBundled(clip: NeuralClip.Bundled): AssetFileDescriptor? =
        runCatching { assets.openFd(clip.assetPath) }
            .onFailure { invalidAssets += clip.assetPath }
            .getOrNull()

    /** Call after installing a new offline pack without restarting the app. */
    fun refreshDownloadedPacks() { localManifests.clear() }
}
