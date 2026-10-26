package com.sawti.domain.model

/**
 * One tile / flashcard.
 *  displayText – friendly label shown on screen ("نبي مية", "مثال")
 *  ttsPrompt   – fully vocalised (tashkeel) text sent to Android TTS ("أُرِيدُ مَاءً")
 *  phonemicCue – first syllable that prompts word retrieval ("إِسْـ...")
 *  sentencePrompt – cloze carrier phrase ("نَأْكُلُ الـ...") for the 💬 cue; null = none
 *  isFavorite  – pinned to the quick-access bar on the home screen
 *  audioPath   – caregiver recording; when present it replaces TTS
 *  photoPath   – photo attached from the gallery
 *  emoji       – built-in picture used until a photo is attached
 */
data class WordItem(
    val id: Long,
    val categoryId: String,
    val subgroup: String,
    val displayText: String,
    val ttsPrompt: String,
    val phonemicCue: String,
    val sentencePrompt: String?,
    val isFavorite: Boolean,
    val audioPath: String?,
    val photoPath: String?,
    val phone: String,
    val emoji: String,
)

data class Category(
    val id: String,
    val title: String,
    val palette: Int,
    /** True for the family category: tapping opens a large photo instead of only speaking. */
    val previewOnTap: Boolean,
    val items: List<WordItem>,
) {
    /** Sub-tab labels in first-appearance order (empty when the category has no sub-tabs). */
    val subgroups: List<String>
        get() = items.map { it.subgroup }.filter { it.isNotBlank() }.distinct()
}

enum class ViewMode { GRID, FLASHCARD }

object CategoryIds {
    const val FAMILY = "family"
    const val DAILY = "daily"
}
