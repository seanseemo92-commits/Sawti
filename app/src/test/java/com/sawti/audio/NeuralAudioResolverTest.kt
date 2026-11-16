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

    @Test fun legacyMicrosoftBundledAudioIsRejected() {
        val app = RuntimeEnvironment.getApplication() as Application
        val resolver = HabibiAudioResolver(app)
        assertNull(resolver.open(ArabicDialect.LIBYAN, "animal.cow", "بقرة"))
        assertNull(resolver.open(ArabicDialect.EGYPTIAN, "request.water", "عايز مية"))
    }

    @Test fun unknownOrInvalidKeysReturnNoAudio() {
        val app = RuntimeEnvironment.getApplication() as Application
        val resolver = HabibiAudioResolver(app)
        assertNull(resolver.open(ArabicDialect.LIBYAN, null, "نبي مية"))
        assertNull(resolver.open(ArabicDialect.LIBYAN, "../a", "نبي مية"))
    }
}
