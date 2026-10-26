package com.sawti.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Records a caregiver voice clip to app storage as AAC (.m4a). Caller must hold RECORD_AUDIO. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    fun start(key: String): Boolean {
        cancel()
        val dir = File(context.filesDir, "custom_audio").apply { mkdirs() }
        val out = File(dir, "${key.replace(':', '_')}_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context)
        else @Suppress("DEPRECATION") MediaRecorder()
        return try {
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
        } catch (e: Exception) {
            r.release()
            out.delete()
            false
        }
    }

    /** Returns the recorded file, or null if nothing usable was captured (e.g. a zero-length tap). */
    fun stop(): File? {
        val r = recorder ?: return null
        val f = file
        recorder = null
        file = null
        return try {
            r.stop()
            f
        } catch (e: RuntimeException) {
            f?.delete()
            null
        } finally {
            r.release()
        }
    }

    fun cancel() {
        stop()?.delete()
    }
}
