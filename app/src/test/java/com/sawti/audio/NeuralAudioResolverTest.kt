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
    @Test fun signedRateManifestResolvesByDialectAndSemanticKeyDespiteRevisedVowels() {
        val app = RuntimeEnvironment.getApplication() as Application
        val folder = File(app.filesDir, "neural_tts/libyan").apply { mkdirs() }
        val audio = File(folder, "request.water.mp3").apply { writeBytes(ByteArray(128) { 1 }) }
        val manifest = File(folder, "manifest.json").apply {
            writeText("""{"version":1,"rate":"-18%","entries":{"request.water":{"file":"request.water.mp3","speechText":"نِبِّي مَيَّة"}}}""")
        }
        try {
            val resolver = NeuralAudioResolver(app)
            assertEquals(NeuralClip.Downloaded(audio),
                resolver.resolve(ArabicDialect.LIBYAN, "request.water", "نِبِّي مَيَّة"))
            assertEquals(NeuralClip.Downloaded(audio),
                resolver.resolve(ArabicDialect.LIBYAN, "request.water", "نِبِّي مَيّه"))
            assertNull(resolver.resolve(ArabicDialect.LIBYAN, "custom.user", "نِبِّي مَيَّة"))
            assertEquals(NeuralClip.Bundled("neural_tts/tunisian/request.water.mp3"),
                resolver.resolve(ArabicDialect.TUNISIAN, "request.water", "نِبِّي مَيَّة"))
        } finally { audio.delete(); manifest.delete(); folder.delete() }
    }

    @Test fun unsafeKeysCannotEscapeBundleDirectory() {
        for (key in listOf("../escape", "a/secret", "", "..", "x\\y", "a..b")) {
            assertFalse(NeuralAudioResolver.safeKey(key))
        }
        assertTrue(NeuralAudioResolver.safeKey("dish.tunisian.brik"))
    }

    @Test fun downloadedClipWithOldVowelsCannotShadowUpdatedBundledClip() {
        val app = RuntimeEnvironment.getApplication() as Application
        val folder = File(app.filesDir, "neural_tts/libyan").apply { mkdirs() }
        val audio = File(folder, "request.water.mp3").apply { writeBytes(ByteArray(256) { 42 }) }
        val manifest = File(folder, "manifest.json").apply {
            writeText("""{"version":1,"rate":"-18%","entries":{"request.water":{"file":"request.water.mp3","speechText":"نَبِّيْ إِمِّيَّهْ"}}}""")
        }
        try {
            assertEquals(NeuralClip.Bundled("neural_tts/libyan/request.water.mp3"),
                NeuralAudioResolver(app).resolve(ArabicDialect.LIBYAN, "request.water", "نِبِّي مَيَّة"))
        } finally { audio.delete(); manifest.delete(); folder.delete() }
    }

    @Test fun obsoleteDownloadedRateFallsBackToFreshBundledVoice() {
        val app = RuntimeEnvironment.getApplication() as Application
        val folder = File(app.filesDir, "neural_tts/libyan").apply { mkdirs() }
        val audio = File(folder, "request.water.mp3").apply { writeBytes(ByteArray(128) { 1 }) }
        val manifest = File(folder, "manifest.json").apply {
            writeText("""{"version":1,"entries":{"request.water":{"file":"request.water.mp3","speechText":"صوت"}}}""")
        }
        try {
            assertEquals(NeuralClip.Bundled("neural_tts/libyan/request.water.mp3"),
                NeuralAudioResolver(app).resolve(ArabicDialect.LIBYAN, "request.water", "صوت"))
        } finally { audio.delete(); manifest.delete(); folder.delete() }
    }
}
