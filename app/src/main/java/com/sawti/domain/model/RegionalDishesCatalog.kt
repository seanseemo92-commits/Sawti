package com.sawti.domain.model

/**
 * Regional AAC vocabulary. All nine variants have separate, stable internal subgroups
 * inside the existing `libyan` category. Switching dialects only filters its projection:
 * previously recorded voices, photos, favourites and edited strings remain in Room.
 *
 * Pronunciations are curated guidance for Arabic TTS; engine/accent support varies by device.
 * A missing photograph uses an emoji placeholder rather than a remote image.
 */
object RegionalDishesCatalog {
    const val CATEGORY_ID = "libyan"
    private const val PREFIX = "regional_cuisine_"

    data class Dish(
        val displayText: String,
        val ttsPrompt: String,
        val phonemicCue: String,
        val sentencePrompt: String,
        val emoji: String,
        val photoPath: String? = null,
    )

    data class Cuisine(val title: String, val dishes: List<Dish>)

    private data class Entry(val display: String, val spoken: String, val cue: String, val emoji: String)
    private fun e(display: String, spoken: String, cue: String, emoji: String) =
        Entry(display, spoken, cue, emoji)

    private fun cuisine(title: String, prompt: String, vararg entries: Entry) = Cuisine(
        title,
        entries.map { Dish(it.display, it.spoken, it.cue, prompt, it.emoji) },
    )

    private val catalog: Map<ArabicDialect, Cuisine> = mapOf(
        ArabicDialect.LIBYAN to cuisine(
            "أكلات ليبية", "نَبِّي نَاكُلْ الـ...",
            e("مبطن", "إمْبَطَّنْ", "مْبَـ...", "🥔"),
            e("حرايمي", "حَرَايْمِي", "حَـ...", "🐟"),
            e("سفنز", "إِسْفَنْزْ", "إِسْـ...", "🍩"),
            e("زميطة", "زَمِّيطَهْ", "زَمْـ...", "🥣"),
            e("عيش باللبن", "عَيْشْ بِاللَّبَنْ", "عَيْـ...", "🥛"),
        ),
        ArabicDialect.TUNISIAN to cuisine(
            "أكلات تونسية", "نْحِبْ نَاكُلْ الـ...",
            e("لبلابي", "لَبْلَابِي", "لَبْـ...", "🥣"),
            e("بريك", "بْرِيكْ", "بْرِيـ...", "🥟"),
            e("كفتاجي", "كِفْتَاجِي", "كِفْـ...", "🥘"),
            e("كسكسي تونسي", "كُسْكُسِي تُونْسِي", "كُسْـ...", "🍲"),
            e("فريكاسي", "فْرِيكَاسِي", "فْرِيـ...", "🥪"),
        ),
        ArabicDialect.ALGERIAN to cuisine(
            "أكلات جزائرية", "نْحَبْ نَاكُلْ الـ...",
            e("شخشوخة", "شَخْشُوخَة", "شَخْـ...", "🥘"),
            e("رشتة", "رِشْتَة", "رِشْـ...", "🍜"),
            e("محاجب", "مْحَاجِبْ", "مْحَا...", "🫓"),
            e("طاجين زيتون", "طَاجِينْ زَيْتُونْ", "طَا...", "🫒"),
            e("كسكس جزائري", "كُسْكُسْ جَزَائِرِي", "كُسْـ...", "🍲"),
        ),
        ArabicDialect.MOROCCAN to cuisine(
            "أكلات مغربية", "بْغِيتْ نَاكُلْ الـ...",
            e("الحريرة", "الْحَرِيرَة", "حَرِيـ...", "🍲"),
            e("الرفيسة", "الرْفِيسَة", "رْفِيـ...", "🥘"),
            e("البسطيلة", "الْبَسْطِيلَة", "بَسْـ...", "🥧"),
            e("طاجين مغربي", "طَاجِينْ مَغْرِبِي", "طَا...", "🍛"),
            e("كسكس بالخضر", "كُسْكُسْ بِالْخْضَرْ", "كُسْـ...", "🥣"),
        ),
        ArabicDialect.EGYPTIAN to cuisine(
            "أكلات مصرية", "عَايِزْ آكُلْ الـ...",
            e("كشري", "كُشَرِي", "كُشْـ...", "🍚"),
            e("ملوخية", "مُلُوخِيَّة", "مُلُو...", "🥬"),
            e("محشي", "مَحْشِي", "مَحْـ...", "🥘"),
            e("فطير مشلتت", "فَطِيرْ مْشَلْتِتْ", "فَطِيـ...", "🫓"),
            e("طعمية", "طَعْمِيَّة", "طَعْـ...", "🧆"),
        ),
        ArabicDialect.SYRIAN to cuisine(
            "أكلات سورية", "بِدِّي آكُلْ الـ...",
            e("كبة", "كِبَّة", "كِبْـ...", "🥙"),
            e("يبرق", "يَبْرَقْ", "يَبْـ...", "🍃"),
            e("فتة شامية", "فَتَّة شَامِيَّة", "فَتْـ...", "🥣"),
            e("محاشي", "مَحَاشِي", "مَحَا...", "🥘"),
            e("فتوش", "فَتُّوشْ", "فَتْـ...", "🥗"),
        ),
        ArabicDialect.IRAQI to cuisine(
            "أكلات عراقية", "أْرِيدْ آكُلْ الـ...",
            e("مسگوف", "مَسْگُوفْ", "مَسْـ...", "🐟"),
            e("دولمة", "دُولْمَة", "دُولْـ...", "🍃"),
            e("تشريب", "تَشْرِيبْ", "تَشْـ...", "🥣"),
            e("كبة موصلية", "كِبَّة مَوْصِلِيَّة", "كِبْـ...", "🥙"),
            e("باجة", "بَاجَة", "بَا...", "🍲"),
        ),
        ArabicDialect.SAUDI to cuisine(
            "أكلات سعودية", "أَبْغَى آكُلْ الـ...",
            e("كبسة", "كَبْسَة", "كَبْـ...", "🍛"),
            e("جريش", "جَرِيشْ", "جَرِيـ...", "🥣"),
            e("قرصان", "قُرْصَانْ", "قُرْ...", "🫓"),
            e("مطازيز", "مَطَازِيزْ", "مَطَا...", "🥘"),
            e("سليق", "سَلِيقْ", "سَلِيـ...", "🍚"),
        ),
        ArabicDialect.STANDARD to cuisine(
            "أكلات عربية", "أُرِيدُ أَنْ آكُلَ الـ...",
            e("المنسف", "الْمَنْسَفْ", "مَنْـ...", "🍖"),
            e("المقلوبة", "الْمَقْلُوبَة", "مَقْـ...", "🍲"),
            e("المحاشي", "الْمَحَاشِي", "مَحَا...", "🥘"),
            e("الكُسْكُس", "الْكُسْكُسْ", "كُسْـ...", "🥣"),
            e("الكبسة", "الْكَبْسَة", "كَبْـ...", "🍛"),
        ),
    )

    fun forDialect(dialect: ArabicDialect): Cuisine = requireNotNull(catalog[dialect])
    fun subgroupFor(dialect: ArabicDialect): String = PREFIX + dialect.name

    /** Legacy Libyan entries use blank subgroup; never rewrite their IDs or media. */
    fun visibleForDialect(subgroup: String, dialect: ArabicDialect): Boolean =
        if (dialect == ArabicDialect.LIBYAN) {
            subgroup.isBlank() || subgroup == subgroupFor(ArabicDialect.LIBYAN)
        } else subgroup == subgroupFor(dialect)

    fun allDialectsCovered(): Boolean = catalog.keys == ArabicDialect.entries.toSet()

    /** Libyan dishes are part of WordSeed, to retain the historical blank subgroup. */
    fun additionalDialects(): List<Pair<ArabicDialect, Dish>> =
        ArabicDialect.entries.filter { it != ArabicDialect.LIBYAN }.flatMap { dialect ->
            forDialect(dialect).dishes.map { dialect to it }
        }
}
