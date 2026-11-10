package com.sawti.presentation.quran

import android.app.Application
import android.media.MediaPlayer
import java.io.File
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class QuranAyahPlayerTest {
    private val created = Collections.synchronizedList(mutableListOf<ShadowMediaPlayer>())
    private val states = Collections.synchronizedList(mutableListOf<RecitationStatus>())
    private lateinit var audio: QuranAyahPlayer
    private lateinit var directory: File
    private val verse = QuranVerse(1, 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val app = RuntimeEnvironment.getApplication() as Application
        directory = File(app.filesDir, "quran_recitations/ar.alafasy")
        directory.mkdirs()
        val file = File(directory, "ayah_1.mp3")
        file.writeBytes(ByteArray(417 * 4).apply {
            for (offset in indices step 417) {
                this[offset] = 0xFF.toByte(); this[offset + 1] = 0xFB.toByte(); this[offset + 2] = 0x90.toByte()
            }
        })
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(file.absolutePath), ShadowMediaPlayer.MediaInfo(10_000, -1))
        ShadowMediaPlayer.setCreateListener { _, shadow -> created.add(shadow) }
        audio = QuranAyahPlayer(app, QuranAudioCache(app)) { states.add(it) }
    }

    @After
    fun cleanup() {
        audio.release()
        directory.deleteRecursively()
        ShadowMediaPlayer.setCreateListener(null)
        Dispatchers.resetMain()
    }

    private fun preparedPlayer(index: Int): ShadowMediaPlayer {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (System.nanoTime() < deadline) {
            if (created.size > index && created[index].state == ShadowMediaPlayer.State.PREPARING) return created[index]
            Thread.sleep(5)
        }
        error("Timed out waiting for asynchronous cached-ayah preparation")
    }

    @Test
    fun tappingSameAyahRestartsAndOldCompletionCannotStopNewPlayback() {
        audio.play(verse, 1)
        val first = preparedPlayer(0)
        first.invokePreparedListener()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        audio.play(verse, 1)
        val second = preparedPlayer(1)
        second.invokePreparedListener()
        assertEquals(ShadowMediaPlayer.State.END, first.state)
        first.invokeCompletionListener()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.STARTED, second.state)
    }

    @Test
    fun stopWhilePreparingDiscardsLatePreparedCallback() {
        audio.play(verse, 1)
        val pending = preparedPlayer(0)
        audio.stop()
        pending.invokePreparedListener()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
        assertNotEquals(ShadowMediaPlayer.State.STARTED, pending.state)
    }

    @Test
    fun threeRepeatsStopAfterThirdCompletion() {
        audio.play(verse, 3)
        val current = preparedPlayer(0)
        current.invokePreparedListener()
        assertEquals(3, states.last().remaining)
        current.invokeCompletionListener()
        assertEquals(2, states.last().remaining)
        current.invokeCompletionListener()
        assertEquals(1, states.last().remaining)
        current.invokeCompletionListener()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
    }
}
