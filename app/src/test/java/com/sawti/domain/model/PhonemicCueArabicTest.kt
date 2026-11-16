package com.sawti.domain.model

import org.junit.Assert.*
import org.junit.Test

class PhonemicCueArabicTest {
    @Test fun emptyAndSingleWordInputsAreSafe() {
        assertEquals("", PhonemicCue.derive(" "))
        assertEquals("دي...", PhonemicCue.derive("ديك"))
        assertEquals("نق...", PhonemicCue.derive("نقعمز"))
        assertEquals("نب...", PhonemicCue.derive("نبي الحمام"))
    }

    @Test fun hintSpeechNeverContainsArtificialTatweel() {
        assertEquals("نب", PhonemicCue.forSpeech("نب..."))
        assertEquals("م", PhonemicCue.forSpeech("م..."))
        assertFalse(HabibiSpeechText.hasForbiddenMarksOrNumbers(
            PhonemicCue.forSpeech(PhonemicCue.derive("نرقد"))))
    }

    @Test fun originalArabicLettersAreNotPhoneticallySubstituted() {
        assertTrue(PhonemicCue.derive("قهوة").startsWith("قه"))
        assertFalse(PhonemicCue.derive("قهوة").contains("G"))
    }
}
