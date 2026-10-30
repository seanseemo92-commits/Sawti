package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import com.sawti.data.db.WordSeed
import org.junit.Test

class DialectCatalogTest {
    private fun item(text: String) = WordItem(
        id = 8L, categoryId = "daily", subgroup = "", displayText = text,
        ttsPrompt = "نَبِّي إِمِّيَّه", phonemicCue = "نَبـ...", sentencePrompt = "نبي ...",
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
        assertEquals("نبي إمّيه", variants[1])
        assertEquals("بغيت الما", variants[7])
        assertEquals(9, variants.size)
    }

    @Test fun localizingDoesNotMutateSavedWordOrMediaReferences() {
        val original = item("نبي مية")
        val localized = DialectCatalog.localize(original, ArabicDialect.SYRIAN)
        assertEquals("نبي مية", original.displayText)
        assertEquals("بدي مي", localized.displayText)
        assertEquals("بَدِّيْ مَيّْ", localized.ttsPrompt)
        assertTrue(DialectSpeechCatalog.isFullyMarked(localized.ttsPrompt))
        assertEquals(original.id, localized.id)
        assertEquals(original.audioPath, localized.audioPath)
        assertEquals(original.photoPath, localized.photoPath)
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

    @Test fun approvedCountryLabelsAreExactlyApplied() {
        val reference = mapOf(
            "بيض" to listOf("بيض", "دحي", "بيض", "بيض", "بيض", "بيض", "عظام", "بيض", "ولاد الجاج"),
            "شخاشير" to listOf("جوارب", "شخاشير", "دلاغات", "شرابات", "جرابات", "جواريب", "كلسيطة", "تقاشر", "تقاشر"),
            "مية" to listOf("ماء", "إمّيه", "ماي", "مية", "مي", "ماي", "ما", "الما", "الما"),
        )
        reference.forEach { (seed, expected) ->
            ArabicDialect.entries.forEachIndexed { index, dialect ->
                assertEquals("$seed in $dialect", expected[index], DialectCatalog.text(seed, dialect))
            }
        }
    }

    @Test fun localizedSharedCardsKeepTheirPersistentSemanticImagesAcrossAllDialects() {
        val shared = WordSeed.itemEntities().filter { it.categoryId != RegionalDishesCatalog.CATEGORY_ID }
        assertTrue(shared.size > 160)
        shared.forEach { dbItem ->
            if (dbItem.categoryId == "family") return@forEach
            val key = WordImageCatalog.keyForSeed(dbItem.categoryId, dbItem.displayText, dbItem.subgroup)
            assertTrue("Image missing for ${dbItem.categoryId}/${dbItem.displayText}", key != null)
            val original = WordItem(
                id = 100L, categoryId = dbItem.categoryId, subgroup = dbItem.subgroup,
                displayText = dbItem.displayText, ttsPrompt = dbItem.ttsPrompt,
                phonemicCue = dbItem.phonemicCue, sentencePrompt = dbItem.sentencePrompt,
                audioPath = "/user/voice.m4a", photoPath = "/user/photo.jpg", phone = "", emoji = dbItem.emoji,
                semanticKey = key,
            )
            for (dialect in ArabicDialect.entries) {
                val localized = DialectCatalog.localize(original, dialect)
                assertEquals("$dialect: ${dbItem.displayText}", key, WordImageCatalog.imageFor(localized))
                assertEquals(original.id, localized.id)
                assertEquals(original.photoPath, localized.photoPath)
                assertEquals(original.audioPath, localized.audioPath)
            }
        }
    }

    @Test fun libyanCommonNamesAreVisibleWithoutChangingSavedWord() {
        val egg = item("بيض").copy(categoryId = "food", semanticKey = "word_eggs")
        val view = DialectCatalog.localize(egg, ArabicDialect.LIBYAN)
        assertEquals("دحي", view.displayText)
        assertEquals("word_eggs", WordImageCatalog.imageFor(view))
        assertEquals("بيض", egg.displayText)
        assertEquals(egg.sentencePrompt, view.sentencePrompt)
    }

    @Test fun customFamilyAndRegionalDishesAreNeverSilentlyTranslated() {
        for (category in listOf("custom_x", "family", RegionalDishesCatalog.CATEGORY_ID)) {
            val original = item("بيض").copy(categoryId = category)
            for (dialect in ArabicDialect.entries)
                assertEquals(original, DialectCatalog.localize(original, dialect))
        }
    }
}
