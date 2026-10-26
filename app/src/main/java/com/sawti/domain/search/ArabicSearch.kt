package com.sawti.domain.search

/** Unicode-aware Arabic folding for searching speech text and display labels. */
object ArabicSearch {
    fun normalize(raw: String): String = buildString(raw.length) {
        raw.forEach { c ->
            if (c == '\u0640' || c == '\u0670' ||
                c in '\u064B'..'\u065F' || c in '\u06D6'..'\u06ED'
            ) return@forEach
            append(when (c) {
                'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                'ة' -> 'ه'
                'ى' -> 'ي'
                else -> c.lowercaseChar()
            })
        }
    }.trim().replace(Regex("\\s+"), " ")

    fun matches(displayText: String, ttsPrompt: String, query: String): Boolean {
        val needle = normalize(query)
        if (needle.isBlank()) return false
        return normalize(displayText).contains(needle) || normalize(ttsPrompt).contains(needle)
    }
}
