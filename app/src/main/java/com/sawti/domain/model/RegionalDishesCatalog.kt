package com.sawti.domain.model

/**
 * Regional AAC vocabulary. All nine variants have separate, stable internal subgroups
 * inside the `cuisine` category. Switching dialects only filters its projection:
 * previously recorded voices, photos and edited strings remain in Room.
 *
 * Speech uses clean display orthography; no historical phonetic spelling overrides.
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

    private data class Entry(val semanticKey: String, val display: String, val cue: String, val emoji: String)
    private fun e(semanticKey: String, display: String, cue: String, emoji: String) =
        Entry(semanticKey, display, cue, emoji)

    private fun cuisine(title: String, prompt: String, vararg entries: Entry) = Cuisine(
        title,
        entries.map { Dish(it.semanticKey, it.display, it.display, it.cue, prompt, it.emoji) },
    )

    private val catalog: Map<ArabicDialect, Cuisine> = mapOf(
        ArabicDialect.LIBYAN to cuisine(
            "أكلات ليبية", "نبي ناكل ال...",
            e("dish.mbattan", "مبطن", "مب...", "🥔"),
            e("dish.hraymi", "حرايمي", "ح...", "🐟"),
            e("dish.sfinz", "سفنز", "سف...", "🍩"),
            e("dish.zmeeta", "زميطة", "ز...", "🥣"),
            e("dish.aysh_laban", "عيش باللبن", "عي...", "🥛"),
        ),
        ArabicDialect.TUNISIAN to cuisine(
            "أكلات تونسية", "نحب ناكل ال...",
            e("dish.tunisian.couscous_fish", "كسكسي بالحوت", "كس...", "🐟"),
            e("dish.tunisian.lablabi", "لبلابي", "لب...", "🥣"),
            e("dish.tunisian.brik", "بريك", "بري...", "🥟"),
            e("dish.tunisian.kaftaji", "كفتاجي", "كف...", "🥘"),
            e("dish.tunisian.tajine", "طاجين تونسي", "طا...", "🍳"),
            e("dish.tunisian.plate", "صحن تونسي", "صح...", "🥗"),
            e("dish.tunisian.bird_tongue_soup", "شربة لسان عصفور", "شر...", "🍲"),
            e("dish.tunisian.molokhia", "ملوخية تونسية", "ملو...", "🥬"),
            e("dish.tunisian.pea_stew", "مرقة جلبانة", "مر...", "🫛"),
            e("dish.tunisian.pasta", "مقرونة بالصلصة", "مق...", "🍝"),
            e("dish.tunisian.merguez_omelette", "عجة بالمرقاز", "عج...", "🍳"),
            e("dish.tunisian.grilled_salad", "سلاطة مشوية", "سلا...", "🥗"),
            e("dish.tunisian.fricassee", "فريكاسي", "فري...", "🥪"),
            e("dish.tunisian.maqroudh", "مقروض قيرواني", "مق...", "🍪"),
            e("dish.tunisian.bambalouni", "بامبالوني", "بم...", "🍩"),
        ),
        ArabicDialect.ALGERIAN to cuisine(
            "أكلات جزائرية", "نحب ناكل ال...",
            e("dish.algerian.rechta", "رشتة", "رش...", "🍜"),
            e("dish.algerian.chakhchoukha", "شخشوخة بسكرية", "شخ...", "🥘"),
            e("dish.algerian.couscous", "طعام", "طعا...", "🍲"),
            e("dish.algerian.olive_tajine", "طاجين الزيتون", "زي...", "🫒"),
            e("dish.algerian.frik_soup", "شربة فريك", "شر...", "🍲"),
            e("dish.algerian.bourek", "بوراك", "بو...", "🥟"),
            e("dish.algerian.garantita", "كرانتيكا", "كر...", "🍮"),
            e("dish.algerian.sweet_tajine", "طاجين الحلو", "حلو...", "🥘"),
            e("dish.algerian.berkoukes", "بركوكس", "بر...", "🥣"),
            e("dish.algerian.potato_stew", "جواز بطاطا", "جوا...", "🥔"),
            e("dish.algerian.mahajeb", "محاجب", "محا...", "🫓"),
            e("dish.algerian.douara", "دوارة", "دوا...", "🍲"),
            e("dish.algerian.stuffed_artichoke", "قرنون معمر", "قر...", "🥘"),
            e("dish.algerian.kalb_el_louz", "قلب اللوز", "قل...", "🍰"),
            e("dish.algerian.makrout", "مقروط العسل", "مق...", "🍯"),
        ),
        ArabicDialect.MOROCCAN to cuisine(
            "أكلات مغربية", "بغيت ناكل ال...",
            e("dish.moroccan.prune_tajine", "طاجين بالبرقوق", "طا...", "🥘"),
            e("dish.moroccan.couscous", "كسكسو سبع خضار", "كس...", "🥣"),
            e("dish.moroccan.pastilla", "بسطيلة بالحوت", "بس...", "🥧"),
            e("dish.moroccan.harira", "حريرة مغربية", "حري...", "🍲"),
            e("dish.moroccan.tangia", "طنجية مراكشية", "طن...", "🥘"),
            e("dish.moroccan.rfissa", "رفيسة بالدجاج", "رفي...", "🍗"),
            e("dish.moroccan.zaalouk", "زعلوك", "زع...", "🍆"),
            e("dish.moroccan.taktouka", "تكتوكة", "تك...", "🥘"),
            e("dish.moroccan.kefta_tajine", "طاجين كفتة بالبيض", "كف...", "🍳"),
            e("dish.moroccan.skewers", "قطبان", "قط...", "🍢"),
            e("dish.moroccan.msemen", "مسمن", "مس...", "🫓"),
            e("dish.moroccan.baghrir", "بغرير", "بغ...", "🥞"),
            e("dish.moroccan.seffa", "سفة مدفونة", "سف...", "🍜"),
            e("dish.moroccan.chebakia", "شباكية", "شب...", "🍯"),
            e("dish.moroccan.sellou", "سلو", "سل...", "🥣"),
        ),
        ArabicDialect.EGYPTIAN to cuisine(
            "أكلات مصرية", "عايز آكل ال...",
            e("dish.egyptian.koshari", "كشري", "ك...", "🍚"),
            e("dish.egyptian.molokhia", "ملوخية بالتقلية", "م...", "🥬"),
            e("dish.egyptian.hawawshi", "حواوشي", "ح...", "🥙"),
            e("dish.egyptian.ful", "فول مدمس", "فو...", "🫘"),
            e("dish.egyptian.taamiya", "طعمية", "طع...", "🧆"),
            e("dish.egyptian.stuffed_pigeon", "حمام محشي", "ح...", "🍗"),
            e("dish.egyptian.fattah", "فتة بالخل والتوم", "فت...", "🍲"),
            e("dish.egyptian.potato_tray", "صينية بطاطس", "صي...", "🥔"),
            e("dish.egyptian.kebab_hawawshi", "كباب وحواوشي", "ك...", "🍖"),
            e("dish.egyptian.mahshi", "محشي مشكل", "مح...", "🥬"),
            e("dish.egyptian.moussaka", "مسقعة بلدي", "م...", "🍆"),
            e("dish.egyptian.trotters_fattah", "كوارع وفتة", "ك...", "🍲"),
            e("dish.egyptian.meat_ruqaq", "رقاق باللحمة", "ر...", "🫓"),
            e("dish.egyptian.bird_tongue_soup", "شربة لسان عصفور", "شو...", "🍜"),
            e("dish.egyptian.umm_ali", "أم علي", "أم...", "🍮"),
        ),
        ArabicDialect.SYRIAN to cuisine(
            "أكلات سورية", "بدي آكل ال...",
            e("dish.syrian.fried_kibbeh", "كبة مقلية", "كب...", "🥙"),
            e("dish.syrian.shakriya", "شاكرية برز", "شا...", "🥣"),
            e("dish.syrian.yabraq", "يبرق ورق عنب", "يب...", "🍃"),
            e("dish.syrian.chicken_shawarma", "شاورما دجاج", "شا...", "🥙"),
            e("dish.syrian.chickpea_fatteh", "فتة حمص", "فت...", "🫘"),
            e("dish.syrian.maqluba", "مقلوبة باذنجان", "مق...", "🍆"),
            e("dish.syrian.fattoush", "فتوش", "فت...", "🥗"),
            e("dish.syrian.tabbouleh", "تبولة", "تب...", "🥗"),
            e("dish.syrian.kebab", "كباب حلبي", "ك...", "🍢"),
            e("dish.syrian.shish_barak", "شيش برك", "شي...", "🥟"),
            e("dish.syrian.stuffed_zucchini", "كوسا بلبن", "كو...", "🥒"),
            e("dish.syrian.mutabbal", "متبل باذنجان", "مت...", "🍆"),
            e("dish.syrian.haraq_osbao", "حراق إصبعو", "ح...", "🥘"),
            e("dish.syrian.halawet_jibn", "حلاوة الجبن", "ح...", "🍮"),
            e("dish.syrian.kunafa", "كنافة نابلسية", "كنا...", "🍰"),
        ),
        ArabicDialect.IRAQI to cuisine(
            "أكلات عراقية", "أريد آكل ال...",
            e("dish.iraqi.dolma", "دولمة عراقية", "دول...", "🍃"),
            e("dish.iraqi.masgouf", "سمچ مسكوف", "س...", "🐟"),
            e("dish.iraqi.quzi", "قوزي عالتمن", "قو...", "🍛"),
            e("dish.iraqi.tashreeb", "تشريب لحم", "تش...", "🥣"),
            e("dish.iraqi.pacha", "باجة", "با...", "🍲"),
            e("dish.iraqi.rice_kibbeh", "كبة تمن", "كب...", "🥙"),
            e("dish.iraqi.mosul_kibbeh", "كبة موصلية", "كب...", "🥟"),
            e("dish.iraqi.biryani", "برياني عراقي", "بر...", "🍚"),
            e("dish.iraqi.fish_mutabbaq", "مطبق سمچ", "م...", "🐟"),
            e("dish.iraqi.makhlama", "فافير / مخلمة", "مخ...", "🍳"),
            e("dish.iraqi.okra", "باميا عالتمن", "با...", "🥘"),
            e("dish.iraqi.turnip", "شلغم مطبوخ", "شل...", "🥣"),
            e("dish.iraqi.harees", "هريسة", "ه...", "🥣"),
            e("dish.iraqi.kleicha", "كليجة", "كلي...", "🍪"),
            e("dish.iraqi.zalabiya", "زلابية", "ز...", "🍩"),
        ),
        ArabicDialect.SAUDI to cuisine(
            "أكلات سعودية", "أبغى آكل ال...",
            e("dish.saudi.kabsa", "كبسة لحم", "كب...", "🍛"),
            e("dish.saudi.mandi", "مندي", "من...", "🍗"),
            e("dish.saudi.jareesh", "جريش", "ج...", "🥣"),
            e("dish.saudi.qursan", "قرصان", "قر...", "🫓"),
            e("dish.saudi.saleeg", "سليق طائفي", "س...", "🍚"),
            e("dish.saudi.haneeth", "حنيذ", "ح...", "🍖"),
            e("dish.saudi.matazeez", "مطازيز", "م...", "🥘"),
            e("dish.saudi.marqooq", "مرقوق", "مر...", "🥣"),
            e("dish.saudi.madhbi", "مظبي دجاج", "م...", "🍗"),
            e("dish.saudi.bukhari", "رز بخاري", "ب...", "🍚"),
            e("dish.saudi.madfoon", "مدفون", "مد...", "🥘"),
            e("dish.saudi.liver", "كبدة حاشي", "ك...", "🍖"),
            e("dish.saudi.masoub", "معصوب", "مع...", "🍌"),
            e("dish.saudi.hanini", "حنيني", "ح...", "🍯"),
            e("dish.saudi.luqaimat", "لقيمات", "ل...", "🍩"),
        ),
        ArabicDialect.STANDARD to cuisine(
            "أكلات عربية", "أريد أن آكل ال...",
            e("dish.arabic.rice_meat", "أرز باللحم", "أ...", "🍚"),
            e("dish.arabic.grilled_chicken", "دجاج مشوي", "د...", "🍗"),
            e("dish.arabic.lentil_soup", "حساء العدس", "ح...", "🍲"),
            e("dish.arabic.vegetable_stew", "يخنة خضار", "ي...", "🥘"),
            e("dish.arabic.mixed_grill", "مشويات مشكلة", "مش...", "🍢"),
            e("dish.arabic.green_salad", "سلطة خضراء", "س...", "🥗"),
            e("dish.arabic.cooked_fish", "سمك مطهو", "س...", "🐟"),
            e("dish.arabic.baked_pastries", "فطائر مخبوزة", "ف...", "🥟"),
            e("dish.arabic.vegetable_soup", "حساء خضار", "ح...", "🍲"),
            e("dish.arabic.boiled_meat", "لحم مسلوق", "لح...", "🥩"),
            e("dish.arabic.baked_bread", "خبز مخبوز", "خب...", "🍞"),
            e("dish.arabic.fruits_legumes", "ثمار وبقوليات", "ث...", "🫘"),
            e("dish.arabic.boiled_eggs", "بيض مسلوق", "بي...", "🥚"),
            e("dish.arabic.dates_milk", "تمر وحليب", "تم...", "🥛"),
            e("dish.arabic.arabic_dessert", "حلوى عربية", "حل...", "🍰"),
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
