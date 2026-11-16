package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionalDishesCatalogTest {
    @Test fun everyDialectHasCompleteAndDistinctCuisine() {
        assertEquals(9, ArabicDialect.entries.size)
        assertTrue(RegionalDishesCatalog.allDialectsCovered())
        for (dialect in ArabicDialect.entries) {
            val cuisine = RegionalDishesCatalog.forDialect(dialect)
            assertTrue("Missing title: $dialect", cuisine.title.isNotBlank())
            assertEquals(
                "Wrong regional cuisine size for $dialect",
                if (dialect == ArabicDialect.LIBYAN) 5 else 15,
                cuisine.dishes.size,
            )
            assertEquals(cuisine.dishes.size, cuisine.dishes.map { it.displayText }.distinct().size)
            for (dish in cuisine.dishes) {
                assertTrue(dish.displayText.isNotBlank())
                assertTrue(dish.ttsPrompt.isNotBlank())
                assertTrue(dish.phonemicCue.isNotBlank())
                assertTrue(dish.sentencePrompt.isNotBlank())
                assertTrue(dish.emoji.isNotBlank() || !dish.photoPath.isNullOrBlank())
            }
        }
    }

    @Test fun titlesAndSubgroupsChangeWithoutReplacingExistingLibyanCards() {
        assertEquals("أكلات ليبية", RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).title)
        assertEquals("أكلات تونسية", RegionalDishesCatalog.forDialect(ArabicDialect.TUNISIAN).title)
        assertEquals("أكلات مصرية", RegionalDishesCatalog.forDialect(ArabicDialect.EGYPTIAN).title)
        assertEquals("أكلات سعودية", RegionalDishesCatalog.forDialect(ArabicDialect.SAUDI).title)
        assertEquals("أكلات عربية", RegionalDishesCatalog.forDialect(ArabicDialect.STANDARD).title)
        assertFalse(RegionalDishesCatalog.visibleForDialect("", ArabicDialect.LIBYAN))
        assertTrue(RegionalDishesCatalog.visibleForDialect(RegionalDishesCatalog.subgroupFor(ArabicDialect.LIBYAN), ArabicDialect.LIBYAN))
        assertFalse(RegionalDishesCatalog.visibleForDialect("", ArabicDialect.EGYPTIAN))
        for (dialect in ArabicDialect.entries.filter { it != ArabicDialect.LIBYAN }) {
            val tag = RegionalDishesCatalog.subgroupFor(dialect)
            assertTrue(RegionalDishesCatalog.visibleForDialect(tag, dialect))
            assertFalse(RegionalDishesCatalog.visibleForDialect(tag, ArabicDialect.LIBYAN))
            assertFalse(RegionalDishesCatalog.visibleForDialect(tag, ArabicDialect.STANDARD.takeUnless { it == dialect } ?: ArabicDialect.EGYPTIAN))
        }
        assertEquals(120, RegionalDishesCatalog.additionalDialects().size)
    }

    @Test fun fiveUserRequestedLibyanDishesUseWrittenArabic() {
        val expected = setOf("مبطن", "حرايمي", "سفنز", "زميطة", "عيش باللبن")
        val dishes = RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).dishes
        assertEquals(expected, dishes.map { it.displayText }.toSet())
        dishes.forEach { dish ->
            assertEquals(dish.displayText, dish.ttsPrompt)
            assertTrue(dish.phonemicCue.isNotBlank())
            assertFalse(HabibiSpeechText.hasForbiddenMarksOrNumbers(dish.ttsPrompt))
        }
        val saved = WordSeed.itemEntities().filter {
            it.semanticKey in dishes.map { dish -> dish.semanticKey }
        }
        assertEquals(5, saved.size)
        saved.forEach { row ->
            val result = DialectCatalog.localize(row.toDomain(), ArabicDialect.LIBYAN)
            assertEquals(row.toDomain(), result.forEditing())
            assertEquals(result.displayText, result.speechText)
        }
    }

    @Test fun exactFullOrderedListsAreNeverAccidentallyTruncatedOrRenamed() {
        val expected = mapOf(
            ArabicDialect.TUNISIAN to "كسكسي بالحوت|لبلابي|بريك|كفتاجي|طاجين تونسي|صحن تونسي|شربة لسان عصفور|ملوخية تونسية|مرقة جلبانة|مقرونة بالصلصة|عجة بالمرقاز|سلاطة مشوية|فريكاسي|مقروض قيرواني|بامبالوني",
            ArabicDialect.EGYPTIAN to "كشري|ملوخية بالتقلية|حواوشي|فول مدمس|طعمية|حمام محشي|فتة بالخل والتوم|صينية بطاطس|كباب وحواوشي|محشي مشكل|مسقعة بلدي|كوارع وفتة|رقاق باللحمة|شربة لسان عصفور|أم علي",
            ArabicDialect.ALGERIAN to "رشتة|شخشوخة بسكرية|طعام|طاجين الزيتون|شربة فريك|بوراك|كرانتيكا|طاجين الحلو|بركوكس|جواز بطاطا|محاجب|دوارة|قرنون معمر|قلب اللوز|مقروط العسل",
            ArabicDialect.MOROCCAN to "طاجين بالبرقوق|كسكسو سبع خضار|بسطيلة بالحوت|حريرة مغربية|طنجية مراكشية|رفيسة بالدجاج|زعلوك|تكتوكة|طاجين كفتة بالبيض|قطبان|مسمن|بغرير|سفة مدفونة|شباكية|سلو",
            ArabicDialect.SYRIAN to "كبة مقلية|شاكرية برز|يبرق ورق عنب|شاورما دجاج|فتة حمص|مقلوبة باذنجان|فتوش|تبولة|كباب حلبي|شيش برك|كوسا بلبن|متبل باذنجان|حراق إصبعو|حلاوة الجبن|كنافة نابلسية",
            ArabicDialect.IRAQI to "دولمة عراقية|سمچ مسكوف|قوزي عالتمن|تشريب لحم|باجة|كبة تمن|كبة موصلية|برياني عراقي|مطبق سمچ|فافير / مخلمة|باميا عالتمن|شلغم مطبوخ|هريسة|كليجة|زلابية",
            ArabicDialect.SAUDI to "كبسة لحم|مندي|جريش|قرصان|سليق طائفي|حنيذ|مطازيز|مرقوق|مظبي دجاج|رز بخاري|مدفون|كبدة حاشي|معصوب|حنيني|لقيمات",
            ArabicDialect.STANDARD to "أرز باللحم|دجاج مشوي|حساء العدس|يخنة خضار|مشويات مشكلة|سلطة خضراء|سمك مطهو|فطائر مخبوزة|حساء خضار|لحم مسلوق|خبز مخبوز|ثمار وبقوليات|بيض مسلوق|تمر وحليب|حلوى عربية",
        )
        assertEquals(8, expected.size)
        expected.forEach { (dialect, sequence) ->
            val dishes = RegionalDishesCatalog.forDialect(dialect).dishes
            assertEquals("Missing or reordered regional dishes in $dialect",
                sequence.split("|"), dishes.map { it.displayText })
            assertEquals(15, dishes.size)
        }
    }

    @Test fun allNineCuisineListsContainOnlyPlainHabibiSpeech() {
        for (dialect in ArabicDialect.entries) for (
            dish in RegionalDishesCatalog.forDialect(dialect).dishes
        ) {
            assertEquals(dish.semanticKey, dish.displayText, dish.ttsPrompt)
            assertFalse(dish.semanticKey,
                HabibiSpeechText.hasForbiddenMarksOrNumbers(dish.ttsPrompt))
            assertTrue(dish.phonemicCue.isNotBlank())
        }
    }


}
