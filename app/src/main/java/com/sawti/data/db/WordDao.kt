package com.sawti.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Lightweight row used when topping up the seed on an existing install. */
data class ItemKey(
    val id: Long,
    val categoryId: String,
    val subgroup: String,
    val displayText: String,
    val ttsPrompt: String,
    val photoPath: String?,
    val audioPath: String?,
)

@Dao
interface WordDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM word_items ORDER BY sortOrder, id")
    fun observeItems(): Flow<List<WordItemEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int

    @Insert
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    suspend fun insertItems(items: List<WordItemEntity>)

    @Insert
    suspend fun insertItem(item: WordItemEntity): Long

    /** Custom family members are never seeded. Editing/deleting is caregiver-only in the UI. */
    @Query("SELECT * FROM word_items WHERE id = :id AND categoryId = 'family'")
    suspend fun familyItemById(id: Long): WordItemEntity?

    @Query("UPDATE word_items SET subgroup = :group WHERE id = :id AND categoryId = 'family'")
    suspend fun updateFamilyGroup(id: Long, group: String)


    @Query(
        "UPDATE word_items SET displayText = :display, ttsPrompt = :tts, phonemicCue = :cue, " +
            "sentencePrompt = :sentence, phone = :phone WHERE id = :id"
    )
    suspend fun updateDetails(id: Long, display: String, tts: String, cue: String, sentence: String?, phone: String)

    @Query("UPDATE word_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** Only fills a missing prompt, so caregiver edits are never overwritten. */
    @Query("UPDATE word_items SET sentencePrompt = :prompt WHERE id = :id AND sentencePrompt IS NULL")
    suspend fun fillSentencePrompt(id: Long, prompt: String)

    @Query("SELECT photoPath FROM word_items WHERE id = :id")
    suspend fun photoPath(id: Long): String?

    @Query("UPDATE word_items SET photoPath = :path WHERE id = :id")
    suspend fun updatePhoto(id: Long, path: String?)

    @Query("SELECT audioPath FROM word_items WHERE id = :id")
    suspend fun audioPath(id: Long): String?

    @Query("UPDATE word_items SET audioPath = :path WHERE id = :id")
    suspend fun updateAudio(id: Long, path: String?)

    // ---- seed top-up (existing installs) ----
    @Query("SELECT id FROM categories")
    suspend fun categoryIds(): List<String>

    @Query("UPDATE categories SET title = :title, palette = :palette, previewOnTap = :preview, sortOrder = :sort WHERE id = :id")
    suspend fun updateCategory(id: String, title: String, palette: Int, preview: Boolean, sort: Int)

    @Query("SELECT id, categoryId, subgroup, displayText, ttsPrompt, photoPath, audioPath FROM word_items")
    suspend fun itemKeys(): List<ItemKey>

    @Query("UPDATE word_items SET sortOrder = :sort WHERE id = :id")
    suspend fun updateSort(id: Long, sort: Int)

    @Query("DELETE FROM word_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("UPDATE word_items SET ttsPrompt = :tts WHERE id = :id")
    suspend fun updateTts(id: Long, tts: String)

    /** Renames a default word in place; photo, audio, phone and favourite flag are NOT touched. */
    @Query(
        "UPDATE word_items SET displayText = :display, ttsPrompt = :tts, phonemicCue = :cue, " +
            "sentencePrompt = :sentence WHERE id = :id"
    )
    suspend fun renameItem(id: Long, display: String, tts: String, cue: String, sentence: String?)

    @Query("UPDATE word_items SET ttsPrompt = :tts, phonemicCue = :cue WHERE id = :id")
    suspend fun updateTtsAndCue(id: Long, tts: String, cue: String)
    /** Fix only the historical placeholder on the known built-in melon card. */
    @Query("UPDATE word_items SET emoji = '🍈' WHERE categoryId = 'fruits' AND displayText = 'قلعاوي' AND emoji = '??'")
    suspend fun correctMelonPlaceholder()

    /** Remove only obsolete built-in action hints, not other user-written phrases. */
    @Query("UPDATE word_items SET sentencePrompt = NULL WHERE categoryId = 'actions' AND sentencePrompt = 'أُرِيدُ الـ...'")
    suspend fun clearLegacyActionPlaceholder()

}
