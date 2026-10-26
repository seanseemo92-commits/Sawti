package com.sawti.domain.model

/** Spoken question for the receptive game: "أَيْنَ التُّفَّاحَة؟" / family: "وَيْنْ إِسْمَاعِيلْ؟". */
object QuestionPhrases {
    private const val SHADDA = '\u0651'
    private val SUN_LETTERS = "تثدذرزسشصضطظلن".toSet()

    private fun isMark(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670'

    fun where(item: WordItem): String =
        if (item.categoryId == CategoryIds.FAMILY) "وَيْنْ ${item.ttsPrompt}؟"
        else "أَيْنَ ${withArticle(item.ttsPrompt)}؟"

    /** Adds "ال" with the right assimilation: sun letters get a shadda, moon letters a sukun on the lam. */
    fun withArticle(word: String): String {
        val w = word.trim()
        if (w.isEmpty() || ' ' in w || w.startsWith("ال")) return w
        val first = w[0]
        return if (first in SUN_LETTERS) {
            var i = 1
            while (i < w.length && isMark(w[i])) i++
            "ال" + first + SHADDA + w.substring(1, i) + w.substring(i)
        } else {
            "الْ$w"
        }
    }
}
