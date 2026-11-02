package com.sawti.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sawti.domain.model.WordItem

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
    indices = [Index("categoryId"), Index(value = ["semanticKey"], unique = true)],
)
data class WordItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val subgroup: String = "",
    val displayText: String,
    val ttsPrompt: String,
    val phonemicCue: String,
    val sentencePrompt: String? = null,
    val audioPath: String? = null,
    val photoPath: String? = null,
    val phone: String = "",
    val emoji: String = "",
    val sortOrder: Int = 0,
    val semanticKey: String? = null,
    @ColumnInfo(defaultValue = "0") val customTextFields: Int = 0,
)

fun WordItemEntity.toDomain() = WordItem(
    id = id, categoryId = categoryId, subgroup = subgroup, displayText = displayText,
    ttsPrompt = ttsPrompt, phonemicCue = phonemicCue, sentencePrompt = sentencePrompt,
    audioPath = audioPath,
    photoPath = photoPath, phone = phone, emoji = emoji, semanticKey = semanticKey, customTextFields = customTextFields,
)

/** Completed practice history survives card deletion; a deleted card leaves a null reference. */
@Entity(
    tableName = "progress_records",
    foreignKeys = [ForeignKey(
        entity = WordItemEntity::class, parentColumns = ["id"], childColumns = ["wordId"],
        onDelete = ForeignKey.SET_NULL,
    )],
    indices = [Index("wordId"), Index(value = ["mode", "outcome"])],
)
data class ProgressRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long?,
    val timestamp: Long,
    val mode: String,
    val outcome: String,
)

