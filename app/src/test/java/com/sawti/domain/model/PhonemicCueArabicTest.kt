package com.sawti.domain.model

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

    @Test fun preservesLongVowelsInsteadOfTruncatingThem() {
        assertEquals("شَاـ...", PhonemicCue.derive("شَاهِي"))
        assertEquals("حُوـ...", PhonemicCue.derive("حُوتْ"))
        assertEquals("دِيـ...", PhonemicCue.derive("دِيكْ"))
    }

    @Test fun preservesExistingShaddaAndNeverCrossesWordBoundaries() {
        assertEquals("مَّـ...", PhonemicCue.derive("مَّ"))
        assertEquals("أَـ...", PhonemicCue.derive("أَ بْ"))
        assertEquals("سُكْـ...", PhonemicCue.derive("سُكَّرْ"))
        assertEquals("قَـ...", PhonemicCue.derive("الْقَمَرْ"))
    }

    @Test fun unvocalizedLabelsDoNotInventAPhonemicCue() {
        assertEquals("", PhonemicCue.derive("بيض"))
        assertEquals("", PhonemicCue.derive("اسم خاص"))
        assertEquals("نْحِـ...", PhonemicCue.derive("نْحِبْ"))
    }
}
