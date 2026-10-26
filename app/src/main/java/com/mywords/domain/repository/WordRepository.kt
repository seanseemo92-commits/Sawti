package com.mywords.domain.repository

import com.mywords.domain.model.Category
import com.mywords.domain.model.PracticeMode
import com.mywords.domain.model.PracticeOutcome
import com.mywords.domain.model.ProgressSummary
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    /** Categories in display order, each with its items. */
    fun observeCategories(): Flow<List<Category>>

    /** Inserts the built-in vocabulary on first run and tops it up when the vocabulary version changes. */
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
    )
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun setPhoto(id: Long, image: InputStream)
    suspend fun clearPhoto(id: Long)
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
