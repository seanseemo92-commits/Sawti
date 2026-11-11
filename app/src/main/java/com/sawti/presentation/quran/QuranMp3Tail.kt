package com.sawti.presentation.quran

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Extract the ending of a RANGE-fetched MP3, without recompressing or merging ayahs.
 *
 * A remote source's full-track duration and 285's validated start time must be
 * recorded together. This parser counts MPEG frame samples in a bounded tail;
 * unlike proportional byte seeking it remains correct for variable bitrate MP3.
 */
internal object QuranMp3Tail {
    private data class FrameFormat(val bytes: Int, val microseconds: Long)
    private data class Frame(val byteOffset: Long, val startMicroseconds: Long, val bytes: Int)

    private fun format(header: Int): FrameFormat? {
        if (header ushr 21 != 0x7FF) return null
        val version = header ushr 19 and 3
        val layer = header ushr 17 and 3
        val bitrateIndex = header ushr 12 and 15
        val sampleIndex = header ushr 10 and 3
        if (version == 1 || layer != 1 || bitrateIndex !in 1..14 || sampleIndex == 3)
            return null
        val bitrates = if (version == 3)
            intArrayOf(0,32,40,48,56,64,80,96,112,128,160,192,224,256,320)
        else intArrayOf(0,8,16,24,32,40,48,56,64,80,96,112,128,144,160)
        val rate = intArrayOf(44100,48000,32000)[sampleIndex] /
            when (version) { 3 -> 1; 2 -> 2; else -> 4 }
        val samples = if (version == 3) 1152 else 576
        val padding = header ushr 9 and 1
        val bytes = (if (version == 3) 144000 else 72000) *
            bitrates[bitrateIndex] / rate + padding
        if (bytes !in 24..2048) return null
        return FrameFormat(bytes, samples.toLong() * 1_000_000L / rate)
    }

    private fun firstFrame(file: RandomAccessFile): Long {
        val limit = minOf(65536L, file.length() - 4L)
        for (start in 0L..limit) {
            var position = start
            var valid = true
            repeat(4) {
                if (position + 4 > file.length()) { valid = false; return@repeat }
                file.seek(position)
                val f = format(file.readInt())
                if (f == null || position + f.bytes > file.length()) {
                    valid = false
                } else position += f.bytes
            }
            if (valid) return start
        }
        throw IOException("No continuous MPEG Layer III frames in downloaded tail")
    }

    /**
     * Keep only the end corresponding to ayah 285..286. With a source-matched
     * full-track duration, fromEndMs = fullDurationMs - ayah285StartMs.
     * A 0.12s decoder pre-roll compensates for the MP3 bit reservoir.
     */
    fun cut(downloadedTail: File, destination: File, fromEndMs: Long): Int {
        require(fromEndMs in 1000L..300_000L) { "Unexpected Baqarah ending duration" }
        RandomAccessFile(downloadedTail, "r").use { source ->
            val start = firstFrame(source)
            val frames = ArrayList<Frame>()
            var pos = start
            var elapsed = 0L
            while (pos + 4 <= source.length()) {
                source.seek(pos)
                val format = format(source.readInt()) ?: break
                if (pos + format.bytes > source.length()) break
                frames.add(Frame(pos,elapsed,format.bytes))
                elapsed += format.microseconds
                pos += format.bytes
            }
            if (frames.size < 4 || elapsed < (fromEndMs + 1000L) * 1000L)
                throw IOException("Audio range does not contain all of verses 285 and 286")
            val desiredStart = elapsed - fromEndMs * 1000L
            val prerollStart = maxOf(0L, desiredStart - 120_000L)
            val chosen = frames.lastOrNull { it.startMicroseconds <= prerollStart }
                ?: frames.first()
            // Writing precisely up to the last complete frame avoids carrying
            // incomplete remote transport data or metadata into the saved clip.
            val cleanEnd = pos
            val length = cleanEnd - chosen.byteOffset
            if (length !in 1024L..(12L * 1024 * 1024))
                throw IOException("Cropped Quran ending has implausible size")
            FileOutputStream(destination).use { output ->
                source.seek(chosen.byteOffset)
                val buffer = ByteArray(8192)
                var remaining = length
                while (remaining > 0) {
                    val n = source.read(buffer,0,minOf(buffer.size.toLong(),remaining).toInt())
                    if (n <= 0) throw IOException("Truncated Quran tail")
                    output.write(buffer,0,n)
                    remaining -= n
                }
                output.fd.sync()
            }
            if (!QuranMp3.isPlayable(destination))
                throw IOException("Cropped Quran ending has invalid MPEG frames")
            return ((elapsed - chosen.startMicroseconds)/1000).toInt()
        }
    }
}
