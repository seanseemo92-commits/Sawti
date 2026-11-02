package com.sawti.data

import androidx.room.withTransaction
import com.sawti.data.db.ProgressRecordEntity
import com.sawti.data.db.AppDatabase
import com.sawti.domain.model.CuedWordStat
import com.sawti.domain.model.PracticeMode
import com.sawti.domain.model.PracticeOutcome
import com.sawti.domain.model.ProgressSummary
import com.sawti.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomProgressRepository(private val db: AppDatabase) : ProgressRepository {
    private val dao = db.progressDao()

    override suspend fun log(wordId: Long, mode: PracticeMode, outcome: PracticeOutcome) = db.storage.cleanupIfActive {
        db.withTransaction {
            // A queued practice result may arrive after its custom card was deleted.
            val reference = db.wordDao().wordById(wordId)?.id
            dao.insertAttempt(ProgressRecordEntity(
                wordId = reference, timestamp = System.currentTimeMillis(),
                mode = mode.name, outcome = outcome.name,
            ))
        }
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

    override suspend fun clear() = db.storage.cleanupIfActive { dao.clearAttempts() }
}

