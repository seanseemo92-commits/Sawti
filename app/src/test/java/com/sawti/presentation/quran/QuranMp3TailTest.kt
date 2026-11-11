package com.sawti.presentation.quran

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class QuranMp3TailTest {
    private fun syntheticMp3(file: File, frames: Int) {
        file.outputStream().use { output ->
            val frame = ByteArray(417)
            frame[0] = 0xFF.toByte()
            frame[1] = 0xFB.toByte()
            frame[2] = 0x90.toByte()
            repeat(frames) { output.write(frame) }
        }
    }

    @Test fun cutsOnlyFinalFramesWithoutChangingSourceOrReencoding() {
        val input = File.createTempFile("quran-full-source", ".mp3")
        val output = File.createTempFile("quran-baqarah-ending", ".mp3")
        try {
            syntheticMp3(input, 1000) // approximately 26.1 seconds
            val duration = QuranMp3Tail.cut(input, output, 8000)
            assertTrue("unexpected cropped duration $duration", duration in 7900..8500)
            assertTrue(output.length() < input.length() / 2)
            assertTrue(QuranMp3.isPlayable(output))
            assertEquals(417L * 1000, input.length())
        } finally {
            input.delete()
            output.delete()
        }
    }

    @Test fun refusesInsufficientTailRatherThanPublishingIncompleteRecitation() {
        val input = File.createTempFile("short-quran", ".mp3")
        val output = File.createTempFile("no-quran", ".mp3")
        try {
            syntheticMp3(input, 50)
            assertThrows(java.io.IOException::class.java) { QuranMp3Tail.cut(input, output, 8000) }
        } finally {
            input.delete()
            output.delete()
        }
    }
}
