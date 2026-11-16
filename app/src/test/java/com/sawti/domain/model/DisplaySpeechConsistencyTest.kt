package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Permanent test of the actual runtime projection, not merely source-file spelling.
 * Old phonetic respellings are removed; Habibi uses visible words directly.
 * Passing this test is NOT a claim of native-speaker or TTS-listening validation.
 */
class DisplaySpeechConsistencyTest {
    private val seeds by lazy {
        WordSeed.itemEntities().map { it.toDomain() }.associateBy { requireNotNull(it.semanticKey) }
    }

    private val auditedLabels = """
        LIBYAN|family.son|ولدي
        LIBYAN|family.paternal_uncle_son|ولد عمي
        LIBYAN|family.paternal_aunt_son|ولد عمتي
        LIBYAN|family.maternal_uncle_son|ولد خالي
        LIBYAN|family.maternal_aunt_son|ولد خالتي
        LIBYAN|family.brother_son|ولد خوي
        LIBYAN|family.brother_daughter|بنت خوي
        LIBYAN|family.sister_son|ولد أختي
        LIBYAN|family.husband_mother|أم راجلي
        LIBYAN|family.wife_mother|أم مرتي
        LIBYAN|family.husband_sister|أخت راجلي
        LIBYAN|family.wife_sister|أخت مرتي
        LIBYAN|family.brother_wife|مرت خوي
        LIBYAN|family.son_wife|مرت ولدي
        LIBYAN|request.cold|نحس بالبرد
        LIBYAN|dish.mbakbaka|مكرونة مبكبكة
        LIBYAN|vegetable.coriander|قصبر
        LIBYAN|vegetable.peas|بازيلية
        LIBYAN|fruit.pear|كمثرى
        LIBYAN|clothing.blouse|سورية
        SAUDI|family.father|أبوي
        SAUDI|dish.saudi.kabsa|كبسة لحم
        SAUDI|dish.saudi.saleeg|سليق طائفي
        SAUDI|dish.saudi.madhbi|مظبي دجاج
        SAUDI|dish.saudi.liver|كبدة حاشي
        EGYPTIAN|family.father|أبويا
        EGYPTIAN|dish.egyptian.molokhia|ملوخية بالتقلية
        EGYPTIAN|dish.egyptian.mahshi|محشي مشكل
        EGYPTIAN|dish.egyptian.moussaka|مسقعة بلدي
        EGYPTIAN|dish.egyptian.trotters_fattah|كوارع وفتة
        EGYPTIAN|vegetable.lemon|لمون
        EGYPTIAN|fruit.orange|برتقان
        EGYPTIAN|clothing.socks|شرابات
        EGYPTIAN|body.mouth|بق
        EGYPTIAN|body.face|وش
        EGYPTIAN|animal.chicken|فرخة
        SYRIAN|family.husband|جوزي
        SYRIAN|family.wife|مرتي
        SYRIAN|request.adjust_pillow|زبط لي المخدة
        SYRIAN|dish.syrian.shakriya|شاكرية برز
        SYRIAN|dish.syrian.maqluba|مقلوبة باذنجان
        SYRIAN|dish.syrian.kebab|كباب حلبي
        SYRIAN|dish.syrian.mutabbal|متبل باذنجان
        SYRIAN|dish.syrian.kunafa|كنافة نابلسية
        SYRIAN|fruit.orange|بردقان
        SYRIAN|home.sofa|كنبة
        SYRIAN|body.mouth|تم
        SYRIAN|body.face|وش
        IRAQI|family.father|أبويه
        IRAQI|dish.iraqi.dolma|دولمة عراقية
        IRAQI|dish.iraqi.tashreeb|تشريب لحم
        IRAQI|dish.iraqi.biryani|برياني عراقي
        IRAQI|home.sofa|قنفة
        IRAQI|home.pot|جدر
        IRAQI|body.mouth|حلك
        TUNISIAN|family.father|بابا
        TUNISIAN|request.toilet|نحب نمشي للتواليت
        TUNISIAN|dish.tunisian.molokhia|ملوخية تونسية
        TUNISIAN|dish.tunisian.pasta|مقرونة بالصلصة
        TUNISIAN|dish.tunisian.maqroudh|مقروض قيرواني
        TUNISIAN|vegetable.carrot|سفنارية
        TUNISIAN|vegetable.zucchini|قرع أخضر
        TUNISIAN|vegetable.lemon|قارص
        TUNISIAN|fruit.banana|موز
        TUNISIAN|fruit.orange|بردقان
        TUNISIAN|home.stove|غاز
        TUNISIAN|home.wardrobe|مريو
        TUNISIAN|home.carpet|زربية
        MOROCCAN|dish.moroccan.pastilla|بسطيلة بالحوت
        MOROCCAN|dish.moroccan.harira|حريرة مغربية
        MOROCCAN|dish.moroccan.tangia|طنجية مراكشية
        MOROCCAN|dish.moroccan.rfissa|رفيسة بالدجاج
        MOROCCAN|dish.moroccan.kefta_tajine|طاجين كفتة بالبيض
        MOROCCAN|vegetable.carrot|خيزو
        MOROCCAN|fruit.banana|بنان
        MOROCCAN|home.water_bottle|قرعة ماء
        ALGERIAN|family.father|بابا
        ALGERIAN|family.mother|يما
        ALGERIAN|dish.algerian.chakhchoukha|شخشوخة بسكرية
        ALGERIAN|dish.algerian.garantita|كرانتيكا
        ALGERIAN|vegetable.carrot|زرودية
        ALGERIAN|vegetable.lemon|قارص
        ALGERIAN|fruit.banana|بنان
        ALGERIAN|fruit.orange|تشينة
        ALGERIAN|home.carpet|زربية
        ALGERIAN|home.water_bottle|قرعة ماء
    """.trimIndent().lines().filter { it.isNotBlank() }

    private fun plain(text: String): String = text
        .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')
        .replace(Regex("[؟،؛:!()./\\u200F]"), "")
        .replace(Regex("\\s+"), " ").trim()

    @Test fun theEightySixAuditedLabelsHaveTheirExactExpectedVisibleForms() {
        assertEquals(86, auditedLabels.size)
        val problems = mutableListOf<String>()
        auditedLabels.forEach { row ->
            val fields = row.split('|', limit = 3)
            val dialect = ArabicDialect.valueOf(fields[0])
            val seed = requireNotNull(seeds[fields[1]]) { "Missing built-in ${fields[1]}" }
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            if (form.displayText != fields[2])
                problems += "${fields[0]} ${fields[1]}: expected '${fields[2]}' got '${form.displayText}'"
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun libyanVocabIsItsOwnUnvowelizedHabibiSpeech() {
        val names = mapOf(
            "family.son_wife" to "مرت ولدي",
            "dish.mbakbaka" to "مكرونة مبكبكة",
            "vegetable.peas" to "بازيلية",
            "dish.zmeeta" to "زميطة",
            "dish.sfinz" to "سفنز",
            "clothing.blouse" to "سورية",
        )
        names.forEach { (key, display) ->
            val form = DialectSpeechCatalog.resolve(
                requireNotNull(seeds[key]), ArabicDialect.LIBYAN)
            assertEquals(key, display, form.displayText)
            assertEquals(key, display, form.speechText)
        }
    }

    @Test fun reviewedRegionalDishesSpeakExactlyTheirCleanDisplayWord() {
        val reviewed = mapOf(
            ArabicDialect.SAUDI to listOf("dish.saudi.liver", "dish.saudi.kabsa",
                "dish.saudi.saleeg", "dish.saudi.madhbi"),
            ArabicDialect.IRAQI to listOf("family.brother", "dish.iraqi.rice_kibbeh",
                "dish.iraqi.tashreeb", "dish.iraqi.biryani"),
            ArabicDialect.SYRIAN to listOf("dish.syrian.shakriya", "dish.syrian.maqluba",
                "dish.syrian.kebab", "dish.syrian.mutabbal"),
            ArabicDialect.EGYPTIAN to listOf("dish.egyptian.mahshi",
                "dish.egyptian.bird_tongue_soup"),
            ArabicDialect.TUNISIAN to listOf("dish.tunisian.maqroudh"),
            ArabicDialect.ALGERIAN to listOf("dish.algerian.garantita"),
        )
        assertEquals(16, reviewed.values.sumOf { it.size })
        for ((dialect, keys) in reviewed) for (key in keys) {
            val source = requireNotNull(seeds[key])
            val form = DialectSpeechCatalog.resolveForHabibi(source, dialect)
            assertEquals("$dialect/$key", form.displayText, form.speechText)
            assertEquals(form.speechText,
                DialectCatalog.localize(source, dialect).ttsPrompt)
        }
    }

    @Test fun builtInSpeechHasNoArtificialVowelsNumbersOrLatinPhonetics() {
        val problems = mutableListOf<String>()
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val text = DialectSpeechCatalog.resolveForHabibi(seed, dialect).speechText
            if (HabibiSpeechText.hasForbiddenMarksOrNumbers(text) ||
                text.any { it in 'A'..'Z' || it in 'a'..'z' })
                problems += "$dialect/$key: $text"
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun everyBuiltInDisplayedWordMatchesHabibiInputExactly() {
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val form = DialectSpeechCatalog.resolveForHabibi(seed, dialect)
            assertEquals("$dialect/$key", form.displayText, form.speechText)
        }
    }

    @Test fun hintsNeverReintroduceArtificialPronunciationMarks() {
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            assertFalse("$dialect/$key",
                HabibiSpeechText.hasForbiddenMarksOrNumbers(form.phonemicCue))
        }
    }

    @Test fun personalLabelAndSpeechOverridesAreNeverReplaced() {
        val sample = requireNotNull(seeds["family.son"]).copy(
            displayText = "اسم مخصص", ttsPrompt = "نطقي الخاص",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH,
        )
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(sample, dialect)
            assertEquals(sample.displayText, form.displayText)
            assertEquals(sample.ttsPrompt, form.speechText)
        }
    }
}
