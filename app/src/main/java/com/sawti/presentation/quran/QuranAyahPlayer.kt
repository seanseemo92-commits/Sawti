package com.sawti.presentation.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.sawti.audio.AudioFocusController
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

enum class RecitationPhase { IDLE, BUFFERING, PLAYING, ERROR }
enum class RecitationFailure { DOWNLOAD, FOCUS, DECODER }

data class RecitationStatus(
    val globalVerseNumber: Int? = null,
    val phase: RecitationPhase = RecitationPhase.IDLE,
    val remaining: Int = 0,
    val failure: RecitationFailure? = null,
)

/** A tap always restarts this ayah. There is deliberately no queue or whole-surah player. */
class QuranAyahPlayer(
    context: Context,
    private val cache: QuranAudioCache,
    private val focus: AudioFocusController = AudioFocusController(context.applicationContext),
    private val beforePlay: () -> Unit = {},
    private val update: (RecitationStatus) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var downloadJob: Job? = null
    private var focusToken: Long? = null
    private var generation = 0L
    private var playsLeft = 0
    private var released = false

    fun play(verse: QuranVerse, repeatCount: Int) {
        require(repeatCount in setOf(1, 3, 5))
        QuranSource.checkAyah(verse.globalNumber, verse.surahNumber, verse.verseNumber)
        if (released) return
        stop()
        beforePlay()
        val request = generation
        playsLeft = repeatCount
        publish(verse, RecitationPhase.BUFFERING)
        downloadJob = scope.launch {
            try {
                val file = cache.obtain(verse.globalNumber, verse.surahNumber, verse.verseNumber)
                ensureActive()
                if (owns(request)) prepareLocalPlayer(verse, file, request, 0)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (owns(request)) fail(verse, RecitationFailure.DOWNLOAD)
            }
        }
    }

    private fun owns(request: Long) = !released && request == generation

    private fun publish(verse: QuranVerse, phase: RecitationPhase) {
        update(RecitationStatus(verse.globalNumber, phase, playsLeft))
    }

    private fun prepareLocalPlayer(verse: QuranVerse, file: File, request: Long, attempt: Int) {
        if (!owns(request)) return
        val candidate = MediaPlayer()
        player = candidate
        try {
            candidate.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            candidate.setDataSource(file.absolutePath)
            candidate.setOnPreparedListener { mp ->
                if (mp !== player || !owns(request)) return@setOnPreparedListener
                val token = focus.acquire { if (owns(request)) stop() }
                if (token == null) {
                    fail(verse, RecitationFailure.FOCUS)
                    return@setOnPreparedListener
                }
                focusToken = token
                try {
                    mp.start()
                    publish(verse, RecitationPhase.PLAYING)
                } catch (_: Exception) {
                    recoverDecode(verse, request, attempt)
                }
            }
            candidate.setOnCompletionListener { mp ->
                if (mp !== player || !owns(request)) return@setOnCompletionListener
                playsLeft--
                if (playsLeft > 0) {
                    try {
                        mp.seekTo(0)
                        mp.start()
                        publish(verse, RecitationPhase.PLAYING)
                    } catch (_: Exception) {
                        recoverDecode(verse, request, attempt)
                    }
                } else stop()
            }
            candidate.setOnErrorListener { mp, what, _ ->
                if (mp === player && owns(request)) {
                    // A dead media service is not evidence of damaged downloaded content.
                    if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                        fail(verse, RecitationFailure.DECODER)
                    } else recoverDecode(verse, request, attempt)
                }
                true
            }
            candidate.prepareAsync()
        } catch (_: Exception) {
            recoverDecode(verse, request, attempt)
        }
    }

    private fun recoverDecode(verse: QuranVerse, request: Long, attempt: Int) {
        if (!owns(request)) return
        clearPlayer()
        publish(verse, RecitationPhase.BUFFERING)
        downloadJob = scope.launch {
            try {
                cache.invalidate(verse.globalNumber)
                ensureActive()
                if (!owns(request)) return@launch
                if (attempt >= 1) {
                    fail(verse, RecitationFailure.DECODER)
                } else {
                    val file = cache.obtain(verse.globalNumber, verse.surahNumber, verse.verseNumber)
                    ensureActive()
                    if (owns(request)) prepareLocalPlayer(verse, file, request, attempt + 1)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (owns(request)) fail(verse, RecitationFailure.DOWNLOAD)
            }
        }
    }

    private fun fail(verse: QuranVerse, failure: RecitationFailure) {
        stop()
        if (!released) update(RecitationStatus(verse.globalNumber, RecitationPhase.ERROR, failure = failure))
    }

    private fun clearPlayer() {
        val active = player
        player = null
        if (active != null) {
            runCatching { active.reset() }
            runCatching { active.release() }
        }
        focusToken?.let(focus::release)
        focusToken = null
    }

    fun stop() {
        generation++
        downloadJob?.cancel()
        downloadJob = null
        playsLeft = 0
        clearPlayer()
        update(RecitationStatus())
    }

    fun release() {
        if (released) return
        released = true
        stop()
        scope.cancel()
    }
}
