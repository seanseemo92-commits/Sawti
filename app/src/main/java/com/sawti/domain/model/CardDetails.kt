package com.sawti.domain.model

/** Persisted editor values, kept separate from dialect-specific display/playback values. */
data class CardDetails(
    val display: String, val speech: String, val cue: String,
    val sentence: String?, val phone: String, val group: String,
) {
    /** Merge only deliberate edits; reject conflicting edits rather than overwriting newer data. */
    fun merge(baseline: CardDetails, edited: CardDetails): CardDetails {
        fun <T> field(current: T, before: T, after: T): T {
            if (before == after) return current
            require(current == before || current == after) { "تغيّرت البطاقة أثناء التحرير؛ افتحها من جديد قبل الحفظ" }
            return after
        }
        return CardDetails(field(display, baseline.display, edited.display),
            field(speech, baseline.speech, edited.speech), field(cue, baseline.cue, edited.cue),
            field(sentence, baseline.sentence, edited.sentence), field(phone, baseline.phone, edited.phone),
            field(group, baseline.group, edited.group))
    }

    fun changedTextFields(other: CardDetails): Int =
        (if (display != other.display) LABEL else 0) or
        (if (speech != other.speech) SPEECH else 0) or
        (if (cue != other.cue) CUE else 0) or
        (if (sentence != other.sentence) SENTENCE else 0)

    companion object {
        const val LABEL = 1
        const val SPEECH = 2
        const val CUE = 4
        const val SENTENCE = 8
        fun from(item: WordItem) = item.sourceDetails ?: CardDetails(
            item.displayText, item.ttsPrompt, item.phonemicCue, item.sentencePrompt, item.phone, item.subgroup)
    }
}

fun WordItem.forEditing(): WordItem {
    val saved = sourceDetails ?: return if (neuralSpeechText == null) this else copy(neuralSpeechText = null)
    return copy(displayText = saved.display, ttsPrompt = saved.speech, phonemicCue = saved.cue,
        sentencePrompt = saved.sentence, phone = saved.phone, subgroup = saved.group,
        sourceDetails = null, neuralSpeechText = null)
}
