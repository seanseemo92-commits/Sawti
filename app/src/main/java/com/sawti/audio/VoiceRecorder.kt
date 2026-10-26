package com.sawti.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Caregiver recordings use only app-private storage; never write outside its audio folder. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    fun start(key: String): Boolean {
        cancel()
        val safeKey = key.replace(Regex("[^A-Za-z0-9_-]"), "_").take(50)
        if (safeKey.isBlank()) return false
        val dir = File(context.filesDir, "custom_audio")
        if (!dir.isDirectory && !dir.mkdirs()) return false
        val out = File(dir, "${safeKey}_${System.nanoTime()}.m4a")
        var created: MediaRecorder? = null
        return try {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context)
                else @Suppress("DEPRECATION") MediaRecorder()
            created = r
            r.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(out.absolutePath)
                prepare()
                start()
            }
            recorder = r
            file = out
            true
        } catch (_: Exception) {
            runCatching { created?.release() }
            out.delete()
            recorder = null
            file = null
            false
        }
    }

    /** Very short or interrupted recordings are discarded instead of saved as broken media. */
    fun stop(): File? {
        val r = recorder ?: return null
        val f = file
        recorder = null
        file = null
        return try {
            r.stop()
            f?.takeIf { it.isFile && it.length() > 0L } ?: run {
                f?.delete()
                null
            }
        } catch (_: RuntimeException) {
            f?.delete()
            null
        } finally {
            runCatching { r.release() }
        }
    }

    fun cancel() {
        stop()?.delete()
    }
}
