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

    @Test fun verifiedExtraTrimRemovesOldIntroWithoutReencodingRestOfClip() {
        val original = File.createTempFile("original-baqarah", ".mp3")
        val oldCut = File.createTempFile("old-baqarah-cut", ".mp3")
        val corrected = File.createTempFile("new-baqarah-cut", ".mp3")
        try {
            syntheticMp3(original, 2500)
            val oldLengthMs = QuranMp3Tail.cut(original, oldCut, 35_000)
            val trimmedMs = QuranMp3Tail.cut(original, corrected, 35_000 - 1_250)
            assertTrue(QuranMp3.isPlayable(corrected))
            assertTrue(corrected.length() < oldCut.length())
            assertEquals("Whole MP3 frames are retained after a ~1.25s later cut",
                1_250, oldLengthMs - trimmedMs, 45)
        } finally {
            original.delete()
            oldCut.delete()
            corrected.delete()
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
