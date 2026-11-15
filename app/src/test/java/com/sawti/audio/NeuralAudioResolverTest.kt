package com.sawti.audio

import android.app.Application
import com.sawti.domain.model.ArabicDialect
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class NeuralAudioResolverTest {
    @Test fun onlyManifestMatchedSpeechCanUseAnOfflineClip() {
        val app = RuntimeEnvironment.getApplication() as Application
        val folder = File(app.filesDir, "neural_tts/libyan").apply { mkdirs() }
        val audio = File(folder, "request.water.mp3").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val manifest = File(folder, "manifest.json").apply {
            writeText("""{"version":1,"entries":{"request.water":{"file":"request.water.mp3","speechText":"نَبِّي إِمِّيَّهْ"}}}""")
        }
        try {
            val resolver = NeuralAudioResolver(app)
            assertEquals(NeuralClip.Downloaded(audio),
                resolver.resolve(ArabicDialect.LIBYAN, "request.water", "نَبِّي إِمِّيَّهْ"))
            assertNull(resolver.resolve(ArabicDialect.LIBYAN, "request.water", "النص بعد التعديل"))
            assertNull(resolver.resolve(ArabicDialect.LIBYAN, "custom.user", "نَبِّي إِمِّيَّهْ"))
            assertNull(resolver.resolve(ArabicDialect.TUNISIAN, "request.water", "نَبِّي إِمِّيَّهْ"))
        } finally { audio.delete(); manifest.delete(); folder.delete() }
    }

    @Test fun unsafeKeysAreNeverResolvedIntoPaths() {
        for (key in listOf("../escape", "a/secret", "", "..", "x\\y", "a..b")) {
            assertFalse(NeuralAudioResolver.safeKey(key))
        }
        assertTrue(NeuralAudioResolver.safeKey("dish.tunisian.brik"))
    }
}
