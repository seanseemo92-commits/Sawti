package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialectCatalogTest {
    private fun item(text: String) = WordItem(
        id = 8L, categoryId = "daily", subgroup = "", displayText = text,
        ttsPrompt = "نَبِّي إِمِّيَّه", phonemicCue = "نَبـ...",
        sentencePrompt = "نبي ...", isFavorite = true,
        audioPath = "/protected/voice.m4a", photoPath = "/protected/photo.jpg",
        phone = "", emoji = "💧"
    )

    @Test fun nineDialectsHaveLocaleAndNonemptySample() {
        assertEquals(9, ArabicDialect.entries.size)
        ArabicDialect.entries.forEach {
            assertTrue(it.localeTag.startsWith("ar"))
            assertTrue(it.sample.isNotBlank())
        }
    }

    @Test fun frequentWaterRequestUsesNineLocalizedWrittenVariants() {
        val variants = ArabicDialect.entries.map { DialectCatalog.text("نبي مية", it) }
        assertEquals("أريد ماءً", variants[0])
        assertEquals("نبي مية", variants[1])
        assertEquals("بغيت الما", variants[7])
        assertEquals(9, variants.size)
    }

    @Test fun localizingDoesNotMutateSavedWordOrMediaReferences() {
        val original = item("نبي مية")
        val localized = DialectCatalog.localize(original, ArabicDialect.SYRIAN)
        assertEquals("نبي مية", original.displayText)
        assertEquals("بدي مي", localized.displayText)
        assertEquals("بدي مي", localized.ttsPrompt)
        assertEquals(original.id, localized.id)
        assertEquals(original.audioPath, localized.audioPath)
        assertEquals(original.photoPath, localized.photoPath)
        assertEquals(original.isFavorite, localized.isFavorite)
        assertEquals(null, localized.sentencePrompt) // Don't speak a Libyan carrier phrase in a different dialect
    }

    @Test fun unmappedCustomWordsAreNeverSilentlyTranslated() {
        val original = item("اسم مخصص للعائلة")
        ArabicDialect.entries.forEach { dialect ->
            assertEquals(original, DialectCatalog.localize(original, dialect))
        }
    }

    @Test fun switchoverIsReversible() {
        val original = item("نبي شاهي")
        assertNotEquals(original.displayText, DialectCatalog.localize(original, ArabicDialect.EGYPTIAN).displayText)
        assertEquals(original, DialectCatalog.localize(original, ArabicDialect.LIBYAN))
    }
}
