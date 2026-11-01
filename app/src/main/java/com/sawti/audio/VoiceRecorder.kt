package com.sawti.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Caregiver recordings use only app-private storage; never write outside its audio folder. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    @Volatile private var limitReached = false

    fun start(key: String): Boolean {
        cancel()
        val safeKey = key.replace(Regex("[^A-Za-z0-9_-]"), "_").take(50)
        if (safeKey.isBlank()) return false
        val dir = File(context.filesDir, "custom_audio")
        if (!dir.isDirectory && !dir.mkdirs()) return false
        val out = File(dir, "${safeKey}_${System.nanoTime()}.m4a")
        var created: MediaRecorder? = null
        limitReached = false
        return try {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context)
                else @Suppress("DEPRECATION") MediaRecorder()
            created = r
            r.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioEncodingBitRate(BIT_RATE)
                setAudioSamplingRate(SAMPLE_RATE)
                // A forgotten recording must never fill storage or exceed the backup size limit.
                setMaxDuration(MAX_DURATION_MS)
                setMaxFileSize(MAX_FILE_BYTES)
                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                        what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
                    ) limitReached = true
                }
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

    /**
     * Very short or interrupted recordings are discarded instead of saved as broken media.
     * A recording that the recorder itself ended at the duration/size cap is kept.
     */
    fun stop(): File? {
        val r = recorder ?: return null
        val f = file
        recorder = null
        file = null
        return try {
            r.stop()
            keepIfValid(f)
        } catch (_: RuntimeException) {
            if (limitReached) keepIfValid(f) else {
                f?.delete()
                null
            }
        } finally {
            runCatching { r.release() }
        }
    }

    fun cancel() {
        stop()?.delete()
    }

    private fun keepIfValid(f: File?): File? =
        f?.takeIf { it.isFile && it.length() > 0L } ?: run {
            f?.delete()
            null
        }

    companion object {
        const val MAX_DURATION_MS = 120_000
        const val MAX_FILE_BYTES = 5L * 1024 * 1024
        private const val BIT_RATE = 64_000
        private const val SAMPLE_RATE = 44_100
    }
}
