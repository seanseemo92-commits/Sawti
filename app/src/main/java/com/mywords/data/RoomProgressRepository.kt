package com.mywords.data

import com.mywords.data.db.ProgressRecordEntity
import com.mywords.data.db.ProgressDao
import com.mywords.domain.model.CuedWordStat
import com.mywords.domain.model.PracticeMode
import com.mywords.domain.model.PracticeOutcome
import com.mywords.domain.model.ProgressSummary
import com.mywords.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomProgressRepository(private val dao: ProgressDao) : ProgressRepository {

    override suspend fun log(wordId: Long, mode: PracticeMode, outcome: PracticeOutcome) {
        dao.insertAttempt(
            ProgressRecordEntity(
                wordId = wordId,
                timestamp = System.currentTimeMillis(),
                mode = mode.name,
                outcome = outcome.name,
            )
        )
    }

    override fun observeSummary(): Flow<ProgressSummary> =
        combine(dao.observeOutcomeCounts(), dao.observeCuedWords()) { counts, cuedWords ->
            fun n(mode: PracticeMode, outcome: PracticeOutcome) =
                counts.filter { it.mode == mode.name && it.outcome == outcome.name }.sumOf { it.n }
            fun total(mode: PracticeMode) = counts.filter { it.mode == mode.name }.sumOf { it.n }

            ProgressSummary(
                recallAttempts = total(PracticeMode.RECALL),
                selfRecalled = n(PracticeMode.RECALL, PracticeOutcome.SELF),
                cued = n(PracticeMode.RECALL, PracticeOutcome.CUED),
                shown = n(PracticeMode.RECALL, PracticeOutcome.SHOWN),
                choiceAttempts = total(PracticeMode.CHOICE),
                choiceCorrect = n(PracticeMode.CHOICE, PracticeOutcome.CORRECT),
                cuedWords = cuedWords.map { CuedWordStat(it.displayText, it.n) },
            )
        }

    override suspend fun clear() = dao.clearAttempts()
}
