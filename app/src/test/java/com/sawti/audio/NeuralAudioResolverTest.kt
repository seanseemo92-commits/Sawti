package com.sawti.audio

import android.app.Application
import com.sawti.domain.model.ArabicDialect
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Old Microsoft voice manifests cannot satisfy the strict Habibi resolver. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class NeuralAudioResolverTest {
    @Test fun unsafeSemanticKeysCannotEscapeAssets() {
        for (key in listOf("../a", "x/y", "", "..", "a..b", "x\\y")) {
            assertFalse(HabibiAudioResolver.safeKey(key))
        }
        assertTrue(HabibiAudioResolver.safeKey("animal.cow"))
        assertTrue(HabibiAudioResolver.safeKey("dish.tunisian.brik"))
    }

    @Test fun legacyMicrosoftManifestIsAlwaysRejectedEvenAfterHabibiIsInstalled() {
        val v1 = """{"version":1,"rate":"-18%","entries":{}}"""
        val v2 = """{"version":2,"engine":"Habibi-TTS","model":"Unified","license":"CC-BY-NC-SA-4.0","dialectId":"LBY","sampleRate":24000,"bitrate":"96k","entries":{}}"""
        assertFalse(HabibiAudioResolver.acceptsManifestHeader(
            v1, ArabicDialect.LIBYAN))
        assertTrue(HabibiAudioResolver.acceptsManifestHeader(
            v2, ArabicDialect.LIBYAN))
        assertFalse(HabibiAudioResolver.acceptsManifestHeader(
            v2, ArabicDialect.EGYPTIAN))
    }

    @Test fun unknownOrInvalidKeysReturnNoAudio() {
        val app = RuntimeEnvironment.getApplication() as Application
        val resolver = HabibiAudioResolver(app)
        assertNull(resolver.open(ArabicDialect.LIBYAN, null, "نبي مية"))
        assertNull(resolver.open(ArabicDialect.LIBYAN, "../a", "نبي مية"))
    }
}
