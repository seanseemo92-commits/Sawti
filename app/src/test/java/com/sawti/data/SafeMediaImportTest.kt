package com.sawti.data

import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeMediaImportTest {
    private fun privateTarget(): File =
        File.createTempFile("sawti-media-test", ".bin").also { it.delete() }

    @Test fun smallMediaIsCopiedUnchanged() {
        val target = privateTarget()
        try {
            val content = "صوتي".toByteArray(Charsets.UTF_8)
            SafeMediaImport.copy(ByteArrayInputStream(content), target, 128)
            assertArrayEquals(content, target.readBytes())
        } finally {
            target.delete()
        }
    }

    @Test fun oversizeCopyRemovesPartialFile() {
        val target = privateTarget()
        assertThrows(IllegalArgumentException::class.java) {
            SafeMediaImport.copy(ByteArrayInputStream(ByteArray(30)), target, 12)
        }
        assertFalse(target.exists())
    }

    @Test fun emptyCopyRemovesPartialFile() {
        val target = privateTarget()
        assertThrows(IllegalArgumentException::class.java) {
            SafeMediaImport.copy(ByteArrayInputStream(byteArrayOf()), target, 100)
        }
        assertFalse(target.exists())
    }

    @Test fun rejectsUnsafeExtensions() {
        listOf("../file", "audio/../../x", "mp3.", "", "MP3").forEach { extension ->
            assertThrows(IllegalArgumentException::class.java) {
                SafeMediaImport.checkAudioExtension(extension)
            }
        }
        listOf("mp3", "wav", "m4a", "ogg", "audio").forEach {
            SafeMediaImport.checkAudioExtension(it)
        }
        assertTrue(SafeMediaImport.MAX_PHOTO_BYTES < SafeMediaImport.MAX_AUDIO_BYTES)
    }
}
