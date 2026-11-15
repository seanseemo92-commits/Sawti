package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiPronunciationReviewTest {
    @Test fun allGeminiCandidatesAreIndexedWithoutDuplicateKeys() {
        assertEquals(1809, GeminiPronunciationReview.candidateCount())
    }

    @Test fun representativeOriginalTextMatchesBeforeApplying() {
        assertEquals(
            "إِمَّيَّهْ",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "food.water", "مَيَّة")
        )
        assertEquals(
            "مَاءٌ",
            GeminiPronunciationReview.apply(ArabicDialect.STANDARD, "food.water", "مَاء")
        )
    }

    @Test fun unexpectedSourcesAndUnlistedKeysNeverGetOverwritten() {
        assertEquals(
            "مَيَّة خاصة",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "food.water", "مَيَّة خاصة")
        )
        assertEquals(
            "عْرُوق",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "custom.card", "عْرُوق")
        )
    }
}
