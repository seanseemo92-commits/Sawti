package com.mywords.presentation.practice

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mywords.MyWordsApp
import com.mywords.audio.SoftChime
import com.mywords.audio.AudioDispatcher
import com.mywords.domain.model.Category
import com.mywords.domain.model.CategoryIds
import com.mywords.domain.model.PracticeMode
import com.mywords.domain.model.PracticeOutcome
import com.mywords.domain.model.QuestionPhrases
import com.mywords.domain.model.WordItem
import com.mywords.domain.repository.ProgressRepository
import com.mywords.domain.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Exercise { RECALL, CHOICE }

data class PracticeCard(val item: WordItem, val palette: Int)

data class PracticeUiState(
    val exercise: Exercise = Exercise.RECALL,
    val ready: Boolean = false,
    val card: PracticeCard? = null,   // the target word of the current question
    val index: Int = 0,
    val total: Int = 0,
    val score: Int = 0,
    val finished: Boolean = false,
    // naming game
    val revealed: Boolean = false,
    val hintUsed: Boolean = false,
    // "أين…؟" game
    val choices: List<PracticeCard> = emptyList(),
    val wrongIds: Set<Long> = emptySet(),
    val solved: Boolean = false,
)

interface PracticeActions {
    fun setExercise(exercise: Exercise)
    fun hint()
    fun sentenceHint()
    fun reveal()
    fun known()
    fun skip()
    fun choose(wordId: Long)
    fun replayQuestion()
    fun restart()
}

/**
 * Two exercises:
 *  RECALL – see the picture, try to say it, use 💡/💬 if needed, reveal, self-rate.
 *  CHOICE – hear "أين …؟" and tap one of two pictures (gentle chime when right, soft buzz when not).
 * Every finished question is logged for the caregiver's progress tab.
 */
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

    init {
        viewModelScope.launch {
            repository.observeCategories().collect { categories ->
                recallPool = buildPool(categories, excludeActions = false)
                choicePool = buildPool(categories, excludeActions = true)
                if (deck.isEmpty()) startRound()
            }
        }
    }

    /**
     * Words with a picture. Family counts once a real photo is attached; daily phrases are skipped
     * (and actions in the "أين…؟" game, which asks about things). Two words sharing the same
     * built-in emoji are skipped until a photo is attached, so a picture is never ambiguous.
     */
    private fun buildPool(categories: List<Category>, excludeActions: Boolean): List<PracticeCard> {
        val cards = categories
            .filter { it.id != CategoryIds.DAILY && !(excludeActions && it.id == "actions") }
            .flatMap { c ->
                c.items
                    .filter { if (c.id == CategoryIds.FAMILY) it.photoPath != null else it.emoji.isNotBlank() || it.photoPath != null }
                    .map { PracticeCard(it, c.palette) }
            }
        val emojiUse = cards.filter { it.item.photoPath == null }.groupingBy { it.item.emoji }.eachCount()
        return cards.filter { it.item.photoPath != null || emojiUse[it.item.emoji] == 1 }
    }

    private fun startRound(exercise: Exercise = _uiState.value.exercise) {
        advanceJob?.cancel()
        voice.stop()
        val pool = if (exercise == Exercise.RECALL) recallPool else choicePool
        deck = if (exercise == Exercise.CHOICE && pool.size < 2) emptyList() else pool.shuffled().take(ROUND_SIZE)
        _uiState.value = PracticeUiState(exercise = exercise, ready = true, total = deck.size)
        if (deck.isNotEmpty()) showQuestion(0)
    }

    private fun showQuestion(i: Int) {
        val card = deck[i]
        if (_uiState.value.exercise == Exercise.CHOICE) {
            _uiState.update {
                it.copy(
                    card = card, index = i, finished = false,
                    choices = listOf(card, pickDistractor(card)).shuffled(),
                    wrongIds = emptySet(), solved = false,
                )
            }
            replayQuestion()
        } else {
            _uiState.update { it.copy(card = card, index = i, finished = false, revealed = false, hintUsed = false) }
        }
    }

    /** A different picture, preferably from the same category (e.g. another fruit). */
    private fun pickDistractor(target: PracticeCard): PracticeCard {
        val others = choicePool.filter {
            it.item.id != target.item.id &&
                (it.item.photoPath != null || target.item.photoPath != null || it.item.emoji != target.item.emoji)
        }
        return others.filter { it.item.categoryId == target.item.categoryId }.ifEmpty { others }.random()
    }

    private fun advance() {
        val next = _uiState.value.index + 1
        if (next >= deck.size) {
            voice.stop()
            _uiState.update { it.copy(finished = true, solved = false, revealed = false) }
        } else {
            showQuestion(next)
        }
    }

    private fun log(wordId: Long, mode: PracticeMode, outcome: PracticeOutcome) {
        viewModelScope.launch { progress.log(wordId, mode, outcome) }
    }

    // ---- shared -----------------------------------------------------------

    override fun setExercise(exercise: Exercise) {
        if (exercise != _uiState.value.exercise) startRound(exercise)
    }

    override fun restart() = startRound()

    // ---- naming game ------------------------------------------------------

    override fun hint() {
        val card = _uiState.value.card ?: return
        voice.hint(card.item)
        _uiState.update { it.copy(hintUsed = true) }
    }

    override fun sentenceHint() {
        val card = _uiState.value.card ?: return
        voice.sentenceHint(card.item)
        _uiState.update { it.copy(hintUsed = true) }
    }

    override fun reveal() {
        val card = _uiState.value.card ?: return
        _uiState.update { it.copy(revealed = true) }
        voice.play(card.item)
    }

    /** "I said it": unaided = SELF, after a 💡/💬 cue = CUED. */
    override fun known() {
        val s = _uiState.value
        val card = s.card ?: return
        log(card.item.id, PracticeMode.RECALL, if (s.hintUsed) PracticeOutcome.CUED else PracticeOutcome.SELF)
        _uiState.update { it.copy(score = it.score + 1) }
        voice.stop()
        advance()
    }

    /** Moved on after seeing the answer: she needed the answer. */
    override fun skip() {
        val card = _uiState.value.card ?: return
        log(card.item.id, PracticeMode.RECALL, PracticeOutcome.SHOWN)
        voice.stop()
        advance()
    }

    // ---- "أين…؟" game -----------------------------------------------------

    override fun replayQuestion() {
        val card = _uiState.value.card ?: return
        viewModelScope.launch {
            delay(350)
            voice.say(QuestionPhrases.where(card.item))
        }
    }

    override fun choose(wordId: Long) {
        val s = _uiState.value
        val card = s.card ?: return
        if (s.exercise != Exercise.CHOICE || s.solved || wordId in s.wrongIds) return

        if (wordId == card.item.id) {
            val firstTry = s.wrongIds.isEmpty()
            if (firstTry) log(card.item.id, PracticeMode.CHOICE, PracticeOutcome.CORRECT)
            chime.play()
            _uiState.update { it.copy(solved = true, score = if (firstTry) it.score + 1 else it.score) }
            advanceJob?.cancel()
            advanceJob = viewModelScope.launch {
                delay(1800)
                advance()
            }
        } else {
            if (s.wrongIds.isEmpty()) log(card.item.id, PracticeMode.CHOICE, PracticeOutcome.WRONG)
            _uiState.update { it.copy(wrongIds = it.wrongIds + wordId) }
        }
    }

    override fun onCleared() {
        voice.stop()
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val c = (app as MyWordsApp).container
            return PracticeViewModel(c.wordRepository, c.progressRepository, c.audioDispatcher, c.chime) as T
        }
    }

    private companion object {
        const val ROUND_SIZE = 10
    }
}
