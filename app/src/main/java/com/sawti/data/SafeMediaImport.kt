package com.sawti.data

import java.io.File
import java.io.InputStream

/** Pure JVM helper: bounded imports must not exhaust storage or leave partial files. */
internal object SafeMediaImport {
    const val MAX_PHOTO_BYTES = 15L * 1024 * 1024
    const val MAX_AUDIO_BYTES = 50L * 1024 * 1024
    private val audioExtensions = setOf("mp3", "m4a", "wav", "ogg", "audio")

    fun checkAudioExtension(extension: String) {
        require(extension in audioExtensions) { "امتداد صوتي غير مدعوم" }
    }

    fun copy(input: InputStream, target: File, limit: Long) {
        require(limit > 0L)
        try {
            input.use { source ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        total += count
                        require(total <= limit) { "الملف كبير جداً، اختر ملفاً أصغر" }
                        output.write(buffer, 0, count)
                    }
                }
            }
            require(target.length() > 0L) { "الملف فارغ" }
        } catch (error: Exception) {
            target.delete()
            throw error
        }
    }
}
