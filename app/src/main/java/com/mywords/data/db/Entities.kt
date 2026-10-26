package com.mywords.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mywords.domain.model.WordItem

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val palette: Int,
    val previewOnTap: Boolean,
    val sortOrder: Int,
)

@Entity(
    tableName = "word_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("categoryId")],
)
data class WordItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val subgroup: String = "",
    val displayText: String,
    val ttsPrompt: String,
    val phonemicCue: String,
    val sentencePrompt: String? = null,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val audioPath: String? = null,
    val photoPath: String? = null,
    val phone: String = "",
    val emoji: String = "",
    val sortOrder: Int = 0,
)

fun WordItemEntity.toDomain() = WordItem(
    id = id, categoryId = categoryId, subgroup = subgroup, displayText = displayText,
    ttsPrompt = ttsPrompt, phonemicCue = phonemicCue, sentencePrompt = sentencePrompt,
    isFavorite = isFavorite, audioPath = audioPath,
    photoPath = photoPath, phone = phone, emoji = emoji,
)

/** One practice record (added in DB v3). Stored as plain strings so the history survives enum changes. */
@Entity(tableName = "progress_records")
data class ProgressRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val timestamp: Long,
    val mode: String,
    val outcome: String,
)
