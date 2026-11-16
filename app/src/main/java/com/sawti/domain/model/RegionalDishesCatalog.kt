package com.sawti.domain.model

/**
 * Regional AAC vocabulary. All nine variants have separate, stable internal subgroups
 * inside the `cuisine` category. Switching dialects only filters its projection:
 * previously recorded voices, photos and edited strings remain in Room.
 *
 * Written speech candidates are preserved explicitly; none is certified by a native/device review.
 * A missing photograph uses an emoji placeholder rather than a remote image.
 */
object RegionalDishesCatalog {
    const val CATEGORY_ID = "cuisine"
    private const val PREFIX = "regional_cuisine_"

    data class Dish(
        val semanticKey: String,
        val displayText: String,
        val ttsPrompt: String,
        val phonemicCue: String,
        val sentencePrompt: String,
        val emoji: String,
        val photoPath: String? = null,
    )

    data class Cuisine(val title: String, val dishes: List<Dish>)

    private data class Entry(val semanticKey: String, val display: String, val spoken: String, val cue: String, val emoji: String)
    private fun e(semanticKey: String, display: String, spoken: String, cue: String, emoji: String) =
        Entry(semanticKey, display, spoken, cue, emoji)

    private fun cuisine(title: String, prompt: String, vararg entries: Entry) = Cuisine(
        title,
        entries.map { Dish(it.semanticKey, it.display, it.spoken, it.cue, prompt, it.emoji) },
    )

    private val catalog: Map<ArabicDialect, Cuisine> = mapOf(
        ArabicDialect.LIBYAN to cuisine(
            "أكلات ليبية", "نبي ناكل الـ...",
            e("dish.mbattan", "مبطن", "مبطن", "مبـ...", "🥔"),
            e("dish.hraymi", "حرايمي", "حرايمي", "حـ...", "🐟"),
            e("dish.sfinz", "سفنز", "سفنز", "سفـ...", "🍩"),
            e("dish.zmeeta", "زميطة", "زميطة", "زـ...", "🥣"),
            e("dish.aysh_laban", "عيش باللبن", "عيش باللبن", "عي...", "🥛"),
        ),
        ArabicDialect.TUNISIAN to cuisine(
            "أكلات تونسية", "نحب ناكل الـ...",
            e("dish.tunisian.couscous_fish", "كسكسي بالحوت", "كسكسي بالحوت", "كسـ...", "🐟"),
            e("dish.tunisian.lablabi", "لبلابي", "لبلابي", "لبـ...", "🥣"),
            e("dish.tunisian.brik", "بريك", "بريك", "بريـ...", "🥟"),
            e("dish.tunisian.kaftaji", "كفتاجي", "كفتاجي", "كفـ...", "🥘"),
            e("dish.tunisian.tajine", "طاجين تونسي", "طاجين تونسي", "طا...", "🍳"),
            e("dish.tunisian.plate", "صحن تونسي", "صحن تونسي", "صحـ...", "🥗"),
            e("dish.tunisian.bird_tongue_soup", "شربة لسان عصفور", "شربة لسان عصفور", "شر...", "🍲"),
            e("dish.tunisian.molokhia", "ملوخية تونسية", "ملوخية تونسية", "ملو...", "🥬"),
            e("dish.tunisian.pea_stew", "مرقة جلبانة", "مرقة جلبانة", "مر...", "🫛"),
            e("dish.tunisian.pasta", "مقرونة بالصلصة", "مقرونة بالصلصة", "مقـ...", "🍝"),
            e("dish.tunisian.merguez_omelette", "عجة بالمرقاز", "عجة بالمرقاز", "عجـ...", "🍳"),
            e("dish.tunisian.grilled_salad", "سلاطة مشوية", "سلاطة مشوية", "سلا...", "🥗"),
            e("dish.tunisian.fricassee", "فريكاسي", "فريكاسي", "فريـ...", "🥪"),
            e("dish.tunisian.maqroudh", "مقروض قيرواني", "مقروض قيرواني", "مقـ...", "🍪"),
            e("dish.tunisian.bambalouni", "بامبالوني", "بامبالوني", "بمـ...", "🍩"),
        ),
        ArabicDialect.ALGERIAN to cuisine(
            "أكلات جزائرية", "نحب ناكل الـ...",
            e("dish.algerian.rechta", "رشتة", "رشتة", "رشـ...", "🍜"),
            e("dish.algerian.chakhchoukha", "شخشوخة بسكرية", "شخشوخة بسكرية", "شخـ...", "🥘"),
            e("dish.algerian.couscous", "طعام", "طعام", "طعا...", "🍲"),
            e("dish.algerian.olive_tajine", "طاجين الزيتون", "طاجين الزيتون", "زيـ...", "🫒"),
            e("dish.algerian.frik_soup", "شربة فريك", "شربة فريك", "شر...", "🍲"),
            e("dish.algerian.bourek", "بوراك", "بوراك", "بو...", "🥟"),
            e("dish.algerian.garantita", "كرانتيكا", "كرانتيكا", "كر...", "🍮"),
            e("dish.algerian.sweet_tajine", "طاجين الحلو", "طاجين الحلو", "حلو...", "🥘"),
            e("dish.algerian.berkoukes", "بركوكس", "بركوكس", "بر...", "🥣"),
            e("dish.algerian.potato_stew", "جواز بطاطا", "جواز بطاطا", "جوا...", "🥔"),
            e("dish.algerian.mahajeb", "محاجب", "محاجب", "محا...", "🫓"),
            e("dish.algerian.douara", "دوارة", "دوارة", "دوا...", "🍲"),
            e("dish.algerian.stuffed_artichoke", "قرنون معمر", "قرنون معمر", "قر...", "🥘"),
            e("dish.algerian.kalb_el_louz", "قلب اللوز", "قلب اللوز", "قلـ...", "🍰"),
            e("dish.algerian.makrout", "مقروط العسل", "مقروط العسل", "مقـ...", "🍯"),
        ),
        ArabicDialect.MOROCCAN to cuisine(
            "أكلات مغربية", "بغيت ناكل الـ...",
            e("dish.moroccan.prune_tajine", "طاجين بالبرقوق", "طاجين بالبرقوق", "طا...", "🥘"),
            e("dish.moroccan.couscous", "كسكسو سبع خضار", "كسكسو سبع خضار", "كسـ...", "🥣"),
            e("dish.moroccan.pastilla", "بسطيلة بالحوت", "بسطيلة بالحوت", "بسـ...", "🥧"),
            e("dish.moroccan.harira", "حريرة مغربية", "حريرة مغربية", "حريـ...", "🍲"),
            e("dish.moroccan.tangia", "طنجية مراكشية", "طنجية مراكشية", "طنـ...", "🥘"),
            e("dish.moroccan.rfissa", "رفيسة بالدجاج", "رفيسة بالدجاج", "رفيـ...", "🍗"),
            e("dish.moroccan.zaalouk", "زعلوك", "زعلوك", "زعـ...", "🍆"),
            e("dish.moroccan.taktouka", "تكتوكة", "تكتوكة", "تكـ...", "🥘"),
            e("dish.moroccan.kefta_tajine", "طاجين كفتة بالبيض", "طاجين كفتة بالبيض", "كفـ...", "🍳"),
            e("dish.moroccan.skewers", "قطبان", "قطبان", "قطـ...", "🍢"),
            e("dish.moroccan.msemen", "مسمن", "مسمن", "مسـ...", "🫓"),
            e("dish.moroccan.baghrir", "بغرير", "بغرير", "بغـ...", "🥞"),
            e("dish.moroccan.seffa", "سفة مدفونة", "سفة مدفونة", "سفـ...", "🍜"),
            e("dish.moroccan.chebakia", "شباكية", "شباكية", "شبـ...", "🍯"),
            e("dish.moroccan.sellou", "سلو", "سلو", "سلـ...", "🥣"),
        ),
        ArabicDialect.EGYPTIAN to cuisine(
            "أكلات مصرية", "عايز آكل الـ...",
            e("dish.egyptian.koshari", "كشري", "كشري", "كـ...", "🍚"),
            e("dish.egyptian.molokhia", "ملوخية بالتقلية", "ملوخية بالتقلية", "مـ...", "🥬"),
            e("dish.egyptian.hawawshi", "حواوشي", "حواوشي", "حـ...", "🥙"),
            e("dish.egyptian.ful", "فول مدمس", "فول مدمس", "فو...", "🫘"),
            e("dish.egyptian.taamiya", "طعمية", "طعمية", "طعـ...", "🧆"),
            e("dish.egyptian.stuffed_pigeon", "حمام محشي", "حمام محشي", "حـ...", "🍗"),
            e("dish.egyptian.fattah", "فتة بالخل والتوم", "فتة بالخل والتوم", "فتـ...", "🍲"),
            e("dish.egyptian.potato_tray", "صينية بطاطس", "صينية بطاطس", "صيـ...", "🥔"),
            e("dish.egyptian.kebab_hawawshi", "كباب وحواوشي", "كباب وحواوشي", "كـ...", "🍖"),
            e("dish.egyptian.mahshi", "محشي مشكل", "محشي مشكل", "محـ...", "🥬"),
            e("dish.egyptian.moussaka", "مسقعة بلدي", "مسقعة بلدي", "مـ...", "🍆"),
            e("dish.egyptian.trotters_fattah", "كوارع وفتة", "كوارع وفتة", "كـ...", "🍲"),
            e("dish.egyptian.meat_ruqaq", "رقاق باللحمة", "رقاق باللحمة", "ر...", "🫓"),
            e("dish.egyptian.bird_tongue_soup", "شربة لسان عصفور", "شوربة لسان عصفور", "شو...", "🍜"),
            e("dish.egyptian.umm_ali", "أم علي", "أم علي", "أم...", "🍮"),
        ),
        ArabicDialect.SYRIAN to cuisine(
            "أكلات سورية", "بدي آكل الـ...",
            e("dish.syrian.fried_kibbeh", "كبة مقلية", "كبة مقلية", "كبـ...", "🥙"),
            e("dish.syrian.shakriya", "شاكرية برز", "شاكرية برز", "شا...", "🥣"),
            e("dish.syrian.yabraq", "يبرق ورق عنب", "يبرق ورق عنب", "يبـ...", "🍃"),
            e("dish.syrian.chicken_shawarma", "شاورما دجاج", "شاورما دجاج", "شا...", "🥙"),
            e("dish.syrian.chickpea_fatteh", "فتة حمص", "فتة حمص", "فتـ...", "🫘"),
            e("dish.syrian.maqluba", "مقلوبة باذنجان", "مقلوبة باذنجان", "مقـ...", "🍆"),
            e("dish.syrian.fattoush", "فتوش", "فتوش", "فتـ...", "🥗"),
            e("dish.syrian.tabbouleh", "تبولة", "تبولة", "تبـ...", "🥗"),
            e("dish.syrian.kebab", "كباب حلبي", "كباب حلبي", "كـ...", "🍢"),
            e("dish.syrian.shish_barak", "شيش برك", "شيش برك", "شيـ...", "🥟"),
            e("dish.syrian.stuffed_zucchini", "كوسا بلبن", "كوسا بلبن", "كو...", "🥒"),
            e("dish.syrian.mutabbal", "متبل باذنجان", "متبل باذنجان", "متـ...", "🍆"),
            e("dish.syrian.haraq_osbao", "حراق إصبعو", "حراق أصبعو", "حـ...", "🥘"),
            e("dish.syrian.halawet_jibn", "حلاوة الجبن", "حلاوة الجبن", "حـ...", "🍮"),
            e("dish.syrian.kunafa", "كنافة نابلسية", "كنافة نابلسية", "كنا...", "🍰"),
        ),
        ArabicDialect.IRAQI to cuisine(
            "أكلات عراقية", "أريد آكل الـ...",
            e("dish.iraqi.dolma", "دولمة عراقية", "دولمة عراقية", "دولـ...", "🍃"),
            e("dish.iraqi.masgouf", "سمچ مسكوف", "سمتش مسكوف", "سـ...", "🐟"),
            e("dish.iraqi.quzi", "قوزي عالتمن", "قوزي عالتمن", "قو...", "🍛"),
            e("dish.iraqi.tashreeb", "تشريب لحم", "تشريب لحم", "تشـ...", "🥣"),
            e("dish.iraqi.pacha", "باجة", "باجة", "با...", "🍲"),
            e("dish.iraqi.rice_kibbeh", "كبة تمن", "كبة تمن", "كبـ...", "🥙"),
            e("dish.iraqi.mosul_kibbeh", "كبة موصلية", "كبة موصلية", "كبـ...", "🥟"),
            e("dish.iraqi.biryani", "برياني عراقي", "برياني عراقي", "بر...", "🍚"),
            e("dish.iraqi.fish_mutabbaq", "مطبق سمچ", "مطبق سمتش", "مـ...", "🐟"),
            e("dish.iraqi.makhlama", "فافير / مخلمة", "مخلمة", "مخـ...", "🍳"),
            e("dish.iraqi.okra", "باميا عالتمن", "باميا عالتمن", "با...", "🥘"),
            e("dish.iraqi.turnip", "شلغم مطبوخ", "شلغم مطبوخ", "شلـ...", "🥣"),
            e("dish.iraqi.harees", "هريسة", "هريسة", "هـ...", "🥣"),
            e("dish.iraqi.kleicha", "كليجة", "كليجة", "كليـ...", "🍪"),
            e("dish.iraqi.zalabiya", "زلابية", "زلابية", "ز...", "🍩"),
        ),
        ArabicDialect.SAUDI to cuisine(
            "أكلات سعودية", "أبغى آكل الـ...",
            e("dish.saudi.kabsa", "كبسة لحم", "كبسة لحم", "كبـ...", "🍛"),
            e("dish.saudi.mandi", "مندي", "مندي", "منـ...", "🍗"),
            e("dish.saudi.jareesh", "جريش", "جريش", "جـ...", "🥣"),
            e("dish.saudi.qursan", "قرصان", "قرصان", "قر...", "🫓"),
            e("dish.saudi.saleeg", "سليق طائفي", "سليق طائفي", "سـ...", "🍚"),
            e("dish.saudi.haneeth", "حنيذ", "حنيذ", "حـ...", "🍖"),
            e("dish.saudi.matazeez", "مطازيز", "مطازيز", "مـ...", "🥘"),
            e("dish.saudi.marqooq", "مرقوق", "مرقوق", "مر...", "🥣"),
            e("dish.saudi.madhbi", "مظبي دجاج", "مظبي دجاج", "مـ...", "🍗"),
            e("dish.saudi.bukhari", "رز بخاري", "رز بخاري", "بـ...", "🍚"),
            e("dish.saudi.madfoon", "مدفون", "مدفون", "مد...", "🥘"),
            e("dish.saudi.liver", "كبدة حاشي", "كبدة حاشي", "كـ...", "🍖"),
            e("dish.saudi.masoub", "معصوب", "معصوب", "معـ...", "🍌"),
            e("dish.saudi.hanini", "حنيني", "حنيني", "حـ...", "🍯"),
            e("dish.saudi.luqaimat", "لقيمات", "لقيمات", "لـ...", "🍩"),
        ),
        ArabicDialect.STANDARD to cuisine(
            "أكلات عربية", "أريد أن آكل الـ...",
            e("dish.arabic.rice_meat", "أرز باللحم", "أرز باللحم", "أ...", "🍚"),
            e("dish.arabic.grilled_chicken", "دجاج مشوي", "دجاج مشوي", "د...", "🍗"),
            e("dish.arabic.lentil_soup", "حساء العدس", "حساء العدس", "حـ...", "🍲"),
            e("dish.arabic.vegetable_stew", "يخنة خضار", "يخنة خضار", "يـ...", "🥘"),
            e("dish.arabic.mixed_grill", "مشويات مشكلة", "مشويات مشكلة", "مشـ...", "🍢"),
            e("dish.arabic.green_salad", "سلطة خضراء", "سلطة خضراء", "سـ...", "🥗"),
            e("dish.arabic.cooked_fish", "سمك مطهو", "سمك مطهو", "سـ...", "🐟"),
            e("dish.arabic.baked_pastries", "فطائر مخبوزة", "فطائر مخبوزة", "فـ...", "🥟"),
            e("dish.arabic.vegetable_soup", "حساء خضار", "حساء خضار", "حـ...", "🍲"),
            e("dish.arabic.boiled_meat", "لحم مسلوق", "لحم مسلوق", "لحـ...", "🥩"),
            e("dish.arabic.baked_bread", "خبز مخبوز", "خبز مخبوز", "خبـ...", "🍞"),
            e("dish.arabic.fruits_legumes", "ثمار وبقوليات", "ثمار وبقوليات", "ثـ...", "🫘"),
            e("dish.arabic.boiled_eggs", "بيض مسلوق", "بيض مسلوق", "بيـ...", "🥚"),
            e("dish.arabic.dates_milk", "تمر وحليب", "تمر وحليب", "تمـ...", "🥛"),
            e("dish.arabic.arabic_dessert", "حلوى عربية", "حلوى عربية", "حلـ...", "🍰"),
        ),
    )

    fun forDialect(dialect: ArabicDialect): Cuisine = requireNotNull(catalog[dialect])
    fun subgroupFor(dialect: ArabicDialect): String = PREFIX + dialect.name

    fun visibleForDialect(subgroup: String, dialect: ArabicDialect): Boolean =
        subgroup == subgroupFor(dialect)

    fun variant(semanticKey: String, dialect: ArabicDialect): Dish? =
        forDialect(dialect).dishes.firstOrNull { it.semanticKey == semanticKey }

    fun allDialectsCovered(): Boolean = catalog.keys == ArabicDialect.entries.toSet()

    fun additionalDialects(): List<Pair<ArabicDialect, Dish>> =
        ArabicDialect.entries.filter { it != ArabicDialect.LIBYAN }.flatMap { dialect ->
            forDialect(dialect).dishes.map { dialect to it }
        }
}
