package com.sawti.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicSearchTest {
    @Test fun foldsHamzaAndDiacritics() {
        assertEquals("ابني", ArabicSearch.normalize("إِبْنِي"))
        assertEquals("اكل", ArabicSearch.normalize("آكُلُ"))
        assertTrue(ArabicSearch.matches("أمي", "أُمِّي", "امي"))
    }

    @Test fun foldsYaAndTaaMarbuta() {
        assertEquals("علي", ArabicSearch.normalize("عَلَى"))
        assertEquals("عائله", ArabicSearch.normalize("عائلة"))
        assertTrue(ArabicSearch.matches("عائلة", "عَائِلَة", "عائله"))
    }

    @Test fun searchesSpokenPromptWhenDisplayDiffers() {
        assertTrue(ArabicSearch.matches("نبي مية", "نَبِّيْ إِمِّيَّهْ", "اميه"))
        assertFalse(ArabicSearch.matches("نبي مية", "نبي مية", "شاهي"))
        assertFalse(ArabicSearch.matches("نبي مية", "نبي مية", " َ  "))
    }

    @Test fun removesTatweelAndSpacingArtifacts() {
        assertEquals("محمد علي", ArabicSearch.normalize("مُحَمَّد ـ  عَلَى"))
    }
}
