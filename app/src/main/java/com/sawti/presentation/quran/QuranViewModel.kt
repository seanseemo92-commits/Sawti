package com.sawti.presentation.quran

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sawti.SawtiApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class QuranUiState(
    val surahId: Int? = null,
    val reciter: QuranReciter = QuranReciter.ALAFASY,
    val repeats: Int = 1,
    val verses: List<QuranVerse> = emptyList(),
    val loading: Boolean = false,
    val loadError: String? = null,
    val audio: RecitationStatus = RecitationStatus(),
    val fullCues: List<QuranFullCue> = emptyList(),
    val savedAyahs: Int = 0,
    val downloading: Boolean = false,
    val downloadError: String? = null,
)

/** Screen jobs have explicit ownership; late cancellation cannot change a newer request. */
internal class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SawtiApp
    private val preferences = app.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(QuranUiState(reciter = QuranReciter.fromEdition(
        preferences.getString("selected_edition", null)
    )))
    val state = _state.asStateFlow()
    private var cache = QuranAudioCache(app, _state.value.reciter, usePackagedSurahs = true)
    private var playerGeneration = 0L
    private var loadGeneration = 0L
    private var downloadGeneration = 0L
    private var loadJob: Job? = null
    private var downloadJob: Job? = null
    private var countJob: Job? = null
    private var timingJob: Job? = null
    private var player = createPlayer()

    private fun createPlayer(): QuranAyahPlayer {
        val owner = ++playerGeneration
        return QuranAyahPlayer(app, cache, app.container.audioFocus,
            beforePlay = { app.container.audioDispatcher.stop() }) { status ->
            if (owner == playerGeneration) {
                _state.update { it.copy(audio = status) }
                if (status.phase == RecitationPhase.PLAYING) refreshSavedCount()
            }
        }
    }

    fun openSurah(id: Int?, retry: Boolean = false) {
        if (!retry && id == _state.value.surahId &&
            (_state.value.loading || _state.value.verses.isNotEmpty())) return
        val surah = QuranSource.selections.firstOrNull { it.selectionId == id }
        val request = ++loadGeneration
        loadJob?.cancel()
        cancelDownload()
        countJob?.cancel()
        timingJob?.cancel()
        player.stop()
        _state.update { it.copy(surahId = surah?.selectionId, verses = emptyList(),
            loading = surah != null, loadError = null, savedAyahs = 0, downloadError = null,
            fullCues = emptyList()) }
        if (surah == null) return
        loadJob = viewModelScope.launch {
            try {
                val verses = QuranSource.load(app, surah)
                if (request == loadGeneration) {
                    _state.update { it.copy(verses = verses, loading = false) }
                    refreshSavedCount()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request == loadGeneration) _state.update { it.copy(loading = false,
                    loadError = "تعذّر تحميل نص السورة. تحقّق من الاتصال والمساحة المتاحة، ثم أعد المحاولة.") }
            }
        }
    }

    fun selectReciter(reciter: QuranReciter) {
        if (_state.value.reciter == reciter) return
        cancelDownload()
        countJob?.cancel()
        timingJob?.cancel()
        ++playerGeneration
        player.release()
        cache = QuranAudioCache(app, reciter, usePackagedSurahs = true)
        _state.update { it.copy(reciter = reciter, audio = RecitationStatus(), savedAyahs = 0,
            fullCues = emptyList(),
            downloadError = null) }
        player = createPlayer()
        preferences.edit().putString("selected_edition", reciter.editionId).apply()
        refreshSavedCount()
    }

    fun selectRepeats(count: Int) {
        require(count in setOf(1, 3, 5))
        _state.update { it.copy(repeats = count) }
    }

    fun play(verse: QuranVerse) {
        if (verse !in _state.value.verses) return
        player.play(verse, _state.value.repeats)
    }

    /** Whole recording is independent from per-ayah bundled audio and never enters the APK. */
    fun playFull() {
        val current = _state.value
        val surah = QuranSource.selections.firstOrNull { it.selectionId == current.surahId } ?: return
        if (current.loading || current.verses.isEmpty() || !QuranFullSources.available(surah, current.reciter)) return
        timingJob?.cancel()
        val selectionId = surah.selectionId
        val requestedReciter = current.reciter
        _state.update { it.copy(fullCues = emptyList()) }
        // The full-audio download and timings run independently, so a slow API
        // never blocks the patient's play button or first-time audio cache.
        timingJob = viewModelScope.launch {
            val cues = try {
                QuranFullTimings.obtain(app, surah, requestedReciter)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emptyList()
            }
            if (_state.value.surahId == selectionId && _state.value.reciter == requestedReciter) {
                _state.update { it.copy(fullCues = cues) }
            }
        }
        player.playFull(surah, current.verses.first(), current.repeats)
    }

    fun stopAudio() = player.stop()

    private fun refreshSavedCount() {
        countJob?.cancel()
        val currentCache = cache
        val verses = _state.value.verses
        val owner = playerGeneration
        countJob = viewModelScope.launch {
            val count = currentCache.cachedCount(verses)
            if (owner == playerGeneration && verses == _state.value.verses) {
                _state.update { it.copy(savedAyahs = count) }
            }
        }
    }

    fun toggleDownload() {
        if (_state.value.downloading) { cancelDownload(); return }
        val verses = _state.value.verses
        if (verses.isEmpty()) return
        val currentCache = cache
        val request = ++downloadGeneration
        _state.update { it.copy(downloading = true, downloadError = null) }
        downloadJob = viewModelScope.launch {
            try {
                for (verse in verses) {
                    currentCache.obtain(verse)
                    val count = currentCache.cachedCount(verses)
                    if (request == downloadGeneration) _state.update { it.copy(savedAyahs = count) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request == downloadGeneration) _state.update { it.copy(
                    downloadError = "تعذّر حفظ بعض الآيات. تحقّق من الاتصال والمساحة ثم أعد المحاولة؛ الآيات المحفوظة باقية.") }
            } finally {
                if (request == downloadGeneration) _state.update { it.copy(downloading = false) }
            }
        }
    }

    private fun cancelDownload() {
        ++downloadGeneration
        downloadJob?.cancel()
        downloadJob = null
        _state.update { it.copy(downloading = false) }
    }

    fun leaveScreen() {
        timingJob?.cancel()
        player.stop()
        cancelDownload()
        ++loadGeneration
        loadJob?.cancel()
        loadJob = null
        _state.update { it.copy(loading = false) }
    }

    override fun onCleared() {
        timingJob?.cancel()
        ++playerGeneration
        player.release()
        super.onCleared()
    }
}
