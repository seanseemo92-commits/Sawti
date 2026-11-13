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
import com.sawti.audio.PlaybackVisual
import com.sawti.data.SettingsRepository
import com.sawti.domain.model.SentencePiece
import com.sawti.domain.model.SentenceComposer
import com.sawti.domain.model.Category
import com.sawti.domain.model.FamilyRelationships
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.WordItem
import com.sawti.domain.model.CardDetails
import com.sawti.domain.model.forEditing
import com.sawti.domain.repository.WordRepository
import com.sawti.speech.SpeechIssue
import com.sawti.speech.SpeechManager
import com.sawti.state.CaregiverMode
import kotlinx.coroutines.CancellationException
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

data class LibraryUiState(
    val categories: List<Category> = emptyList(),
    val caregiverMode: Boolean = false,
    val activeItemId: Long? = null,
    val playbackVisual: PlaybackVisual = PlaybackVisual(),
    val previewItemId: Long? = null,
    val focusItemId: Long? = null,
    val editingItemId: Long? = null,
    val addingFamilyGroup: String? = null,
    val recordingItemId: Long? = null,
    val speechIssue: SpeechIssue? = null,
    val sentenceStripEnabled: Boolean = false,
    val sentencePieces: List<SentencePiece> = emptyList(),
    val errorMessage: String? = null,
    val isLoadingCategories: Boolean = true,
    val categoryLoadError: String? = null,
)

interface LibraryActions {
    fun onItemTapped(item: WordItem, flashcard: Boolean)
    fun onItemLongPressed(item: WordItem)
    fun onHint(item: WordItem)
    fun addCustomCard(categoryId: String, title: String, spoken: String)
    fun moveCardToCustomCategory(id: Long, destinationId: String)
    fun deleteCustomCard(id: Long)
    fun onSpeakAgain(item: WordItem)
    fun closePreview()
    fun closeFocus()
    fun closeEditor()
    fun openAddFamilyMember(group: String)
    fun closeAddFamilyMember()
    fun addFamilyMember(name: String, group: String, phone: String)
    fun deleteFamilyMember(id: Long)
    fun saveDetails(id: Long, displayText: String, ttsPrompt: String, cue: String, sentence: String, phone: String, group: String, baseline: CardDetails)
    fun onPhotoPicked(itemId: Long, uri: Uri)
    fun onAudioPicked(itemId: Long, uri: Uri)
    fun removePhoto(itemId: Long)
    fun startRecording(itemId: Long)
    fun stopRecording(itemId: Long)
    fun cancelRecording(itemId: Long)
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
    private val settingsRepository: SettingsRepository,
) : ViewModel(), LibraryActions {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()
    private var highlightJob: Job? = null
    private var lastTap: Pair<String, Long>? = null
    private var cardLatchUntil = 0L
    private val categoryLoader = CategoryLoadController(
        scope = viewModelScope,
        seed = { withContext(Dispatchers.IO) { repository.seedOrUpdateDefaults() } },
        observe = repository::observeCategories,
    )
    init {
        viewModelScope.launch {
            voice.playback.collect { playback -> _uiState.update { it.copy(playbackVisual = playback) } }
        }
        viewModelScope.launch {
            settingsRepository.state.collect { settings ->
                _uiState.update { it.copy(sentenceStripEnabled = settings.sentenceStripEnabled) }
            }
        }
        viewModelScope.launch {
            categoryLoader.state.collect { load ->
                _uiState.update { it.copy(categories = load.categories, isLoadingCategories = load.loading, categoryLoadError = load.error) }
            }
        }
        categoryLoader.start()
        viewModelScope.launch {
            caregiver.enabled.collect { on ->
                if (!on) recorder.cancel()
                _uiState.update {
                    it.copy(caregiverMode = on,
                        editingItemId = if (on) it.editingItemId else null,
                        addingFamilyGroup = if (on) it.addingFamilyGroup else null,
                        previewItemId = if (on) null else it.previewItemId,
                        recordingItemId = if (on) it.recordingItemId else null)
                }
            }
        }
        viewModelScope.launch {
            speech.issue.collect { issue -> _uiState.update { it.copy(speechIssue = issue) } }
        }
    }
    fun toggleCaregiver() = caregiver.toggle()
    private fun canEdit(): Boolean = caregiver.enabled.value
    fun onAppBackgrounded() {
        recorder.cancel()
        voice.stop()
        _uiState.update { it.copy(recordingItemId = null) }
    }
    override fun onItemTapped(item: WordItem, flashcard: Boolean) {
        val state = _uiState.value
        if ((state.caregiverMode || !state.sentenceStripEnabled) && cardLatched()) return
        if (state.caregiverMode) {
            _uiState.update { it.copy(editingItemId = item.id) }
            return
        }
        if (state.sentenceStripEnabled) {
            _uiState.update { it.copy(sentencePieces = SentenceComposer.add(it.sentencePieces, item)) }
            highlight(item.id)
            return
        }
        voice.play(item)
        highlight(item.id)
        val previewOnTap = state.categories.firstOrNull { it.id == item.categoryId }?.previewOnTap == true
        if (previewOnTap && !flashcard && item.subgroup != FamilyRelationships.RELATIVES) {
            _uiState.update { it.copy(previewItemId = item.id) }
        }
    }
    fun playSentenceStrip() {
        val state = _uiState.value
        if (!state.sentenceStripEnabled) return
        voice.speakSentence(state.sentencePieces)
    }
    fun deleteLastSentencePiece() {
        voice.stop()
        _uiState.update { it.copy(sentencePieces = it.sentencePieces.dropLast(1)) }
    }
    fun clearSentenceStrip() {
        voice.stop()
        _uiState.update { it.copy(sentencePieces = emptyList()) }
    }
    fun setSentenceStripEnabled(enabled: Boolean) = settingsRepository.setSentenceStripEnabled(enabled)
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    private fun report(error: Throwable) {
        _uiState.update { it.copy(errorMessage = error.message ?: "تعذر حفظ التغيير") }
    }
    fun retryLoad() { categoryLoader.retry() }
    private fun launchSafe(action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action() }
            catch (cancel: CancellationException) { throw cancel }
            catch (error: Exception) { report(error) }
        }
    }
    fun createCategory(title: String, palette: Int, sortOrder: Int) {
        if (!canEdit()) return
        launchSafe { if (!canEdit()) return@launchSafe; repository.addCustomCategory(title, palette, sortOrder) }
    }
    fun editCategory(id: String, title: String, palette: Int, sortOrder: Int) {
        if (!canEdit()) return
        launchSafe { if (!canEdit()) return@launchSafe; repository.editCustomCategory(id, title, palette, sortOrder) }
    }
    fun deleteCategory(id: String) {
        if (!canEdit()) return
        launchSafe { if (!canEdit()) return@launchSafe; repository.deleteEmptyCustomCategory(id) }
    }
    override fun addCustomCard(categoryId: String, title: String, spoken: String) {
        if (!canEdit()) return
        launchSafe {
            if (!canEdit()) return@launchSafe
            val id = repository.addCustomCard(categoryId, title, spoken)
            if (canEdit()) _uiState.update { it.copy(editingItemId = id) }
        }
    }
    override fun deleteCustomCard(id: Long) {
        if (!canEdit()) return
        launchSafe {
            if (!canEdit()) return@launchSafe
            repository.deleteCustomCard(id)
            _uiState.update { it.copy(editingItemId = null) }
        }
    }
    override fun moveCardToCustomCategory(id: Long, destinationId: String) {
        if (!canEdit()) return
        launchSafe {
            if (!canEdit()) return@launchSafe
            repository.moveCardToCustomCategory(id, destinationId)
            _uiState.update { it.copy(editingItemId = null) }
        }
    }
    override fun onItemLongPressed(item: WordItem) {
        if (_uiState.value.caregiverMode) _uiState.update { it.copy(editingItemId = item.id) }
        else if (_uiState.value.sentenceStripEnabled) { voice.play(item); highlight(item.id) }
        else _uiState.update { it.copy(focusItemId = item.id) }
    }
    override fun onHint(item: WordItem) {
        if (isRepeat("h${item.id}")) return
        voice.hint(item)
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
    private fun cardLatched(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now < cardLatchUntil) return true
        cardLatchUntil = now + CARD_LATCH_MS
        return false
    }
    private fun isRepeat(key: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        lastTap?.let { (k, t) -> if (k == key && now - t < DEBOUNCE_MS) return true }
        lastTap = key to now
        return false
    }
    private fun highlight(id: Long) {
        highlightJob?.cancel()
        _uiState.update { it.copy(activeItemId = id) }
        highlightJob = viewModelScope.launch { delay(HIGHLIGHT_MS); _uiState.update { it.copy(activeItemId = null) } }
    }
    private fun emit(@StringRes message: Int) { _messages.tryEmit(message) }
    override fun openAddFamilyMember(group: String) {
        if (canEdit() && FamilyRelationships.validPeopleGroup(group)) _uiState.update { it.copy(addingFamilyGroup = group) }
    }
    override fun closeAddFamilyMember() { _uiState.update { it.copy(addingFamilyGroup = null) } }
    override fun addFamilyMember(name: String, group: String, phone: String) {
        if (!canEdit() || name.isBlank() || _uiState.value.addingFamilyGroup != group || !FamilyRelationships.validPeopleGroup(group)) return
        launchSafe {
            if (!canEdit()) return@launchSafe
            val id = repository.addFamilyMember(name, group, phone)
            _uiState.update { it.copy(addingFamilyGroup = null, editingItemId = id) }
        }
    }
    override fun deleteFamilyMember(id: Long) {
        if (!canEdit()) return
        val current = find(id) ?: return
        if (current.categoryId != CategoryIds.FAMILY || current.subgroup == FamilyRelationships.RELATIVES) return
        recorder.cancel()
        voice.stop()
        _uiState.update { it.copy(editingItemId = null, previewItemId = null, focusItemId = null, recordingItemId = null) }
        launchSafe { if (canEdit()) repository.deleteFamilyMember(id) }
    }
    override fun closeEditor() {
        recorder.cancel()
        voice.stop()
        _uiState.update { it.copy(editingItemId = null, recordingItemId = null) }
    }
    override fun saveDetails(id: Long, displayText: String, ttsPrompt: String, cue: String, sentence: String, phone: String, group: String, baseline: CardDetails) {
        if (!canEdit()) return
        val current = find(id)?.forEditing() ?: return
        val finalDisplay = displayText.ifBlank { baseline.display }
        val enteredTts = if (baseline.speech == baseline.display && ttsPrompt == baseline.speech &&
            finalDisplay != baseline.display) finalDisplay else ttsPrompt.ifBlank { baseline.speech }
        val finalTts = enteredTts
        val finalCue = if (cue == baseline.cue) baseline.cue else cue.ifBlank { PhonemicCue.derive(finalTts) }
        val finalSentence = if (sentence == baseline.sentence.orEmpty()) baseline.sentence else sentence.ifBlank { null }
        // Finish the active recording before the Save action closes its editor.
        // Store the audio first so a failure to save the text cannot silently discard it.
        val pendingRecording = if (_uiState.value.recordingItemId == id) {
            recorder.stop().also { _uiState.update { state -> state.copy(recordingItemId = null) } }
        } else null
        launchSafe {
            if (!canEdit()) return@launchSafe
            if (pendingRecording != null) repository.setAudio(id, pendingRecording)
            repository.updateDetails(id, finalDisplay, finalTts, finalCue, finalSentence, phone,
                baseline = baseline, group = if (current.categoryId == CategoryIds.FAMILY) group else baseline.group)
            closeEditor()
        }
    }
    override fun onPhotoPicked(itemId: Long, uri: Uri) {
        if (!canEdit()) return
        viewModelScope.launch {
            if (!canEdit()) return@launch
            try {
                withContext(Dispatchers.IO) {
                    val input = resolver.openInputStream(uri) ?: error("ملف الصورة غير متاح")
                    repository.setPhoto(itemId, input)
                }
            } catch (cancel: CancellationException) { throw cancel }
              catch (_: Exception) { emit(R.string.media_import_failed) }
        }
    }
    override fun onAudioPicked(itemId: Long, uri: Uri) {
        if (!canEdit()) return
        viewModelScope.launch {
            if (!canEdit()) return@launch
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
            } catch (cancel: CancellationException) { throw cancel }
              catch (_: Exception) { emit(R.string.media_import_failed) }
        }
    }
    override fun removePhoto(itemId: Long) {
        if (!canEdit()) return
        launchSafe { if (canEdit()) repository.clearPhoto(itemId) }
    }
    override fun startRecording(itemId: Long) {
        if (!canEdit()) return
        voice.stop()
        if (recorder.start(
                "word_$itemId",
                onLimitReached = {
                    if (_uiState.value.recordingItemId == itemId) stopRecording(itemId)
                },
                onInterrupted = { cancelRecording(itemId) },
            )) _uiState.update { it.copy(recordingItemId = itemId) }
        else report(IllegalStateException("تعذّر بدء تسجيل الصوت"))
    }
    override fun stopRecording(itemId: Long) {
        if (!canEdit()) {
            recorder.cancel()
            _uiState.update { it.copy(recordingItemId = null) }
            return
        }
        if (_uiState.value.recordingItemId != itemId) return
        val file = recorder.stop()
        _uiState.update { it.copy(recordingItemId = null) }
        if (file != null) launchSafe { if (canEdit()) repository.setAudio(itemId, file) else file.delete() }
    }
    override fun cancelRecording(itemId: Long) {
        if (_uiState.value.recordingItemId != itemId) return
        recorder.cancel()
        _uiState.update { it.copy(recordingItemId = null) }
    }
    override fun previewRecording(itemId: Long) { find(itemId)?.audioPath?.let { voice.playFile(it) } }
    override fun deleteRecording(itemId: Long) {
        if (!canEdit()) return
        voice.stop()
        launchSafe { if (canEdit()) repository.clearAudio(itemId) }
    }
    private fun find(id: Long): WordItem? = _uiState.value.categories.firstNotNullOfOrNull { c -> c.items.firstOrNull { it.id == id } }
    override fun onCleared() { recorder.cancel() }
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val c = (app as SawtiApp).container
            return LibraryViewModel(c.wordRepository, c.speech, c.audioDispatcher, c.recorder,
                app.contentResolver, c.caregiver, c.settingsRepository) as T
        }
    }
    companion object {
        private const val DEBOUNCE_MS = 500L
        private const val CARD_LATCH_MS = 1200L
        private const val HIGHLIGHT_MS = 1200L
    }
}
