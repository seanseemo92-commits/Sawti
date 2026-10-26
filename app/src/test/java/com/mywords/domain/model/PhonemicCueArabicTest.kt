package com.mywords.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhonemicCueArabicTest {
    @Test fun emptyInputIsSafe() {
        assertEquals("", PhonemicCue.derive("   "))
    }

    @Test fun removesVisualDecorationFromSpeech() {
        assertEquals("إِسْ", PhonemicCue.forSpeech("إِسْـ..."))
        assertEquals("مَ", PhonemicCue.forSpeech("مَـ..."))
    }

    @Test fun arabicWordProducesCue() {
        assertTrue(PhonemicCue.derive("أُمّ").isNotBlank())
    }
}
