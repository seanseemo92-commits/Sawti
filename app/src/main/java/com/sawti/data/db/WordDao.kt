package com.sawti.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    // Custom categories use the existing Room v4 schema. Prefix + row checks prevent
    // accidental editing/deletion of built-in categories, even during future seed updates.
    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun categoryById(id: String): CategoryEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM categories")
    suspend fun maxCategorySortOrder(): Int

    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Query("UPDATE categories SET title = :title, palette = :palette, sortOrder = :sortOrder WHERE id = :id AND id LIKE 'custom_%'")
    suspend fun editCustomCategory(id: String, title: String, palette: Int, sortOrder: Int): Int

    @Query("SELECT COUNT(*) FROM word_items WHERE categoryId = :categoryId")
    suspend fun countItemsInCategory(categoryId: String): Int

    /** Deleting populated categories is never automatic: moving their cards comes first. */
    @Query("DELETE FROM categories WHERE id = :id AND id LIKE 'custom_%' AND NOT EXISTS (SELECT 1 FROM word_items WHERE categoryId = :id)")
    suspend fun deleteEmptyCustomCategory(id: String): Int

    @Query("SELECT * FROM word_items WHERE id = :id LIMIT 1")
    suspend fun wordById(id: Long): WordItemEntity?

    @Query("UPDATE word_items SET categoryId = :categoryId, subgroup = '' WHERE id = :id")
    suspend fun moveWord(id: Long, categoryId: String)

    @Query("SELECT photoPath FROM word_items WHERE photoPath IS NOT NULL UNION SELECT audioPath FROM word_items WHERE audioPath IS NOT NULL")
    suspend fun mediaPaths(): List<String>

    /** Custom family members are never seeded. Editing/deleting is caregiver-only in the UI. */
    @Query("SELECT * FROM word_items WHERE id = :id AND categoryId = 'family'")
    suspend fun familyItemById(id: Long): WordItemEntity?

    @Query("UPDATE word_items SET subgroup = :group WHERE id = :id AND categoryId = 'family'")
    suspend fun updateFamilyGroup(id: Long, group: String)


    @Update
    suspend fun updateEditedItem(item: WordItemEntity): Int

    @Query("UPDATE word_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** Only fills a missing prompt, so caregiver edits are never overwritten. */
    @Query("UPDATE word_items SET sentencePrompt = :prompt WHERE id = :id AND sentencePrompt IS NULL AND (customTextFields & 8) = 0")
    suspend fun fillSentencePrompt(id: Long, prompt: String)

    @Query("SELECT photoPath FROM word_items WHERE id = :id")
    suspend fun photoPath(id: Long): String?

    @Query("UPDATE word_items SET photoPath = :path WHERE id = :id")
    suspend fun updatePhoto(id: Long, path: String?): Int

    @Query("SELECT audioPath FROM word_items WHERE id = :id")
    suspend fun audioPath(id: Long): String?

    @Query("UPDATE word_items SET audioPath = :path WHERE id = :id")
    suspend fun updateAudio(id: Long, path: String?): Int

    @Query("SELECT * FROM word_items WHERE semanticKey IS NULL")
    suspend fun itemsWithoutSemanticKeys(): List<WordItemEntity>

    @Query("UPDATE word_items SET semanticKey = :key WHERE id = :id AND semanticKey IS NULL")
    suspend fun setSemanticKeyIfMissing(id: Long, key: String)

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
    suspend fun deleteItem(id: Long): Int

    @Query("UPDATE word_items SET ttsPrompt = :tts WHERE id = :id AND (customTextFields & 2) = 0")
    suspend fun updateTts(id: Long, tts: String)

    /** Renames a default word in place; photo, audio, phone and favourite flag are NOT touched. */
    @Query(
        "UPDATE word_items SET displayText = :display, ttsPrompt = CASE WHEN (customTextFields & 2) = 0 THEN :tts ELSE ttsPrompt END, " +
            "phonemicCue = CASE WHEN (customTextFields & 4) = 0 THEN :cue ELSE phonemicCue END, " +
            "sentencePrompt = CASE WHEN (customTextFields & 8) = 0 THEN :sentence ELSE sentencePrompt END WHERE id = :id AND (customTextFields & 1) = 0"
    )
    suspend fun renameItem(id: Long, display: String, tts: String, cue: String, sentence: String?)

    @Query("UPDATE word_items SET ttsPrompt = :tts, phonemicCue = CASE WHEN (customTextFields & 4) = 0 THEN :cue ELSE phonemicCue END WHERE id = :id AND (customTextFields & 2) = 0")
    suspend fun updateTtsAndCue(id: Long, tts: String, cue: String)
    /** Fix only the historical placeholder on the known built-in melon card. */
    @Query("UPDATE word_items SET emoji = '🍈' WHERE categoryId = 'fruits' AND displayText = 'قلعاوي' AND emoji = '??'")
    suspend fun correctMelonPlaceholder()

    /** Remove only obsolete built-in action hints, not other user-written phrases. */
    @Query("UPDATE word_items SET sentencePrompt = NULL WHERE categoryId = 'actions' AND sentencePrompt = 'أُرِيدُ الـ...' AND (customTextFields & 8) = 0")
    suspend fun clearLegacyActionPlaceholder()

}

