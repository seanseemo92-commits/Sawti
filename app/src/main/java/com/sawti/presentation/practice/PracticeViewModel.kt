package com.sawti.presentation.practice

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sawti.SawtiApp
import com.sawti.audio.SoftChime
import com.sawti.audio.AudioDispatcher
import com.sawti.domain.model.Category
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.GreetingsCatalog
import com.sawti.domain.model.PracticeMode
import com.sawti.domain.model.PracticeOutcome
import com.sawti.domain.model.QuestionPhrases
import com.sawti.domain.model.WordImageCatalog
import com.sawti.domain.model.WordItem
import com.sawti.domain.repository.ProgressRepository
import com.sawti.domain.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Exercise { RECALL, CHOICE }

/**
 * The \"Where is...? / أين؟\" exercise needs objects/places that can be
 * located in a photo. Social greetings are speech acts, not things one can
 * locate, so they remain available for picture naming (RECALL) but must never
 * become choice questions or distractor answers.
 */
internal fun practiceCategoryEligible(categoryId: String, exercise: Exercise): Boolean =
    categoryId != CategoryIds.DAILY &&
        (exercise != Exercise.CHOICE ||
            (categoryId != "actions" && categoryId != GreetingsCatalog.CATEGORY_ID))

data class PracticeCard(val item: WordItem, val palette: Int)
data class PracticeUiState(
    val exercise: Exercise = Exercise.RECALL,
    val ready: Boolean = false,
    val loadError: Boolean = false,
    val card: PracticeCard? = null,
    val index: Int = 0,
    val total: Int = 0,
    val score: Int = 0,
    val finished: Boolean = false,
    val revealed: Boolean = false,
    val hintUsed: Boolean = false,
    val choices: List<PracticeCard> = emptyList(),
    val wrongIds: Set<Long> = emptySet(),
    val solved: Boolean = false,
)

interface PracticeActions {
    fun setExercise(exercise: Exercise)
    fun hint()
    fun reveal()
    fun known()
    fun skip()
    fun choose(wordId: Long)
    fun replayQuestion()
    fun restart()
}

/** Picture recall uses the phonemic lightbulb; choice practice asks where an item is. */
class PracticeViewModel(
    private val repository: WordRepository,
    private val progress: ProgressRepository,
    private val voice: AudioDispatcher,
    private val chime: SoftChime,
) : ViewModel(), PracticeActions {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()
    private var recallPool: List<PracticeCard> = emptyList()
    private var choicePool: List<PracticeCard> = emptyList()
    private var deck: List<PracticeCard> = emptyList()
    private var advanceJob: Job? = null
    private var questionJob: Job? = null
    private var practiceActive = false
    private var observationJob: Job? = null

    fun onEnterPractice() {
        if (practiceActive) return
        practiceActive = true
        val state = _uiState.value
        if (state.exercise == Exercise.CHOICE && state.card != null && !state.finished) {
            if (state.solved) scheduleAdvance() else replayQuestion()
        }
    }

    fun onLeavePractice() {
        practiceActive = false
        advanceJob?.cancel()
        questionJob?.cancel()
        voice.stop()
    }

    private fun scheduleAdvance() {
        advanceJob?.cancel()
        if (practiceActive) advanceJob = viewModelScope.launch {
            delay(1800)
            if (practiceActive) advance()
        }
    }
    init { observeCards() }

    private fun observeCards() {
        observationJob?.cancel()
        _uiState.update { it.copy(loadError = false) }
        observationJob = viewModelScope.launch {
            repository.observeCategories().catch {
                Log.e("SawtiPractice", "Category observation failed", it)
                _uiState.update { state -> state.copy(loadError = true) }
            }.collect { categories ->
                recallPool = buildPool(categories, exercise = Exercise.RECALL)
                choicePool = buildPool(categories, exercise = Exercise.CHOICE)
                if (deck.isEmpty()) startRound()
            }
        }
    }
    private fun buildPool(categories: List<Category>, exercise: Exercise): List<PracticeCard> {
        val cards = categories.filter { practiceCategoryEligible(it.id, exercise) }.flatMap { c ->
            c.items.filter { if (c.id == CategoryIds.FAMILY) it.photoPath != null else it.emoji.isNotBlank() || it.photoPath != null }
                .map { PracticeCard(it, c.palette) }
        }
        return cards.distinctBy { visualKey(it.item) }
    }
    private fun visualKey(item: WordItem): String = item.photoPath ?: WordImageCatalog.imageFor(item) ?: item.displayText.take(1)

    private fun startRound(exercise: Exercise = _uiState.value.exercise) {
        advanceJob?.cancel()
        questionJob?.cancel()
        voice.stop()
        val pool = if (exercise == Exercise.RECALL) recallPool else choicePool
        deck = if (exercise == Exercise.CHOICE && pool.size < 2) emptyList() else pool.shuffled().take(ROUND_SIZE)
        _uiState.value = PracticeUiState(exercise = exercise, ready = true, total = deck.size)
        if (deck.isNotEmpty()) showQuestion(0)
    }
    private fun showQuestion(i: Int) {
        val card = deck[i]
        if (_uiState.value.exercise == Exercise.CHOICE) {
            val distractor = pickDistractor(card)
            if (distractor == null) {
                voice.stop()
                _uiState.update { it.copy(card = null, finished = true, choices = emptyList()) }
                return
            }
            _uiState.update {
                it.copy(card = card, index = i, finished = false,
                    choices = listOf(card, distractor).shuffled(), wrongIds = emptySet(), solved = false)
            }
            replayQuestion()
        } else _uiState.update { it.copy(card = card, index = i, finished = false, revealed = false, hintUsed = false) }
    }
    private fun pickDistractor(target: PracticeCard): PracticeCard? {
        val others = (choicePool + deck).distinctBy { it.item.id }.filter {
            it.item.id != target.item.id && visualKey(it.item) != visualKey(target.item)
        }
        return others.filter { it.item.categoryId == target.item.categoryId }.ifEmpty { others }.randomOrNull()
    }
    private fun advance() {
        questionJob?.cancel()
        val next = _uiState.value.index + 1
        if (next >= deck.size) {
            voice.stop()
            _uiState.update { it.copy(finished = true, solved = false, revealed = false) }
        } else showQuestion(next)
    }
    private fun log(wordId: Long, mode: PracticeMode, outcome: PracticeOutcome) {
        viewModelScope.launch { progress.log(wordId, mode, outcome) }
    }
    override fun setExercise(exercise: Exercise) {
        if (exercise != _uiState.value.exercise) startRound(exercise)
    }
    override fun restart() {
        if (_uiState.value.loadError) observeCards() else startRound()
    }
    override fun hint() {
        val card = _uiState.value.card ?: return
        questionJob?.cancel()
        voice.hint(card.item)
        _uiState.update { it.copy(hintUsed = true) }
    }
    override fun reveal() {
        val card = _uiState.value.card ?: return
        _uiState.update { it.copy(revealed = true) }
        questionJob?.cancel()
        voice.play(card.item)
    }
    /** Unaided recall = SELF; recall after the phonemic hint = CUED. */
    override fun known() {
        val s = _uiState.value
        val card = s.card ?: return
        log(card.item.id, PracticeMode.RECALL, if (s.hintUsed) PracticeOutcome.CUED else PracticeOutcome.SELF)
        _uiState.update { it.copy(score = it.score + 1) }
        voice.stop()
        advance()
    }
    override fun skip() {
        val card = _uiState.value.card ?: return
        log(card.item.id, PracticeMode.RECALL, PracticeOutcome.SHOWN)
        voice.stop()
        advance()
    }
    override fun replayQuestion() {
        questionJob?.cancel()
        val state = _uiState.value
        val card = state.card ?: return
        if (!practiceActive || state.finished || state.solved || state.exercise != Exercise.CHOICE) return
        questionJob = viewModelScope.launch {
            delay(350)
            if (practiceActive && _uiState.value.card?.item?.id == card.item.id && !_uiState.value.solved) {
                voice.say(QuestionPhrases.where(card.item))
            }
        }
    }
    override fun choose(wordId: Long) {
        val s = _uiState.value
        val card = s.card ?: return
        if (s.exercise != Exercise.CHOICE || s.solved || wordId in s.wrongIds) return
        questionJob?.cancel()
        voice.stop()
        if (wordId == card.item.id) {
            val firstTry = s.wrongIds.isEmpty()
            if (firstTry) log(card.item.id, PracticeMode.CHOICE, PracticeOutcome.CORRECT)
            chime.play()
            _uiState.update { it.copy(solved = true, score = if (firstTry) it.score + 1 else it.score) }
            scheduleAdvance()
        } else {
            if (s.wrongIds.isEmpty()) log(card.item.id, PracticeMode.CHOICE, PracticeOutcome.WRONG)
            _uiState.update { it.copy(wrongIds = it.wrongIds + wordId) }
        }
    }
    override fun onCleared() { onLeavePractice() }
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val c = (app as SawtiApp).container
            return PracticeViewModel(c.wordRepository, c.progressRepository, c.audioDispatcher, c.chime) as T
        }
    }
    private companion object { const val ROUND_SIZE = 10 }
}


