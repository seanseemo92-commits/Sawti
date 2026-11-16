package com.sawti.domain.model

/**
 * Generated verbatim from scripts/neural_v162_overrides.csv (2,399 rows).
 * DO NOT normalize Unicode, remove diacritics or edit the strings by hand.
 * This is the single built-in dialect display / neural speech table.
 */
internal object NeuralLexicon162 {
    data class Entry(val displayText: String, val speechText: String)

    private fun parse(rows: String): Map<String, Entry> = rows.trimIndent()
        .lineSequence().filter { it.isNotBlank() }.associate { line ->
            val fields = line.split('|', limit = 3)
            check(fields.size == 3) { "Invalid v1.6.2 neural row" }
            fields[0] to Entry(fields[1], fields[2])
        }

    private val entries: Map<ArabicDialect, Map<String, Entry>> by lazy {
        mapOf(
            ArabicDialect.STANDARD to parse("""
                family.father|أبي|أَبِي
                family.mother|أمي|أُمِّي
                family.husband|زوجي|زَوْجِي
                family.wife|زوجتي|زَوْجَتِي
                family.son|ابني|اِبْنِي
                family.daughter|ابنتي|اِبْنَتِي
                family.brother|أخي|أَخِي
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمَّتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالَتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدَتِي
                family.paternal_uncle_son|ابن عمي|اِبْنُ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتُ عَمِّي
                family.paternal_aunt_son|ابن عمتي|اِبْنُ عَمَّتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتُ عَمَّتِي
                family.maternal_uncle_son|ابن خالي|اِبْنُ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتُ خَالِي
                family.maternal_aunt_son|ابن خالتي|اِبْنُ خَالَتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتُ خَالَتِي
                family.brother_son|ابن أخي|اِبْنُ أَخِي
                family.brother_daughter|بنت أخي|بِنْتُ أَخِي
                family.sister_son|ابن أختي|اِبْنُ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتُ أُخْتِي
                family.husband_father|أبو زوجي|أَبُو زَوْجِي
                family.husband_mother|أم زوجي|أُمُّ زَوْجِي
                family.wife_father|أبو زوجتي|أَبُو زَوْجَتِي
                family.wife_mother|أم زوجتي|أُمُّ زَوْجَتِي
                family.husband_brother|أخو زوجي|أَخُو زَوْجِي
                family.husband_sister|أخت زوجي|أُخْتُ زَوْجِي
                family.wife_brother|أخو زوجتي|أَخُو زَوْجَتِي
                family.wife_sister|أخت زوجتي|أُخْتُ زَوْجَتِي
                family.sister_husband|زوج أختي|زَوْجُ أُخْتِي
                family.brother_wife|زوجة أخي|زَوْجَةُ أَخِي
                family.daughter_husband|زوج ابنتي|زَوْجُ ابْنَتِي
                family.son_wife|زوجة ابني|زَوْجَةُ ابْنِي
                family.male_in_law|نسيبي|نَسِيبِي
                family.female_in_law|نسيبتي|نَسِيبَتِي
                family.male_relative|قريبي|قَرِيبِي
                family.female_relative|قريبتي|قَرِيبَتِي
                request.water|أريد ماءً|أُرِيدُ مَاءً
                request.tea|أريد شايًا|أُرِيدُ شَايًا
                request.rest|أريد أن أرتاح|أُرِيدُ أَنْ أَرْتَاحَ
                request.medicine|أريد دوائي|أُرِيدُ دَوَائِي
                request.toilet|أريد الذهاب إلى الحمام|أُرِيدُ الذَّهَابَ إِلَى الْحَمَّامِ
                request.adjust_pillow|عدّل لي الوسادة|عَدِّلْ لِيَ الْوِسَادَةَ
                request.headache|رأسي يؤلمني|رَأْسِي يُؤْلِمُنِي
                request.cold|أشعر بالبرد|أَشْعُرُ بِالْبَرْدِ
                dish.arabic.rice_meat|أرز باللحم|أَرُزٌّ بِاللَّحْمِ
                dish.arabic.grilled_chicken|دجاج مشوي|دَجَاج مَشْوِيّ
                dish.arabic.lentil_soup|حساء العدس|حَسَاءُ الْعَدَسِ
                dish.arabic.vegetable_stew|يخنة خضار|يَخْنَةُ خُضَار
                dish.arabic.mixed_grill|مشويات مشكلة|مَشْوِيَّاتٌ مُشَكَّلَةٌ
                dish.arabic.green_salad|سلطة خضراء|سَلَطَةٌ خَضْرَاءُ
                dish.arabic.cooked_fish|سمك مطهو|سَمَك مَطْهُوّ
                dish.arabic.baked_pastries|فطائر مخبوزة|فَطَائِرُ مَخْبُوزَةٌ
                dish.arabic.vegetable_soup|حساء خضار|حَسَاءُ خُضَارٍ
                dish.arabic.boiled_meat|لحم مسلوق|لَحْمٌ مَسْلُوقٌ
                dish.arabic.baked_bread|خبز مخبوز|خُبْز مَخْبُوز
                dish.arabic.fruits_legumes|ثمار وبقوليات|ثِمَار وَبُقُولِيَّات
                dish.arabic.boiled_eggs|بيض مسلوق|بَيْضٌ مَسْلُوقٌ
                dish.arabic.dates_milk|تمر وحليب|تَمْرٌ وَحَلِيبٌ
                dish.arabic.arabic_dessert|حلوى عربية|حَلْوَى عَرَبِيَّةٌ
                routine.call_to_prayer|أذان|أَذَانٌ
                routine.ablution|وضوء|وُضُوءٌ
                routine.prayer|صلاة|صَلَاةٌ
                routine.prayer_rug|سجادة صلاة|سَجَّادَةُ صَلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَةٌ
                routine.quran|مصحف|مُصْحَفٌ
                routine.evening_coffee|قهوة المساء|قَهْوَةُ المَسَاء
                routine.sleep|نوم|نَوْمٌ
                routine.rest|راحة|رَاحَةٌ
                routine.walk|مشي|مَشْيٌ
                vegetable.tomato|طماطم|طَمَاطِمُ
                vegetable.potato|بطاطس|بَطَاطِسُ
                vegetable.onion|بصل|بَصَلٌ
                vegetable.garlic|ثوم|ثُومٌ
                vegetable.carrot|جزر|جَزَرٌ
                vegetable.cucumber|خيار|خِيَارٌ
                vegetable.pepper|فلفل|فُلْفُلٌ
                vegetable.eggplant|باذنجان|بَاذِنْجَانٌ
                vegetable.zucchini|كوسة|كُوسَةٌ
                vegetable.lettuce|خس|خَسٌّ
                vegetable.parsley|بقدونس|بَقْدُونِسٌ
                vegetable.coriander|كزبرة|كُزْبُرَةٌ
                vegetable.lemon|ليمون|لَيْمُون
                vegetable.peas|بازلاء|بَازِلَّاءُ
                vegetable.green_beans|فاصوليا خضراء|فَاصُولِيَا خَضْرَاءُ
                vegetable.spinach|سبانخ|سَبَانِخُ
                vegetable.cauliflower|قرنبيط|قَرْنَبِيط
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فُجْلٌ
                vegetable.pumpkin|يقطين|يَقْطِينٌ
                vegetable.mint|نعناع|نَعْنَاعٌ
                vegetable.corn|ذرة|ذُرَةٌ
                fruit.apple|تفاح|تُفَّاحٌ
                fruit.banana|موز|مَوْزٌ
                fruit.orange|برتقال|بُرْتُقَالٌ
                fruit.grapes|عنب|عِنَبٌ
                fruit.strawberry|فراولة|فَرَاوِلَةٌ
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|بطيخ|بِطِّيخٌ
                fruit.melon|شمام|شَمَّامٌ
                fruit.peach|خوخ|خَوْخٌ
                fruit.apricot|مشمش|مِشْمِشٌ
                fruit.pear|كمثرى|كُمَّثْرَى
                fruit.pomegranate|رمان|رُمَّانٌ
                fruit.figs|تين|تِينٌ
                fruit.dates|تمر|تَمْرٌ
                fruit.pineapple|أناناس|أَنَانَاسُ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزٌ
                fruit.berries|توت|تُوتٌ
                fruit.mandarin|يوسفي|يُوسُفِيٌّ
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جَوْزُ الْهِنْدِ
                food.bread|خبز|خُبْزٌ
                food.milk|حليب|حَلِيبٌ
                food.eggs|بيض|بَيْضٌ
                food.cheese|جبن|جُبْنٌ
                food.yogurt|زبادي|زَبَادِي
                food.butter|زبدة|زُبْدَةٌ
                food.rice|أرز|أَرُزٌّ
                food.pasta|معكرونة|مَعْكَرُونَةٌ
                food.soup|حساء|حَسَاءٌ
                food.meat|لحم|لَحْمٌ
                food.chicken|دجاج|دَجَاجٌ
                food.fish|سمك|سَمَكٌ
                food.olives|زيتون|زَيْتُونٌ
                food.oil|زيت|زَيْتٌ
                food.tea|شاي|شَايٌ
                food.coffee|قهوة|قَهْوَةٌ
                food.juice|عصير|عَصِيرٌ
                food.water|ماء|مَاءٌ
                food.honey|عسل|عَسَلٌ
                food.biscuit|بسكويت|بَسْكَوِيت
                food.salt|ملح|مِلْحٌ
                food.sugar|سكر|سُكَّرٌ
                home.bed|سرير|سَرِيرٌ
                home.pillow|وسادة|وِسَادَةٌ
                home.blanket|بطانية|بَطَّانِيَّةٌ
                home.table|طاولة|طَاوِلَةٌ
                home.chair|كرسي|كُرْسِيٌّ
                home.sofa|أريكة|أَرِيكَةٌ
                home.door|باب|بَابٌ
                home.window|نافذة|نَافِذَةٌ
                home.television|تلفزيون|تِلْفِزْيُونٌ
                home.fridge|ثلاجة|ثَلَّاجَةٌ
                home.washing_machine|غسالة|غَسَّالَةٌ
                home.stove|موقد|مَوْقِدٌ
                home.oven|فرن|فُرْنٌ
                home.air_conditioner|مكيف|مُكَيِّفٌ
                home.fan|مروحة|مِرْوَحَةٌ
                home.wardrobe|خزانة|خِزَانَةٌ
                home.key|مفتاح|مِفْتَاحٌ
                home.clock|ساعة|سَاعَةٌ
                home.carpet|بساط|بِسَاطٌ
                home.kitchen|مطبخ|مَطْبَخٌ
                home.cup|كوب|كُوبٌ
                home.tray|صينية|صِينِيَّةٌ
                home.spoon|ملعقة|مِلْعَقَةٌ
                home.fork|شوكة|شَوْكَةٌ
                home.knife|سكين|سِكِّينٌ
                home.teapot|إبريق شاي|إِبْرِيقُ شَاي
                home.pot|طنجرة|طَنْجَرَةٌ
                home.water_bottle|قارورة ماء|قَارُورَةُ مَاء
                clothing.tshirt|قميص قصير الأكمام|قَمِيصٌ قَصِيرُ الأَكْمَام
                clothing.shirt|قميص|قَمِيصٌ
                clothing.jacket|سترة|سُتْرَةٌ
                clothing.shorts|سروال قصير|سِرْوَالٌ قَصِيرٌ
                clothing.blouse|بلوزة|بُلُوزَةٌ
                clothing.trousers|سروال|سِرْوَالٌ
                clothing.dress|فستان|فُسْتَانٌ
                clothing.robe|رداء|رِدَاءٌ
                clothing.abaya|عباءة|عَبَاءَةٌ
                clothing.hijab|حجاب|حِجَابٌ
                clothing.scarf|وشاح|وِشَاحٌ
                clothing.shoes|حذاء|حِذَاءٌ
                clothing.socks|جوارب|جَوَارِبُ
                clothing.coat|معطف|مِعْطَفٌ
                clothing.glasses|نظارات|نَظَّارَاتٌ
                clothing.cap|قبعة|قُبَّعَةٌ
                clothing.watch|ساعة يد|سَاعَةُ يَد
                clothing.bag|حقيبة|حَقِيبَةٌ
                clothing.sandals|صندل|صَنْدَلٌ
                clothing.high_heels|حذاء بكعب عالٍ|حِذَاءٌ بِكَعْبٍ عَالٍ
                clothing.pajamas|ملابس النوم|مَلَابِسُ النَّوْم
                clothing.belt|حزام|حِزَامٌ
                clothing.ring|خاتم|خَاتَمٌ
                body.eye|عين|عَيْنٌ
                body.nose|أنف|أَنْفٌ
                body.mouth|فم|فَمٌ
                body.tongue|لسان|لِسَانٌ
                body.ear|أذن|أُذُنٌ
                body.hand|يد|يَدٌ
                body.leg|ساق|سَاقٌ
                body.head|رأس|رَأْسٌ
                body.hair|شعر|شَعْرٌ
                body.back|ظهر|ظَهْرٌ
                body.abdomen|بطن|بَطْنٌ
                body.knee|ركبة|رُكْبَةٌ
                body.finger|إصبع|إِصْبَعٌ
                body.neck|رقبة|رَقَبَةٌ
                body.chest|صدر|صَدْرٌ
                body.face|وجه|وَجْهٌ
                body.tooth|سن|سِنٌّ
                body.heart|قلب|قَلْبٌ
                body.shoulder|كتف|كَتِفٌ
                action.eat|آكل|آكُلُ
                action.drink|أشرب|أَشْرَبُ
                action.sleep|أنام|أَنَامُ
                action.sit|أجلس|أَجْلِسُ
                action.walk|أمشي|أَمْشِي
                action.stand|أقف|أَقِفُ
                action.wash|أغسل|أَغْسِلُ
                action.speak|أتكلم|أَتَكَلَّمُ
                action.hear|أسمع|أَسْمَعُ
                action.open|أفتح|أَفْتَحُ
                action.close|أغلق|أُغْلِقُ
                action.read|أقرأ|أَقْرَأُ
                action.laugh|أضحك|أَضْحَكُ
                action.see|أرى|أَرَى
                action.dress|ألبس|أَلْبَسُ
                action.pray|أصلّي|أُصَلِّي
                animal.cat|قطة|قِطَّةٌ
                animal.dog|كلب|كَلْبٌ
                animal.sheep|خروف|خَرُوفٌ
                animal.horse|حصان|حِصَانٌ
                animal.cow|بقرة|بَقَرَةٌ
                animal.sparrow|عصفور|عُصْفُورٌ
                animal.pigeon|حمامة|حَمَامَةٌ
                animal.chicken|دجاجة|دَجَاجَةٌ
                animal.duck|بطة|بَطَّةٌ
                animal.camel|جمل|جَمَلٌ
                animal.rooster|ديك|دِيكٌ
                animal.rabbit|أرنب|أَرْنَبٌ
                transport.car|سيارة|سَيَّارَةٌ
                transport.taxi|سيارة أجرة|سَيَّارَةُ أُجْرَة
                transport.bus|حافلة|حَافِلَةٌ
                transport.minibus|حافلة صغيرة|حَافِلَةٌ صَغِيرَةٌ
                transport.bicycle|دراجة هوائية|دَرَّاجَةٌ هَوَائِيَّةٌ
                transport.motorcycle|دراجة نارية|دَرَّاجَةٌ نَارِيَّةٌ
                transport.train|قطار|قِطَارٌ
                transport.airplane|طائرة|طَائِرَةٌ
                transport.helicopter|مروحية|مِرْوَحِيَّةٌ
                transport.ship|سفينة|سَفِينَةٌ
                transport.boat|قارب|قَارِبٌ
                transport.truck|شاحنة|شَاحِنَةٌ
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامُ
                greeting.hello|مرحبًا|مَرْحَبًا
                greeting.welcome|أهلًا وسهلًا|أَهْلًا وَسَهْلًا
                greeting.how_are_you|كيف حالك؟|كَيْفَ حَالُكَ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهِ
                greeting.good_morning|صباح الخير|صَبَاحُ الْخَيْرِ
                greeting.good_evening|مساء الخير|مَسَاءُ الْخَيْرِ
                greeting.brighten|نورت|نَوَّرْتَ
                greeting.good_health|يعطيك العافية|يُعْطِيكَ الْعَافِيَةَ
                greeting.goodbye|مع السلامة|مَعَ السَّلَامَةِ
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهِ
                greeting.thanks|شكرًا لك|شُكْرًا لَكَ
                greeting.bless|بارك الله فيك|بَارَكَ اللَّهُ فِيكَ
            """),
            ArabicDialect.LIBYAN to parse("""
                family.father|بوي|بُويْ
                family.mother|أمي|أُمِّي
                family.husband|راجلي|رَاجْلِي
                family.wife|مرتي|مَرْتِي
                family.son|ولدي|وِلْدِي
                family.daughter|بنتي|بِنْتِي
                family.brother|خوي|خُويْ
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جِدِّي
                family.grandmother|حناي|حَنَّايْ
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وِلْدْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وِلْدْ عَمْتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتِي
                family.maternal_uncle_son|ولد خالي|وِلْدْ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وِلْدْ خَالْتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتِي
                family.brother_son|ولد خوي|وِلْدْ خُويْ
                family.brother_daughter|بنت خوي|بِنْتْ خُويْ
                family.sister_son|ولد أختي|وِلْدْ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|بو راجلي|بُو رَاجْلِي
                family.husband_mother|أم راجلي|أُمْ رَاجْلِي
                family.wife_father|بو مرتي|بُو مَرْتِي
                family.wife_mother|أم مرتي|أُمْ مَرْتِي
                family.husband_brother|خو راجلي|خُو رَاجْلِي
                family.husband_sister|أخت راجلي|أُخْتْ رَاجْلِي
                family.wife_brother|خو مرتي|خُو مَرْتِي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتِي
                family.sister_husband|راجل أختي|رَاجِلْ أُخْتِي
                family.brother_wife|مرت خوي|مَرْتْ خُويْ
                family.daughter_husband|راجل بنتي|رَاجِلْ بِنْتِي
                family.son_wife|مرت ولدي|مَرْت وِلْدِي
                family.male_in_law|نسيبي|نَسِيبِي
                family.female_in_law|نسيبتي|نَسِيبْتِي
                family.male_relative|قريبي|گَرِيبِي
                family.female_relative|قريبتي|قَرِيبْتِي
                request.water|نبي مية|نِبِّي إِمَّيَّهْ
                request.tea|نبي شاهي|نِبِّي شَاهِي
                request.rest|نبي نرتاح|نِبِّي نِرْتَاحْ
                request.medicine|نبي دوايا|نِبِّي دْوَايَا
                request.toilet|نبي الحمام|نِبِّي الْحَمَّامْ
                request.adjust_pillow|عدّل لي المخدة|عَدِّلْ لِي الْمِخَدَّة
                request.headache|راسي يوجع فيا|رَاسِي يُوجَعْ فِيَّا
                request.cold|نحس بالبرد|نِحِسّ بِالْبَرْدْ
                dish.bazin|بازين|بَازِينْ
                dish.reshda_kaskas|رشدة كسكاس|رِشْدِة كِسْكَاسْ
                dish.reshda_borma|رشدة برمة|رِشْدِة بُرْمَة
                dish.mbakbaka|مكرونة مبكبكة|مَكْرُونَة مْبَكْبَكَة
                dish.pasta_masqiya|مكرونة مسقية|مَكْرُونَة مِسْگِيَّة
                dish.couscous_fish|كسكسي بالحوت|كُسْكُسِي بِالحُوتْ
                dish.rice_blaw|رز بلاو|رُزّ بِلَاو
                dish.libyan_soup|شربة ليبية|شَرْبَة لِيبِيَّة
                dish.potato_stew|طبيخة بطاطا|طْبِيخَة بَطَاطَا
                dish.osban|عصبان|عُصْبَانْ
                dish.bourek|بوريك|بُورِيكْ
                dish.khobz_tajin|خبزة طاجين|خُبْزِة طَاجِينْ
                dish.maqroudh|مقروض|مَگْرُوضْ
                dish.mint_tea|شاهي بالنعناع|شَاهِي بِالنَّعْنَاعْ
                dish.arabic_coffee|قهوة عربية|گَهْوَة عَرَبِيَّة
                dish.mbattan|مبطن|مْبَطَّنْ
                dish.hraymi|حرايمي|حْرَايْمِي
                dish.sfinz|سفنز|إِسْفِنْزْ
                dish.zmeeta|زميطة|زُمِيطَة
                dish.aysh_laban|عيش باللبن|عِيشْ بِاللَّبَنْ
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صَلَاة
                routine.prayer_rug|سجادة|سَجَّادَة
                routine.prayer_beads|سبحة|سِبْحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العشية|گَهْوِة الْعَشِيَّة
                routine.sleep|نوم|نُومْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|طماطم|طَمَاطِمْ
                vegetable.potato|بطاطا|بَطَاطَا
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|تُومْ
                vegetable.carrot|جزر|جَزَرْ
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجَانْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|معدنوس|مَعْدَنُوسْ
                vegetable.coriander|قصبر|گُصْبُرْ
                vegetable.lemon|ليمون|لِيمُونْ
                vegetable.peas|بازيلية|بَازِيلْيَا
                vegetable.green_beans|فاصوليا|فَاصُولِيَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|قرنبيط|گَرْنَبِيطْ
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرعة|گَرْعَة
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|برتقال|بُرْتُقَالْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|دلاع|دَلَّاعْ
                fruit.melon|قلعاوي|گَلْعَاوِي
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|كمثرى|كُمِّثْرَى
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|كرموس|كَرْمُوسْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|مندرين|مَنْدَرِينْ
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبزة|خُبْزَة
                food.milk|حليب|حَلِيبْ
                food.eggs|بيض|بِيضْ
                food.cheese|جبنة|جِبْنَة
                food.yogurt|زبادي|زَبَادِي
                food.butter|زبدة|زُبْدَة
                food.rice|رز|رُزّ
                food.pasta|مكرونة|مَكْرُونَة
                food.soup|شوربة|شُورْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دْجَاجْ
                food.fish|حوت|حُوتْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|شاهي|شَاهِي
                food.coffee|قهوة|گَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|مية|إِمَّيَّهْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مِخَدَّة
                home.blanket|ورغان|وِرْغَانْ
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|صالون|صَالُونْ
                home.door|باب|بَابْ
                home.window|روشن|رُوشِنْ
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|غاز|غَازْ
                home.oven|كوشة|كُوشَة
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|دولاب|دُولَابْ
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|بساط|بِسَاطْ
                home.kitchen|كوجينة|كُوجِينَة
                home.cup|طاسة|طَاسَة
                home.tray|صونية|صُونِيَّة
                home.spoon|كاشيك|كَاشِيكْ
                home.fork|شوكة|شُوكَة
                home.knife|موس|مُوسْ
                home.teapot|براد|بَرَّادْ
                home.pot|طنجرة|طَنْجْرَة
                home.water_bottle|شيشة مية|شِيشَة إِمَّيَّهْ
                clothing.tshirt|تيشرت|تِيشِرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|جاكة|جَاكَة
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|سورية|سُورِيَّة
                clothing.trousers|سروال|سِرْوَالْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|كندرة|كَنْدْرَة
                clothing.socks|شخاشير|شْخَاشِيرْ
                clothing.coat|كبوط|كَبُّوطْ
                clothing.glasses|نظارة|نَظَّارَة
                clothing.cap|طاقية|طَاگِيَّة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|شنطة|شَنْطَة
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|كندرة كعب|كَنْدْرِة كَعَبْ
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|سبتة|سِبْتَة
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|خشم|خَشِمْ
                body.mouth|فم|فَمْ
                body.tongue|لسان|لِسَانْ
                body.ear|وذن|وِذِنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجِلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|صبع|صْبَعْ
                body.neck|رقبة|رَگْبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجِهْ
                body.tooth|سنة|سِنَّة
                body.heart|قلب|گَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|ناكل|نَاكُلْ
                action.drink|نشرب|نِشْرَبْ
                action.sleep|نرقد|نُرْگُدْ
                action.sit|نقعمز|نْگَعْمِزْ
                action.walk|نمشي|نِمْشِي
                action.stand|نوقف|نُوگَفْ
                action.wash|نغسل|نِغْسِلْ
                action.speak|ندوي|نِدْوِي
                action.hear|نسمع|نِسْمَعْ
                action.open|نفتح|نِفْتَحْ
                action.close|نسكر|نِسَكِّرْ
                action.read|نقرا|نَگْرَا
                action.laugh|نضحك|نِضْحَكْ
                action.see|نشوف|نِشُوفْ
                action.dress|نلبس|نِلْبِسْ
                action.pray|نصلي|نِصَلِّي
                animal.cat|قطوسة|گَطُّوسَة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَرُوفْ
                animal.horse|حصان|حْصَانْ
                animal.cow|بقرة|بَگْرَة
                animal.sparrow|عصفور|عُصْفُورْ
                animal.pigeon|حمامة|حْمَامَة
                animal.chicken|دجاجة|دْجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جْمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|سيارة|سَيَّارَة
                transport.taxi|تاكسي|تَاكْسِي
                transport.bus|باص|بَاصْ
                transport.minibus|ميكروباص|مِيكْرُوبَاصْ
                transport.bicycle|دراجة|دَرَّاجَة
                transport.motorcycle|دراجة نارية|دَرَّاجَة نَارِيَّة
                transport.train|قطار|قِطَارْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|باخرة|بَاخِرَة
                transport.boat|قارب|قَارِبْ
                transport.truck|شاحنة|شَاحِنَة
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|هلا بيك|هَلَا بِيكْ
                greeting.welcome|أهلا وسهلا|أَهْلًا وَسَهْلًا
                greeting.how_are_you|شن حالك؟|شِنْ حَالَكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك الصحة|يَعْطِيكْ الصَّحَّة
                greeting.goodbye|مع السلامة|مَعَ السَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|شكراً ليك|شُكْرًا لِيكْ
                greeting.bless|بارك الله فيك|بَارَكَ اللَّهُ فِيكْ
            """),
            ArabicDialect.SAUDI to parse("""
                family.father|أبوي|أَبُويْ
                family.mother|أمي|أُمِّي
                family.husband|زوجي|زَوْجِي
                family.wife|زوجتي|زَوْجِتِي
                family.son|ولدي|وَلَدِي
                family.daughter|بنتي|بِنْتِي
                family.brother|أخوي|أَخُويْ
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمَّتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالَتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وَلَدْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وَلَدْ عَمَّتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمَّتِي
                family.maternal_uncle_son|ولد خالي|وَلَدْ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وَلَدْ خَالَتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالَتِي
                family.brother_son|ولد أخوي|وَلَدْ أَخُويْ
                family.brother_daughter|بنت أخوي|بِنْتْ أَخُويْ
                family.sister_son|ولد أختي|وَلَدْ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|أبو زوجي|أَبُو زَوْجِي
                family.husband_mother|أم زوجي|أُمْ زَوْجِي
                family.wife_father|أبو زوجتي|أَبُو زَوْجَتِي
                family.wife_mother|أم زوجتي|أُمْ زَوْجَتِي
                family.husband_brother|أخو زوجي|أَخُو زَوْجِي
                family.husband_sister|أخت زوجي|أُخْتْ زَوْجِي
                family.wife_brother|أخو زوجتي|أَخُو زَوْجَتِي
                family.wife_sister|أخت زوجتي|أُخْتْ زَوْجَتِي
                family.sister_husband|زوج أختي|زَوْجْ أُخْتِي
                family.brother_wife|زوجة أخوي|زَوْجِة أَخُويْ
                family.daughter_husband|زوج بنتي|زَوْجْ بِنْتِي
                family.son_wife|زوجة ولدي|زَوْجِة وَلَدِي
                family.male_in_law|نسيبي|نَسِيبِي
                family.female_in_law|نسيبتي|نَسِيبْتِي
                family.male_relative|قريبي|گَرِيبِي
                family.female_relative|قريبتي|قَرِيبْتِي
                request.water|أبغى ماي|أَبْغَى مَايْ
                request.tea|أبغى شاي|أَبْغَى شَايْ
                request.rest|أبغى أرتاح|أَبْغَى أَرْتَاحْ
                request.medicine|أبغى دواي|أَبْغَى دَوَايْ
                request.toilet|أبغى أروح الحمام|أَبْغَى أَرُوحْ الْحَمَّامْ
                request.adjust_pillow|عدّل لي المخدة|عَدِّلْ لِي الْمِخَدَّة
                request.headache|راسي يعورني|رَاسِي يْعَوِّرْنِي
                request.cold|أحس بالبرد|أَحِسْ بِالْبَرْدْ
                dish.saudi.kabsa|كبسة لحم|كَبْسِة لَحَمْ
                dish.saudi.mandi|مندي|مَنْدِي
                dish.saudi.jareesh|جريش|جِرِيشْ
                dish.saudi.qursan|قرصان|گُرْصَانْ
                dish.saudi.saleeg|سليق طائفي|سَلِيگْ طَايْفِي
                dish.saudi.haneeth|حنيذ|حَنِيذْ
                dish.saudi.matazeez|مطازيز|مَطَازِيزْ
                dish.saudi.marqooq|مرقوق|مَرْگُوگْ
                dish.saudi.madhbi|مظبي دجاج|مَظْبِي دَجَاجْ
                dish.saudi.bukhari|رز بخاري|رُزّ بُخَارِي
                dish.saudi.madfoon|مدفون|مَدْفُونْ
                dish.saudi.liver|كبدة حاشي|كِبْدِة حَاشِي
                dish.saudi.masoub|معصوب|مَعْصُوبْ
                dish.saudi.hanini|حنيني|حَنِينِي
                dish.saudi.luqaimat|لقيمات|لْگَيْمَاتْ
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صَلَاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صَلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العصر|گَهْوِة الْعَصْرْ
                routine.sleep|نوم|نُومْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|طماطم|طَمَاطِمْ
                vegetable.potato|بطاطس|بَطَاطِسْ
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|ثُومْ
                vegetable.carrot|جزر|جَزَرْ
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجَانْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|بقدونس|بَقْدُونِسْ
                vegetable.coriander|كزبرة|كُزْبَرَة
                vegetable.lemon|ليمون|لِيمُونْ
                vegetable.peas|بازلا|بَازِلَّا
                vegetable.green_beans|فاصوليا خضرا|فَاصُولْيَا خَضْرَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|زهرة|زَهْرَة
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرع|قَرَعْ
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|برتقال|بُرْتُقَالْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|حبحب|حَبْحَبْ
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|كمثرى|كُمَّثْرَى
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|تين|تِينْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|يوسفي|يُوسُفِي
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبْزْ
                food.milk|حليب|حَلِيبْ
                food.eggs|بيض|بِيضْ
                food.cheese|جبن|جُبْنْ
                food.yogurt|زبادي|زَبَادِي
                food.butter|زبدة|زُبْدَة
                food.rice|رز|رُزّ
                food.pasta|مكرونة|مَكَرُونَة
                food.soup|شوربة|شُورْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دَجَاجْ
                food.fish|سمك|سِمَكْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|شاهي|شَاهِي
                food.coffee|قهوة|گَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|ماي|مَايْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مَخَدَّة
                home.blanket|بطانية|بَطَّانِيَّة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|كنب|كَنَبْ
                home.door|باب|بَابْ
                home.window|شباك|شُبَّاكْ
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|فرن غاز|فُرْنْ غَازْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|دولاب|دُولَابْ
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|بساط|بِسَاطْ
                home.kitchen|مطبخ|مَطْبَخْ
                home.cup|كوب|كُوبْ
                home.tray|صينية|صِينِيَّة
                home.spoon|ملعقة|مِلْعَقَة
                home.fork|شوكة|شُوكَة
                home.knife|سكين|سِكِّينْ
                home.teapot|إبريق شاي|إِبْرِيقْ شَايْ
                home.pot|قدر|گِدْرْ
                home.water_bottle|قارورة موية|قَارُورِة مُويَة
                clothing.tshirt|تيشيرت|تِيشِيرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|جاكيت|جَاكِيتْ
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلُوزَة
                clothing.trousers|بنطلون|بَنْطَلُونْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|جزمة|جَزْمَة
                clothing.socks|دلاغات|دِلَاغَاتْ
                clothing.coat|معطف|مِعْطَفْ
                clothing.glasses|نظارة|نَظَّارَة
                clothing.cap|طاقية|طَاقِيَّة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|شنطة|شَنْطَة
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|جزمة كعب|جَزْمِة كَعَبْ
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|حزام|حِزَامْ
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|خشم|خَشِمْ
                body.mouth|فم|فَمْ
                body.tongue|لسان|لِسَانْ
                body.ear|إذن|إِذْنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجِلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|إصبع|إِصْبَعْ
                body.neck|رقبة|رْگَبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجْهْ
                body.tooth|سن|سِنّ
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|آكل|آكُلْ
                action.drink|أشرب|أَشْرَبْ
                action.sleep|أنام|أَنَامْ
                action.sit|أجلس|أَجْلِسْ
                action.walk|أمشي|أَمْشِي
                action.stand|أقوم|أَگُومْ
                action.wash|أغسل|أَغْسِلْ
                action.speak|أتكلم|أَتَكَلَّمْ
                action.hear|أسمع|أَسْمَعْ
                action.open|أفتح|أَفْتَحْ
                action.close|أسكر|أَسَكِّرْ
                action.read|أقرا|أَگْرَا
                action.laugh|أضحك|أَضْحَكْ
                action.see|أشوف|أَشُوفْ
                action.dress|ألبس|أَلْبَسْ
                action.pray|أصلّي|أَصَلِّي
                animal.cat|بسة|بِسَّة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَرُوفْ
                animal.horse|حصان|حِصَانْ
                animal.cow|بقرة|بَگْرَة
                animal.sparrow|عصفور|عُصْفُورْ
                animal.pigeon|حمامة|حَمَامَة
                animal.chicken|دجاجة|دَجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|سيارة|سَيَّارَة
                transport.taxi|تاكسي|تَاكْسِي
                transport.bus|باص|بَاصْ
                transport.minibus|باص صغير|بَاصْ صْغِيرْ
                transport.bicycle|سيكل|سَيْكَلْ
                transport.motorcycle|دباب|دَبَّابْ
                transport.train|قطار|قِطَارْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|سفينة|سَفِينَة
                transport.boat|قارب|قَارِبْ
                transport.truck|شاحنة|شَاحِنَة
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|هلا|هَلَا
                greeting.welcome|حيّاك الله|حَيَّاكْ اللَّهْ
                greeting.how_are_you|كيف حالك؟|كِيفْ حَالَكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك العافية|يَعْطِيكْ الْعَافْيَة
                greeting.goodbye|مع السلامة|مَعَ السَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|شكراً لك|شُكْرًا لَكْ
                greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيكْ
            """),
            ArabicDialect.EGYPTIAN to parse("""
                family.father|أبويا|أَبويا
                family.mother|أمي|أُمِّي
                family.husband|جوزي|جُوزي
                family.wife|مراتي|مِراتي
                family.son|ابني|اِبْني
                family.daughter|بنتي|بِنْتي
                family.brother|أخويا|أَخويا
                family.sister|أختي|أُخْتي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمِّتِي
                family.maternal_uncle|خالي|خَالي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتي
                family.grandson|حفيدي|حَفيدي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ابن عمي|اِبْنْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ابن عمتي|اِبْنْ عَمِّتي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمِّتي
                family.maternal_uncle_son|ابن خالي|اِبْنْ خَالي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالي
                family.maternal_aunt_son|ابن خالتي|اِبْنْ خَالْتي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتي
                family.brother_son|ابن أخويا|اِبْنْ أَخويا
                family.brother_daughter|بنت أخويا|بِنْتْ أَخويا
                family.sister_son|ابن أختي|اِبْنْ أُخْتي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتي
                family.husband_father|أبو جوزي|أَبو جُوزي
                family.husband_mother|أم جوزي|أُمْ جُوزي
                family.wife_father|أبو مراتي|أَبو مِراتي
                family.wife_mother|أم مراتي|أُمْ مِراتي
                family.husband_brother|أخو جوزي|أَخو جُوزي
                family.husband_sister|أخت جوزي|أُخْتْ جُوزي
                family.wife_brother|أخو مراتي|أَخو مِراتي
                family.wife_sister|أخت مراتي|أُخْتْ مِراتي
                family.sister_husband|جوز أختي|جُوزْ أُخْتي
                family.brother_wife|مرات أخويا|مِراتْ أَخويا
                family.daughter_husband|جوز بنتي|جُوزْ بِنْتي
                family.son_wife|مرات ابني|مِراتْ اِبْني
                family.male_in_law|نسيبي|نَسيبي
                family.female_in_law|نسيبتي|نَسِيبْتِي
                family.male_relative|قريبي|قَريبي
                family.female_relative|قريبتي|قَرِيبْتِي
                request.water|عايز مية|عَايِزْ مَيَّة
                request.tea|ممكن شاي|مُمْكِنْ شَايْ
                request.rest|ممكن أرتاح شوية|مُمْكِنْ أَرْتاحْ شْوَيَّة
                request.medicine|ممكن دوايا|مُمْكِنْ دَوايا
                request.toilet|ممكن أدخل الحمام|مُمْكِنْ أَدْخُلْ الْحَمَّامْ
                request.adjust_pillow|ظبطلي المخدة|ظَبَّطْلي الْمَخَدَّة
                request.headache|دماغي بتوجعني|دِمَاغِي بِتُوجَعْنِي
                request.cold|أنا سقعان|أَنا سَقْعانْ
                dish.egyptian.koshari|كشري|كُشَري
                dish.egyptian.molokhia|ملوخية بالتقلية|مُلوخِيَّة بِالتَّقْلِيَة
                dish.egyptian.hawawshi|حواوشي|حَواوْشي
                dish.egyptian.ful|فول مدمس|فُولْ مِدَمِّسْ
                dish.egyptian.taamiya|طعمية|طَعْمِيَّة
                dish.egyptian.stuffed_pigeon|حمام محشي|حَمامْ مَحْشي
                dish.egyptian.fattah|فتة بالخل والتوم|فَتَّة بِالْخَلّ وَالتُّومْ
                dish.egyptian.potato_tray|صينية بطاطس|صِينِيِّة بَطاطِسْ
                dish.egyptian.kebab_hawawshi|كباب وحواوشي|كَبابْ وَحَواوْشي
                dish.egyptian.mahshi|محشي مشكل|مَحْشِي مِشَكِّلْ
                dish.egyptian.moussaka|مسقعة بلدي|مِسَقَّعَة بَلَدِي
                dish.egyptian.trotters_fattah|كوارع وفتة|كَوارِعْ وَفَتَّة
                dish.egyptian.meat_ruqaq|رقاق باللحمة|رُقاقْ بِاللَّحْمَة
                dish.egyptian.bird_tongue_soup|شربة لسان عصفور|شُورْبِة لِسَانْ عَصْفُورْ
                dish.egyptian.umm_ali|أم علي|أُمّ عَلي
                routine.call_to_prayer|أذان|أَذانْ
                routine.ablution|وضوء|وُضوءْ
                routine.prayer|صلاة|صَلاة
                routine.prayer_rug|سجادة صلاة|سِجَّادِة صَلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة المسا|قَهْوِة الْمَسَا
                routine.sleep|نوم|نُومْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|طماطم|طَماطِمْ
                vegetable.potato|بطاطس|بَطاطِسْ
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|توم|تُومْ
                vegetable.carrot|جزر|جَزَرْ
                vegetable.cucumber|خيار|خِيارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|بتنجان|بِتِنْجانْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|بقدونس|بَقْدونِسْ
                vegetable.coriander|كزبرة|كُزْبَرَة
                vegetable.lemon|لمون|لَمونْ
                vegetable.peas|بسلة|بِسِلَّة
                vegetable.green_beans|فاصوليا خضرا|فَاصولْيا خَضْرا
                vegetable.spinach|سبانخ|سَبانِخْ
                vegetable.cauliflower|قرنبيط|قَرْنَبيطْ
                vegetable.broccoli|بروكلي|بُروكْلي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرع|قَرَعْ
                vegetable.mint|نعناع|نَعْناعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|برتقان|بُرْتُقانْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَراوْلَة
                fruit.mango|مانجو|مَانْجو
                fruit.watermelon|بطيخ|بَطِّيخْ
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|كمثرى|كُمِّتْرَى
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|تين|تِينْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَناناسْ
                fruit.kiwi|كيوي|كِيويْ
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|يوسفي|يُوسَفي
                fruit.avocado|أفوكادو|أَفوكادو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|عيش|عِيشْ
                food.milk|لبن|لَبَنْ
                food.eggs|بيض|بِيضْ
                food.cheese|جبنة|جِبْنَة
                food.yogurt|زبادي|زَبادي
                food.butter|زبدة|زُبْدَة
                food.rice|رز|رُزّ
                food.pasta|مكرونة|مَكَرونَة
                food.soup|شوربة|شُورْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|فراخ|فِراخْ
                food.fish|سمك|سَمَكْ
                food.olives|زيتون|زِيتونْ
                food.oil|زيت|زِيتْ
                food.tea|شاي|شَايْ
                food.coffee|قهوة|قَهْوَة
                food.juice|عصير|عَصيرْ
                food.water|مية|مَيَّة
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَريرْ
                home.pillow|مخدة|مَخَدَّة
                home.blanket|بطانية|بَطَّانِيَّة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسي
                home.sofa|كنبة|كَنَبَة
                home.door|باب|بَابْ
                home.window|شباك|شُبَّاكْ
                home.television|تلفزيون|تِلِفِزْيونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|بوتاجاز|بُوتاجازْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|دولاب|دُولابْ
                home.key|مفتاح|مِفْتاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|سجادة|سِجَّادَة
                home.kitchen|مطبخ|مَطْبَخْ
                home.cup|كوباية|كُوبَّايَة
                home.tray|صينية|صِينِيَّة
                home.spoon|معلقة|مَعْلَقَة
                home.fork|شوكة|شُوكَة
                home.knife|سكينة|سِكِّينَة
                home.teapot|براد|بَرَّادْ
                home.pot|حلة|حَلَّة
                home.water_bottle|إزازة مية|إِزَازِة مَيَّة
                clothing.tshirt|تيشيرت|تِيشيرْتْ
                clothing.shirt|قميص|قَميصْ
                clothing.jacket|جاكيت|جَاكيتْ
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلوزَة
                clothing.trousers|بنطلون|بَنْطَلونْ
                clothing.dress|فستان|فُسْتانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبايَة
                clothing.hijab|حجاب|حِجابْ
                clothing.scarf|وشاح|وِشاحْ
                clothing.shoes|جزمة|جَزْمَة
                clothing.socks|شرابات|شَراباتْ
                clothing.coat|بالطو|بَالْطو
                clothing.glasses|نضارة|نَضَّارَة
                clothing.cap|كاب|كَابْ
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|شنطة|شَنْطَة
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|جزمة بكعب|جَزْمَة بِكَعْبْ
                clothing.pajamas|بيجامة|بِيجامَة
                clothing.belt|حزام|حِزامْ
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|مناخير|مَناخيرْ
                body.mouth|بق|بُقّ
                body.tongue|لسان|لِسانْ
                body.ear|ودن|وِدْنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجْلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|صباع|صُباعْ
                body.neck|رقبة|رَقْبَة
                body.chest|صدر|صَدْرْ
                body.face|وش|وِشّ
                body.tooth|سنة|سِنَّة
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|آكل|آكُلْ
                action.drink|أشرب|أَشْرَبْ
                action.sleep|أنام|أَنامْ
                action.sit|أقعد|أَقْعُدْ
                action.walk|أمشي|أَمْشي
                action.stand|أقوم|أَقُومْ
                action.wash|أغسل|أَغْسِلْ
                action.speak|أتكلم|أَتْكَلِّمْ
                action.hear|أسمع|أَسْمَعْ
                action.open|أفتح|أَفْتَحْ
                action.close|أقفل|أَقْفِلْ
                action.read|أقرا|أَقْرا
                action.laugh|أضحك|أَضْحَكْ
                action.see|أشوف|أَشوفْ
                action.dress|ألبس|أَلْبِسْ
                action.pray|أصلّي|أَصَلِّي
                animal.cat|قطة|قِطَّة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَروفْ
                animal.horse|حصان|حِصانْ
                animal.cow|بقرة|بَقَرَة
                animal.sparrow|عصفور|عُصْفورْ
                animal.pigeon|حمامة|حَمامَة
                animal.chicken|فرخة|فَرْخَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|عربية|عَرَبِيَّة
                transport.taxi|تاكسي|تَاكْسي
                transport.bus|أتوبيس|أُتوبيسْ
                transport.minibus|ميكروباص|مِيكْروباصْ
                transport.bicycle|عجلة|عَجَلَة
                transport.motorcycle|موتوسيكل|مُوتوسيكِلْ
                transport.train|قطر|قَطْرْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِليكوبْتَرْ
                transport.ship|مركب كبير|مَرْكِبْ كِبيرْ
                transport.boat|مركب|مَرْكِبْ
                transport.truck|عربية نقل|عَرَبِيِّة نَقْلْ
                greeting.salam|السلام عليكم|السَّلامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلامْ
                greeting.hello|أهلاً|أَهْلًا
                greeting.welcome|أهلاً وسهلاً|أَهْلًا وَسَهْلًا
                greeting.how_are_you|أخبارك إيه؟|أَخْبارَكْ إِيهْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَباحْ الْخيرْ
                greeting.good_evening|مسا الخير|مَسا الْخيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|تسلم|تِسْلَمْ
                greeting.goodbye|مع السلامة|مَعَ السَّلامَة
                greeting.safe|في أمان الله|فِي أَمانِ اللَّهْ
                greeting.thanks|شكراً ليك|شُكْرًا لِيكْ
                greeting.bless|ربنا يبارك فيك|رَبِّنَا يْبَارِك فِيكْ
            """),
            ArabicDialect.SYRIAN to parse("""
                family.father|أبوي|أَبويْ
                family.mother|أمي|أُمِّي
                family.husband|جوزي|جُوزِي
                family.wife|مرتي|مَرْتي
                family.son|ابني|اِبْني
                family.daughter|بنتي|بِنْتي
                family.brother|أخوي|أَخويْ
                family.sister|أختي|أُخْتي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتي
                family.grandson|حفيدي|حَفيدي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ابن عمي|اِبْنْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ابن عمتي|اِبْنْ عَمْتي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتي
                family.maternal_uncle_son|ابن خالي|اِبْنْ خَالي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالي
                family.maternal_aunt_son|ابن خالتي|اِبْنْ خَالْتي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتي
                family.brother_son|ابن أخوي|اِبْنْ أَخويْ
                family.brother_daughter|بنت أخوي|بِنْتْ أَخويْ
                family.sister_son|ابن أختي|اِبْنْ أُخْتي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتي
                family.husband_father|أبو جوزي|أَبو جُوزي
                family.husband_mother|أم جوزي|أُمْ جُوزي
                family.wife_father|أبو مرتي|أَبو مَرْتي
                family.wife_mother|أم مرتي|أُمْ مَرْتي
                family.husband_brother|أخو جوزي|أَخو جُوزي
                family.husband_sister|أخت جوزي|أُخْتْ جُوزي
                family.wife_brother|أخو مرتي|أَخو مَرْتي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتي
                family.sister_husband|جوز أختي|جُوزْ أُخْتي
                family.brother_wife|مرت أخوي|مَرْتْ أَخويْ
                family.daughter_husband|جوز بنتي|جُوزْ بِنْتي
                family.son_wife|مرت ابني|مَرْتْ اِبْني
                family.male_in_law|نسيبي|نَسيبي
                family.female_in_law|نسيبتي|نَسِيبْتِي
                family.male_relative|قريبي|قَريبي
                family.female_relative|قريبتي|قَرِيبْتِي
                request.water|بدي مي|بِدِّي مَيّ
                request.tea|بدي شاي|بِدِّي شَايْ
                request.rest|بدي ارتاح|بِدِّي إِرْتَاحْ
                request.medicine|بدي دواي|بِدِّي دَوايْ
                request.toilet|بدي روح عالحمام|بِدِّي رُوحْ عَالْحَمَّامْ
                request.adjust_pillow|زبط لي المخدة|زَبِّط لِي المِخَدَّة
                request.headache|راسي عم يوجعني|رَاسي عَمْ يُوجَعْني
                request.cold|كتير برد|كْتِيرْ بَرْدْ
                dish.syrian.fried_kibbeh|كبة مقلية|كِبِّة مَقْلِيِّة
                dish.syrian.shakriya|شاكرية برز|شَاكْرِيَّة بِرِزّ
                dish.syrian.yabraq|يبرق ورق عنب|يَبْرَقْ وَرَقْ عِنَبْ
                dish.syrian.chicken_shawarma|شاورما دجاج|شَاوَرْما دَجاجْ
                dish.syrian.chickpea_fatteh|فتة حمص|فَتِّة حُمُّصْ
                dish.syrian.maqluba|مقلوبة باذنجان|مَقْلوبِة بَاذِنْجانْ
                dish.syrian.fattoush|فتوش|فَتُّوشْ
                dish.syrian.tabbouleh|تبولة|تَبُّولِة
                dish.syrian.kebab|كباب حلبي|كَبابْ حَلَبي
                dish.syrian.shish_barak|شيش برك|شِيشْ بَرَكْ
                dish.syrian.stuffed_zucchini|كوسا بلبن|كُوسا بِلَبَنْ
                dish.syrian.mutabbal|متبل باذنجان|مْتَبَّلْ بَاذِنْجَانْ
                dish.syrian.haraq_osbao|حراق إصبعو|حَرَّاق إِصْبَعُو
                dish.syrian.halawet_jibn|حلاوة الجبن|حَلَاوِة الْجِبِنْ
                dish.syrian.kunafa|كنافة نابلسية|كْنَافَة نَابُلْسِيَّة
                routine.call_to_prayer|أذان|أَذانْ
                routine.ablution|وضوء|وُضوءْ
                routine.prayer|صلاة|صَلاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صَلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة المسا|قَهْوِة الْمَسَا
                routine.sleep|نوم|نُومْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|بندورة|بَنْدورَة
                vegetable.potato|بطاطا|بَطاطا
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|توم|تُومْ
                vegetable.carrot|جزر|جَزَرْ
                vegetable.cucumber|خيار|خِيارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجانْ
                vegetable.zucchini|كوسا|كُوسا
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|بقدونس|بَقْدونِسْ
                vegetable.coriander|كزبرة|كُزْبَرَة
                vegetable.lemon|ليمون|لِيمونْ
                vegetable.peas|بازيلا|بَازيلَّا
                vegetable.green_beans|فاصوليا خضرا|فَاصولْيا خَضْرا
                vegetable.spinach|سبانخ|سَبانِخْ
                vegetable.cauliflower|زهرة|زَهْرَة
                vegetable.broccoli|بروكلي|بُروكْلي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرع|قَرَعْ
                vegetable.mint|نعناع|نَعْناعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|بردقان|بُرْدْقانْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَراوْلَة
                fruit.mango|مانجو|مَانْجو
                fruit.watermelon|بطيخ|بَطِّيخْ
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|إجاص|إِجَّاصْ
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|تين|تِينْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَناناسْ
                fruit.kiwi|كيوي|كِيويْ
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|يوسفي|يُوسْفي
                fruit.avocado|أفوكادو|أَفوكادو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبِزْ
                food.milk|حليب|حَليبْ
                food.eggs|بيض|بِيضْ
                food.cheese|جبنة|جِبْنَة
                food.yogurt|لبن|لَبَنْ
                food.butter|زبدة|زُبْدَة
                food.rice|رز|رِزّ
                food.pasta|معكرونة|مَعْكَرونَة
                food.soup|شوربة|شُورْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|جاج|جَاجْ
                food.fish|سمك|سَمَكْ
                food.olives|زيتون|زِيتونْ
                food.oil|زيت|زِيتْ
                food.tea|شاي|شَايْ
                food.coffee|قهوة|قَهْوَة
                food.juice|عصير|عَصيرْ
                food.water|مي|مَيّ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَريرْ
                home.pillow|مخدة|مِخَدَّة
                home.blanket|حرام|حْرَامْ
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسي
                home.sofa|كنبة|كَنَبَة
                home.door|باب|بَابْ
                home.window|شباك|شُبَّاكْ
                home.television|تلفزيون|تِلِفِزْيونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|بوتاجاز|بُوتاجازْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|خزانة|خْزَانَة
                home.key|مفتاح|مِفْتاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|بساط|بِساطْ
                home.kitchen|مطبخ|مَطْبَخْ
                home.cup|كاسة|كَاسَة
                home.tray|صينية|صِينِيَّة
                home.spoon|ملعقة|مِلْعَقَة
                home.fork|شوكة|شُوكَة
                home.knife|سكين|سِكِّينْ
                home.teapot|إبريق شاي|إِبْريقْ شَايْ
                home.pot|طنجرة|طَنْجَرَة
                home.water_bottle|قنينة مي|قَنِّينِة مَيّ
                clothing.tshirt|تيشيرت|تِيشيرْتْ
                clothing.shirt|قميص|قَميصْ
                clothing.jacket|جاكيت|جَاكيتْ
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلوزَة
                clothing.trousers|بنطلون|بَنْطَلونْ
                clothing.dress|فستان|فُسْتانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبايَة
                clothing.hijab|حجاب|حِجابْ
                clothing.scarf|وشاح|وِشاحْ
                clothing.shoes|كندرة|كَنْدَرَة
                clothing.socks|جرابات|جْرَابَاتْ
                clothing.coat|معطف|مِعْطَفْ
                clothing.glasses|نظارة|نَظَّارَة
                clothing.cap|طاقية|طَاقِيَّة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|شنطة|شَنْطَة
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|كندرة كعب عالي|كَنْدْرِة كَعِبْ عَالِي
                clothing.pajamas|بيجامة|بِيجامَة
                clothing.belt|حزام|حْزَامْ
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|منخار|مَنْخارْ
                body.mouth|تم|تِمّ
                body.tongue|لسان|لِسانْ
                body.ear|إذن|إِذِنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجِلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|إصبع|إِصْبَعْ
                body.neck|رقبة|رَقْبَة
                body.chest|صدر|صَدْرْ
                body.face|وش|وِشّ
                body.tooth|سن|سِنّ
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|آكل|آكُلْ
                action.drink|أشرب|أَشْرَبْ
                action.sleep|أنام|أَنامْ
                action.sit|أقعد|أَقْعُدْ
                action.walk|أمشي|أَمْشي
                action.stand|أقوم|أَقومْ
                action.wash|أغسل|أَغْسِلْ
                action.speak|أحكي|أَحْكي
                action.hear|أسمع|أَسْمَعْ
                action.open|أفتح|أَفْتَحْ
                action.close|أسكر|أَسَكِّرْ
                action.read|أقرا|أَقْرا
                action.laugh|أضحك|أَضْحَكْ
                action.see|أشوف|أَشوفْ
                action.dress|ألبس|أَلْبَسْ
                action.pray|أصلي|أَصَلِّي
                animal.cat|بسة|بِسَّة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَروفْ
                animal.horse|حصان|حِصانْ
                animal.cow|بقرة|بَقَرَة
                animal.sparrow|عصفور|عُصْفورْ
                animal.pigeon|حمامة|حَمامَة
                animal.chicken|جاجة|جَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|سيارة|سَيَّارَة
                transport.taxi|تكسي|تَكْسي
                transport.bus|باص|بَاصْ
                transport.minibus|سرفيس|سِرْفيسْ
                transport.bicycle|بسكليت|بِسْكْليتْ
                transport.motorcycle|موتور|مُوتورْ
                transport.train|قطار|قْطَارْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِليكوبْتَرْ
                transport.ship|باخرة|بَاخِرَة
                transport.boat|قارب|قَارِبْ
                transport.truck|شاحنة|شَاحِنَة
                greeting.salam|السلام عليكم|السَّلامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلامْ
                greeting.hello|مرحبا|مَرْحَبا
                greeting.welcome|أهلين وسهلين|أَهْلينْ وَسَهْلينْ
                greeting.how_are_you|كيفك؟|كِيفَكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَباحْ الْخيرْ
                greeting.good_evening|مسا الخير|مَسا الْخيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك العافية|يَعْطيكْ الْعافْيَة
                greeting.goodbye|مع السلامة|مَعَ السَّلامَة
                greeting.safe|في أمان الله|فِي أَمانِ اللَّهْ
                greeting.thanks|يسلمو|يِسْلَمو
                greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيكْ
            """),
            ArabicDialect.IRAQI to parse("""
                family.father|أبويه|أَبُويَهْ
                family.mother|أمي|أُمِّي
                family.husband|زوجي|زَوْجِي
                family.wife|مرتي|مَرْتِي
                family.son|ابني|اِبْنِي
                family.daughter|بنتي|بِنْتِي
                family.brother|أخوي|أَخُويْ
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وَلَدْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وَلَدْ عَمْتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتِي
                family.maternal_uncle_son|ولد خالي|وَلَدْ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وَلَدْ خَالْتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتِي
                family.brother_son|ولد أخوي|وَلَدْ أَخُويْ
                family.brother_daughter|بنت أخوي|بِنْتْ أَخُويْ
                family.sister_son|ولد أختي|وَلَدْ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|أبو زوجي|أَبُو زُوجِي
                family.husband_mother|أم زوجي|أُمْ زُوجِي
                family.wife_father|أبو مرتي|أَبُو مَرْتِي
                family.wife_mother|أم مرتي|أُمْ مَرْتِي
                family.husband_brother|أخو زوجي|أَخُو زُوجِي
                family.husband_sister|أخت زوجي|أُخْتْ زُوجِي
                family.wife_brother|أخو مرتي|أَخُو مَرْتِي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتِي
                family.sister_husband|زوج أختي|زُوجْ أُخْتِي
                family.brother_wife|مرت أخوي|مَرْتْ أَخُويْ
                family.daughter_husband|زوج بنتي|زُوجْ بِنْتِي
                family.son_wife|مرت ابني|مَرْتْ اِبْنِي
                family.male_in_law|نسيبي|نَسِيبِي
                family.female_in_law|نسيبتي|نَسِيبْتِي
                family.male_relative|قريبي|گَرِيبِي
                family.female_relative|قريبتي|قَرِيبْتِي
                request.water|أريد ماي|أَرِيدْ مَايْ
                request.tea|أريد چاي|أَرِيدْ تْشَايْ
                request.rest|أريد أرتاح|أَرِيدْ أَرْتَاحْ
                request.medicine|أريد دواي|أَرِيدْ دَوَايْ
                request.toilet|أريد أروح للحمام|أَرِيدْ أَرُوحْ لِلْحَمَّامْ
                request.adjust_pillow|عدّل لي المخدة|عَدِّلْ لِي الْمْخَدَّة
                request.headache|راسي يوجعني|رَاسِي يُوجَعْنِي
                request.cold|أحس ببرد|أَحِسْ بِبَرْدْ
                dish.iraqi.dolma|دولمة عراقية|دُولْمَة عِرَاقِيَّة
                dish.iraqi.masgouf|سمچ مسكوف|سِمَتْش مَسْكُوفْ
                dish.iraqi.quzi|قوزي عالتمن|قُوزِي عَالْتِمَّنْ
                dish.iraqi.tashreeb|تشريب لحم|تَشْرِيبْ لَحَمْ
                dish.iraqi.pacha|باجة|بَاتْشَة
                dish.iraqi.rice_kibbeh|كبة تمن|كُبِّة تِمَّنْ
                dish.iraqi.mosul_kibbeh|كبة موصلية|كُبَّة مَوْصِلِيَّة
                dish.iraqi.biryani|برياني عراقي|بِرْيَانِي عِرَاقِي
                dish.iraqi.fish_mutabbaq|مطبق سمچ|مُطَبَّق سِمَتْشْ
                dish.iraqi.makhlama|مخلمة|مَخْلَمَة
                dish.iraqi.okra|باميا عالتمن|بَامْيَا عَالْتِمَّنْ
                dish.iraqi.turnip|شلغم مطبوخ|شَلْغَمْ مَطْبُوخْ
                dish.iraqi.harees|هريسة|هَرِيسَة
                dish.iraqi.kleicha|كليجة|كْلِيتْشَة
                dish.iraqi.zalabiya|زلابية|زَلَابْيَة
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صَلَاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صَلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العصر|گَهْوِة الْعَصِرْ
                routine.sleep|نوم|نُومْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|طماطة|طَمَاطَة
                vegetable.potato|بتيتة|بُتِيتَة
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|ثُومْ
                vegetable.carrot|جزر|جَزَرْ
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجَانْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|بقدونس|بَقْدُونِسْ
                vegetable.coriander|كزبرة|كُزْبَرَة
                vegetable.lemon|ليمون|لِيمُونْ
                vegetable.peas|بزاليا|بَزَالْيَا
                vegetable.green_beans|فاصوليا خضرا|فَاصُولْيَا خَضْرَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|قرنابيط|قَرْنَابِيطْ
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرع|قَرَعْ
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|برتقال|بُرْتُقَالْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|رقي|رَگِّي
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|عرموط|عَرْمُوطْ
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|تين|تِينْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|لالنكي|لَالَنْگِي
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبُزْ
                food.milk|حليب|حَلِيبْ
                food.eggs|بيض|بِيضْ
                food.cheese|جبن|جُبْنْ
                food.yogurt|لبن|لَبَنْ
                food.butter|زبدة|زُبْدَة
                food.rice|تمن|تِمَّنْ
                food.pasta|معكرونة|مَعْكَرُونَة
                food.soup|شوربة|شُورْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دَجَاجْ
                food.fish|سمچ|سِمَتْشْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|چاي|تْشَايْ
                food.coffee|قهوة|گَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|ماي|مَايْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مَخَدَّة
                home.blanket|بطانية|بَطَّانِيَّة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|قنفة|قَنَفَة
                home.door|باب|بَابْ
                home.window|شباك|شُبَّاكْ
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|طباخ|طَبَّاخْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|كنتور|كَنْتُورْ
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|زولية|زُولِيَّة
                home.kitchen|مطبخ|مَطْبَخْ
                home.cup|كوب|كُوبْ
                home.tray|صينية|صِينِيَّة
                home.spoon|ملعقة|مِلْعَقَة
                home.fork|شوكة|شُوكَة
                home.knife|سكين|سِكِّينْ
                home.teapot|قوري|گُورِي
                home.pot|جدر|جِدِرْ
                home.water_bottle|بطل مي|بُطُلْ مَيّ
                clothing.tshirt|تيشيرت|تِيشِيرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|جاكيت|جَاكِيتْ
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلُوزَة
                clothing.trousers|بنطرون|بَنْطَرُونْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|قندرة|قُنْدَرَة
                clothing.socks|جواريب|جْوَارِيبْ
                clothing.coat|قبوط|گَبُّوطْ
                clothing.glasses|نظارة|نَظَّارَة
                clothing.cap|طاقية|طَاقِيَّة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|جنطة|جَنْطَة
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|قندرة كعب عالي|قُنْدْرِة كَعَبْ عَالِي
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|حزام|حِزَامْ
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|خشم|خَشِمْ
                body.mouth|حلك|حَلِگْ
                body.tongue|لسان|لِسَانْ
                body.ear|إذن|إِذِنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجِلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|إصبع|إِصْبَعْ
                body.neck|رقبة|رُگْبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجْهْ
                body.tooth|سن|سِنّ
                body.heart|قلب|گَلُبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|آكل|آكُلْ
                action.drink|أشرب|أَشْرَبْ
                action.sleep|أنام|أَنَامْ
                action.sit|أقعد|أَگْعُدْ
                action.walk|أمشي|أَمْشِي
                action.stand|أقوم|أَگُومْ
                action.wash|أغسل|أَغْسِلْ
                action.speak|أحچي|أَحْتْشِي
                action.hear|أسمع|أَسْمَعْ
                action.open|أفتح|أَفْتَحْ
                action.close|أسدّ|أَسِدّ
                action.read|أقرا|أَگْرَا
                action.laugh|أضحك|أَضْحَكْ
                action.see|أشوف|أَشُوفْ
                action.dress|ألبس|أَلْبَسْ
                action.pray|أصلّي|أَصَلِّي
                animal.cat|بزونة|بْزُونَة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَرُوفْ
                animal.horse|حصان|حِصَانْ
                animal.cow|بقرة|بَگْرَة
                animal.sparrow|عصفور|عُصْفُورْ
                animal.pigeon|حمامة|حَمَامَة
                animal.chicken|دجاجة|دَجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|سيارة|سَيَّارَة
                transport.taxi|تاكسي|تَاكْسِي
                transport.bus|باص|بَاصْ
                transport.minibus|باص صغير|بَاصْ صْغِيرْ
                transport.bicycle|بايسكل|بَايْسِكِلْ
                transport.motorcycle|دراجة نارية|دَرَّاجَة نَارِيَّة
                transport.train|قطار|قِطَارْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|باخرة|بَاخِرَة
                transport.boat|زورق|زَوْرَقْ
                transport.truck|لوري|لُورِي
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|هلا بيك|هَلَا بِيكْ
                greeting.welcome|أهلين وسهلين|أَهْلِينْ وَسَهْلِينْ
                greeting.how_are_you|شلونك؟|شْلُونَكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|عاشت إيدك|عَاشَتْ إِيدَكْ
                greeting.goodbye|مع السلامة|مَعَ السَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|مشكور|مَشْكُورْ
                greeting.bless|الله يبارك بيك|اللَّه يْبَارِك بِيكْ
            """),
            ArabicDialect.TUNISIAN to parse("""
                family.father|بابا|بَابَا
                family.mother|أمي|أُمِّي
                family.husband|راجلي|رَاجْلِي
                family.wife|مرتي|مَرْتِي
                family.son|ولدي|وِلْدِي
                family.daughter|بنتي|بِنْتِي
                family.brother|خويا|خُويَا
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وِلْد عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وِلْد عَمْتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتِي
                family.maternal_uncle_son|ولد خالي|وِلْد خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وِلْد خَالْتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتِي
                family.brother_son|ولد خويا|وِلْد خُويَا
                family.brother_daughter|بنت خويا|بِنْتْ خُويَا
                family.sister_son|ولد أختي|وِلْد أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|بو راجلي|بُو رَاجْلِي
                family.husband_mother|أم راجلي|أُمْ رَاجْلِي
                family.wife_father|بو مرتي|بُو مَرْتِي
                family.wife_mother|أم مرتي|أُمْ مَرْتِي
                family.husband_brother|خو راجلي|خُو رَاجْلِي
                family.husband_sister|أخت راجلي|أُخْتْ رَاجْلِي
                family.wife_brother|خو مرتي|خُو مَرْتِي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتِي
                family.sister_husband|راجل أختي|رَاجِلْ أُخْتِي
                family.brother_wife|مرت خويا|مَرْتْ خُويَا
                family.daughter_husband|راجل بنتي|رَاجِلْ بِنْتِي
                family.son_wife|مرت ولدي|مَرْت وِلْدِي
                family.male_in_law|نسيبي|نْسِيبِي
                family.female_in_law|نسيبتي|نْسِيبْتِي
                family.male_relative|قريبي|قْرِيبِي
                family.female_relative|قريبتي|قْرِيبْتِي
                request.water|نحب شوية ما|نْحِبّ شْوَيَّة مَا
                request.tea|نحب تاي|نْحِبّ تَايْ
                request.rest|نحب نرتاح|نْحِبّ نِرْتَاحْ
                request.medicine|نحب دوايا|نْحِبّ دْوَايَا
                request.toilet|نحب نمشي للتواليت|نْحِبّ نِمْشِي لِلتْوَالِيتْ
                request.adjust_pillow|صلّح لي المخدة|صَلَّح لِي المِخَدَّة
                request.headache|راسي يوجع فيا|رَاسِي يُوجَعْ فِيَّا
                request.cold|البرد قوي|الْبَرْدْ قْوِيّ
                dish.tunisian.couscous_fish|كسكسي بالحوت|كُسْكُسِي بِالحُوتْ
                dish.tunisian.lablabi|لبلابي|لَبْلَابِي
                dish.tunisian.brik|بريك|بْرِيكْ
                dish.tunisian.kaftaji|كفتاجي|كَفْتَاجِي
                dish.tunisian.tajine|طاجين تونسي|طَاجِينْ تُونْسِي
                dish.tunisian.plate|صحن تونسي|صْحَنْ تُونْسِي
                dish.tunisian.bird_tongue_soup|شربة لسان عصفور|شُرْبِة لْسَانْ عَصْفُورْ
                dish.tunisian.molokhia|ملوخية تونسية|مْلُوخِيَّة تُونْسِيَّة
                dish.tunisian.pea_stew|مرقة جلبانة|مَرْڨِة جِلْبَانَة
                dish.tunisian.pasta|مقرونة بالصلصة|مَڨْرُونَة بِالصَّلْصَة
                dish.tunisian.merguez_omelette|عجة بالمرقاز|عُجَّة بِالْمَرْڨَازْ
                dish.tunisian.grilled_salad|سلاطة مشوية|سَلَاطَة مَشْوِيَّة
                dish.tunisian.fricassee|فريكاسي|فْرِيكَاسِي
                dish.tunisian.maqroudh|مقروض قيرواني|مَڨْرُوضْ قَيْرَوَانِي
                dish.tunisian.bambalouni|بامبالوني|بَامْبَالُونِي
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صْلَاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صْلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العشية|قَهْوِة الْعَشِيَّة
                routine.sleep|رقاد|رْڨَادْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مَشِي
                vegetable.tomato|طماطم|طَمَاطِمْ
                vegetable.potato|بطاطا|بَطَاطَا
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|تُومْ
                vegetable.carrot|سفنارية|سْفِنَّارِيَّة
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجَانْ
                vegetable.zucchini|قرع أخضر|ڨْرَعْ أَخْضَرْ
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|معدنوس|مَعْدَنُوسْ
                vegetable.coriander|كزبرة|كُزْبْرَة
                vegetable.lemon|قارص|قَارِصْ
                vegetable.peas|جلبانة|جِلْبَانَة
                vegetable.green_beans|لوبيا خضرا|لُوبْيَا خَضْرَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|شفلور|شَفْلُورْ
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرع|ڨْرَعْ
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|موز|مُوزْ
                fruit.orange|بردقان|بُرْدْڨَانْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|دلاّع|دَلَّاعْ
                fruit.melon|بطيخ|بِطِّيخْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|إجاص|إِجَّاصْ
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|كرموس|كَرْمُوسْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|مندرينة|مَنْدَرِينَة
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبْزْ
                food.milk|حليب|حَلِيبْ
                food.eggs|عظم|عْظَمْ
                food.cheese|جبن|جُبْنْ
                food.yogurt|ياغرت|يَاغُرْتْ
                food.butter|زبدة|زُبْدَة
                food.rice|روز|رُوزْ
                food.pasta|مقرونة|مَڨْرُونَة
                food.soup|شربة|شُرْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دَجَاجْ
                food.fish|حوت|حُوتْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|تاي|تَايْ
                food.coffee|قهوة|قَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|ماء|مَاءْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مِخَدَّة
                home.blanket|فراشية|فْرَاشِيَّة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|فوتاي|فُوتَايْ
                home.door|باب|بَابْ
                home.window|شباك|شُبَّاكْ
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|غاز|غَازْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|مريو|مَرْيُو
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|زربية|زَرْبِيَّة
                home.kitchen|كوجينة|كُوجِينَة
                home.cup|كاس|كَاسْ
                home.tray|صينية|صِينِيَّة
                home.spoon|مغرفة|مَغْرْفَة
                home.fork|فرشيطة|فَرْشِيطَة
                home.knife|سكينة|سِكِّينَة
                home.teapot|براد|بَرَّادْ
                home.pot|طنجرة|طَنْجْرَة
                home.water_bottle|دبوزة ماء|دَبُّوزِة مَاءْ
                clothing.tshirt|تيشيرت|تِيشِيرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|فيستة|فِيسْتَة
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلُوزَة
                clothing.trousers|سروال|سِرْوَالْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|سباط|سْبَاطْ
                clothing.socks|كلسيطة|كَلْسِيطَة
                clothing.coat|مانتو|مَانْتُو
                clothing.glasses|نظارات|نَظَّارَاتْ
                clothing.cap|كاسكيطة|كَاسْكِيطَة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|صاك|صَاكْ
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|سباط طالون|سْبَاطْ طَالُونْ
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|سبتة|سِبْتَة
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|خشم|خَشِمْ
                body.mouth|فم|فَمْ
                body.tongue|لسان|لِسَانْ
                body.ear|وذن|وِذِنْ
                body.hand|يد|يَدْ
                body.leg|ساق|سَاقْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|صبع|صُبُعْ
                body.neck|رقبة|رَڨْبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجْهْ
                body.tooth|سنة|سِنَّة
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|ناكل|نَاكِلْ
                action.drink|نشرب|نِشْرِبْ
                action.sleep|نرقد|نُرْڨُدْ
                action.sit|نقعد|نُڨْعُدْ
                action.walk|نمشي|نِمْشِي
                action.stand|نوقف|نُوقَفْ
                action.wash|نغسل|نِغْسِلْ
                action.speak|نحكي|نِحْكِي
                action.hear|نسمع|نِسْمَعْ
                action.open|نحل|نْحِلّ
                action.close|نسكر|نْسَكِّرْ
                action.read|نقرا|نَقْرَا
                action.laugh|نضحك|نِضْحَكْ
                action.see|نشوف|نْشُوفْ
                action.dress|نلبس|نِلْبِسْ
                action.pray|نصلي|نْصَلِّي
                animal.cat|قطوسة|ڨَطُّوسَة
                animal.dog|كلب|كَلْبْ
                animal.sheep|علوش|عَلُّوشْ
                animal.horse|حصان|حِصَانْ
                animal.cow|بقرة|بَڨْرَة
                animal.sparrow|عصفور|عُصْفُورْ
                animal.pigeon|حمامة|حْمَامَة
                animal.chicken|دجاجة|دْجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|سردوك|سَرْدُوكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|كرهبة|كَرْهَبَة
                transport.taxi|تاكسي|تَاكْسِي
                transport.bus|كار|كَارْ
                transport.minibus|لواج|لُوَاجْ
                transport.bicycle|بسكلات|بِسْكْلَاتْ
                transport.motorcycle|موتور|مُوتُورْ
                transport.train|تران|تْرَانْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|باطو|بَاطُو
                transport.boat|فلوكة|فْلُوكَة
                transport.truck|كاميون|كَامْيُونْ
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|عسلامة|عَسْلَامَة
                greeting.welcome|مرحبا بيك|مَرْحَبَا بِيكْ
                greeting.how_are_you|شنوة أحوالك؟|شْنُوَّة أَحْوَالَكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك الصحة|يَعْطِيكْ الصَّحَّة
                greeting.goodbye|بالسلامة|بِالسَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|شكراً ليك|شُكْرًا لِيكْ
                greeting.bless|ربي يبارك فيك|رَبِّي يْبَارِك فِيكْ
            """),
            ArabicDialect.MOROCCAN to parse("""
                family.father|بّا|بَّا
                family.mother|مّي|مِّي
                family.husband|راجلي|رَاجْلِي
                family.wife|مرتي|مَرْتِي
                family.son|ولدي|وَلْدِي
                family.daughter|بنتي|بَنْتِي
                family.brother|خويا|خُويَا
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وَلْدْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وَلْدْ عَمْتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتِي
                family.maternal_uncle_son|ولد خالي|وَلْدْ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وَلْدْ خَالْتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتِي
                family.brother_son|ولد خويا|وَلْدْ خُويَا
                family.brother_daughter|بنت خويا|بِنْتْ خُويَا
                family.sister_son|ولد أختي|وَلْدْ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|بو راجلي|بُو رَاجْلِي
                family.husband_mother|أم راجلي|أُمْ رَاجْلِي
                family.wife_father|بو مرتي|بُو مَرْتِي
                family.wife_mother|أم مرتي|أُمْ مَرْتِي
                family.husband_brother|خو راجلي|خُو رَاجْلِي
                family.husband_sister|أخت راجلي|أُخْتْ رَاجْلِي
                family.wife_brother|خو مرتي|خُو مَرْتِي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتِي
                family.sister_husband|راجل أختي|رَاجِلْ أُخْتِي
                family.brother_wife|مرت خويا|مَرْتْ خُويَا
                family.daughter_husband|راجل بنتي|رَاجِلْ بِنْتِي
                family.son_wife|مرت ولدي|مَرْتْ وَلْدِي
                family.male_in_law|نسيبي|نْسِيبِي
                family.female_in_law|نسيبتي|نْسِيبْتِي
                family.male_relative|قريبي|قْرِيبِي
                family.female_relative|قريبتي|قْرِيبْتِي
                request.water|بغيت الما|بْغِيتْ الْمَا
                request.tea|بغيت أتاي|بْغِيتْ أَتَايْ
                request.rest|بغيت نرتاح|بْغِيتْ نِرْتَاحْ
                request.medicine|بغيت الدوا ديالي|بْغِيتْ الدّوَا دْيَالِي
                request.toilet|بغيت نمشي للحمام|بْغِيتْ نْمْشِي لِلْحَمَّامْ
                request.adjust_pillow|صوّب ليا المخدة|صَوَّبْ لِيَّا الْمْخَدَّة
                request.headache|راسي كيوجعني|رَاسِي كَيُوجَعْنِي
                request.cold|حسّيت بالبرد|حَسِّيتْ بِالْبَرْدْ
                dish.moroccan.prune_tajine|طاجين بالبرقوق|طَاجِينْ بِالْبَرْقُوقْ
                dish.moroccan.couscous|كسكسو سبع خضار|كُسْكْسُو سْبَعْ خْضَارْ
                dish.moroccan.pastilla|بسطيلة بالحوت|بَسْطِيلَة بِالْحُوتْ
                dish.moroccan.harira|حريرة مغربية|حْرِيرَة مَغْرِبِيَّة
                dish.moroccan.tangia|طنجية مراكشية|طَنْجِيَّة مَرَّاكْشِيَّة
                dish.moroccan.rfissa|رفيسة بالدجاج|رْفِيسَة بِالدّجَاجْ
                dish.moroccan.zaalouk|زعلوك|زَعْلُوكْ
                dish.moroccan.taktouka|تكتوكة|تَكْتُوكَة
                dish.moroccan.kefta_tajine|طاجين كفتة بالبيض|طَاجِينْ كُفْتَة بِالْبِيضْ
                dish.moroccan.skewers|قطبان|گُطْبَانْ
                dish.moroccan.msemen|مسمن|مْسَمَّنْ
                dish.moroccan.baghrir|بغرير|بَغْرِيرْ
                dish.moroccan.seffa|سفة مدفونة|سَفَّة مَدْفُونَة
                dish.moroccan.chebakia|شباكية|شَبَّاكِيَّة
                dish.moroccan.sellou|سلو|سَلُّو
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صْلَاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صْلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العشية|قَهْوِة الْعَشِيَّة
                routine.sleep|نعاس|نْعَاسْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مْشِي
                vegetable.tomato|مطيشة|مَطِيشَة
                vegetable.potato|بطاطا|بَطَاطَا
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|تُومْ
                vegetable.carrot|خيزو|خِيزُّو
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|دنجال|دَنْجَالْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|معدنوس|مَعْدَنُوسْ
                vegetable.coriander|قزبور|گَزْبُورْ
                vegetable.lemon|حامض|حَامِضْ
                vegetable.peas|جلبانة|جِلْبَانَة
                vegetable.green_beans|لوبيا خضرا|لُوبْيَا خَضْرَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|شيفلور|شِيفْلُورْ
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرعة حمرا|گَرْعَة حَمْرَا
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|بنان|بَنَانْ
                fruit.orange|برتقال|بُرْتُقَالْ
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|دلاّح|دَلَّاحْ
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|إجاص|إِجَّاصْ
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|كرموس|كَرْمُوسْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|مندرين|مَنْدَرِينْ
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبْزْ
                food.milk|حليب|حَلِيبْ
                food.eggs|بيض|بِيضْ
                food.cheese|فرماج|فُرْمَاجْ
                food.yogurt|ياغورت|يَاغُورْتْ
                food.butter|زبدة|زُبْدَة
                food.rice|روز|رُوزْ
                food.pasta|مقرونية|مَگْرُونِيَّة
                food.soup|حريرة|حْرِيرَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دْجَاجْ
                food.fish|حوت|حُوتْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|أتاي|أَتَايْ
                food.coffee|قهوة|قَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|ماء|مَاءْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مْخَدَّة
                home.blanket|مانطة|مَانْطَة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|سداري|سَدَّارِي
                home.door|باب|بَابْ
                home.window|شرجم|شَرْجَمْ
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|بوطة|بُوطَة
                home.oven|فران|فَرَّانْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|خزانة|خْزَانَة
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|زربية|زَرْبِيَّة
                home.kitchen|كوزينة|كُوزِينَة
                home.cup|كاس|كَاسْ
                home.tray|صينية|صِينِيَّة
                home.spoon|معلقة|مْعِلْقَة
                home.fork|فرشيطة|فَرْشِيطَة
                home.knife|موس|مُوسْ
                home.teapot|براد|بَرَّادْ
                home.pot|طنجرة|طَنْجْرَة
                home.water_bottle|قرعة ماء|گَرْعِة مَاءْ
                clothing.tshirt|تيشيرت|تِيشِيرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|جاكيطة|جَاكِيطَة
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلُوزَة
                clothing.trousers|سروال|سِرْوَالْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|صباط|صْبَاطْ
                clothing.socks|تقاشر|تْگَاشِرْ
                clothing.coat|مانتو|مَانْتُو
                clothing.glasses|نضاضر|نْضَاضِرْ
                clothing.cap|طاقية|طَاقِيَّة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|صاك|صَاكْ
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|صباط طالون|صْبَاطْ طَالُونْ
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|سمطة|سَمْطَة
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|نيف|نِيفْ
                body.mouth|فم|فَمْ
                body.tongue|لسان|لِسَانْ
                body.ear|وذن|وِذِنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجْلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|صبع|صُبَعْ
                body.neck|رقبة|رَگْبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجْهْ
                body.tooth|سن|سِنّ
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|ناكل|نَاكُلْ
                action.drink|نشرب|نِشْرَبْ
                action.sleep|ننعس|نِنْعَسْ
                action.sit|نجلس|نِجْلِسْ
                action.walk|نمشي|نْمْشِي
                action.stand|نوقف|نُوقَفْ
                action.wash|نغسل|نِغْسِلْ
                action.speak|نهضر|نَهْضَرْ
                action.hear|نسمع|نِسْمَعْ
                action.open|نحل|نْحِلّ
                action.close|نسد|نْسَدّ
                action.read|نقرا|نَقْرَا
                action.laugh|نضحك|نِضْحَكْ
                action.see|نشوف|نْشُوفْ
                action.dress|نلبس|نِلْبَسْ
                action.pray|نصلي|نْصَلِّي
                animal.cat|مش|مُشّ
                animal.dog|كلب|كَلْبْ
                animal.sheep|حولي|حَوْلِي
                animal.horse|حصان|حِصَانْ
                animal.cow|بقرة|بَگْرَة
                animal.sparrow|عصفور|عُصْفُورْ
                animal.pigeon|حمامة|حْمَامَة
                animal.chicken|دجاجة|دْجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|قنية|گْنِيَّة
                transport.car|طوموبيل|طُومُوبِيلْ
                transport.taxi|طاكسي|طَاكْسِي
                transport.bus|طوبيس|طُوبِيسْ
                transport.minibus|طوبيس صغير|طُوبِيسْ صْغِيرْ
                transport.bicycle|بيكالة|بِيكَالَة
                transport.motorcycle|موطور|مُوطُورْ
                transport.train|تران|تْرَانْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|باخرة|بَاخِرَة
                transport.boat|فلوكة|فْلُوكَة
                transport.truck|كاميو|كَامْيُو
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|مرحبا|مَرْحَبَا
                greeting.welcome|مرحبا بيك|مَرْحَبَا بِيكْ
                greeting.how_are_you|لاباس عليك؟|لَابَاسْ عَلِيكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك الصحة|يَعْطِيكْ الصَّحَّة
                greeting.goodbye|بالسلامة|بِالسَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|شكراً بزاف|شُكْرًا بِزَّافْ
                greeting.bless|الله يبارك فيك|اللَّه يْبَارِك فِيكْ
            """),
            ArabicDialect.ALGERIAN to parse("""
                family.father|بابا|بَابَا
                family.mother|يما|يَمَّا
                family.husband|راجلي|رَاجْلِي
                family.wife|مرتي|مَرْتِي
                family.son|ولدي|وَلْدِي
                family.daughter|بنتي|بَنْتِي
                family.brother|خويا|خُويَا
                family.sister|أختي|أُخْتِي
                family.paternal_uncle|عمي|عَمِّي
                family.paternal_aunt|عمتي|عَمْتِي
                family.maternal_uncle|خالي|خَالِي
                family.maternal_aunt|خالتي|خَالْتِي
                family.grandfather|جدي|جَدِّي
                family.grandmother|جدتي|جَدَّتِي
                family.grandson|حفيدي|حَفِيدِي
                family.granddaughter|حفيدتي|حَفِيدْتِي
                family.paternal_uncle_son|ولد عمي|وَلْدْ عَمِّي
                family.paternal_uncle_daughter|بنت عمي|بِنْتْ عَمِّي
                family.paternal_aunt_son|ولد عمتي|وَلْدْ عَمْتِي
                family.paternal_aunt_daughter|بنت عمتي|بِنْتْ عَمْتِي
                family.maternal_uncle_son|ولد خالي|وَلْدْ خَالِي
                family.maternal_uncle_daughter|بنت خالي|بِنْتْ خَالِي
                family.maternal_aunt_son|ولد خالتي|وَلْدْ خَالْتِي
                family.maternal_aunt_daughter|بنت خالتي|بِنْتْ خَالْتِي
                family.brother_son|ولد خويا|وَلْدْ خُويَا
                family.brother_daughter|بنت خويا|بِنْتْ خُويَا
                family.sister_son|ولد أختي|وَلْدْ أُخْتِي
                family.sister_daughter|بنت أختي|بِنْتْ أُخْتِي
                family.husband_father|بو راجلي|بُو رَاجْلِي
                family.husband_mother|أم راجلي|أُمْ رَاجْلِي
                family.wife_father|بو مرتي|بُو مَرْتِي
                family.wife_mother|أم مرتي|أُمْ مَرْتِي
                family.husband_brother|خو راجلي|خُو رَاجْلِي
                family.husband_sister|أخت راجلي|أُخْتْ رَاجْلِي
                family.wife_brother|خو مرتي|خُو مَرْتِي
                family.wife_sister|أخت مرتي|أُخْتْ مَرْتِي
                family.sister_husband|راجل أختي|رَاجِلْ أُخْتِي
                family.brother_wife|مرت خويا|مَرْتْ خُويَا
                family.daughter_husband|راجل بنتي|رَاجِلْ بِنْتِي
                family.son_wife|مرت ولدي|مَرْتْ وَلْدِي
                family.male_in_law|نسيبي|نْسِيبِي
                family.female_in_law|نسيبتي|نْسِيبْتِي
                family.male_relative|قريبي|قْرِيبِي
                family.female_relative|قريبتي|قْرِيبْتِي
                request.water|نحب نشرب الما|نْحَبّ نَشْرَبْ الْمَا
                request.tea|نحب أتاي|نْحَبّ أَتَايْ
                request.rest|نحب نرتاح|نْحَبّ نِرْتَاحْ
                request.medicine|نحب دوايا|نْحَبّ دْوَايَا
                request.toilet|بغيت نروح للتواليت|بْغِيت نْرُوح لِلتْوَالِيتْ
                request.adjust_pillow|صلّحلي المخدة|صَلَّحْلِي الْمْخَدَّة
                request.headache|راسي يوجعني|رَاسِي يُوجَعْنِي
                request.cold|راني بردان|رَانِي بَرْدَانْ
                dish.algerian.rechta|رشتة|رَشْتَة
                dish.algerian.chakhchoukha|شخشوخة بسكرية|شَخْشُوخَة بِسْكْرِيَّة
                dish.algerian.couscous|طعام|طْعَامْ
                dish.algerian.olive_tajine|طاجين الزيتون|طَاجِينْ الزِّيتُونْ
                dish.algerian.frik_soup|شربة فريك|شُرْبِة فْرِيكْ
                dish.algerian.bourek|بوراك|بُورَاكْ
                dish.algerian.garantita|كرانتيكا|كْرَانْتِيكَا
                dish.algerian.sweet_tajine|طاجين الحلو|طَاجِينْ الْحْلُو
                dish.algerian.berkoukes|بركوكس|بَرْكُوكَسْ
                dish.algerian.potato_stew|جواز بطاطا|جْوَازْ بَطَاطَا
                dish.algerian.mahajeb|محاجب|مْحَاجِبْ
                dish.algerian.douara|دوارة|دْوَارَة
                dish.algerian.stuffed_artichoke|قرنون معمر|ڨَرْنُونْ مْعَمَّرْ
                dish.algerian.kalb_el_louz|قلب اللوز|قَلْبْ اللُّوزْ
                dish.algerian.makrout|مقروط العسل|مَقْرُوطْ الْعَسَلْ
                routine.call_to_prayer|أذان|أَذَانْ
                routine.ablution|وضوء|وُضُوءْ
                routine.prayer|صلاة|صْلَاة
                routine.prayer_rug|سجادة صلاة|سَجَّادِة صْلَاة
                routine.prayer_beads|مسبحة|مِسْبَحَة
                routine.quran|مصحف|مُصْحَفْ
                routine.evening_coffee|قهوة العشية|قَهْوِة الْعَشِيَّة
                routine.sleep|رقاد|رْڨَادْ
                routine.rest|راحة|رَاحَة
                routine.walk|مشي|مْشِي
                vegetable.tomato|طماطم|طْمَاطَمْ
                vegetable.potato|بطاطا|بَطَاطَا
                vegetable.onion|بصل|بَصَلْ
                vegetable.garlic|ثوم|تُومْ
                vegetable.carrot|زرودية|زْرُودِيَّة
                vegetable.cucumber|خيار|خِيَارْ
                vegetable.pepper|فلفل|فِلْفِلْ
                vegetable.eggplant|باذنجان|بَاذِنْجَانْ
                vegetable.zucchini|كوسة|كُوسَة
                vegetable.lettuce|خس|خَسّ
                vegetable.parsley|معدنوس|مَعْدَنُوسْ
                vegetable.coriander|قزبر|ڨُزْبُرْ
                vegetable.lemon|قارص|قَارَصْ
                vegetable.peas|جلبانة|جِلْبَانَة
                vegetable.green_beans|لوبيا خضرا|لُوبْيَا خَضْرَا
                vegetable.spinach|سبانخ|سَبَانِخْ
                vegetable.cauliflower|شيفلور|شِيفْلُورْ
                vegetable.broccoli|بروكلي|بُرُوكْلِي
                vegetable.radish|فجل|فِجِلْ
                vegetable.pumpkin|قرعة حمرا|ڨَرْعَة حَمْرَا
                vegetable.mint|نعناع|نَعْنَاعْ
                vegetable.corn|ذرة|ذُرَّة
                fruit.apple|تفاح|تُفَّاحْ
                fruit.banana|بنان|بَنَانْ
                fruit.orange|تشينة|تْشِينَة
                fruit.grapes|عنب|عِنَبْ
                fruit.strawberry|فراولة|فَرَاوْلَة
                fruit.mango|مانجو|مَانْجُو
                fruit.watermelon|دلاّع|دَلَّاعْ
                fruit.melon|شمام|شَمَّامْ
                fruit.peach|خوخ|خُوخْ
                fruit.apricot|مشمش|مِشْمِشْ
                fruit.pear|بوعويدة|بُوعْوِيدَة
                fruit.pomegranate|رمان|رُمَّانْ
                fruit.figs|كرموس|كَرْمُوسْ
                fruit.dates|تمر|تَمْرْ
                fruit.pineapple|أناناس|أَنَانَاسْ
                fruit.kiwi|كيوي|كِيوِي
                fruit.cherry|كرز|كَرَزْ
                fruit.berries|توت|تُوتْ
                fruit.mandarin|مندارين|مَنْدَارِينْ
                fruit.avocado|أفوكادو|أَفُوكَادُو
                fruit.coconut|جوز الهند|جُوزْ الْهِنْدْ
                food.bread|خبز|خُبْزْ
                food.milk|حليب|حَلِيبْ
                food.eggs|بيض|بِيضْ
                food.cheese|فرماج|فُرْمَاجْ
                food.yogurt|ياغرت|يَاغُرْتْ
                food.butter|زبدة|زُبْدَة
                food.rice|روز|رُوزْ
                food.pasta|مقرون|مَڨْرُونْ
                food.soup|شربة|شُرْبَة
                food.meat|لحم|لَحْمْ
                food.chicken|دجاج|دْجَاجْ
                food.fish|حوت|حُوتْ
                food.olives|زيتون|زِيتُونْ
                food.oil|زيت|زِيتْ
                food.tea|أتاي|أَتَايْ
                food.coffee|قهوة|قَهْوَة
                food.juice|عصير|عَصِيرْ
                food.water|ماء|مَاءْ
                food.honey|عسل|عَسَلْ
                food.biscuit|بسكويت|بَسْكَوِيتْ
                food.salt|ملح|مِلْحْ
                food.sugar|سكر|سُكَّرْ
                home.bed|سرير|سَرِيرْ
                home.pillow|مخدة|مْخَدَّة
                home.blanket|كوفيرتة|كُوفِيرْتَة
                home.table|طاولة|طَاوِلَة
                home.chair|كرسي|كُرْسِي
                home.sofa|فوتاي|فُوتَايْ
                home.door|باب|بَابْ
                home.window|طاقة|طَاقَة
                home.television|تلفزيون|تِلِفِزْيُونْ
                home.fridge|ثلاجة|ثَلَّاجَة
                home.washing_machine|غسالة|غَسَّالَة
                home.stove|كويزينيير|كُويزِينْيِيرْ
                home.oven|فرن|فُرْنْ
                home.air_conditioner|مكيف|مُكَيِّفْ
                home.fan|مروحة|مَرْوَحَة
                home.wardrobe|خزانة|خْزَانَة
                home.key|مفتاح|مِفْتَاحْ
                home.clock|ساعة|سَاعَة
                home.carpet|زربية|زَرْبِيَّة
                home.kitchen|كوزينة|كُوزِينَة
                home.cup|كاس|كَاسْ
                home.tray|صينية|صِينِيَّة
                home.spoon|مغرف|مْغْرُفْ
                home.fork|فرشيطة|فَرْشِيطَة
                home.knife|موس|مُوسْ
                home.teapot|براد|بَرَّادْ
                home.pot|طنجرة|طَنْجْرَة
                home.water_bottle|قرعة ماء|ڨَرْعِة مَاءْ
                clothing.tshirt|تيشيرت|تِيشِيرْتْ
                clothing.shirt|قميص|قَمِيصْ
                clothing.jacket|فيستة|فِيسْتَة
                clothing.shorts|شورت|شُورْتْ
                clothing.blouse|بلوزة|بْلُوزَة
                clothing.trousers|سروال|سِرْوَالْ
                clothing.dress|فستان|فُسْتَانْ
                clothing.robe|روب|رُوبْ
                clothing.abaya|عباية|عَبَايَة
                clothing.hijab|حجاب|حِجَابْ
                clothing.scarf|وشاح|وِشَاحْ
                clothing.shoes|سباط|سْبَاطْ
                clothing.socks|تقاشر|تْڨَاشِرْ
                clothing.coat|مانتو|مَانْتُو
                clothing.glasses|نظارة|نَظَّارَة
                clothing.cap|كاسكيطة|كَاسْكِيطَة
                clothing.watch|ساعة يد|سَاعِة يَدّ
                clothing.bag|صاك|صَاكْ
                clothing.sandals|صندل|صَنْدَلْ
                clothing.high_heels|سباط طالون|سْبَاطْ طَالُونْ
                clothing.pajamas|بيجامة|بِيجَامَة
                clothing.belt|سبتة|سِبْتَة
                clothing.ring|خاتم|خَاتَمْ
                body.eye|عين|عِينْ
                body.nose|نيف|نِيفْ
                body.mouth|فم|فَمْ
                body.tongue|لسان|لِسَانْ
                body.ear|وذن|وِذِنْ
                body.hand|يد|يَدْ
                body.leg|رجل|رِجْلْ
                body.head|راس|رَاسْ
                body.hair|شعر|شَعْرْ
                body.back|ظهر|ظَهْرْ
                body.abdomen|بطن|بَطْنْ
                body.knee|ركبة|رُكْبَة
                body.finger|صبع|صُبُعْ
                body.neck|رقبة|رَڨْبَة
                body.chest|صدر|صَدْرْ
                body.face|وجه|وَجْهْ
                body.tooth|سنة|سِنَّة
                body.heart|قلب|قَلْبْ
                body.shoulder|كتف|كِتْفْ
                action.eat|ناكل|نَاكُلْ
                action.drink|نشرب|نِشْرُبْ
                action.sleep|نرقد|نُرْڨُدْ
                action.sit|نقعد|نُڨْعُدْ
                action.walk|نمشي|نْمْشِي
                action.stand|نوقف|نُوقَفْ
                action.wash|نغسل|نِغْسِلْ
                action.speak|نهدر|نَهْدَرْ
                action.hear|نسمع|نِسْمَعْ
                action.open|نحل|نْحِلّ
                action.close|نسكر|نْسَكِّرْ
                action.read|نقرا|نَقْرَا
                action.laugh|نضحك|نِضْحَكْ
                action.see|نشوف|نْشُوفْ
                action.dress|نلبس|نِلْبِسْ
                action.pray|نصلي|نْصَلِّي
                animal.cat|قطة|ڨَطَّة
                animal.dog|كلب|كَلْبْ
                animal.sheep|خروف|خَرُوفْ
                animal.horse|حصان|حِصَانْ
                animal.cow|بقرة|بَڨْرَة
                animal.sparrow|زاوش|زَاوْشْ
                animal.pigeon|حمامة|حْمَامَة
                animal.chicken|دجاجة|دْجَاجَة
                animal.duck|بطة|بَطَّة
                animal.camel|جمل|جَمَلْ
                animal.rooster|ديك|دِيكْ
                animal.rabbit|أرنب|أَرْنَبْ
                transport.car|طوموبيل|طُومُوبِيلْ
                transport.taxi|طاكسي|طَاكْسِي
                transport.bus|طوبيس|طُوبِيسْ
                transport.minibus|حافلة صغيرة|حَافِلَة صَغِيرَة
                transport.bicycle|فيلو|فِيلُو
                transport.motorcycle|موتو|مُوتُو
                transport.train|تران|تْرَانْ
                transport.airplane|طيارة|طَيَّارَة
                transport.helicopter|هليكوبتر|هِلِيكُوبْتَرْ
                transport.ship|باخرة|بَاخِرَة
                transport.boat|قارب|قَارِبْ
                transport.truck|كاميون|كَامْيُونْ
                greeting.salam|السلام عليكم|السَّلَامُ عَلَيْكُمْ
                greeting.reply|وعليكم السلام|وَعَلَيْكُمُ السَّلَامْ
                greeting.hello|مرحبا|مَرْحَبَا
                greeting.welcome|أهلا وسهلا|أَهْلًا وَسَهْلًا
                greeting.how_are_you|لاباس عليك؟|لَابَاسْ عَلِيكْ؟
                greeting.fine|الحمد لله|الْحَمْدُ لِلَّهْ
                greeting.good_morning|صباح الخير|صَبَاحْ الْخِيرْ
                greeting.good_evening|مسا الخير|مَسَا الْخِيرْ
                greeting.brighten|نورت|نَوَّرْتْ
                greeting.good_health|يعطيك الصحة|يَعْطِيكْ الصَّحَّة
                greeting.goodbye|بالسلامة|بِالسَّلَامَة
                greeting.safe|في أمان الله|فِي أَمَانِ اللَّهْ
                greeting.thanks|صحا|صَحَّا
                greeting.bless|ربي يبارك فيك|رَبِّي يْبَارِك فِيكْ
            """),
        )
    }

    fun get(dialect: ArabicDialect, semanticKey: String): Entry? =
        entries[dialect]?.get(semanticKey)

    fun sizes(): Map<ArabicDialect, Int> = entries.mapValues { it.value.size }
}
