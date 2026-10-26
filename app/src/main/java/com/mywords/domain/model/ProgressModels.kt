package com.mywords.domain.model

/** RECALL = name the picture; CHOICE = "أين …؟" two-picture receptive game. */
enum class PracticeMode { RECALL, CHOICE }

/**
 * RECALL outcomes:  SELF (said it with no cue), CUED (needed 💡/💬 first), SHOWN (needed the answer).
 * CHOICE outcomes:  CORRECT (right picture on the first try), WRONG (first tap was the other picture).
 */
enum class PracticeOutcome { SELF, CUED, SHOWN, CORRECT, WRONG }

data class CuedWordStat(val word: String, val count: Int)

/** Private caregiver summary shown under Caregiver Mode → متابعة التقدم. */
data class ProgressSummary(
    val recallAttempts: Int = 0,
    val selfRecalled: Int = 0,
    val cued: Int = 0,
    val shown: Int = 0,
    val choiceAttempts: Int = 0,
    val choiceCorrect: Int = 0,
    val cuedWords: List<CuedWordStat> = emptyList(),
)
