package com.sawti.domain.model

/**
 * Exactly the 199 CODE_AND_DATA_CONFIRMED rows from the v1.6.1 audit CSV.
 * The other 89 findings require native/device listening and are NOT applied.
 *
 * Runtime-only projection: stable semantic IDs, Room data, personal caregiver
 * fields, audio/photo paths and pack cardinality remain unchanged.
 */
internal object Audit161Confirmed {
    data class Correction(val displayText: String, val speechText: String)
    private val raw = """
        STANDARD|clothing.high_heels|حذاء بكعب عالٍ|حِذَاءٌ بِكَعْبٍ عَالٍ
        EGYPTIAN|action.stand|أقوم|أَقُوم
        SYRIAN|family.husband|جوزي|جُوزِي
        IRAQI|action.close|أسدّ|أَسِدّ
        IRAQI|action.speak|أحچي|أَحْتْشِي
        IRAQI|dish.iraqi.fish_mutabbaq|مطبق سمچ|مُطَبَّق سِمَتْش
        IRAQI|dish.iraqi.masgouf|سمچ مسكوف|سِمَتْش مَسْكُوف
        IRAQI|food.fish|سمچ|سِمَتْش
        IRAQI|food.tea|چاي|تْشَاي
        IRAQI|request.tea|أريد چاي|أَرِيد تْشَاي
        MOROCCAN|dish.moroccan.couscous|كسكسو سبع خضار|كُسْكْسُو سْبَع خْضَار
        ALGERIAN|dish.algerian.douara|دوارة|دْوَارَة
        ALGERIAN|fruit.orange|تشينة|تْشِينَة
        STANDARD|clothing.pajamas|ملابس النوم|مَلَابِسُ النَّوْم
        STANDARD|clothing.tshirt|قميص قصير الأكمام|قَمِيصٌ قَصِيرُ الأَكْمَام
        STANDARD|clothing.watch|ساعة يد|سَاعَةُ يَد
        STANDARD|dish.arabic.vegetable_stew|يخنة خضار|يَخْنَةُ خُضَار
        STANDARD|family.brother_wife|زوجة أخي|زَوْجَةُ أَخِي
        STANDARD|family.son_wife|زوجة ابني|زَوْجَةُ ابْنِي
        STANDARD|food.biscuit|بسكويت|بَسْكَوِيت
        STANDARD|home.teapot|إبريق شاي|إِبْرِيقُ شَاي
        STANDARD|home.water_bottle|قارورة ماء|قَارُورَةُ مَاء
        STANDARD|routine.evening_coffee|قهوة المساء|قَهْوَةُ المَسَاء
        STANDARD|routine.prayer_rug|سجادة صلاة|سَجَّادَةُ صَلَاة
        STANDARD|transport.taxi|سيارة أجرة|سَيَّارَةُ أُجْرَة
        STANDARD|vegetable.cauliflower|قرنبيط|قَرْنَبِيط
        STANDARD|vegetable.lemon|ليمون|لَيْمُون
        LIBYAN|animal.camel|جمل|جْمَل
        LIBYAN|animal.chicken|دجاجة|دْجَاجَة
        LIBYAN|animal.horse|حصان|حْصَان
        LIBYAN|body.leg|رجل|رِجِل
        LIBYAN|clothing.socks|شخاشير|شْخَاشِير
        LIBYAN|dish.couscous_fish|كسكسي بالحوت|كُسْكُسِي بِالحُوت
        LIBYAN|dish.hraymi|حرايمي|حْرَايْمِي
        LIBYAN|dish.mbakbaka|مكرونة مبكبكة|مَكَرُونَة مْبَكْبَكَة
        LIBYAN|dish.pasta_masqiya|مكرونة مسقية|مَكَرُونَة مِسْقِيَّة
        LIBYAN|dish.potato_stew|طبيخة بطاطا|طْبِيخَة بَطَاطَا
        LIBYAN|dish.rice_blaw|رز بلاو|رُزّ بِلَاو
        LIBYAN|dish.sfinz|سفنز|إِسْفِنْز
        LIBYAN|family.female_in_law|نسيبتي|نَسِيبْتِي
        LIBYAN|family.female_relative|قريبتي|قَرِيبْتِي
        LIBYAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        LIBYAN|family.son_wife|مرت ولدي|مَرْت وِلْدِي
        LIBYAN|food.biscuit|بسكويت|بَسْكَوِيت
        LIBYAN|food.chicken|دجاج|دْجَاج
        LIBYAN|home.water_bottle|شيشة مية|شِيشَة مَيَّة
        LIBYAN|routine.walk|مشي|مَشِي
        SAUDI|action.pray|أصلّي|أَصَلِّي
        SAUDI|body.leg|رجل|رِجِل
        SAUDI|dish.saudi.bukhari|رز بخاري|رُزّ بُخَارِي
        SAUDI|family.female_in_law|نسيبتي|نَسِيبْتِي
        SAUDI|family.female_relative|قريبتي|قَرِيبْتِي
        SAUDI|family.granddaughter|حفيدتي|حَفِيدْتِي
        SAUDI|food.biscuit|بسكويت|بَسْكَوِيت
        SAUDI|greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيك
        SAUDI|routine.walk|مشي|مَشِي
        EGYPTIAN|action.pray|أصلّي|أَصَلِّي
        EGYPTIAN|family.female_in_law|نسيبتي|نَسِيبْتِي
        EGYPTIAN|family.female_relative|قريبتي|قَرِيبْتِي
        EGYPTIAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        EGYPTIAN|family.maternal_aunt|خالتي|خَالْتِي
        EGYPTIAN|food.biscuit|بسكويت|بَسْكَوِيت
        EGYPTIAN|greeting.bless|ربنا يبارك فيك|رَبِّنَا يْبَارِك فِيك
        EGYPTIAN|request.headache|دماغي بتوجعني|دِمَاغِي بِتُوجَعْنِي
        EGYPTIAN|routine.walk|مشي|مَشِي
        SYRIAN|body.leg|رجل|رِجِل
        SYRIAN|dish.syrian.haraq_osbao|حراق إصبعو|حَرَّاق إِصْبَعُو
        SYRIAN|family.female_in_law|نسيبتي|نَسِيبْتِي
        SYRIAN|family.female_relative|قريبتي|قَرِيبْتِي
        SYRIAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        SYRIAN|family.maternal_aunt|خالتي|خَالْتِي
        SYRIAN|family.paternal_aunt|عمتي|عَمْتِي
        SYRIAN|food.biscuit|بسكويت|بَسْكَوِيت
        SYRIAN|greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيك
        SYRIAN|routine.walk|مشي|مَشِي
        IRAQI|action.pray|أصلّي|أَصَلِّي
        IRAQI|body.leg|رجل|رِجِل
        IRAQI|family.female_in_law|نسيبتي|نَسِيبْتِي
        IRAQI|family.female_relative|قريبتي|قَرِيبْتِي
        IRAQI|family.granddaughter|حفيدتي|حَفِيدْتِي
        IRAQI|family.maternal_aunt|خالتي|خَالْتِي
        IRAQI|family.paternal_aunt|عمتي|عَمْتِي
        IRAQI|food.biscuit|بسكويت|بَسْكَوِيت
        IRAQI|greeting.bless|الله يبارك بيك|اللَّه يْبَارِك بِيك
        IRAQI|greeting.how_are_you|شلونك؟|شْلُونَك؟
        IRAQI|routine.walk|مشي|مَشِي
        TUNISIAN|dish.tunisian.couscous_fish|كسكسي بالحوت|كُسْكُسِي بِالحُوت
        TUNISIAN|family.brother_son|ولد خويا|وِلْد خُويَا
        TUNISIAN|family.female_in_law|نسيبتي|نْسِيبْتِي
        TUNISIAN|family.female_relative|قريبتي|قْرِيبْتِي
        TUNISIAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        TUNISIAN|family.male_in_law|نسيبي|نْسِيبِي
        TUNISIAN|family.male_relative|قريبي|قْرِيبِي
        TUNISIAN|family.maternal_aunt|خالتي|خَالْتِي
        TUNISIAN|family.maternal_aunt_son|ولد خالتي|وِلْد خَالْتِي
        TUNISIAN|family.maternal_uncle_son|ولد خالي|وِلْد خَالِي
        TUNISIAN|family.paternal_aunt|عمتي|عَمْتِي
        TUNISIAN|family.paternal_aunt_son|ولد عمتي|وِلْد عَمْتِي
        TUNISIAN|family.paternal_uncle_son|ولد عمي|وِلْد عَمِّي
        TUNISIAN|family.sister_son|ولد أختي|وِلْد أُخْتِي
        TUNISIAN|family.son_wife|مرت ولدي|مَرْت وِلْدِي
        TUNISIAN|food.biscuit|بسكويت|بَسْكَوِيت
        TUNISIAN|greeting.bless|ربي يبارك فيك|رَبِّي يْبَارِك فِيك
        TUNISIAN|greeting.how_are_you|شنوة أحوالك؟|شْنُوَّة أَحْوَالَك؟
        TUNISIAN|routine.walk|مشي|مَشِي
        MOROCCAN|action.close|نسد|نْسَدّ
        MOROCCAN|action.sit|نجلس|نِجْلِس
        MOROCCAN|dish.moroccan.baghrir|بغرير|بَغْرِير
        MOROCCAN|dish.moroccan.chebakia|شباكية|شَبَّاكِيَّة
        MOROCCAN|family.female_in_law|نسيبتي|نْسِيبْتِي
        MOROCCAN|family.female_relative|قريبتي|قْرِيبْتِي
        MOROCCAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        MOROCCAN|family.male_in_law|نسيبي|نْسِيبِي
        MOROCCAN|family.male_relative|قريبي|قْرِيبِي
        MOROCCAN|family.maternal_aunt|خالتي|خَالْتِي
        MOROCCAN|family.paternal_aunt|عمتي|عَمْتِي
        MOROCCAN|food.biscuit|بسكويت|بَسْكَوِيت
        MOROCCAN|greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيك
        MOROCCAN|greeting.thanks|شكراً بزاف|شُكْرًا بِزَّاف
        MOROCCAN|routine.walk|مشي|مْشِي
        ALGERIAN|family.female_in_law|نسيبتي|نْسِيبْتِي
        ALGERIAN|family.female_relative|قريبتي|قْرِيبْتِي
        ALGERIAN|family.granddaughter|حفيدتي|حَفِيدْتِي
        ALGERIAN|family.male_in_law|نسيبي|نْسِيبِي
        ALGERIAN|family.male_relative|قريبي|قْرِيبِي
        ALGERIAN|family.maternal_aunt|خالتي|خَالْتِي
        ALGERIAN|family.paternal_aunt|عمتي|عَمْتِي
        ALGERIAN|food.biscuit|بسكويت|بَسْكَوِيت
        ALGERIAN|greeting.bless|ربي يبارك فيك|رَبِّي يْبَارِك فِيك
        ALGERIAN|routine.walk|مشي|مْشِي
        STANDARD|dish.arabic.baked_bread|خبز مخبوز|خُبْز مَخْبُوز
        STANDARD|dish.arabic.cooked_fish|سمك مطهو|سَمَك مَطْهُوّ
        STANDARD|dish.arabic.fruits_legumes|ثمار وبقوليات|ثِمَار وَبُقُولِيَّات
        STANDARD|dish.arabic.grilled_chicken|دجاج مشوي|دَجَاج مَشْوِيّ
        STANDARD|request.medicine|أريد دوائي|أُرِيدُ دَوَائِي
        STANDARD|request.tea|أريد شايًا|أُرِيدُ شَايًا
        STANDARD|request.water|أريد ماءً|أُرِيدُ مَاءً
        LIBYAN|family.husband_mother|أم راجلي|أُمّ رَاجْلِي
        LIBYAN|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        SAUDI|family.husband_mother|أم زوجي|أُمّ زَوْجِي
        SAUDI|family.wife_mother|أم زوجتي|أُمّ زَوْجَتِي
        SAUDI|request.adjust_pillow|عدّل لي المخدة|عَدِّل لِي المِخَدَّة
        SAUDI|request.cold|أحس بالبرد|أَحِسّ بِالبَرْد
        SAUDI|request.medicine|أبغى دواي|أَبْغَى دَوَاي
        SAUDI|request.tea|أبغى شاي|أَبْغَى شَاي
        SAUDI|request.toilet|أبغى أروح الحمام|أَبْغَى أَرُوح الحَمَّام
        SAUDI|request.water|أبغى ماي|أَبْغَى مَاي
        EGYPTIAN|family.husband_mother|أم جوزي|أُمّ جُوزِي
        EGYPTIAN|family.wife_mother|أم مراتي|أُمّ مِرَاتِي
        EGYPTIAN|request.adjust_pillow|ظبطلي المخدة|ظَبَّطْلِي المَخَدَّة
        EGYPTIAN|request.cold|أنا سقعان|أَنَا سَقْعَان
        EGYPTIAN|request.medicine|ممكن دوايا|مُمْكِن دَوَايَا
        EGYPTIAN|request.rest|ممكن أرتاح شوية|مُمْكِن أَرْتَاح شْوَيَّة
        EGYPTIAN|request.tea|ممكن شاي|مُمْكِن شَاي
        EGYPTIAN|request.toilet|ممكن أدخل الحمام|مُمْكِن أَدْخُل الحَمَّام
        EGYPTIAN|request.water|عايز مية|عَايِز مَيَّة
        SYRIAN|family.husband_mother|أم جوزي|أُمّ جُوزِي
        SYRIAN|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        SYRIAN|request.adjust_pillow|زبط لي المخدة|زَبِّط لِي المِخَدَّة
        SYRIAN|request.cold|كتير برد|كْتِير بَرْد
        IRAQI|family.husband_mother|أم زوجي|أُمّ زُوجِي
        IRAQI|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        IRAQI|request.adjust_pillow|عدّل لي المخدة|عَدِّل لِي المْخَدَّة
        IRAQI|request.cold|أحس ببرد|أَحِسّ بِبَرْد
        IRAQI|request.medicine|أريد دواي|أَرِيد دَوَاي
        IRAQI|request.toilet|أريد أروح للحمام|أَرِيد أَرُوح لِلحَمَّام
        IRAQI|request.water|أريد ماي|أَرِيد مَاي
        TUNISIAN|dish.tunisian.grilled_salad|سلاطة مشوية|سَلَاطَة مَشْوِيَّة
        TUNISIAN|family.husband_mother|أم راجلي|أُمّ رَاجْلِي
        TUNISIAN|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        TUNISIAN|request.adjust_pillow|صلّح لي المخدة|صَلَّح لِي المِخَدَّة
        TUNISIAN|request.medicine|نحب دوايا|نْحِبّ دْوَايَا
        TUNISIAN|request.rest|نحب نرتاح|نْحِبّ نِرْتَاح
        TUNISIAN|request.tea|نحب تاي|نْحِبّ تَاي
        TUNISIAN|request.toilet|نحب نمشي للتواليت|نْحِبّ نِمْشِي لِلتْوَالِيت
        TUNISIAN|request.water|نحب شوية ما|نْحِبّ شْوَيَّة مَا
        MOROCCAN|dish.moroccan.kefta_tajine|طاجين كفتة بالبيض|طَاجِين كُفْتَة بِالبِيض
        MOROCCAN|family.husband_mother|أم راجلي|أُمّ رَاجْلِي
        MOROCCAN|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        MOROCCAN|request.adjust_pillow|صوّب ليا المخدة|صَوَّب لِيَّا المْخَدَّة
        MOROCCAN|request.cold|حسّيت بالبرد|حَسِّيت بِالبَرْد
        MOROCCAN|request.medicine|بغيت الدوا ديالي|بْغِيت الدْوَا دْيَالِي
        MOROCCAN|request.rest|بغيت نرتاح|بْغِيت نِرْتَاح
        MOROCCAN|request.tea|بغيت أتاي|بْغِيت أَتَاي
        MOROCCAN|request.toilet|بغيت نمشي للحمام|بْغِيت نَمْشِي لِلحَمَّام
        MOROCCAN|request.water|بغيت الما|بْغِيت المَا
        ALGERIAN|animal.cat|قطة|قِطَّة
        ALGERIAN|dish.algerian.chakhchoukha|شخشوخة بسكرية|شَخْشُوخَة بِسْكَرِيَّة
        ALGERIAN|dish.algerian.olive_tajine|طاجين الزيتون|طَاجِين الزِّيتُون
        ALGERIAN|dish.algerian.sweet_tajine|طاجين الحلو|طَاجِين الحْلُو
        ALGERIAN|family.husband_mother|أم راجلي|أُمّ رَاجْلِي
        ALGERIAN|family.wife_mother|أم مرتي|أُمّ مَرْتِي
        ALGERIAN|request.adjust_pillow|صلّحلي المخدة|صَلَّحْلِي المْخَدَّة
        ALGERIAN|request.cold|راني بردان|رَانِي بَرْدَان
        ALGERIAN|request.medicine|نحب دوايا|نْحَبّ دْوَايَا
        ALGERIAN|request.rest|نحب نرتاح|نْحَبّ نِرْتَاح
        ALGERIAN|request.tea|نحب أتاي|نْحَبّ أَتَاي
        ALGERIAN|request.toilet|بغيت نروح للتواليت|بْغِيت نْرُوح لِلتْوَالِيت
        ALGERIAN|request.water|نحب نشرب الما|نْحَبّ نَشْرَب المَا
    """.trimIndent()

    val corrections: Map<Pair<ArabicDialect, String>, Correction> by lazy {
        val forms = linkedMapOf<Pair<ArabicDialect, String>, Correction>()
        for (line in raw.lineSequence().filter { it.isNotBlank() }) {
            val parts = line.split('|', limit = 4)
            require(parts.size == 4) { "Malformed confirmed v1.6.1 audit row" }
            val dialect = ArabicDialect.valueOf(parts[0])
            val key = dialect to parts[1]
            check(forms.put(key, Correction(parts[2], parts[3])) == null) {
                "Duplicate confirmed v1.6.1 audit item: $key"
            }
        }
        check(forms.size == 199) { "Audit correction cardinality changed" }
        forms
    }

    fun overrideLabel(dialect: ArabicDialect, key: String, fallback: String): String =
        corrections[dialect to key]?.displayText ?: fallback

    fun apply(item: WordItem, dialect: ArabicDialect, form: SpeechForm): SpeechForm {
        if (form.source == SpeechSource.USER || item.categoryId.startsWith("custom_") ||
            item.customTextFields and (CardDetails.LABEL or CardDetails.SPEECH) != 0)
            return form
        val correction = corrections[dialect to (item.semanticKey ?: return form)] ?: return form
        return form.copy(displayText = correction.displayText, speechText = correction.speechText)
    }

    /** Repair the four audited hints that still quote an obsolete visible label. */
    fun reconcileSentence(original: WordItem, dialect: ArabicDialect, projected: WordItem): WordItem {
        if (original.customTextFields and CardDetails.SENTENCE != 0) return projected
        val key = original.semanticKey ?: return projected
        val needsRepair = when (dialect) {
            ArabicDialect.SYRIAN -> key == "request.adjust_pillow"
            ArabicDialect.TUNISIAN -> key == "request.adjust_pillow" || key == "request.toilet"
            ArabicDialect.ALGERIAN -> key == "request.toilet"
            else -> false
        }
        if (!needsRepair) return projected
        val prefix = "عِنْدَ الطَّلَبِ أَقُولُ: "
        val sentence = projected.sentencePrompt.orEmpty()
        if (!sentence.startsWith(prefix)) return projected
        return projected.copy(sentencePrompt = prefix + projected.displayText)
    }
}
