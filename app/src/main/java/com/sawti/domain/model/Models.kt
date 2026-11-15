package com.sawti.domain.model

/**
 * One tile / flashcard.
 *  displayText – friendly label shown on screen ("نبي مية", "مثال")
 *  ttsPrompt   – speech-source text sent to Android TTS; listening review is a separate step ("أُرِيدُ مَاءً")
 *  phonemicCue – first syllable that prompts word retrieval ("إِسْـ...")
 *  sentencePrompt – cloze carrier phrase ("نَأْكُلُ الـ...") for the 💬 cue; null = none
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
    val audioPath: String?,
    val photoPath: String?,
    val phone: String,
    val emoji: String,
    /** Immutable concept identity; independent of captions, locale and drawable filename. */
    val semanticKey: String? = null,
    val customTextFields: Int = 0,
    val sourceDetails: CardDetails? = null,
) {
    /** Transient, non-Room voice projection; excluded from persistent identity and equality. */
    var neuralSpeechText: String? = null
    /** Dialect which produced the transient neural text; never persisted to Room. */
    var neuralDialect: ArabicDialect? = null
    /** Speech-only channel backed by the existing Room ttsPrompt column. */
    val speechText: String get() = ttsPrompt

    /** Supports a lightweight visual-only category preview item. */
    constructor(
        id: Long,
        categoryId: String,
        displayText: String,
        speechText: String,
        emoji: String,
    ) : this(
        id = id,
        categoryId = categoryId,
        subgroup = "",
        displayText = displayText,
        ttsPrompt = speechText,
        phonemicCue = "",
        sentencePrompt = null,
        audioPath = null,
        photoPath = null,
        phone = "",
        emoji = emoji,
    )
}

data class Category(
    val id: String,
    val title: String,
    val palette: Int,
    /** True for the family category: tapping opens a large photo instead of only speaking. */
    val previewOnTap: Boolean,
    val items: List<WordItem>,
    val sortOrder: Int = 0,
) {
    /** Sub-tab labels in first-appearance order (empty when the category has no sub-tabs). */
    val subgroups: List<String>
        get() = items.map { it.subgroup }.filter { it.isNotBlank() }.distinct()
}

enum class ViewMode { GRID, FLASHCARD }

object CategoryIds {
    const val FAMILY = "family"
    const val DAILY = "daily"
    const val CUISINE = "cuisine"
    const val TRANSPORT = "transport"
}
