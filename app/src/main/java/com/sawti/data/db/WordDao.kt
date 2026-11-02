package com.sawti.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, id")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM word_items ORDER BY sortOrder, id")
    fun observeItems(): Flow<List<WordItemEntity>>

    @Insert
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    suspend fun insertItems(items: List<WordItemEntity>)

    @Insert
    suspend fun insertItem(item: WordItemEntity): Long

    // Prefix + row checks prevent
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


    @Query("SELECT photoPath FROM word_items WHERE id = :id")
    suspend fun photoPath(id: Long): String?

    @Query("UPDATE word_items SET photoPath = :path WHERE id = :id")
    suspend fun updatePhoto(id: Long, path: String?): Int

    @Query("SELECT audioPath FROM word_items WHERE id = :id")
    suspend fun audioPath(id: Long): String?

    @Query("UPDATE word_items SET audioPath = :path WHERE id = :id")
    suspend fun updateAudio(id: Long, path: String?): Int

    /** Content identity only: labels, order and media are user-editable. */
    @Query("SELECT semanticKey FROM word_items WHERE semanticKey IS NOT NULL")
    suspend fun semanticKeys(): List<String>

    @Query("SELECT id FROM categories")
    suspend fun categoryIds(): List<String>

    @Query("DELETE FROM word_items WHERE id = :id")
    suspend fun deleteItem(id: Long): Int
}
