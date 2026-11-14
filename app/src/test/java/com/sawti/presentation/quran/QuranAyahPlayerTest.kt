package com.sawti.presentation.quran

import android.app.Application
import android.media.MediaPlayer
import android.media.AudioManager
import android.content.Context
import android.os.Looper
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
import org.robolectric.Shadows.shadowOf
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
        // A repeat must resume only AFTER Android completes the seek.
        current.setSeekDelay(-1)
        current.invokeCompletionListener()
        assertEquals(RecitationPhase.BUFFERING, states.last().phase)
        assertEquals(2, states.last().remaining)
        current.invokeSeekCompleteListener()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        current.invokeCompletionListener()
        assertEquals(RecitationPhase.BUFFERING, states.last().phase)
        assertEquals(1, states.last().remaining)
        current.invokeSeekCompleteListener()
        current.invokeCompletionListener()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
    }

    @Test
    fun audioFocusRefusalReportsFocusErrorAndKeepsDownloadedFile() {
        val app = RuntimeEnvironment.getApplication() as Application
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        shadowOf(manager).setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)
        audio.play(verse, 1)
        preparedPlayer(0).invokePreparedListener()
        assertEquals(RecitationFailure.FOCUS, states.last().failure)
        assertTrue(File(directory, "ayah_1.mp3").isFile)
    }

    @Test
    fun notificationKeepsTheSameVersePlayingWithoutLosingRepeats() {
        val app = RuntimeEnvironment.getApplication() as Application
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.play(verse, 3)
        val current = preparedPlayer(0)
        current.invokePreparedListener()
        val focusListener = shadowOf(manager).lastAudioFocusRequest.listener
        focusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        assertEquals(3, states.last().remaining)
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        focusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        assertEquals(1, created.size)
    }

    @Test
    fun callPausesAndResumesSameVerseUnlessUserExplicitlyStops() {
        val app = RuntimeEnvironment.getApplication() as Application
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.play(verse, 3)
        val current = preparedPlayer(0)
        current.invokePreparedListener()
        val listener = shadowOf(manager).lastAudioFocusRequest.listener
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.PAUSED, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.PAUSED, current.state)
        assertEquals(3, states.last().remaining)
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.STARTED, current.state)
        audio.stop()
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.END, current.state)
    }

    @Test
    fun permanentFocusLossStopsAndDoesNotAutoResume() {
        val app = RuntimeEnvironment.getApplication() as Application
        val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.play(verse, 1)
        val current = preparedPlayer(0)
        current.invokePreparedListener()
        val listener = shadowOf(manager).lastAudioFocusRequest.listener
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        shadowOf(Looper.getMainLooper()).idle()
        listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
        assertEquals(ShadowMediaPlayer.State.END, current.state)
    }
    @Test
    fun completeRecitationUsesOneFileAndContinuesThroughNotificationThenStopsOnAyahTap() {
        val app = RuntimeEnvironment.getApplication() as Application
        val wholeDirectory = File(app.filesDir, "quran_full_recitations/ar.alafasy")
        wholeDirectory.mkdirs()
        val complete = File(wholeDirectory, "surah_1.mp3")
        complete.writeBytes(ByteArray(417 * 4).apply {
            for (offset in indices step 417) {
                this[offset] = 0xFF.toByte()
                this[offset + 1] = 0xFB.toByte()
                this[offset + 2] = 0x90.toByte()
            }
        })
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(complete.absolutePath), ShadowMediaPlayer.MediaInfo(12_000, -1))
        try {
            val surah = QuranSource.shortSurahs.first()
            val manager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audio.playFull(surah, verse, 3)
            val playing = preparedPlayer(0)
            playing.invokePreparedListener()
            assertEquals(1, states.last().fullSelectionId)
            assertNull(states.last().globalVerseNumber)
            assertEquals(3, states.last().remaining)
            val listener = shadowOf(manager).lastAudioFocusRequest.listener
            listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(RecitationPhase.PLAYING, states.last().phase)
            assertEquals(ShadowMediaPlayer.State.STARTED, playing.state)
            listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(RecitationPhase.PLAYING, states.last().phase)
            playing.invokeCompletionListener()
            assertEquals(2, states.last().remaining)
            audio.play(verse, 1)
            val individual = preparedPlayer(1)
            individual.invokePreparedListener()
            assertEquals(ShadowMediaPlayer.State.END, playing.state)
            playing.invokeCompletionListener()
            assertNull(states.last().fullSelectionId)
            assertEquals(verse.globalNumber, states.last().globalVerseNumber)
            assertEquals(RecitationPhase.PLAYING, states.last().phase)
        } finally {
            wholeDirectory.deleteRecursively()
        }
    }

    private fun registerSegment(verse: QuranVerse) {
        val app = RuntimeEnvironment.getApplication() as Application
        val file = QuranPassages.target(app, verse, QuranReciter.ALAFASY)
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(file.absolutePath), ShadowMediaPlayer.MediaInfo(8_000, -1))
    }

    @Test
    fun differentSegmentsOfTheSameAyahKeepIndependentPlaybackAndIgnoreStaleCompletion() {
        val app = RuntimeEnvironment.getApplication() as Application
        val parts = QuranPassages.load(app, 2001)
        registerSegment(parts[0])
        registerSegment(parts[1])
        audio.play(parts[0], 1)
        val first = preparedPlayer(0)
        first.invokePreparedListener()
        assertEquals(parts[0].segmentKey, states.last().segmentKey)
        audio.play(parts[1], 1)
        val second = preparedPlayer(1)
        second.invokePreparedListener()
        assertEquals(parts[1].segmentKey, states.last().segmentKey)
        assertEquals(ShadowMediaPlayer.State.END, first.state)
        first.invokeCompletionListener()
        assertEquals(parts[1].segmentKey, states.last().segmentKey)
        assertEquals(RecitationPhase.PLAYING, states.last().phase)
    }

    @Test
    fun repeatedSegmentTapsRestartAndThreeRepeatsNeverAdvanceToAnotherPart() {
        val app = RuntimeEnvironment.getApplication() as Application
        val part = QuranPassages.load(app, 2002).first()
        registerSegment(part)
        audio.play(part, 3)
        val old = preparedPlayer(0)
        old.invokePreparedListener()
        audio.play(part, 3)
        val current = preparedPlayer(1)
        current.invokePreparedListener()
        old.invokeCompletionListener()
        assertEquals(3, states.last().remaining)
        current.invokeCompletionListener()
        assertEquals(2, states.last().remaining)
        assertEquals(part.segmentKey, states.last().segmentKey)
        current.invokeCompletionListener()
        assertEquals(1, states.last().remaining)
        current.invokeCompletionListener()
        assertEquals(RecitationPhase.IDLE, states.last().phase)
        assertEquals(2, created.size)
    }

}
