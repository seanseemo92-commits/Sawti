package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Permanent test of the actual runtime projection, not merely source-file spelling.
 * Android TTS-specific phonetic helper spellings remain untouched.
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
        LIBYAN|family.son_wife|مرت ابني
        LIBYAN|request.cold|إنحس بالبرد
        LIBYAN|dish.mbakbaka|مكرونه مبكبكه
        LIBYAN|vegetable.coriander|قصبر
        LIBYAN|vegetable.peas|بازيليا
        LIBYAN|fruit.pear|كمثرى
        LIBYAN|clothing.blouse|بلوزة
        SAUDI|family.father|أبوي
        SAUDI|dish.saudi.kabsa|كبسة لحم
        SAUDI|dish.saudi.saleeg|سليق طايفي
        SAUDI|dish.saudi.madhbi|مظبي دجاج
        SAUDI|dish.saudi.liver|كبدة بالحوار
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
        SYRIAN|dish.syrian.maqluba|مقلوبة باتنجان
        SYRIAN|dish.syrian.kebab|كباب حلبي
        SYRIAN|dish.syrian.mutabbal|متبل باتنجان
        SYRIAN|dish.syrian.kunafa|كنافة نابلسية
        SYRIAN|fruit.orange|بردقان
        SYRIAN|home.sofa|كنباية
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
        TUNISIAN|fruit.banana|بنان
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
        MOROCCAN|home.water_bottle|قرعة ما
        ALGERIAN|family.father|بابا
        ALGERIAN|family.mother|يما
        ALGERIAN|dish.algerian.chakhchoukha|شخشوخة بسكرية
        ALGERIAN|dish.algerian.garantita|كارانتيكا
        ALGERIAN|vegetable.carrot|زرودية
        ALGERIAN|vegetable.lemon|قارص
        ALGERIAN|fruit.banana|بنان
        ALGERIAN|fruit.orange|تشينة
        ALGERIAN|home.carpet|زربية
        ALGERIAN|home.water_bottle|قرعة ما
    """.trimIndent().lines().filter { it.isNotBlank() }

    private fun plain(text: String): String = text
        .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')
        .replace(Regex("[؟،؛:!()./\\u200F]"), "")
        .replace(Regex("\\s+"), " ").trim()

    // Only documented device-TTS spellings may differ from the visible label.
    private val phoneticAllowlist = setOf(
        "LIBYAN|request.water", "LIBYAN|request.medicine", "LIBYAN|food.water",
        "LIBYAN|action.sit", "LIBYAN|action.close", "LIBYAN|action.see",
        "LIBYAN|action.pray", "LIBYAN|clothing.socks", "LIBYAN|dish.potato_stew",
        "IRAQI|food.fish", "IRAQI|action.speak", "IRAQI|food.tea", "IRAQI|request.tea",
        "IRAQI|dish.iraqi.masgouf", "IRAQI|dish.iraqi.fish_mutabbaq",
        "EGYPTIAN|fruit.pear", "IRAQI|action.sit",
    )

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

    @Test fun allBuiltInCardsAvoidUnsupportedTtsConsonantGlyphs() {
        val problems = mutableListOf<String>()
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val spoken = DialectSpeechCatalog.resolve(seed, dialect).speechText
            if (spoken.any { it in "چپگڤ" }) problems += "$dialect|$key: $spoken"
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun allBuiltInCardLabelsMatchSpeechExceptDocumentedPhoneticSpellings() {
        val problems = mutableListOf<String>()
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            val visible = plain(form.displayText).replace("چ", "تش")
            val spoken = plain(form.speechText)
            val allow = "${dialect.name}|$key" in phoneticAllowlist
            if (visible != spoken && !allow)
                problems += "$dialect|$key: '${form.displayText}' vs '${form.speechText}'"
        }
        assertTrue("Unapproved display/audio differences (count=${problems.size}):\n" +
            problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun phonemicCueStartsWithTheSameSpokenSound() {
        val problems = mutableListOf<String>()
        for (dialect in ArabicDialect.entries) for ((key, seed) in seeds) {
            val form = DialectSpeechCatalog.resolve(seed, dialect)
            val cue = plain(PhonemicCue.forSpeech(form.phonemicCue))
            val spoken = plain(form.speechText)
            // The definite article is silent in prompts such as "الخضار"; cue derivation
            // deliberately starts at the following consonant.
            val firstSpeech = if (spoken.startsWith("ال") && spoken.length > 2) spoken[2] else spoken.firstOrNull()
            if (cue.isNotBlank() && firstSpeech != null && cue[0] != firstSpeech &&
                "${dialect.name}|$key" !in phoneticAllowlist)
                problems += "$dialect|$key: cue '${form.phonemicCue}', speech '${form.speechText}'"
        }
        assertTrue("Misleading first-syllable hints:\n" + problems.joinToString("\n"),
            problems.isEmpty())
    }

    @Test fun personalLabelAndSpeechOverridesAreNeverReplaced() {
        val sample = requireNotNull(seeds["family.son"]).copy(
            displayText = "اسم مخصص", ttsPrompt = "نُطْقِي الْخَاصّ",
            customTextFields = CardDetails.LABEL or CardDetails.SPEECH,
        )
        for (dialect in ArabicDialect.entries) {
            val form = DialectSpeechCatalog.resolve(sample, dialect)
            assertEquals(sample.displayText, form.displayText)
            assertEquals(sample.ttsPrompt, form.speechText)
        }
    }
}
