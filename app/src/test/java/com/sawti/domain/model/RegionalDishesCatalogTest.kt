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

    @Test fun fiveUserRequestedLibyanDishesHaveExactSpeechAndCue() {
        val expected = mapOf(
            "مبطن" to ("إمْبَطَّنْ" to "مْبَـ..."),
            "حرايمي" to ("حَرَايْمِي" to "حَـ..."),
            "سفنز" to ("إِسْفَنْزْ" to "إِسْـ..."),
            "زميطة" to ("زَمِّيطَهْ" to "زَمْـ..."),
            "عيش باللبن" to ("عَيْشْ بِاللَّبَنْ" to "عَيْـ..."),
        )
        val dishes = RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).dishes
        assertEquals(expected.keys, dishes.map { it.displayText }.toSet())
        dishes.forEach {
            assertEquals(expected.getValue(it.displayText).first, it.ttsPrompt)
            assertEquals(expected.getValue(it.displayText).second, it.phonemicCue)
        }
        val saved = WordSeed.itemEntities().filter {
            it.semanticKey in dishes.map { dish -> dish.semanticKey }
        }
        assertEquals(5, saved.size)
        assertTrue(saved.all { it.subgroup == RegionalDishesCatalog.subgroupFor(ArabicDialect.LIBYAN) && !it.sentencePrompt.isNullOrBlank() })
        saved.forEach { source ->
            val dish = DialectCatalog.localize(source.toDomain(), ArabicDialect.LIBYAN)
            assertEquals(expected.getValue(dish.displayText).first, dish.ttsPrompt)
            assertEquals(expected.getValue(dish.displayText).second, dish.phonemicCue)
        }
    }
    @Test fun exactFullOrderedListsAreNeverAccidentallyTruncatedOrRenamed() {
        val expected = mapOf(
            ArabicDialect.TUNISIAN to "كسكسي بالحوت|لبلابي|بريك|كفتاجي|طاجين تونسي|صحن تونسي|شربة لسان عصفور|ملوخية تونسية|مرقة جلبانة|مقرونة بالصلصة|عجة بالمرقاز|سلاطة مشوية|فريكاسي|مقروض قيرواني|بامبالوني",
            ArabicDialect.EGYPTIAN to "كشري|ملوخية بالتقلية|حواوشي|فول مدمس|طعمية|حمام محشي|فتة بالخل والتوم|صينية بطاطس|كباب وحواوشي|محشي مشكل|مسقعة بلدي|كوارع وفتة|رقاق باللحمة|شربة لسان عصفور|أم علي",
            ArabicDialect.ALGERIAN to "رشتة|شخشوخة بسكرية|كسكسي جزائري|طاجين الزيتون|شربة فريك|بوراك جزائري|كرانتيكا|طاجين الحلو|بركوكس|جواز بطاطا|مطبقة / محاجب|دوارة|قرنون معمر|قلب اللوز|مقروط العسل",
            ArabicDialect.MOROCCAN to "طاجين بالبرقوق|كسكسو سبع خضار|بسطيلة بالحوت|حريرة مغربية|طنجية مراكشية|رفيسة بالدجاج|زعلوك|تكتوكة|طاجين كفتة بالبيض|شوا مغربي|مسمن|بغرير|سفة مدفونة|شباكية|سلو",
            ArabicDialect.SYRIAN to "كبة مقلية|شاكرية برز|يبرق ورق عنب|شاورما دجاج|فتة حمص|مقلوبة باذنجان|فتوش|تبولة|كباب حلبي|شيش برك|كوسا بلبن / شيخ المحشي|متبل باذنجان|حراق بأصبعه|حلاوة الجبن|كنافة نابلسية",
            ArabicDialect.IRAQI to "دولمة عراقية|سمك مسكوف|قوزي على تمن|تشريب لحم|باجة|كبة تمن / حلب|كبة موصلية|برياني عراقي|مطبق سمك|فافير / مخلمة|بامية عراقية|شلغم مطبوخ|هريسة|كليجة|زلابية",
            ArabicDialect.SAUDI to "كبسة لحم|مندي|جريش|قرصان|سليق طائفي|حنيذ|مطازيز|مرقوق|مظبي دجاج|بخاري|مدفون|كبدة بالحوار|معصوب|حنيني|لقيمات",
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

    @Test fun culturallyDistinctPronunciationsAndCluesStayVerbatim() {
        val examples = mapOf(
            ArabicDialect.TUNISIAN to Triple("كسكسي بالحوت", "كُسْكْسِي بِالحُوتْ", "كُسْـ..."),
            ArabicDialect.EGYPTIAN to Triple("كباب وحواوشي", "كَبَابْ وَحَوَاوْشِي", "كَـ..."),
            ArabicDialect.ALGERIAN to Triple("كسكسي جزائري", "طْعَامْ", "طْعَا..."),
            ArabicDialect.MOROCCAN to Triple("شوا مغربي", "قُطْبَانْ", "قُطْـ..."),
            ArabicDialect.SYRIAN to Triple("كوسا بلبن / شيخ المحشي", "كُوسَا بِلَبَنْ", "كُو..."),
            ArabicDialect.IRAQI to Triple("سمك مسكوف", "سِمَچْ مَسْكُوفْ", "سِـ..."),
            ArabicDialect.SAUDI to Triple("بخاري", "رُزْ بُخَارِي", "بُـ..."),
            ArabicDialect.STANDARD to Triple("أرز باللحم", "أَرُزٌّ بِاللَّحْمْ", "أَ..."),
        )
        examples.forEach { (dialect, expected) ->
            val item = RegionalDishesCatalog.forDialect(dialect).dishes.single { it.displayText == expected.first }
            assertEquals(expected.second, item.ttsPrompt)
            assertEquals(expected.third, item.phonemicCue)
        }
    }

}
