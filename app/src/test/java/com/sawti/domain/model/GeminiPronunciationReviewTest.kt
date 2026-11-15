package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiPronunciationReviewTest {
    @Test fun allGeminiCandidatesAreIndexedWithoutDuplicateKeys() {
        // Preserve the full submitted review as auditable proposals, including
        // forms that are unsafe to synthesize until listened to on-device.
        assertEquals(1809, GeminiPronunciationReview.candidateCount())
    }

    @Test fun existingReviewedFormsAndUnsupportedSymbolsRemainUntouched() {
        assertEquals("بُوي",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "family.father", "بُوي"))
        assertEquals("زَوْجَة أَخِي",
            GeminiPronunciationReview.apply(ArabicDialect.STANDARD,
                "family.brother_wife", "زَوْجَة أَخِي"))
        assertEquals("مَيَّة",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "request.water", "مَيَّة"))
        assertEquals("أَقُوم",
            GeminiPronunciationReview.apply(ArabicDialect.SAUDI, "action.stand", "أَقُوم"))
    }

    @Test fun safeDiacriticOnlySuggestionsRemainAvailable() {
        assertEquals("حَفِيدْتِي",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN,
                "family.granddaughter", "حَفِيدَتِي"))
        // The older Libyan approved pillow pronunciation must remain stable.
        assertEquals("مَخَدَّة",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "home.pillow", "مَخَدَّة"))
        assertEquals("دجَاجَة",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "animal.chicken", "دجَاجَة"))
    }

    @Test fun unexpectedSourcesAndUnlistedKeysNeverGetOverwritten() {
        assertEquals("مَيَّة خاصة",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "food.water", "مَيَّة خاصة"))
        assertEquals("عْرُوق",
            GeminiPronunciationReview.apply(ArabicDialect.LIBYAN, "custom.card", "عْرُوق"))
    }
}
