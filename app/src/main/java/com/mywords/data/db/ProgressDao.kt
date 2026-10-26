package com.mywords.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class OutcomeCount(val mode: String, val outcome: String, val n: Int)
data class CuedWordRow(val displayText: String, val n: Int)

@Dao
interface ProgressDao {
    @Insert
    suspend fun insertAttempt(attempt: ProgressRecordEntity)

    @Query("SELECT mode, outcome, COUNT(*) AS n FROM progress_records GROUP BY mode, outcome")
    fun observeOutcomeCounts(): Flow<List<OutcomeCount>>

    /** Words that needed a cue or the answer in the naming game, most frequent first. */
    @Query(
        "SELECT w.displayText AS displayText, COUNT(*) AS n FROM progress_records a " +
            "JOIN word_items w ON w.id = a.wordId " +
            "WHERE a.mode = 'RECALL' AND a.outcome IN ('CUED', 'SHOWN') " +
            "GROUP BY a.wordId ORDER BY n DESC LIMIT 8"
    )
    fun observeCuedWords(): Flow<List<CuedWordRow>>

    @Query("DELETE FROM progress_records")
    suspend fun clearAttempts()
}
