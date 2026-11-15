package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiPronunciationReviewTest {
    @Test fun allGeminiCandidatesAreIndexedWithoutDuplicateKeys() {
        assertEquals(1809, GeminiPronunciationReview.candidateCount())
    }

    @Test fun representativeOriginalTextMatchesBeforeApplying() {
        assertEquals(
            "بُويْ",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "family.father", "بُوي")
        )
        assertEquals(
            "زَوْجَةُ أَخِي",
            GeminiPronunciationReview.apply(ArabicDialect.STANDARD, "family.brother_wife", "زَوْجَة أَخِي")
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
