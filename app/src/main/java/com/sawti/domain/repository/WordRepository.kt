package com.sawti.domain.repository

import com.sawti.domain.model.CardDetails
import com.sawti.domain.model.Category
import com.sawti.domain.model.PracticeMode
import com.sawti.domain.model.PracticeOutcome
import com.sawti.domain.model.ProgressSummary
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    /** Categories in display order, each with its items. */
    fun observeCategories(): Flow<List<Category>>

    suspend fun reorderCategories(orderedIds: List<String>)
    suspend fun reorderWords(orderedIds: List<Long>)

    /** Existing v4 tables also store custom categories; never drop their cards on delete. */
    suspend fun addCustomCategory(title: String, palette: Int, sortOrder: Int): String
    suspend fun editCustomCategory(id: String, title: String, palette: Int, sortOrder: Int)
    suspend fun deleteEmptyCustomCategory(id: String)
    suspend fun addCustomCard(categoryId: String, displayText: String, spokenText: String): Long
    suspend fun moveCardToCustomCategory(itemId: Long, destinationId: String)
    suspend fun deleteCustomCard(itemId: Long)

    /** Inserts missing built-in concepts by semantic identity without rewriting user-owned rows. */
    suspend fun seedOrUpdateDefaults()

    /** Creates a new, initially blank-photo family card; returns its database identity. */
    suspend fun addFamilyMember(name: String, group: String, phone: String): Long
    suspend fun updateFamilyGroup(id: Long, group: String)
    suspend fun deleteFamilyMember(id: Long)

    suspend fun updateDetails(
        id: Long,
        displayText: String,
        ttsPrompt: String,
        phonemicCue: String,
        sentencePrompt: String?,
        phone: String,
        baseline: CardDetails? = null,
        group: String? = null,
    )
    suspend fun setPhoto(id: Long, image: InputStream)
    suspend fun clearPhoto(id: Long)
    /** Transfers a finished private recording to the repository; callers must not delete it on failure. */
    suspend fun setAudio(id: Long, file: File)
    suspend fun importAudio(id: Long, input: InputStream, extension: String)
    suspend fun clearAudio(id: Long)
}

/** Practice history for the caregiver tracker. */
interface ProgressRepository {
    suspend fun log(wordId: Long, mode: PracticeMode, outcome: PracticeOutcome)
    fun observeSummary(): Flow<ProgressSummary>
    suspend fun clear()
}

