package com.sawti.presentation.quran

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/** Checks real consecutive MPEG Layer III frames, rather than trusting an ID3 tag.
 * This is a format sanity check; decoder failures still invalidate and retry the file.
 * Original recitations are never transcoded or modified.
 */
internal object QuranMp3 {
    fun isPlayable(file: File): Boolean {
        if (!file.isFile || file.length() !in 1024L..(12L * 1024 * 1024)) return false
        return try {
            RandomAccessFile(file, "r").use { input ->
                var start = 0L
                if (input.readUnsignedByte() == 0x49 && input.readUnsignedByte() == 0x44 &&
                    input.readUnsignedByte() == 0x33) {
                    val version = input.readUnsignedByte()
                    input.readUnsignedByte()
                    val flags = input.readUnsignedByte()
                    var size = 0L
                    repeat(4) {
                        val part = input.readUnsignedByte()
                        if (part >= 128) return@use false
                        size = (size shl 7) or part.toLong()
                    }
                    start = 10L + size + if (version == 4 && flags and 0x10 != 0) 10 else 0
                }
                val scanEnd = minOf(start + 4096, input.length() - 4)
                while (start <= scanEnd) {
                    var position = start
                    var frames = 0
                    while (frames < 3 && position + 4 <= input.length()) {
                        input.seek(position)
                        val size = frameBytes(input.readInt())
                        if (size == 0 || position + size > input.length()) break
                        position += size
                        frames++
                    }
                    if (frames == 3) return@use true
                    start++
                }
                false
            }
        } catch (_: IOException) {
            false
        }
    }

    private fun frameBytes(header: Int): Int {
        if (header ushr 21 != 0x7FF) return 0
        val version = (header ushr 19) and 3
        val layer = (header ushr 17) and 3
        val bitrateIndex = (header ushr 12) and 15
        val sampleIndex = (header ushr 10) and 3
        if (version == 1 || layer != 1 || bitrateIndex !in 1..14 || sampleIndex == 3) return 0
        val rates = if (version == 3) {
            intArrayOf(0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320)
        } else {
            intArrayOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160)
        }
        val sampleRate = intArrayOf(44100, 48000, 32000)[sampleIndex] /
            when (version) { 3 -> 1; 2 -> 2; else -> 4 }
        val padding = (header ushr 9) and 1
        return (if (version == 3) 144000 else 72000) * rates[bitrateIndex] / sampleRate + padding
    }
}
