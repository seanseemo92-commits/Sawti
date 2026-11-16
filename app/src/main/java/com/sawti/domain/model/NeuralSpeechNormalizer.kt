package com.sawti.domain.model

/**
 * Speech-only guard before neural synthesis. Do not touch user-defined labels,
 * stored Room text or phonetic cues; retain all harakat and final sukun.
 */
internal object NeuralSpeechNormalizer {
    fun normalize(text: String): String = buildString(text.length) {
        for (char in text) append(when (char) {
            'گ', 'ڨ' -> 'ق'
            'چ' -> 'ج'
            'پ' -> 'ب'
            'ڤ' -> 'ف'
            else -> char
        })
    }
}
