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
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

enum class RecitationPhase { IDLE, BUFFERING, PLAYING, PAUSED, ERROR }
enum class RecitationFailure { DOWNLOAD, FOCUS, DECODER }

data class RecitationStatus(
    val globalVerseNumber: Int? = null,
    val phase: RecitationPhase = RecitationPhase.IDLE,
    val remaining: Int = 0,
    val failure: RecitationFailure? = null,
    val segmentKey: String? = null,
    val fullSelectionId: Int? = null,
    val positionMs: Int = 0,
)

/** Per-ayah taps restart the selected clip. Optional full recordings use the same single owner. */
class QuranAyahPlayer(
    context: Context,
    private val cache: QuranAudioCache,
    private val focus: AudioFocusController = AudioFocusController(context.applicationContext),
    private val beforePlay: () -> Unit = {},
    private val update: (RecitationStatus) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val fullCache = QuranFullAudioCache(context, cache.reciter)
    private var fullSurah: ShortSurah? = null
    private var player: MediaPlayer? = null
    private var downloadJob: Job? = null
    private var progressJob: Job? = null
    private var fullPositionMs = 0
    private var currentPhase: RecitationPhase = RecitationPhase.IDLE
    private var focusToken: Long? = null
    private var generation = 0L
    private var playsLeft = 0
    private var pausedForFocus = false
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
                val file = cache.obtain(verse)
                ensureActive()
                if (owns(request)) prepareLocalPlayer(verse, file, request, 0)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (owns(request)) fail(verse, RecitationFailure.DOWNLOAD)
            }
        }
    }

    /** A single original recording, downloaded on first use and retained privately. */
    fun playFull(surah: ShortSurah, representativeVerse: QuranVerse, repeatCount: Int) {
        require(repeatCount in setOf(1, 3, 5))
        require(QuranFullSources.available(surah))
        require(representativeVerse.surahNumber == surah.number)
        if (released) return
        stop()
        beforePlay()
        fullSurah = surah
        val request = generation
        playsLeft = repeatCount
        publish(representativeVerse, RecitationPhase.BUFFERING)
        downloadJob = scope.launch {
            try {
                val file = fullCache.obtain(surah)
                ensureActive()
                if (owns(request)) prepareLocalPlayer(representativeVerse, file, request, 0)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (owns(request)) fail(representativeVerse, RecitationFailure.DOWNLOAD)
            }
        }
    }

    private fun owns(request: Long) = !released && request == generation

    private fun publish(verse: QuranVerse, phase: RecitationPhase) {
        val full = fullSurah
        currentPhase = phase
        update(RecitationStatus(if (full == null) verse.globalNumber else null, phase, playsLeft,
            segmentKey = if (full == null) verse.segmentKey else null,
            fullSelectionId = full?.selectionId,
            positionMs = if (full != null) fullPositionMs else 0))
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
                val token = focus.acquire(
                    onLost = { if (owns(request) && player === mp) stop() },
                    onTransientLoss = {
                        if (owns(request) && player === mp && !pausedForFocus) {
                            runCatching { mp.pause() }
                                .onSuccess {
                                    pausedForFocus = true
                                    publish(verse, RecitationPhase.PAUSED)
                                }
                                .onFailure { fail(verse, RecitationFailure.FOCUS) }
                        }
                    },
                    // Quran is speech: pause for notification sounds so the
                    // listener never misses part of a verse or repeat.
                    onDuck = {
                        if (owns(request) && player === mp && !pausedForFocus) {
                            runCatching { mp.pause() }
                                .onSuccess {
                                    pausedForFocus = true
                                    publish(verse, RecitationPhase.PAUSED)
                                }
                                .onFailure { fail(verse, RecitationFailure.FOCUS) }
                        }
                    },
                    onGain = {
                        if (owns(request) && player === mp) {
                            if (pausedForFocus) {
                                pausedForFocus = false
                                runCatching {
                                    mp.start()
                                    publish(verse, RecitationPhase.PLAYING)
                                }.onFailure { fail(verse, RecitationFailure.FOCUS) }
                            }
                        }
                    },
                )
                if (token == null) {
                    fail(verse, RecitationFailure.FOCUS)
                    return@setOnPreparedListener
                }
                focusToken = token
                try {
                    mp.start()
                    publish(verse, RecitationPhase.PLAYING)
                    monitorFullPlayback(mp, request)
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

    /** MediaPlayer is authoritative; no wall-clock guess while buffering, paused or repeating. */
    private fun monitorFullPlayback(mp: MediaPlayer, request: Long) {
        progressJob?.cancel()
        if (fullSurah == null) return
        progressJob = scope.launch {
            var previous = -1000
            while (owns(request) && player === mp && fullSurah != null) {
                delay(120)
                if (currentPhase != RecitationPhase.PLAYING || pausedForFocus) continue
                val position = runCatching { mp.currentPosition }.getOrNull() ?: break
                if (position < 0 || (kotlin.math.abs(position - previous) < 130 && position != 0)) continue
                previous = position
                fullPositionMs = position
                val full = fullSurah ?: break
                update(RecitationStatus(
                    phase = RecitationPhase.PLAYING, remaining = playsLeft,
                    fullSelectionId = full.selectionId, positionMs = position))
            }
        }
    }

    private fun recoverDecode(verse: QuranVerse, request: Long, attempt: Int) {
        if (!owns(request)) return
        clearPlayer()
        publish(verse, RecitationPhase.BUFFERING)
        downloadJob = scope.launch {
            try {
                val full = fullSurah
                if (full == null) cache.invalidate(verse) else fullCache.invalidate(full)
                ensureActive()
                if (!owns(request)) return@launch
                if (attempt >= 1) {
                    fail(verse, RecitationFailure.DECODER)
                } else {
                    val file = if (full == null) cache.obtain(verse) else fullCache.obtain(full)
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
        val full = fullSurah
        stop()
        if (!released) update(RecitationStatus(
            globalVerseNumber = if (full == null) verse.globalNumber else null,
            phase = RecitationPhase.ERROR, failure = failure,
            segmentKey = if (full == null) verse.segmentKey else null,
            fullSelectionId = full?.selectionId,
        ))
    }

    private fun clearPlayer() {
        progressJob?.cancel()
        progressJob = null
        val active = player
        player = null
        pausedForFocus = false
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
        fullSurah = null
        fullPositionMs = 0
        currentPhase = RecitationPhase.IDLE
        update(RecitationStatus())
    }

    fun release() {
        if (released) return
        released = true
        stop()
        scope.cancel()
    }
}
