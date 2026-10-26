package com.sawti.presentation.category

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sawti.SawtiApp
import com.sawti.R
import com.sawti.audio.VoiceRecorder
import com.sawti.audio.AudioDispatcher
import com.sawti.domain.model.Category
import com.sawti.domain.model.FamilyRelationships
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.TtsEndings
import com.sawti.domain.model.WordItem
import com.sawti.domain.repository.WordRepository
import com.sawti.speech.SpeechIssue
import com.sawti.speech.SpeechManager
import com.sawti.state.CaregiverMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A pinned card for the quick-access bar, with its category colour. */
data class FavoriteEntry(val item: WordItem, val palette: Int)

data class LibraryUiState(
    val categories: List<Category> = emptyList(),
    val favorites: List<FavoriteEntry> = emptyList(),
    val caregiverMode: Boolean = false,
    val activeItemId: Long? = null,     // card highlighted after a tap
    val previewItemId: Long? = null,    // family member shown in the large-photo dialog
    val focusItemId: Long? = null,      // card shown in the distraction-free focus dialog
    val editingItemId: Long? = null,    // card shown in the caregiver editor
    val addingFamilyGroup: String? = null,
    val recordingItemId: Long? = null,  // card whose voice is being recorded right now
    val speechIssue: SpeechIssue? = null,
)

/** Everything the category screens can ask for. */
interface LibraryActions {
    fun onItemTapped(item: WordItem, flashcard: Boolean)
    fun onItemLongPressed(item: WordItem)
    fun onHint(item: WordItem)
    fun onSentenceHint(item: WordItem)
    fun onSpeakAgain(item: WordItem)
    fun onToggleFavorite(item: WordItem)
    fun closePreview()
    fun closeFocus()
    fun closeEditor()
    fun openAddFamilyMember(group: String)
    fun closeAddFamilyMember()
    fun addFamilyMember(name: String, group: String, phone: String)
    fun deleteFamilyMember(id: Long)
    fun saveDetails(id: Long, displayText: String, ttsPrompt: String, cue: String, sentence: String, phone: String, group: String)
    fun onPhotoPicked(itemId: Long, uri: Uri)
    fun onAudioPicked(itemId: Long, uri: Uri)
    fun removePhoto(itemId: Long)
    fun startRecording(itemId: Long)
    fun stopRecording(itemId: Long)
    fun previewRecording(itemId: Long)
    fun deleteRecording(itemId: Long)
}

class LibraryViewModel(
    private val repository: WordRepository,
    private val speech: SpeechManager,
    private val voice: AudioDispatcher,
    private val recorder: VoiceRecorder,
    private val resolver: ContentResolver,
    private val caregiver: CaregiverMode,
) : ViewModel(), LibraryActions {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    /** One-shot messages for the UI (string resource ids), e.g. "favourites are full". */
    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    private var highlightJob: Job? = null
    private var lastTap: Pair<String, Long>? = null
    private var cardLatchUntil = 0L

    init {
        viewModelScope.launch {
            repository.observeCategories().collect { list ->
                val favorites = list.flatMap { c ->
                    c.items.filter { it.isFavorite }.map { FavoriteEntry(it, c.palette) }
                }.take(MAX_FAVORITES)
                _uiState.update { it.copy(categories = list, favorites = favorites) }
            }
        }
        viewModelScope.launch {
            caregiver.enabled.collect { on ->
                _uiState.update {
                    it.copy(
                        caregiverMode = on,
                        editingItemId = if (on) it.editingItemId else null,
                        addingFamilyGroup = if (on) it.addingFamilyGroup else null,
                        previewItemId = if (on) null else it.previewItemId,
                    )
                }
            }
        }
        viewModelScope.launch {
            speech.issue.collect { issue -> _uiState.update { it.copy(speechIssue = issue) } }
        }
    }

    fun toggleCaregiver() = caregiver.toggle()

    // ---- Speaking -------------------------------------------------------

    override fun onItemTapped(item: WordItem, flashcard: Boolean) {
        if (cardLatched()) return
        val state = _uiState.value
        if (state.caregiverMode) {
            _uiState.update { it.copy(editingItemId = item.id) }
            return
        }

        voice.play(item)
        highlight(item.id)

        // Family: show the photo large while the name is spoken.
        val previewOnTap = state.categories.firstOrNull { it.id == item.categoryId }?.previewOnTap == true
        if (previewOnTap && !flashcard && item.subgroup != FamilyRelationships.RELATIVES) _uiState.update { it.copy(previewItemId = item.id) }
    }

    /** 1-tap speech from the favourites bar on the home screen. */
    fun onQuickTap(item: WordItem) {
        if (cardLatched()) return
        voice.play(item)
        highlight(item.id)
    }

    /** Long-press in the grid: distraction-free full-screen view of one card. */
    override fun onItemLongPressed(item: WordItem) {
        // Accidental long-press must never open a destructive editor in patient mode.
        if (_uiState.value.caregiverMode) {
            _uiState.update { it.copy(editingItemId = item.id) }
        } else {
            _uiState.update { it.copy(focusItemId = item.id) }
        }
    }

    override fun onHint(item: WordItem) {
        if (isRepeat("h${item.id}")) return
        voice.hint(item)
    }

    override fun onSentenceHint(item: WordItem) {
        if (isRepeat("s${item.id}")) return
        voice.sentenceHint(item)
    }

    override fun onSpeakAgain(item: WordItem) = voice.play(item)

    override fun closePreview() {
        voice.stop()
        _uiState.update { it.copy(previewItemId = null) }
    }

    override fun closeFocus() {
        voice.stop()
        _uiState.update { it.copy(focusItemId = null) }
    }

    /** 1.2-second latch shared by every card click (grid, flashcard, favourites): a tremor can't re-fire speech. */
    private fun cardLatched(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now < cardLatchUntil) return true
        cardLatchUntil = now + CARD_LATCH_MS
        return false
    }

    /** Short per-button debounce for the 💡 / 💬 buttons. */
    private fun isRepeat(key: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        lastTap?.let { (k, t) -> if (k == key && now - t < DEBOUNCE_MS) return true }
        lastTap = key to now
        return false
    }

    private fun highlight(id: Long) {
        highlightJob?.cancel()
        _uiState.update { it.copy(activeItemId = id) }
        highlightJob = viewModelScope.launch {
            delay(HIGHLIGHT_MS)
            _uiState.update { it.copy(activeItemId = null) }
        }
    }

    // ---- Favourites -----------------------------------------------------

    override fun onToggleFavorite(item: WordItem) {
        if (!item.isFavorite) {
            val pinned = _uiState.value.categories.sumOf { c -> c.items.count { it.isFavorite } }
            if (pinned >= MAX_FAVORITES) {
                emit(R.string.favorites_full)
                return
            }
        }
        viewModelScope.launch { repository.setFavorite(item.id, !item.isFavorite) }
    }

    private fun emit(@StringRes message: Int) {
        _messages.tryEmit(message)
    }

    // ---- Caregiver editor ----------------------------------------------

    override fun openAddFamilyMember(group: String) {
        if (FamilyRelationships.validPeopleGroup(group)) {
            _uiState.update { it.copy(addingFamilyGroup = group) }
        }
    }

    override fun closeAddFamilyMember() {
        _uiState.update { it.copy(addingFamilyGroup = null) }
    }

    override fun addFamilyMember(name: String, group: String, phone: String) {
        if (name.isBlank() || _uiState.value.addingFamilyGroup != group ||
            !FamilyRelationships.validPeopleGroup(group)) return
        viewModelScope.launch {
            val id = repository.addFamilyMember(name, group, phone)
            _uiState.update { it.copy(addingFamilyGroup = null, editingItemId = id) }
        }
    }

    override fun deleteFamilyMember(id: Long) {
        val current = find(id) ?: return
        if (current.categoryId != CategoryIds.FAMILY ||
            current.subgroup == FamilyRelationships.RELATIVES) return
        recorder.cancel()
        voice.stop()
        _uiState.update { it.copy(editingItemId = null, previewItemId = null,
            focusItemId = null, recordingItemId = null) }
        viewModelScope.launch { repository.deleteFamilyMember(id) }
    }

    override fun closeEditor() {
        recorder.cancel()
        voice.stop()
        _uiState.update { it.copy(editingItemId = null, recordingItemId = null) }
    }

    override fun saveDetails(id: Long, displayText: String, ttsPrompt: String, cue: String, sentence: String, phone: String, group: String) {
        val current = find(id) ?: return
        val finalDisplay = displayText.ifBlank { current.displayText }
        val enteredTts = if (current.ttsPrompt == current.displayText &&
            ttsPrompt == current.ttsPrompt && finalDisplay != current.displayText
        ) finalDisplay else ttsPrompt.ifBlank { current.ttsPrompt }
        val finalTts = TtsEndings.normalize(enteredTts)
        val finalCue = cue.ifBlank { PhonemicCue.derive(finalTts) }
        val finalSentence = sentence.ifBlank { null } // blank removes the 💬 button for this card
        viewModelScope.launch {
            repository.updateDetails(id, finalDisplay, finalTts, finalCue, finalSentence, phone)
            if (current.categoryId == com.sawti.domain.model.CategoryIds.FAMILY) {
                repository.updateFamilyGroup(id, group)
            }
            closeEditor()
        }
    }

    override fun onPhotoPicked(itemId: Long, uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val input = resolver.openInputStream(uri) ?: error("ملف الصورة غير متاح")
                    repository.setPhoto(itemId, input)
                }
            } catch (cancel: kotlinx.coroutines.CancellationException) {
                throw cancel
            } catch (_: Exception) {
                emit(R.string.media_import_failed)
            }
        }
    }

    /** Copies a picked audio file into filesDir/custom_audio/ and stores its absolute path. */
    override fun onAudioPicked(itemId: Long, uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val mime = resolver.getType(uri).orEmpty()
                    val extension = when {
                        "mpeg" in mime -> "mp3"
                        "mp4" in mime || "m4a" in mime || "aac" in mime -> "m4a"
                        "ogg" in mime -> "ogg"
                        "wav" in mime -> "wav"
                        else -> "audio"
                    }
                    val input = resolver.openInputStream(uri) ?: error("ملف الصوت غير متاح")
                    repository.importAudio(itemId, input, extension)
                }
            } catch (cancel: kotlinx.coroutines.CancellationException) {
                throw cancel
            } catch (_: Exception) {
                emit(R.string.media_import_failed)
            }
        }
    }

    override fun removePhoto(itemId: Long) {
        viewModelScope.launch { repository.clearPhoto(itemId) }
    }

    /** The UI must have RECORD_AUDIO granted before calling this. */
    override fun startRecording(itemId: Long) {
        voice.stop()
        if (recorder.start("word_$itemId")) _uiState.update { it.copy(recordingItemId = itemId) }
    }

    override fun stopRecording(itemId: Long) {
        val file = recorder.stop()
        _uiState.update { it.copy(recordingItemId = null) }
        if (file != null) viewModelScope.launch { repository.setAudio(itemId, file) }
    }

    override fun previewRecording(itemId: Long) {
        find(itemId)?.audioPath?.let { voice.playFile(it) }
    }

    override fun deleteRecording(itemId: Long) {
        voice.stop()
        viewModelScope.launch { repository.clearAudio(itemId) }
    }

    private fun find(id: Long): WordItem? =
        _uiState.value.categories.firstNotNullOfOrNull { c -> c.items.firstOrNull { it.id == id } }

    override fun onCleared() {
        recorder.cancel()
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val c = (app as SawtiApp).container
            return LibraryViewModel(
                c.wordRepository, c.speech, c.audioDispatcher, c.recorder, app.contentResolver, c.caregiver,
            ) as T
        }
    }

    companion object {
        const val MAX_FAVORITES = 8
        private const val DEBOUNCE_MS = 500L
        private const val CARD_LATCH_MS = 1200L
        private const val HIGHLIGHT_MS = 1200L
    }
}
