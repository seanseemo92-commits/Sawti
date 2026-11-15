package com.sawti.domain.model

/**
 * Verbatim v1.6.1 Gemini written proposals, keyed by dialect + semanticKey.
 * The proposals have NOT undergone a native-speaker or MP3 listening review.
 * Original speech text is retained for drift detection. Never applies to USER cards.
 * Source: docs/pronunciation-review/gemini-v161-corrections-1809.csv
 */
internal object GeminiPronunciationReview {
    private data class Candidate(val original: String, val proposed: String)

    private val entries: Map<Pair<ArabicDialect, String>, Candidate> by lazy {
        val data = listOf(
            """
                STANDARD|family.brother_wife|زَوْجَة أَخِي|زَوْجَةُ أَخِي
                STANDARD|family.son_wife|زَوْجَة ابْنِي|زَوْجَةُ ابْنِي
                STANDARD|request.rest|أُرِيدُ أَن أَرْتَاح|أُرِيدُ أَنْ أَرْتَاحَ
                STANDARD|request.toilet|أُرِيدُ الذَّهَابَ إِلَى الحَمَّام|أُرِيدُ الذَّهَابَ إِلَى الْحَمَّامِ
                STANDARD|request.adjust_pillow|عَدِّل لِي الوِسَادَة|عَدِّلْ لِيَ الْوِسَادَةَ
                STANDARD|request.cold|أَشْعُرُ بِالبَرْد|أَشْعُرُ بِالْبَرْدِ
                STANDARD|dish.arabic.rice_meat|أَرُزّ بِاللَّحْم|أَرُزٌّ بِاللَّحْمِ
                STANDARD|dish.arabic.grilled_chicken|دَجَاجٌ مَشْوِي|دَجَاجٌ مَشْوِيٌّ
                STANDARD|dish.arabic.lentil_soup|حَسَاءُ العَدَسِ|حَسَاءُ الْعَدَسِ
                STANDARD|dish.arabic.vegetable_stew|يَخْنَة خُضَار|يَخْنَةُ خُضَارٍ
                STANDARD|dish.arabic.mixed_grill|مَشْوِيَّات مُشَكَّلَة|مَشْوِيَّاتٌ مُشَكَّلَةٌ
                STANDARD|dish.arabic.green_salad|سَلَطَة خَضْرَاء|سَلَطَةٌ خَضْرَاءُ
                STANDARD|dish.arabic.cooked_fish|سَمَكٌ مَطْهُوّ|سَمَكٌ مَطْهُوٌّ
                STANDARD|dish.arabic.baked_pastries|فَطَائِرُ مَخْبُوزَة|فَطَائِرُ مَخْبُوزَةٌ
                STANDARD|dish.arabic.vegetable_soup|حَسَاءُ خُضَار|حَسَاءُ خُضَارٍ
                STANDARD|dish.arabic.boiled_meat|لَحْم مَسْلُوق|لَحْمٌ مَسْلُوقٌ
                STANDARD|dish.arabic.baked_bread|خُبْزٌ مَخْبُوز|خُبْزٌ مَخْبُوزٌ
                STANDARD|dish.arabic.fruits_legumes|ثِمَارٌ وَبُقُولِيَّات|ثِمَارٌ وَبُقُولِيَّاتٌ
                STANDARD|dish.arabic.boiled_eggs|بَيْض مَسْلُوق|بَيْضٌ مَسْلُوقٌ
                STANDARD|dish.arabic.dates_milk|تَمْر وَحَلِيب|تَمْرٌ وَحَلِيبٌ
                STANDARD|dish.arabic.arabic_dessert|حَلْوَى عَرَبِيَّة|حَلْوَى عَرَبِيَّةٌ
                STANDARD|routine.call_to_prayer|أَذَان|أَذَانٌ
                STANDARD|routine.ablution|وُضُوء|وُضُوءٌ
                STANDARD|routine.prayer|صَلَاة|صَلَاةٌ
                STANDARD|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادَةُ صَلَاةٍ
                STANDARD|routine.prayer_beads|مِسْبَحَة|مِسْبَحَةٌ
                STANDARD|routine.quran|مُصْحَف|مُصْحَفٌ
                STANDARD|routine.evening_coffee|قَهْوَة المَسَاء|قَهْوَةُ الْمَسَاءِ
                STANDARD|routine.sleep|نَوْم|نَوْمٌ
                STANDARD|routine.rest|رَاحَة|رَاحَةٌ
                STANDARD|routine.walk|مَشْي|مَشْيٌ
                STANDARD|vegetable.tomato|طَمَاطِم|طَمَاطِمُ
                STANDARD|vegetable.potato|بَطَاطِس|بَطَاطِسُ
                STANDARD|vegetable.onion|بَصَل|بَصَلٌ
                STANDARD|vegetable.garlic|ثُوم|ثُومٌ
                STANDARD|vegetable.carrot|جَزَر|جَزَرٌ
                STANDARD|vegetable.cucumber|خِيَار|خِيَارٌ
                STANDARD|vegetable.pepper|فُلْفُل|فُلْفُلٌ
                STANDARD|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانٌ
                STANDARD|vegetable.zucchini|كُوسَة|كُوسَةٌ
                STANDARD|vegetable.lettuce|خَسّ|خَسٌّ
                STANDARD|vegetable.parsley|بَقْدُونِس|بَقْدُونِسٌ
                STANDARD|vegetable.coriander|كُزْبُرَة|كُزْبُرَةٌ
                STANDARD|vegetable.lemon|لِيمُون|لَيْمُونٌ
                STANDARD|vegetable.peas|بَازِلَّاء|بَازِلَّاءُ
                STANDARD|vegetable.green_beans|فَاصُولِيَا خَضْرَاء|فَاصُولِيَا خَضْرَاءُ
                STANDARD|vegetable.spinach|سَبَانِخ|سَبَانِخُ
                STANDARD|vegetable.cauliflower|قُرْنَبِيط|قَرْنَبِيطٌ
                STANDARD|vegetable.radish|فُجْل|فُجْلٌ
                STANDARD|vegetable.pumpkin|يَقْطِين|يَقْطِينٌ
                STANDARD|vegetable.mint|نَعْنَاع|نَعْنَاعٌ
                STANDARD|vegetable.corn|ذُرَة|ذُرَةٌ
                STANDARD|fruit.apple|تُفَّاح|تُفَّاحٌ
                STANDARD|fruit.banana|مَوْز|مَوْزٌ
                STANDARD|fruit.orange|بُرْتُقَال|بُرْتُقَالٌ
                STANDARD|fruit.grapes|عِنَب|عِنَبٌ
                STANDARD|fruit.strawberry|فَرَاوْلَة|فَرَاوِلَةٌ
                STANDARD|fruit.watermelon|بِطِّيخ|بِطِّيخٌ
                STANDARD|fruit.melon|شَمَّام|شَمَّامٌ
                STANDARD|fruit.peach|خَوْخ|خَوْخٌ
                STANDARD|fruit.apricot|مِشْمِش|مِشْمِشٌ
                STANDARD|fruit.pomegranate|رُمَّان|رُمَّانٌ
                STANDARD|fruit.figs|تِين|تِينٌ
                STANDARD|fruit.dates|تَمْر|تَمْرٌ
                STANDARD|fruit.pineapple|أَنَانَاس|أَنَانَاسُ
                STANDARD|fruit.cherry|كَرَز|كَرَزٌ
                STANDARD|fruit.berries|تُوت|تُوتٌ
                STANDARD|fruit.mandarin|يُوسُفِيّ|يُوسُفِيٌّ
                STANDARD|fruit.coconut|جَوْزُ الهِنْد|جَوْزُ الْهِنْدِ
                STANDARD|food.bread|خُبْز|خُبْزٌ
                STANDARD|food.milk|حَلِيب|حَلِيبٌ
                STANDARD|food.eggs|بَيْض|بَيْضٌ
                STANDARD|food.cheese|جُبْن|جُبْنٌ
                STANDARD|food.butter|زُبْدَة|زُبْدَةٌ
                STANDARD|food.rice|أَرُزّ|أَرُزٌّ
                STANDARD|food.pasta|مَعْكَرُونَة|مَعْكَرُونَةٌ
                STANDARD|food.soup|حَسَاء|حَسَاءٌ
                STANDARD|food.meat|لَحْم|لَحْمٌ
                STANDARD|food.chicken|دَجَاج|دَجَاجٌ
                STANDARD|food.fish|سَمَك|سَمَكٌ
                STANDARD|food.olives|زَيْتُون|زَيْتُونٌ
                STANDARD|food.oil|زَيْت|زَيْتٌ
                STANDARD|food.tea|شَاي|شَايٌ
                STANDARD|food.coffee|قَهْوَة|قَهْوَةٌ
                STANDARD|food.juice|عَصِير|عَصِيرٌ
                STANDARD|food.water|مَاء|مَاءٌ
                STANDARD|food.honey|عَسَل|عَسَلٌ
                STANDARD|food.biscuit|بِسْكُوِيت|بَسْكَوِيتٌ
                STANDARD|food.salt|مِلْح|مِلْحٌ
                STANDARD|food.sugar|سُكَّر|سُكَّرٌ
                STANDARD|home.bed|سَرِير|سَرِيرٌ
                STANDARD|home.pillow|وِسَادَة|وِسَادَةٌ
                STANDARD|home.blanket|بَطَّانِيَّة|بَطَّانِيَّةٌ
                STANDARD|home.table|طَاوِلَة|طَاوِلَةٌ
                STANDARD|home.chair|كُرْسِي|كُرْسِيٌّ
                STANDARD|home.sofa|أَرِيكَة|أَرِيكَةٌ
                STANDARD|home.door|بَاب|بَابٌ
                STANDARD|home.window|نَافِذَة|نَافِذَةٌ
                STANDARD|home.television|تِلِفِزْيُون|تِلْفِزْيُونٌ
                STANDARD|home.fridge|ثَلَّاجَة|ثَلَّاجَةٌ
                STANDARD|home.washing_machine|غَسَّالَة|غَسَّالَةٌ
                STANDARD|home.stove|مَوْقِد|مَوْقِدٌ
                STANDARD|home.oven|فُرْن|فُرْنٌ
                STANDARD|home.air_conditioner|مُكَيِّف|مُكَيِّفٌ
                STANDARD|home.fan|مِرْوَحَة|مِرْوَحَةٌ
                STANDARD|home.wardrobe|خِزَانَة|خِزَانَةٌ
                STANDARD|home.key|مِفْتَاح|مِفْتَاحٌ
                STANDARD|home.clock|سَاعَة|سَاعَةٌ
                STANDARD|home.carpet|بِسَاط|بِسَاطٌ
                STANDARD|home.kitchen|مَطْبَخ|مَطْبَخٌ
                STANDARD|home.cup|كُوب|كُوبٌ
                STANDARD|home.tray|صِينِيَّة|صِينِيَّةٌ
                STANDARD|home.spoon|مِلْعَقَة|مِلْعَقَةٌ
                STANDARD|home.fork|شَوْكَة|شَوْكَةٌ
                STANDARD|home.knife|سِكِّين|سِكِّينٌ
                STANDARD|home.teapot|إِبْرِيق شَاي|إِبْرِيقُ شَايٍ
                STANDARD|home.pot|طَنْجَرَة|طَنْجَرَةٌ
                STANDARD|home.water_bottle|قَارُورَة مَاء|قَارُورَةُ مَاءٍ
                STANDARD|clothing.tshirt|قَمِيص قَصِير الأَكْمَام|قَمِيصٌ قَصِيرُ الْأَكْمَامِ
                STANDARD|clothing.shirt|قَمِيص|قَمِيصٌ
                STANDARD|clothing.jacket|سُتْرَة|سُتْرَةٌ
                STANDARD|clothing.shorts|سِرْوَال قَصِير|سِرْوَالٌ قَصِيرٌ
                STANDARD|clothing.blouse|بُلُوزَة|بُلُوزَةٌ
                STANDARD|clothing.trousers|سِرْوَال|سِرْوَالٌ
                STANDARD|clothing.dress|فُسْتَان|فُسْتَانٌ
                STANDARD|clothing.robe|رِدَاء|رِدَاءٌ
                STANDARD|clothing.abaya|عَبَاءَة|عَبَاءَةٌ
                STANDARD|clothing.hijab|حِجَاب|حِجَابٌ
                STANDARD|clothing.scarf|وِشَاح|وِشَاحٌ
                STANDARD|clothing.shoes|حِذَاء|حِذَاءٌ
                STANDARD|clothing.socks|جَوَارِب|جَوَارِبُ
                STANDARD|clothing.coat|مِعْطَف|مِعْطَفٌ
                STANDARD|clothing.glasses|نَظَّارَات|نَظَّارَاتٌ
                STANDARD|clothing.cap|قُبَّعَة|قُبَّعَةٌ
                STANDARD|clothing.watch|سَاعَة يَد|سَاعَةُ يَدٍ
                STANDARD|clothing.bag|حَقِيبَة|حَقِيبَةٌ
                STANDARD|clothing.sandals|صَنْدَل|صَنْدَلٌ
                STANDARD|clothing.high_heels|حِذَاء بِكَعْبٍ عَال|حِذَاءٌ بِكَعْبٍ عَالٍ
                STANDARD|clothing.pajamas|مَلَابِس النَّوْم|مَلَابِسُ النَّوْمِ
                STANDARD|clothing.belt|حِزَام|حِزَامٌ
                STANDARD|clothing.ring|خَاتَم|خَاتَمٌ
                STANDARD|body.eye|عَيْن|عَيْنٌ
                STANDARD|body.nose|أَنْف|أَنْفٌ
                STANDARD|body.mouth|فَم|فَمٌ
                STANDARD|body.tongue|لِسَان|لِسَانٌ
                STANDARD|body.ear|أُذُن|أُذُنٌ
                STANDARD|body.hand|يَد|يَدٌ
                STANDARD|body.leg|سَاق|سَاقٌ
                STANDARD|body.head|رَأْس|رَأْسٌ
                STANDARD|body.hair|شَعْر|شَعْرٌ
                STANDARD|body.back|ظَهْر|ظَهْرٌ
                STANDARD|body.abdomen|بَطْن|بَطْنٌ
                STANDARD|body.knee|رُكْبَة|رُكْبَةٌ
                STANDARD|body.finger|إِصْبَع|إِصْبَعٌ
                STANDARD|body.neck|رَقَبَة|رَقَبَةٌ
                STANDARD|body.chest|صَدْر|صَدْرٌ
                STANDARD|body.face|وَجْه|وَجْهٌ
                STANDARD|body.tooth|سِنّ|سِنٌّ
                STANDARD|body.heart|قَلْب|قَلْبٌ
                STANDARD|body.shoulder|كَتِف|كَتِفٌ
                STANDARD|action.eat|آكُل|آكُلُ
                STANDARD|action.drink|أَشْرَب|أَشْرَبُ
                STANDARD|action.sleep|أَنَام|أَنَامُ
                STANDARD|action.sit|أَجْلِس|أَجْلِسُ
                STANDARD|action.stand|أَقِف|أَقِفُ
                STANDARD|action.wash|أَغْسِل|أَغْسِلُ
                STANDARD|action.speak|أَتَكَلَّم|أَتَكَلَّمُ
                STANDARD|action.hear|أَسْمَع|أَسْمَعُ
                STANDARD|action.open|أَفْتَح|أَفْتَحُ
                STANDARD|action.close|أُغْلِق|أُغْلِقُ
                STANDARD|action.read|أَقْرَأ|أَقْرَأُ
                STANDARD|action.laugh|أَضْحَك|أَضْحَكُ
                STANDARD|action.dress|أَلْبَس|أَلْبَسُ
                STANDARD|animal.cat|قِطَّة|قِطَّةٌ
                STANDARD|animal.dog|كَلْب|كَلْبٌ
                STANDARD|animal.sheep|خَرُوف|خَرُوفٌ
                STANDARD|animal.horse|حِصَان|حِصَانٌ
                STANDARD|animal.cow|بَقَرَة|بَقَرَةٌ
                STANDARD|animal.sparrow|عُصْفُور|عُصْفُورٌ
                STANDARD|animal.pigeon|حَمَامَة|حَمَامَةٌ
                STANDARD|animal.chicken|دَجَاجَة|دَجَاجَةٌ
                STANDARD|animal.duck|بَطَّة|بَطَّةٌ
                STANDARD|animal.camel|جَمَل|جَمَلٌ
                STANDARD|animal.rooster|دِيك|دِيكٌ
                STANDARD|animal.rabbit|أَرْنَب|أَرْنَبٌ
                STANDARD|transport.car|سَيَّارَة|سَيَّارَةٌ
                STANDARD|transport.taxi|سَيَّارَة أُجْرَة|سَيَّارَةُ أُجْرَةٍ
                STANDARD|transport.bus|حَافِلَة|حَافِلَةٌ
                STANDARD|transport.minibus|حَافِلَة صَغِيرَة|حَافِلَةٌ صَغِيرَةٌ
                STANDARD|transport.bicycle|دَرَّاجَة هَوَائِيَّة|دَرَّاجَةٌ هَوَائِيَّةٌ
                STANDARD|transport.motorcycle|دَرَّاجَة نَارِيَّة|دَرَّاجَةٌ نَارِيَّةٌ
                STANDARD|transport.train|قِطَار|قِطَارٌ
                STANDARD|transport.airplane|طَائِرَة|طَائِرَةٌ
                STANDARD|transport.helicopter|مِرْوَحِيَّة|مِرْوَحِيَّةٌ
                STANDARD|transport.ship|سَفِينَة|سَفِينَةٌ
                STANDARD|transport.boat|قَارِب|قَارِبٌ
                STANDARD|transport.truck|شَاحِنَة|شَاحِنَةٌ
                STANDARD|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                STANDARD|greeting.how_are_you|كَيْفَ حَالُك؟|كَيْفَ حَالُكَ؟
                STANDARD|greeting.fine|الحَمْدُ لِلَّهِ|الْحَمْدُ لِلَّهِ
                STANDARD|greeting.good_morning|صَبَاحُ الخَيْرِ|صَبَاحُ الْخَيْرِ
                STANDARD|greeting.good_evening|مَسَاءُ الخَيْرِ|مَسَاءُ الْخَيْرِ
                STANDARD|greeting.brighten|نَوَّرْت|نَوَّرْتَ
                STANDARD|greeting.good_health|يُعْطِيكَ العَافِيَة|يُعْطِيكَ الْعَافِيَةَ
                STANDARD|greeting.thanks|شُكْرًا لَك|شُكْرًا لَكَ
                STANDARD|greeting.bless|بَارَكَ اللَّهُ فِيك|بَارَكَ اللَّهُ فِيكَ
            """.trimIndent(),
            """
                LIBYAN|family.father|بُوي|بُويْ
                LIBYAN|family.brother|خُوي|خُويْ
                LIBYAN|family.grandmother|حَنَّاي|حَنَّايْ
                LIBYAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                LIBYAN|family.paternal_uncle_son|وِلْد عَمِّي|وِلْدْ عَمِّي
                LIBYAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                LIBYAN|family.paternal_aunt_son|وِلْد عَمْتِي|وِلْدْ عَمْتِي
                LIBYAN|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتِي
                LIBYAN|family.maternal_uncle_son|وِلْد خَالِي|وِلْدْ خَالِي
                LIBYAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                LIBYAN|family.maternal_aunt_son|وِلْد خَالْتِي|وِلْدْ خَالْتِي
                LIBYAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتِي
                LIBYAN|family.brother_son|وِلْد خُوي|وِلْدْ خُويْ
                LIBYAN|family.brother_daughter|بِنْت خُوي|بِنْتْ خُويْ
                LIBYAN|family.sister_son|وِلْد أُخْتِي|وِلْدْ أُخْتِي
                LIBYAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                LIBYAN|family.husband_mother|أُم رَاجْلِي|أُمْ رَاجْلِي
                LIBYAN|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتِي
                LIBYAN|family.husband_sister|أُخْت رَاجْلِي|أُخْتْ رَاجْلِي
                LIBYAN|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتِي
                LIBYAN|family.sister_husband|رَاجِل أُخْتِي|رَاجِلْ أُخْتِي
                LIBYAN|family.brother_wife|مَرْت خُوي|مَرْتْ خُويْ
                LIBYAN|family.daughter_husband|رَاجِل بِنْتِي|رَاجِلْ بِنْتِي
                LIBYAN|family.son_wife|مَرْت وَلْدِي|مَرْتْ وِلْدِي
                LIBYAN|family.female_in_law|نَسِيبَتِي|نَسِيبْتِي
                LIBYAN|family.male_relative|قَرِيبِي|گَرِيبِي
                LIBYAN|family.female_relative|قَرِيبَتِي|گَرِيبْتِي
                LIBYAN|request.water|نِبِّي مَيَّة|نِبِّي إِمَّيَّهْ
                LIBYAN|request.rest|نِبِّي نِرْتَاح|نِبِّي نِرْتَاحْ
                LIBYAN|request.toilet|نِبِّي الحَمَّام|نِبِّي الْحَمَّامْ
                LIBYAN|request.adjust_pillow|عَدِّل لِي المِخَدَّة|عَدِّلْ لِي الْمِخَدَّة
                LIBYAN|request.headache|رَاسِي يُوجَع فِيَّا|رَاسِي يُوجَعْ فِيَّا
                LIBYAN|request.cold|نِحِسّ بِالبَرْد|نِحِسّْ بِالْبَرْدْ
                LIBYAN|dish.bazin|بَازِين|بَازِينْ
                LIBYAN|dish.reshda_kaskas|رِشْدَة كِسْكَاس|رِشْدِة كِسْكَاسْ
                LIBYAN|dish.reshda_borma|رِشْدَة بُرْمَة|رِشْدِة بُرْمَة
                LIBYAN|dish.mbakbaka|مَكَرُونَة مبَكْبَكَة|مَكْرُونَة مْبَكْبْكَة
                LIBYAN|dish.pasta_masqiya|مَكَرُونَة مَسْقِيَّة|مَكْرُونَة مِسْگِيَّة
                LIBYAN|dish.couscous_fish|كُسْكْسِي بِالحُوت|كُسْكْسِي بِالْحُوتْ
                LIBYAN|dish.rice_blaw|رُز بِلَاو|رُزّْ بِلَاوْ
                LIBYAN|dish.potato_stew|طبِيخَة بَطَاطَا|طْبِيخِة بَطَاطَا
                LIBYAN|dish.osban|عُصْبَان|عُصْبَانْ
                LIBYAN|dish.bourek|بُورِيك|بُورِيكْ
                LIBYAN|dish.khobz_tajin|خُبْزَة طَاجِين|خُبْزِة طَاجِينْ
                LIBYAN|dish.maqroudh|مَقْرُوض|مَگْرُوضْ
                LIBYAN|dish.mint_tea|شَاهِي بِالنَّعْنَاع|شَاهِي بِالنَّعْنَاعْ
                LIBYAN|dish.arabic_coffee|قَهْوَة عَرَبِيَّة|گَهْوَة عَرَبِيَّة
                LIBYAN|dish.mbattan|مِبَطَّن|مْبَطَّنْ
                LIBYAN|dish.hraymi|حَرَايْمِي|حْرَايْمِي
                LIBYAN|dish.sfinz|سفِنْز|سْفِنْزْ
                LIBYAN|dish.zmeeta|زُمِيطَة|زْمِيطَة
                LIBYAN|dish.aysh_laban|عِيش بِاللَّبَن|عِيشْ بِاللَّبَنْ
                LIBYAN|routine.call_to_prayer|أَذَان|أَذَانْ
                LIBYAN|routine.ablution|وُضُوء|وُضُوءْ
                LIBYAN|routine.quran|مُصْحَف|مُصْحَفْ
                LIBYAN|routine.evening_coffee|قَهْوَة العَشِيَّة|گَهْوِة الْعَشِيَّة
                LIBYAN|routine.sleep|نُوم|نُومْ
                LIBYAN|routine.walk|مَشْي|مَشِي
                LIBYAN|vegetable.tomato|طَمَاطِم|طَمَاطِمْ
                LIBYAN|vegetable.onion|بَصَل|بَصَلْ
                LIBYAN|vegetable.garlic|تُوم|تُومْ
                LIBYAN|vegetable.carrot|جَزَر|جَزَرْ
                LIBYAN|vegetable.cucumber|خِيَار|خِيَارْ
                LIBYAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                LIBYAN|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانْ
                LIBYAN|vegetable.lettuce|خَسّ|خَسّْ
                LIBYAN|vegetable.parsley|مَعْدَنُوس|مَعْدَنُوسْ
                LIBYAN|vegetable.coriander|قُصْبُر|گُصْبُرْ
                LIBYAN|vegetable.lemon|لِيمُون|لِيمُونْ
                LIBYAN|vegetable.peas|بَازِيلْيَة|بَازِيلْيَا
                LIBYAN|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                LIBYAN|vegetable.cauliflower|قَرْنَبِيط|گَرْنَبِيطْ
                LIBYAN|vegetable.radish|فِجِل|فِجِلْ
                LIBYAN|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                LIBYAN|fruit.apple|تُفَّاح|تُفَّاحْ
                LIBYAN|fruit.banana|مُوز|مُوزْ
                LIBYAN|fruit.orange|بُرْتُقَال|بُرْتُقَالْ
                LIBYAN|fruit.grapes|عِنَب|عِنَبْ
                LIBYAN|fruit.watermelon|دَلَّاع|دَلَّاعْ
                LIBYAN|fruit.melon|قَلْعَاوِي|گَلْعَاوِي
                LIBYAN|fruit.peach|خُوخ|خُوخْ
                LIBYAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                LIBYAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                LIBYAN|fruit.figs|كَرْمُوس|كَرْمُوسْ
                LIBYAN|fruit.dates|تَمْر|تَمْرْ
                LIBYAN|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                LIBYAN|fruit.cherry|كَرَز|كَرَزْ
                LIBYAN|fruit.berries|تُوت|تُوتْ
                LIBYAN|fruit.mandarin|مَنْدَرِين|مَنْدَرِينْ
                LIBYAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                LIBYAN|food.milk|حَلِيب|حَلِيبْ
                LIBYAN|food.eggs|بِيض|بِيضْ
                LIBYAN|food.rice|رُزّ|رُزّْ
                LIBYAN|food.pasta|مَكَرُونَة|مَكْرُونَة
                LIBYAN|food.meat|لَحْم|لَحْمْ
                LIBYAN|food.chicken|دجَاج|دْجَاجْ
                LIBYAN|food.fish|حُوت|حُوتْ
                LIBYAN|food.olives|زِيتُون|زِيتُونْ
                LIBYAN|food.oil|زِيت|زِيتْ
                LIBYAN|food.coffee|قَهْوَة|گَهْوَة
                LIBYAN|food.juice|عَصِير|عَصِيرْ
                LIBYAN|food.water|مَيَّة|إِمَّيَّهْ
                LIBYAN|food.honey|عَسَل|عَسَلْ
                LIBYAN|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                LIBYAN|food.salt|مِلْح|مِلْحْ
                LIBYAN|food.sugar|سُكَّر|سُكَّرْ
                LIBYAN|home.bed|سَرِير|سَرِيرْ
                LIBYAN|home.pillow|مَخَدَّة|مِخَدَّة
                LIBYAN|home.blanket|وِرْغَان|وِرْغَانْ
                LIBYAN|home.sofa|صَالُون|صَالُونْ
                LIBYAN|home.door|بَاب|بَابْ
                LIBYAN|home.window|رُوشِن|رُوشِنْ
                LIBYAN|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                LIBYAN|home.stove|غَاز|غَازْ
                LIBYAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                LIBYAN|home.wardrobe|دُولَاب|دُولَابْ
                LIBYAN|home.key|مِفْتَاح|مِفْتَاحْ
                LIBYAN|home.carpet|بِسَاط|بِسَاطْ
                LIBYAN|home.spoon|كَاشِيك|كَاشِيكْ
                LIBYAN|home.knife|مُوس|مُوسْ
                LIBYAN|home.teapot|بَرَّاد|بَرَّادْ
                LIBYAN|home.pot|طَنْجَرَة|طَنْجْرَة
                LIBYAN|home.water_bottle|شِيشَة امَيَّه|شِيشِة إِمَّيَّهْ
                LIBYAN|clothing.tshirt|تِيشِرْت|تِيشِرْتْ
                LIBYAN|clothing.shirt|قَمِيص|قَمِيصْ
                LIBYAN|clothing.shorts|شُورْت|شُورْتْ
                LIBYAN|clothing.trousers|سِرْوَال|سِرْوَالْ
                LIBYAN|clothing.dress|فُسْتَان|فُسْتَانْ
                LIBYAN|clothing.robe|رُوب|رُوبْ
                LIBYAN|clothing.hijab|حِجَاب|حِجَابْ
                LIBYAN|clothing.scarf|وِشَاح|وِشَاحْ
                LIBYAN|clothing.shoes|كَنْدَرَة|كَنْدْرَة
                LIBYAN|clothing.socks|شخَاشِير|شْخَاشِيرْ
                LIBYAN|clothing.coat|كَبُّوط|كَبُّوطْ
                LIBYAN|clothing.cap|طَاقِيَّة|طَاگِيَّة
                LIBYAN|clothing.watch|سَاعِة يَد|سَاعِة يَدّْ
                LIBYAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                LIBYAN|clothing.high_heels|كَنْدَرَة كَعَب|كَنْدْرِة كَعَبْ
                LIBYAN|clothing.ring|خَاتَم|خَاتَمْ
                LIBYAN|body.eye|عِين|عِينْ
                LIBYAN|body.nose|خَشِم|خَشِمْ
                LIBYAN|body.mouth|فَم|فَمْ
                LIBYAN|body.tongue|لِسَان|لِسَانْ
                LIBYAN|body.ear|وِذِن|وِذِنْ
                LIBYAN|body.hand|يَد|يَدْ
                LIBYAN|body.leg|رِجْل|رِجْلْ
                LIBYAN|body.head|رَاس|رَاسْ
                LIBYAN|body.hair|شَعْر|شَعْرْ
                LIBYAN|body.back|ظَهْر|ظَهْرْ
                LIBYAN|body.abdomen|بَطْن|بَطْنْ
                LIBYAN|body.finger|صْبَع|صْبَعْ
                LIBYAN|body.neck|رَقْبَة|رَگْبَة
                LIBYAN|body.chest|صَدْر|صَدْرْ
                LIBYAN|body.face|وَجِه|وَجِهْ
                LIBYAN|body.heart|قَلْب|گَلْبْ
                LIBYAN|body.shoulder|كِتْف|كِتْفْ
                LIBYAN|action.eat|نَاكُل|نَاكُلْ
                LIBYAN|action.drink|نِشْرَب|نِشْرَبْ
                LIBYAN|action.sleep|نُرْقُد|نُرْگُدْ
                LIBYAN|action.sit|نِقَعْمِز|نْگَعْمِزْ
                LIBYAN|action.stand|نُوقَف|نُوگَفْ
                LIBYAN|action.wash|نِغْسِل|نِغْسِلْ
                LIBYAN|action.hear|نِسْمَع|نِسْمَعْ
                LIBYAN|action.open|نِفْتَح|نِفْتَحْ
                LIBYAN|action.close|نِسَكِّر|نِسَكِّرْ
                LIBYAN|action.read|نَقْرَا|نَگْرَا
                LIBYAN|action.laugh|نِضْحَك|نِضْحَكْ
                LIBYAN|action.see|نِشُوف|نِشُوفْ
                LIBYAN|action.dress|نِلْبِس|نِلْبِسْ
                LIBYAN|animal.cat|قَطُّوسَة|گَطُّوسَة
                LIBYAN|animal.dog|كَلْب|كَلْبْ
                LIBYAN|animal.sheep|خَرُوف|خَرُوفْ
                LIBYAN|animal.horse|حصَان|حْصَانْ
                LIBYAN|animal.cow|بَقْرَة|بَگْرَة
                LIBYAN|animal.sparrow|عُصْفُور|عُصْفُورْ
                LIBYAN|animal.pigeon|حَمَامَة|حْمَامَة
                LIBYAN|animal.chicken|دجَاجَة|دْجَاجَة
                LIBYAN|animal.camel|جمَل|جْمَلْ
                LIBYAN|animal.rooster|دِيك|دِيكْ
                LIBYAN|animal.rabbit|أَرْنَب|أَرْنَبْ
                LIBYAN|transport.bus|بَاص|بَاصْ
                LIBYAN|transport.minibus|مِيكْرُوبَاص|مِيكْرُوبَاصْ
                LIBYAN|transport.train|قِطَار|قِطَارْ
                LIBYAN|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                LIBYAN|transport.boat|قَارِب|قَارِبْ
                LIBYAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                LIBYAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                LIBYAN|greeting.hello|هَلَا بِيك|هَلَا بِيكْ
                LIBYAN|greeting.how_are_you|شِن حَالَك؟|شِنْ حَالَكْ؟
                LIBYAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                LIBYAN|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                LIBYAN|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                LIBYAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                LIBYAN|greeting.good_health|يَعْطِيك الصَّحَّة|يَعْطِيكْ الصَّحَّة
                LIBYAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                LIBYAN|greeting.thanks|شُكْرًا لِيك|شُكْرًا لِيكْ
                LIBYAN|greeting.bless|بَارَكَ اللَّهُ فِيك|بَارَكَ اللَّهُ فِيكْ
            """.trimIndent(),
            """
                SAUDI|family.father|أَبُوي|أَبُويْ
                SAUDI|family.brother|أَخُوي|أَخُويْ
                SAUDI|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                SAUDI|family.paternal_uncle_son|وَلَد عَمِّي|وَلَدْ عَمِّي
                SAUDI|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                SAUDI|family.paternal_aunt_son|وَلَد عَمَّتِي|وَلَدْ عَمَّتِي
                SAUDI|family.paternal_aunt_daughter|بِنْت عَمَّتِي|بِنْتْ عَمَّتِي
                SAUDI|family.maternal_uncle_son|وَلَد خَالِي|وَلَدْ خَالِي
                SAUDI|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                SAUDI|family.maternal_aunt_son|وَلَد خَالَتِي|وَلَدْ خَالَتِي
                SAUDI|family.maternal_aunt_daughter|بِنْت خَالَتِي|بِنْتْ خَالَتِي
                SAUDI|family.brother_son|وَلَد أَخُوي|وَلَدْ أَخُويْ
                SAUDI|family.brother_daughter|بِنْت أَخُوي|بِنْتْ أَخُويْ
                SAUDI|family.sister_son|وَلَد أُخْتِي|وَلَدْ أُخْتِي
                SAUDI|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                SAUDI|family.husband_mother|أُم زَوْجِي|أُمْ زَوْجِي
                SAUDI|family.wife_mother|أُم زَوْجَتِي|أُمْ زَوْجَتِي
                SAUDI|family.husband_sister|أُخْت زَوْجِي|أُخْتْ زَوْجِي
                SAUDI|family.wife_sister|أُخْت زَوْجَتِي|أُخْتْ زَوْجَتِي
                SAUDI|family.sister_husband|زَوْج أُخْتِي|زَوْجْ أُخْتِي
                SAUDI|family.brother_wife|زَوْجَة أَخُوي|زَوْجِة أَخُويْ
                SAUDI|family.daughter_husband|زَوْج بِنْتِي|زَوْجْ بِنْتِي
                SAUDI|family.son_wife|زَوْجَة وَلَدِي|زَوْجِة وَلَدِي
                SAUDI|family.female_in_law|نَسِيبَتِي|نَسِيبْتِي
                SAUDI|family.male_relative|قَرِيبِي|گَرِيبِي
                SAUDI|family.female_relative|قَرِيبَتِي|گَرِيبْتِي
                SAUDI|request.water|أَبْغَى مَاي|أَبْغَى مَايْ
                SAUDI|request.tea|أَبْغَى شَاي|أَبْغَى شَايْ
                SAUDI|request.rest|أَبْغَى أَرْتَاح|أَبْغَى أَرْتَاحْ
                SAUDI|request.medicine|أَبْغَى دَوَاي|أَبْغَى دَوَايْ
                SAUDI|request.toilet|أَبْغَى أَرُوح الحَمَّام|أَبْغَى أَرُوحْ الْحَمَّامْ
                SAUDI|request.adjust_pillow|عَدِّل لِي المِخَدَّة|عَدِّلْ لِي الْمِخَدَّة
                SAUDI|request.cold|أَحِس بِالبَرْد|أَحِسْ بِالْبَرْدْ
                SAUDI|dish.saudi.kabsa|كَبْسَة لَحَم|كَبْسِة لَحَمْ
                SAUDI|dish.saudi.jareesh|جِرِيش|جِرِيشْ
                SAUDI|dish.saudi.qursan|قُرْصَان|گُرْصَانْ
                SAUDI|dish.saudi.saleeg|سَلِيق طَائِفِي|سَلِيگْ طَايْفِي
                SAUDI|dish.saudi.haneeth|حَنِيذ|حَنِيذْ
                SAUDI|dish.saudi.matazeez|مَطَازِيز|مَطَازِيزْ
                SAUDI|dish.saudi.marqooq|مَرْقُوق|مَرْگُوگْ
                SAUDI|dish.saudi.madhbi|مَظْبِي دَجَاج|مَظْبِي دَجَاجْ
                SAUDI|dish.saudi.bukhari|رُز بُخَارِي|رُزْ بُخَارِي
                SAUDI|dish.saudi.madfoon|مَدْفُون|مَدْفُونْ
                SAUDI|dish.saudi.liver|كِبْدَة حَاشِي|كِبْدِة حَاشِي
                SAUDI|dish.saudi.masoub|مَعْصُوب|مَعْصُوبْ
                SAUDI|dish.saudi.luqaimat|لِقَيْمَات|لْگَيْمَاتْ
                SAUDI|routine.call_to_prayer|أَذَان|أَذَانْ
                SAUDI|routine.ablution|وُضُوء|وُضُوءْ
                SAUDI|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صَلَاة
                SAUDI|routine.quran|مُصْحَف|مُصْحَفْ
                SAUDI|routine.evening_coffee|قَهْوَة العَصْر|گَهْوِة الْعَصْرْ
                SAUDI|routine.sleep|نُوم|نُومْ
                SAUDI|routine.walk|مَشْي|مَشِي
                SAUDI|vegetable.tomato|طَمَاطِم|طَمَاطِمْ
                SAUDI|vegetable.potato|بَطَاطِس|بَطَاطِسْ
                SAUDI|vegetable.onion|بَصَل|بَصَلْ
                SAUDI|vegetable.garlic|ثُوم|ثُومْ
                SAUDI|vegetable.carrot|جَزَر|جَزَرْ
                SAUDI|vegetable.cucumber|خِيَار|خِيَارْ
                SAUDI|vegetable.pepper|فِلْفِل|فِلْفِلْ
                SAUDI|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانْ
                SAUDI|vegetable.lettuce|خَسّ|خَسّْ
                SAUDI|vegetable.parsley|بَقْدُونِس|بَقْدُونِسْ
                SAUDI|vegetable.lemon|لِيمُون|لِيمُونْ
                SAUDI|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                SAUDI|vegetable.radish|فِجِل|فِجِلْ
                SAUDI|vegetable.pumpkin|قَرَع|قَرَعْ
                SAUDI|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                SAUDI|fruit.apple|تُفَّاح|تُفَّاحْ
                SAUDI|fruit.banana|مُوز|مُوزْ
                SAUDI|fruit.orange|بُرْتُقَال|بُرْتُقَالْ
                SAUDI|fruit.grapes|عِنَب|عِنَبْ
                SAUDI|fruit.watermelon|حَبْحَب|حَبْحَبْ
                SAUDI|fruit.melon|شَمَّام|شَمَّامْ
                SAUDI|fruit.peach|خُوخ|خُوخْ
                SAUDI|fruit.apricot|مِشْمِش|مِشْمِشْ
                SAUDI|fruit.pomegranate|رُمَّان|رُمَّانْ
                SAUDI|fruit.figs|تِين|تِينْ
                SAUDI|fruit.dates|تَمْر|تَمْرْ
                SAUDI|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                SAUDI|fruit.cherry|كَرَز|كَرَزْ
                SAUDI|fruit.berries|تُوت|تُوتْ
                SAUDI|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                SAUDI|food.bread|خُبْز|خُبْزْ
                SAUDI|food.milk|حَلِيب|حَلِيبْ
                SAUDI|food.eggs|بِيض|بِيضْ
                SAUDI|food.cheese|جُبْن|جُبْنْ
                SAUDI|food.rice|رُزّ|رُزّْ
                SAUDI|food.meat|لَحْم|لَحْمْ
                SAUDI|food.chicken|دَجَاج|دَجَاجْ
                SAUDI|food.fish|سِمَك|سِمَكْ
                SAUDI|food.olives|زِيتُون|زِيتُونْ
                SAUDI|food.oil|زِيت|زِيتْ
                SAUDI|food.coffee|قَهْوَة|گَهْوَة
                SAUDI|food.juice|عَصِير|عَصِيرْ
                SAUDI|food.water|مَاي|مَايْ
                SAUDI|food.honey|عَسَل|عَسَلْ
                SAUDI|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                SAUDI|food.salt|مِلْح|مِلْحْ
                SAUDI|food.sugar|سُكَّر|سُكَّرْ
                SAUDI|home.bed|سَرِير|سَرِيرْ
                SAUDI|home.sofa|كَنَب|كَنَبْ
                SAUDI|home.door|بَاب|بَابْ
                SAUDI|home.window|شُبَّاك|شُبَّاكْ
                SAUDI|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                SAUDI|home.stove|فُرْن غَاز|فُرْنْ غَازْ
                SAUDI|home.oven|فُرْن|فُرْنْ
                SAUDI|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                SAUDI|home.wardrobe|دُولَاب|دُولَابْ
                SAUDI|home.key|مِفْتَاح|مِفْتَاحْ
                SAUDI|home.carpet|بِسَاط|بِسَاطْ
                SAUDI|home.kitchen|مَطْبَخ|مَطْبَخْ
                SAUDI|home.cup|كُوب|كُوبْ
                SAUDI|home.knife|سِكِّين|سِكِّينْ
                SAUDI|home.teapot|إِبْرِيق شَاي|إِبْرِيقْ شَايْ
                SAUDI|home.pot|قِدْر|گِدْرْ
                SAUDI|home.water_bottle|قَارُورَة مُويَة|قَارُورِة مُويَة
                SAUDI|clothing.tshirt|تِيشِيرْت|تِيشِيرْتْ
                SAUDI|clothing.shirt|قَمِيص|قَمِيصْ
                SAUDI|clothing.jacket|جَاكِيت|جَاكِيتْ
                SAUDI|clothing.shorts|شُورْت|شُورْتْ
                SAUDI|clothing.trousers|بَنْطَلُون|بَنْطَلُونْ
                SAUDI|clothing.dress|فُسْتَان|فُسْتَانْ
                SAUDI|clothing.robe|رُوب|رُوبْ
                SAUDI|clothing.hijab|حِجَاب|حِجَابْ
                SAUDI|clothing.scarf|وِشَاح|وِشَاحْ
                SAUDI|clothing.socks|دِلَاغَات|دِلَاغَاتْ
                SAUDI|clothing.coat|مِعْطَف|مِعْطَفْ
                SAUDI|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                SAUDI|clothing.sandals|صَنْدَل|صَنْدَلْ
                SAUDI|clothing.high_heels|جَزْمَة كَعَب|جَزْمِة كَعَبْ
                SAUDI|clothing.belt|حِزَام|حِزَامْ
                SAUDI|clothing.ring|خَاتَم|خَاتَمْ
                SAUDI|body.eye|عِين|عِينْ
                SAUDI|body.nose|خَشِم|خَشِمْ
                SAUDI|body.mouth|فَم|فَمْ
                SAUDI|body.tongue|لِسَان|لِسَانْ
                SAUDI|body.ear|إِذْن|إِذْنْ
                SAUDI|body.hand|يَد|يَدْ
                SAUDI|body.leg|رِجْل|رِجْلْ
                SAUDI|body.head|رَاس|رَاسْ
                SAUDI|body.hair|شَعْر|شَعْرْ
                SAUDI|body.back|ظَهْر|ظَهْرْ
                SAUDI|body.abdomen|بَطْن|بَطْنْ
                SAUDI|body.finger|إِصْبَع|إِصْبَعْ
                SAUDI|body.neck|رَقْبَة|رْگَبَة
                SAUDI|body.chest|صَدْر|صَدْرْ
                SAUDI|body.face|وَجْه|وَجْهْ
                SAUDI|body.tooth|سِنّ|سِنّْ
                SAUDI|body.heart|قَلْب|قَلْبْ
                SAUDI|body.shoulder|كِتْف|كِتْفْ
                SAUDI|action.eat|آكُل|آكُلْ
                SAUDI|action.drink|أَشْرَب|أَشْرَبْ
                SAUDI|action.sleep|أَنَام|أَنَامْ
                SAUDI|action.sit|أَجْلِس|أَجْلِسْ
                SAUDI|action.stand|أَقُوم|أَگُومْ
                SAUDI|action.wash|أَغْسِل|أَغْسِلْ
                SAUDI|action.speak|أَتَكَلَّم|أَتَكَلَّمْ
                SAUDI|action.hear|أَسْمَع|أَسْمَعْ
                SAUDI|action.open|أَفْتَح|أَفْتَحْ
                SAUDI|action.close|أَسَكِّر|أَسَكِّرْ
                SAUDI|action.read|أَقْرَا|أَگْرَا
                SAUDI|action.laugh|أَضْحَك|أَضْحَكْ
                SAUDI|action.see|أَشُوف|أَشُوفْ
                SAUDI|action.dress|أَلْبَس|أَلْبَسْ
                SAUDI|action.pray|أُصَلِّي|أَصَلِّي
                SAUDI|animal.dog|كَلْب|كَلْبْ
                SAUDI|animal.sheep|خَرُوف|خَرُوفْ
                SAUDI|animal.horse|حِصَان|حِصَانْ
                SAUDI|animal.cow|بَقَرَة|بَگْرَة
                SAUDI|animal.sparrow|عُصْفُور|عُصْفُورْ
                SAUDI|animal.camel|جَمَل|جَمَلْ
                SAUDI|animal.rooster|دِيك|دِيكْ
                SAUDI|animal.rabbit|أَرْنَب|أَرْنَبْ
                SAUDI|transport.bus|بَاص|بَاصْ
                SAUDI|transport.minibus|بَاص صْغِير|بَاصْ صْغِيرْ
                SAUDI|transport.bicycle|سَيْكَل|سَيْكَلْ
                SAUDI|transport.motorcycle|دَبَّاب|دَبَّابْ
                SAUDI|transport.train|قِطَار|قِطَارْ
                SAUDI|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                SAUDI|transport.boat|قَارِب|قَارِبْ
                SAUDI|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                SAUDI|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                SAUDI|greeting.welcome|حَيَّاكَ اللَّه|حَيَّاكْ اللَّهْ
                SAUDI|greeting.how_are_you|كِيف حَالَك؟|كِيفْ حَالَكْ؟
                SAUDI|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                SAUDI|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                SAUDI|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                SAUDI|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                SAUDI|greeting.good_health|يَعْطِيك العَافْيَة|يَعْطِيكْ الْعَافْيَة
                SAUDI|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                SAUDI|greeting.thanks|شُكْرًا لَك|شُكْرًا لَكْ
                SAUDI|greeting.bless|اللَّهُ يبَارِك فِيك|اللَّهْ يْبَارِكْ فِيكْ
            """.trimIndent(),
            """
                EGYPTIAN|family.father|أَبُويَا|أَبويا
                EGYPTIAN|family.husband|جُوزِي|جُوزي
                EGYPTIAN|family.wife|مِرَاتِي|مِراتي
                EGYPTIAN|family.son|اِبْنِي|اِبْني
                EGYPTIAN|family.daughter|بِنْتِي|بِنْتي
                EGYPTIAN|family.brother|أَخُويَا|أَخويا
                EGYPTIAN|family.sister|أُخْتِي|أُخْتي
                EGYPTIAN|family.paternal_aunt|عَمَّتِي|عَمِّتِي
                EGYPTIAN|family.maternal_uncle|خَالِي|خَالي
                EGYPTIAN|family.maternal_aunt|خَالَتِي|خَالْتِي
                EGYPTIAN|family.grandmother|جَدَّتِي|جَدَّتي
                EGYPTIAN|family.grandson|حَفِيدِي|حَفيدي
                EGYPTIAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                EGYPTIAN|family.paternal_uncle_son|اِبْن عَمِّي|اِبْنْ عَمِّي
                EGYPTIAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                EGYPTIAN|family.paternal_aunt_son|اِبْن عَمِّتِي|اِبْنْ عَمِّتي
                EGYPTIAN|family.paternal_aunt_daughter|بِنْت عَمِّتِي|بِنْتْ عَمِّتي
                EGYPTIAN|family.maternal_uncle_son|اِبْن خَالِي|اِبْنْ خَالي
                EGYPTIAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالي
                EGYPTIAN|family.maternal_aunt_son|اِبْن خَالْتِي|اِبْنْ خَالْتي
                EGYPTIAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتي
                EGYPTIAN|family.brother_son|اِبْن أَخُويَا|اِبْنْ أَخويا
                EGYPTIAN|family.brother_daughter|بِنْت أَخُويَا|بِنْتْ أَخويا
                EGYPTIAN|family.sister_son|اِبْن أُخْتِي|اِبْنْ أُخْتي
                EGYPTIAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتي
                EGYPTIAN|family.husband_father|أَبُو جُوزِي|أَبو جُوزي
                EGYPTIAN|family.husband_mother|أُم جُوزِي|أُمْ جُوزي
                EGYPTIAN|family.wife_father|أَبُو مِرَاتِي|أَبو مِراتي
                EGYPTIAN|family.wife_mother|أُم مِرَاتِي|أُمْ مِراتي
                EGYPTIAN|family.husband_brother|أَخُو جُوزِي|أَخو جُوزي
                EGYPTIAN|family.husband_sister|أُخْت جُوزِي|أُخْتْ جُوزي
                EGYPTIAN|family.wife_brother|أَخُو مِرَاتِي|أَخو مِراتي
                EGYPTIAN|family.wife_sister|أُخْت مِرَاتِي|أُخْتْ مِراتي
                EGYPTIAN|family.sister_husband|جُوز أُخْتِي|جُوزْ أُخْتي
                EGYPTIAN|family.brother_wife|مِرَات أَخُويَا|مِراتْ أَخويا
                EGYPTIAN|family.daughter_husband|جُوز بِنْتِي|جُوزْ بِنْتي
                EGYPTIAN|family.son_wife|مِرَات اِبْنِي|مِراتْ اِبْني
                EGYPTIAN|family.male_in_law|نَسِيبِي|نَسيبي
                EGYPTIAN|family.female_in_law|نَسِيبَتِي|نَسِيبْتِي
                EGYPTIAN|family.male_relative|قَرِيبِي|قَريبي
                EGYPTIAN|family.female_relative|قَرِيبَتِي|قَرِيبْتِي
                EGYPTIAN|request.water|عَايِز مَيَّة|عَايِزْ مَيَّة
                EGYPTIAN|request.tea|مُمْكِن شَاي|مُمْكِنْ شَايْ
                EGYPTIAN|request.rest|مُمْكِن أَرْتَاح شْوَيَّة|مُمْكِنْ أَرْتاحْ شْوَيَّة
                EGYPTIAN|request.medicine|مُمْكِن دَوَايَا|مُمْكِنْ دَوايا
                EGYPTIAN|request.toilet|مُمْكِن أَدْخُل الحَمَّام|مُمْكِنْ أَدْخُلْ الْحَمَّامْ
                EGYPTIAN|request.adjust_pillow|ظَبَّطْلِي المَخَدَّة|ظَبَّطْلي الْمَخَدَّة
                EGYPTIAN|request.headache|دِمَاغِي بِتِوجَعْنِي|دِماغي بِتِوجَعْني
                EGYPTIAN|request.cold|أَنَا سَقْعَان|أَنا سَقْعانْ
                EGYPTIAN|dish.egyptian.koshari|كُشَرِي|كُشَري
                EGYPTIAN|dish.egyptian.molokhia|مُلُوخِيَّة بِالتَّقْلِيَة|مُلوخِيَّة بِالتَّقْلِيَة
                EGYPTIAN|dish.egyptian.hawawshi|حَوَاوْشِي|حَواوْشي
                EGYPTIAN|dish.egyptian.ful|فُول مِدَمِّس|فُولْ مِدَمِّسْ
                EGYPTIAN|dish.egyptian.stuffed_pigeon|حَمَام مَحْشِي|حَمامْ مَحْشي
                EGYPTIAN|dish.egyptian.fattah|فَتَّة بِالخَلِّ وَالتُّوم|فَتَّة بِالْخَلّْ وَالتُّومْ
                EGYPTIAN|dish.egyptian.potato_tray|صِينِيِّة بَطَاطِس|صِينِيِّة بَطاطِسْ
                EGYPTIAN|dish.egyptian.kebab_hawawshi|كَبَاب وَحَوَاوْشِي|كَبابْ وَحَواوْشي
                EGYPTIAN|dish.egyptian.mahshi|مَحْشِي مِشَكِّل|مَحْشِي مِشَكِّلْ
                EGYPTIAN|dish.egyptian.trotters_fattah|كَوَارِع وَفَتَّة|كَوارِعْ وَفَتَّة
                EGYPTIAN|dish.egyptian.meat_ruqaq|رُقَاق بِاللَّحْمَة|رُقاقْ بِاللَّحْمَة
                EGYPTIAN|dish.egyptian.bird_tongue_soup|شُورْبِة لِسَان عَصْفُور|شُورْبِة لِسَانْ عَصْفُورْ
                EGYPTIAN|dish.egyptian.umm_ali|أُمّ عَلِي|أُمّْ عَلي
                EGYPTIAN|routine.call_to_prayer|أَذَان|أَذانْ
                EGYPTIAN|routine.ablution|وُضُوء|وُضوءْ
                EGYPTIAN|routine.prayer|صَلَاة|صَلاة
                EGYPTIAN|routine.prayer_rug|سَجَّادَة صَلَاة|سِجَّادِة صَلَاة
                EGYPTIAN|routine.quran|مُصْحَف|مُصْحَفْ
                EGYPTIAN|routine.evening_coffee|قَهْوَة المَسَا|قَهْوِة الْمَسَا
                EGYPTIAN|routine.sleep|نُوم|نُومْ
                EGYPTIAN|routine.walk|مَشْي|مَشْيْ
                EGYPTIAN|vegetable.tomato|طَمَاطِم|طَماطِمْ
                EGYPTIAN|vegetable.potato|بَطَاطِس|بَطاطِسْ
                EGYPTIAN|vegetable.onion|بَصَل|بَصَلْ
                EGYPTIAN|vegetable.garlic|تُوم|تُومْ
                EGYPTIAN|vegetable.carrot|جَزَر|جَزَرْ
                EGYPTIAN|vegetable.cucumber|خِيَار|خِيارْ
                EGYPTIAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                EGYPTIAN|vegetable.eggplant|بِتِنْجَان|بِتِنْجانْ
                EGYPTIAN|vegetable.lettuce|خَسّ|خَسّْ
                EGYPTIAN|vegetable.parsley|بَقْدُونِس|بَقْدونِسْ
                EGYPTIAN|vegetable.lemon|لَمُون|لَمونْ
                EGYPTIAN|vegetable.green_beans|فَاصُولْيَا خَضْرَا|فَاصولْيا خَضْرا
                EGYPTIAN|vegetable.spinach|سَبَانِخ|سَبانِخْ
                EGYPTIAN|vegetable.cauliflower|قَرْنَبِيط|قَرْنَبيطْ
                EGYPTIAN|vegetable.broccoli|بُرُوكْلِي|بُروكْلي
                EGYPTIAN|vegetable.radish|فِجِل|فِجِلْ
                EGYPTIAN|vegetable.pumpkin|قَرَع|قَرَعْ
                EGYPTIAN|vegetable.mint|نَعْنَاع|نَعْناعْ
                EGYPTIAN|fruit.apple|تُفَّاح|تُفَّاحْ
                EGYPTIAN|fruit.banana|مُوز|مُوزْ
                EGYPTIAN|fruit.orange|بُرْتُقَان|بُرْتُقانْ
                EGYPTIAN|fruit.grapes|عِنَب|عِنَبْ
                EGYPTIAN|fruit.strawberry|فَرَاوْلَة|فَراوْلَة
                EGYPTIAN|fruit.mango|مَانْجُو|مَانْجو
                EGYPTIAN|fruit.watermelon|بَطِّيخ|بَطِّيخْ
                EGYPTIAN|fruit.melon|شَمَّام|شَمَّامْ
                EGYPTIAN|fruit.peach|خُوخ|خُوخْ
                EGYPTIAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                EGYPTIAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                EGYPTIAN|fruit.figs|تِين|تِينْ
                EGYPTIAN|fruit.dates|تَمْر|تَمْرْ
                EGYPTIAN|fruit.pineapple|أَنَانَاس|أَناناسْ
                EGYPTIAN|fruit.kiwi|كِيوِي|كِيويْ
                EGYPTIAN|fruit.cherry|كَرَز|كَرَزْ
                EGYPTIAN|fruit.berries|تُوت|تُوتْ
                EGYPTIAN|fruit.mandarin|يُوسَفِي|يُوسَفي
                EGYPTIAN|fruit.avocado|أَفُوكَادُو|أَفوكادو
                EGYPTIAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                EGYPTIAN|food.bread|عِيش|عِيشْ
                EGYPTIAN|food.milk|لَبَن|لَبَنْ
                EGYPTIAN|food.eggs|بِيض|بِيضْ
                EGYPTIAN|food.yogurt|زَبَادِي|زَبادي
                EGYPTIAN|food.rice|رُزّ|رُزّْ
                EGYPTIAN|food.pasta|مَكَرُونَة|مَكَرونَة
                EGYPTIAN|food.meat|لَحْم|لَحْمْ
                EGYPTIAN|food.chicken|فِرَاخ|فِراخْ
                EGYPTIAN|food.fish|سَمَك|سَمَكْ
                EGYPTIAN|food.olives|زِيتُون|زِيتونْ
                EGYPTIAN|food.oil|زِيت|زِيتْ
                EGYPTIAN|food.tea|شَاي|شَايْ
                EGYPTIAN|food.juice|عَصِير|عَصيرْ
                EGYPTIAN|food.honey|عَسَل|عَسَلْ
                EGYPTIAN|food.biscuit|بِسْكُوِيت|بِسْكُويتْ
                EGYPTIAN|food.salt|مِلْح|مِلْحْ
                EGYPTIAN|food.sugar|سُكَّر|سُكَّرْ
                EGYPTIAN|home.bed|سَرِير|سَريرْ
                EGYPTIAN|home.chair|كُرْسِي|كُرْسي
                EGYPTIAN|home.door|بَاب|بَابْ
                EGYPTIAN|home.window|شُبَّاك|شُبَّاكْ
                EGYPTIAN|home.television|تِلِفِزْيُون|تِلِفِزْيونْ
                EGYPTIAN|home.stove|بُوتَاجَاز|بُوتاجازْ
                EGYPTIAN|home.oven|فُرْن|فُرْنْ
                EGYPTIAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                EGYPTIAN|home.wardrobe|دُولَاب|دُولابْ
                EGYPTIAN|home.key|مِفْتَاح|مِفْتاحْ
                EGYPTIAN|home.kitchen|مَطْبَخ|مَطْبَخْ
                EGYPTIAN|home.teapot|بَرَّاد|بَرَّادْ
                EGYPTIAN|home.water_bottle|إِزَازَة مَيَّة|إِزَازِة مَيَّة
                EGYPTIAN|clothing.tshirt|تِيشِيرْت|تِيشيرْتْ
                EGYPTIAN|clothing.shirt|قَمِيص|قَميصْ
                EGYPTIAN|clothing.jacket|جَاكِيت|جَاكيتْ
                EGYPTIAN|clothing.shorts|شُورْت|شُورْتْ
                EGYPTIAN|clothing.blouse|بْلُوزَة|بْلوزَة
                EGYPTIAN|clothing.trousers|بَنْطَلُون|بَنْطَلونْ
                EGYPTIAN|clothing.dress|فُسْتَان|فُسْتانْ
                EGYPTIAN|clothing.robe|رُوب|رُوبْ
                EGYPTIAN|clothing.abaya|عَبَايَة|عَبايَة
                EGYPTIAN|clothing.hijab|حِجَاب|حِجابْ
                EGYPTIAN|clothing.scarf|وِشَاح|وِشاحْ
                EGYPTIAN|clothing.socks|شَرَابَات|شَراباتْ
                EGYPTIAN|clothing.coat|بَالْطُو|بَالْطو
                EGYPTIAN|clothing.cap|كَاب|كَابْ
                EGYPTIAN|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                EGYPTIAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                EGYPTIAN|clothing.high_heels|جَزْمَة بِكَعْب|جَزْمَة بِكَعْبْ
                EGYPTIAN|clothing.pajamas|بِيجَامَة|بِيجامَة
                EGYPTIAN|clothing.belt|حِزَام|حِزامْ
                EGYPTIAN|clothing.ring|خَاتَم|خَاتَمْ
                EGYPTIAN|body.eye|عِين|عِينْ
                EGYPTIAN|body.nose|مَنَاخِير|مَناخيرْ
                EGYPTIAN|body.mouth|بُقّ|بُقّْ
                EGYPTIAN|body.tongue|لِسَان|لِسانْ
                EGYPTIAN|body.ear|وِدْن|وِدْنْ
                EGYPTIAN|body.hand|يَد|يَدْ
                EGYPTIAN|body.leg|رِجْل|رِجْلْ
                EGYPTIAN|body.head|رَاس|رَاسْ
                EGYPTIAN|body.hair|شَعْر|شَعْرْ
                EGYPTIAN|body.back|ظَهْر|ظَهْرْ
                EGYPTIAN|body.abdomen|بَطْن|بَطْنْ
                EGYPTIAN|body.finger|صُبَاع|صُباعْ
                EGYPTIAN|body.chest|صَدْر|صَدْرْ
                EGYPTIAN|body.face|وِشّ|وِشّْ
                EGYPTIAN|body.heart|قَلْب|قَلْبْ
                EGYPTIAN|body.shoulder|كِتْف|كِتْفْ
                EGYPTIAN|action.eat|آكُل|آكُلْ
                EGYPTIAN|action.drink|أَشْرَب|أَشْرَبْ
                EGYPTIAN|action.sleep|أَنَام|أَنامْ
                EGYPTIAN|action.sit|أَقْعُد|أَقْعُدْ
                EGYPTIAN|action.walk|أَمْشِي|أَمْشي
                EGYPTIAN|action.stand|أَقَف|أَقَفْ
                EGYPTIAN|action.wash|أَغْسِل|أَغْسِلْ
                EGYPTIAN|action.speak|أَتْكَلِّم|أَتْكَلِّمْ
                EGYPTIAN|action.hear|أَسْمَع|أَسْمَعْ
                EGYPTIAN|action.open|أَفْتَح|أَفْتَحْ
                EGYPTIAN|action.close|أَقْفِل|أَقْفِلْ
                EGYPTIAN|action.read|أَقْرَا|أَقْرا
                EGYPTIAN|action.laugh|أَضْحَك|أَضْحَكْ
                EGYPTIAN|action.see|أَشُوف|أَشوفْ
                EGYPTIAN|action.dress|أَلْبِس|أَلْبِسْ
                EGYPTIAN|action.pray|أُصَلِّي|أَصَلِّي
                EGYPTIAN|animal.dog|كَلْب|كَلْبْ
                EGYPTIAN|animal.sheep|خَرُوف|خَروفْ
                EGYPTIAN|animal.horse|حِصَان|حِصانْ
                EGYPTIAN|animal.sparrow|عُصْفُور|عُصْفورْ
                EGYPTIAN|animal.pigeon|حَمَامَة|حَمامَة
                EGYPTIAN|animal.camel|جَمَل|جَمَلْ
                EGYPTIAN|animal.rooster|دِيك|دِيكْ
                EGYPTIAN|animal.rabbit|أَرْنَب|أَرْنَبْ
                EGYPTIAN|transport.taxi|تَاكْسِي|تَاكْسي
                EGYPTIAN|transport.bus|أُتُوبِيس|أُتوبيسْ
                EGYPTIAN|transport.minibus|مِيكْرُوبَاص|مِيكْروباصْ
                EGYPTIAN|transport.motorcycle|مُوتُوسِيكِل|مُوتوسيكِلْ
                EGYPTIAN|transport.train|قَطْر|قَطْرْ
                EGYPTIAN|transport.helicopter|هِلِيكُوبْتَر|هِليكوبْتَرْ
                EGYPTIAN|transport.ship|مَرْكِب كِبِير|مَرْكِبْ كِبيرْ
                EGYPTIAN|transport.boat|مَرْكِب|مَرْكِبْ
                EGYPTIAN|transport.truck|عَرَبِيَّة نَقْل|عَرَبِيِّة نَقْلْ
                EGYPTIAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلامُ عَلَيْكُمْ
                EGYPTIAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلامْ
                EGYPTIAN|greeting.how_are_you|أَخْبَارَك إِيه؟|أَخْبارَكْ إِيهْ؟
                EGYPTIAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                EGYPTIAN|greeting.good_morning|صَبَاح الخِير|صَباحْ الْخيرْ
                EGYPTIAN|greeting.good_evening|مَسَا الخِير|مَسا الْخيرْ
                EGYPTIAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                EGYPTIAN|greeting.good_health|تِسْلَم|تِسْلَمْ
                EGYPTIAN|greeting.goodbye|مَعَ السَّلَامَة|مَعَ السَّلامَة
                EGYPTIAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمانِ اللَّهْ
                EGYPTIAN|greeting.thanks|شُكْرًا لِيك|شُكْرًا لِيكْ
                EGYPTIAN|greeting.bless|رَبِّنَا يبَارِك فِيك|رَبِّنَا يْبَارِكْ فِيكْ
            """.trimIndent(),
            """
                SYRIAN|family.father|أَبُوي|أَبويْ
                SYRIAN|family.husband|جَوْزِي|جَوْزي
                SYRIAN|family.wife|مَرْتِي|مَرْتي
                SYRIAN|family.son|اِبْنِي|اِبْني
                SYRIAN|family.daughter|بِنْتِي|بِنْتي
                SYRIAN|family.brother|أَخُوي|أَخويْ
                SYRIAN|family.sister|أُخْتِي|أُخْتي
                SYRIAN|family.paternal_aunt|عَمَّتِي|عَمْتِي
                SYRIAN|family.maternal_uncle|خَالِي|خَالي
                SYRIAN|family.maternal_aunt|خَالَتِي|خَالْتِي
                SYRIAN|family.grandmother|جَدَّتِي|جَدَّتي
                SYRIAN|family.grandson|حَفِيدِي|حَفيدي
                SYRIAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                SYRIAN|family.paternal_uncle_son|اِبْن عَمِّي|اِبْنْ عَمِّي
                SYRIAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                SYRIAN|family.paternal_aunt_son|اِبْن عَمْتِي|اِبْنْ عَمْتي
                SYRIAN|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتي
                SYRIAN|family.maternal_uncle_son|اِبْن خَالِي|اِبْنْ خَالي
                SYRIAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالي
                SYRIAN|family.maternal_aunt_son|اِبْن خَالْتِي|اِبْنْ خَالْتي
                SYRIAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتي
                SYRIAN|family.brother_son|اِبْن أَخُوي|اِبْنْ أَخويْ
                SYRIAN|family.brother_daughter|بِنْت أَخُوي|بِنْتْ أَخويْ
                SYRIAN|family.sister_son|اِبْن أُخْتِي|اِبْنْ أُخْتي
                SYRIAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتي
                SYRIAN|family.husband_father|أَبُو جُوزِي|أَبو جُوزي
                SYRIAN|family.husband_mother|أُم جُوزِي|أُمْ جُوزي
                SYRIAN|family.wife_father|أَبُو مَرْتِي|أَبو مَرْتي
                SYRIAN|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتي
                SYRIAN|family.husband_brother|أَخُو جُوزِي|أَخو جُوزي
                SYRIAN|family.husband_sister|أُخْت جُوزِي|أُخْتْ جُوزي
                SYRIAN|family.wife_brother|أَخُو مَرْتِي|أَخو مَرْتي
                SYRIAN|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتي
                SYRIAN|family.sister_husband|جُوز أُخْتِي|جُوزْ أُخْتي
                SYRIAN|family.brother_wife|مَرْت أَخُوي|مَرْتْ أَخويْ
                SYRIAN|family.daughter_husband|جُوز بِنْتِي|جُوزْ بِنْتي
                SYRIAN|family.son_wife|مَرْت اِبْنِي|مَرْتْ اِبْني
                SYRIAN|family.male_in_law|نَسِيبِي|نَسيبي
                SYRIAN|family.female_in_law|نَسِيبَتِي|نَسِيبْتِي
                SYRIAN|family.male_relative|قَرِيبِي|قَريبي
                SYRIAN|family.female_relative|قَرِيبَتِي|قَرِيبْتِي
                SYRIAN|request.water|بِدِّي مَيّ|بِدِّي مَيّْ
                SYRIAN|request.tea|بِدِّي شَاي|بِدِّي شَايْ
                SYRIAN|request.rest|بِدِّي إِرْتَاح|بِدِّي إِرْتَاحْ
                SYRIAN|request.medicine|بِدِّي دَوَاي|بِدِّي دَوايْ
                SYRIAN|request.toilet|بِدِّي رُوح عَالْحَمَّام|بِدِّي رُوحْ عَالْحَمَّامْ
                SYRIAN|request.adjust_pillow|زَبِّط لِي المِخَدَّة|زَبِّطْ لِي الْمِخَدَّة
                SYRIAN|request.headache|رَاسِي عَم يُوجَعْنِي|رَاسي عَمْ يُوجَعْني
                SYRIAN|request.cold|كْتِير بَرْد|كْتِيرْ بَرْدْ
                SYRIAN|dish.syrian.shakriya|شَاكْرِيَّة بِرِزّ|شَاكْرِيَّة بِرِزّْ
                SYRIAN|dish.syrian.yabraq|يَبْرَق وَرَق عِنَب|يَبْرَقْ وَرَقْ عِنَبْ
                SYRIAN|dish.syrian.chicken_shawarma|شَاوَرْمَا دَجَاج|شَاوَرْما دَجاجْ
                SYRIAN|dish.syrian.chickpea_fatteh|فَتِّة حُمُّص|فَتِّة حُمُّصْ
                SYRIAN|dish.syrian.maqluba|مَقْلُوبِة بَاذِنْجَان|مَقْلوبِة بَاذِنْجانْ
                SYRIAN|dish.syrian.fattoush|فَتُّوش|فَتُّوشْ
                SYRIAN|dish.syrian.kebab|كَبَاب حَلَبِي|كَبابْ حَلَبي
                SYRIAN|dish.syrian.shish_barak|شِيش بَرَك|شِيشْ بَرَكْ
                SYRIAN|dish.syrian.stuffed_zucchini|كُوسَا بِلَبَن|كُوسا بِلَبَنْ
                SYRIAN|dish.syrian.mutabbal|مْتَبَّل بَاذِنْجَان|مْتَبَّلْ بَاذِنْجَانْ
                SYRIAN|dish.syrian.haraq_osbao|حَرَّاق أُصْبَعُو|حَرَّاقْ إِصْبَعُو
                SYRIAN|dish.syrian.halawet_jibn|حَلَاوِة الجِبِن|حَلَاوِة الْجِبِنْ
                SYRIAN|routine.call_to_prayer|أَذَان|أَذانْ
                SYRIAN|routine.ablution|وُضُوء|وُضوءْ
                SYRIAN|routine.prayer|صَلَاة|صَلاة
                SYRIAN|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صَلَاة
                SYRIAN|routine.quran|مُصْحَف|مُصْحَفْ
                SYRIAN|routine.evening_coffee|قَهْوَة المَسَا|قَهْوِة الْمَسَا
                SYRIAN|routine.sleep|نُوم|نُومْ
                SYRIAN|routine.walk|مَشْي|مَشِي
                SYRIAN|vegetable.tomato|بَنْدُورَة|بَنْدورَة
                SYRIAN|vegetable.potato|بَطَاطَا|بَطاطا
                SYRIAN|vegetable.onion|بَصَل|بَصَلْ
                SYRIAN|vegetable.garlic|تُوم|تُومْ
                SYRIAN|vegetable.carrot|جَزَر|جَزَرْ
                SYRIAN|vegetable.cucumber|خِيَار|خِيارْ
                SYRIAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                SYRIAN|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجانْ
                SYRIAN|vegetable.zucchini|كُوسَا|كُوسا
                SYRIAN|vegetable.lettuce|خَسّ|خَسّْ
                SYRIAN|vegetable.parsley|بَقْدُونِس|بَقْدونِسْ
                SYRIAN|vegetable.lemon|لِيمُون|لِيمونْ
                SYRIAN|vegetable.peas|بَازِيلَّا|بَازيلَّا
                SYRIAN|vegetable.green_beans|فَاصُولْيَا خَضْرَا|فَاصولْيا خَضْرا
                SYRIAN|vegetable.spinach|سَبَانِخ|سَبانِخْ
                SYRIAN|vegetable.broccoli|بُرُوكْلِي|بُروكْلي
                SYRIAN|vegetable.radish|فِجِل|فِجِلْ
                SYRIAN|vegetable.pumpkin|قَرَع|قَرَعْ
                SYRIAN|vegetable.mint|نَعْنَاع|نَعْناعْ
                SYRIAN|fruit.apple|تُفَّاح|تُفَّاحْ
                SYRIAN|fruit.banana|مُوز|مُوزْ
                SYRIAN|fruit.orange|بُرْدْقَان|بُرْدْقانْ
                SYRIAN|fruit.grapes|عِنَب|عِنَبْ
                SYRIAN|fruit.strawberry|فَرَاوْلَة|فَراوْلَة
                SYRIAN|fruit.mango|مَانْجُو|مَانْجو
                SYRIAN|fruit.watermelon|بَطِّيخ|بَطِّيخْ
                SYRIAN|fruit.melon|شَمَّام|شَمَّامْ
                SYRIAN|fruit.peach|خُوخ|خُوخْ
                SYRIAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                SYRIAN|fruit.pear|إِجَّاص|إِجَّاصْ
                SYRIAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                SYRIAN|fruit.figs|تِين|تِينْ
                SYRIAN|fruit.dates|تَمْر|تَمْرْ
                SYRIAN|fruit.pineapple|أَنَانَاس|أَناناسْ
                SYRIAN|fruit.kiwi|كِيوِي|كِيويْ
                SYRIAN|fruit.cherry|كَرَز|كَرَزْ
                SYRIAN|fruit.berries|تُوت|تُوتْ
                SYRIAN|fruit.mandarin|يُوسْفِي|يُوسْفي
                SYRIAN|fruit.avocado|أَفُوكَادُو|أَفوكادو
                SYRIAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                SYRIAN|food.bread|خُبِز|خُبِزْ
                SYRIAN|food.milk|حَلِيب|حَليبْ
                SYRIAN|food.eggs|بِيض|بِيضْ
                SYRIAN|food.yogurt|لَبَن|لَبَنْ
                SYRIAN|food.rice|رِزّ|رِزّْ
                SYRIAN|food.pasta|مَعْكَرُونَة|مَعْكَرونَة
                SYRIAN|food.meat|لَحْم|لَحْمْ
                SYRIAN|food.chicken|جَاج|جَاجْ
                SYRIAN|food.fish|سَمَك|سَمَكْ
                SYRIAN|food.olives|زِيتُون|زِيتونْ
                SYRIAN|food.oil|زِيت|زِيتْ
                SYRIAN|food.tea|شَاي|شَايْ
                SYRIAN|food.juice|عَصِير|عَصيرْ
                SYRIAN|food.water|مَيّ|مَيّْ
                SYRIAN|food.honey|عَسَل|عَسَلْ
                SYRIAN|food.biscuit|بِسْكُوِيت|بِسْكُويتْ
                SYRIAN|food.salt|مِلْح|مِلْحْ
                SYRIAN|food.sugar|سُكَّر|سُكَّرْ
                SYRIAN|home.bed|سَرِير|سَريرْ
                SYRIAN|home.pillow|مَخَدَّة|مِخَدَّة
                SYRIAN|home.blanket|حْرَام|حْرَامْ
                SYRIAN|home.chair|كُرْسِي|كُرْسي
                SYRIAN|home.door|بَاب|بَابْ
                SYRIAN|home.window|شُبَّاك|شُبَّاكْ
                SYRIAN|home.television|تِلِفِزْيُون|تِلِفِزْيونْ
                SYRIAN|home.stove|بُوتَاجَاز|بُوتاجازْ
                SYRIAN|home.oven|فُرْن|فُرْنْ
                SYRIAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                SYRIAN|home.wardrobe|خِزَانَة|خْزَانَة
                SYRIAN|home.key|مِفْتَاح|مِفْتاحْ
                SYRIAN|home.carpet|بِسَاط|بِساطْ
                SYRIAN|home.kitchen|مَطْبَخ|مَطْبَخْ
                SYRIAN|home.knife|سِكِّين|سِكِّينْ
                SYRIAN|home.teapot|إِبْرِيق شَاي|إِبْريقْ شَايْ
                SYRIAN|home.water_bottle|قَنِّينَة مَيّ|قَنِّينِة مَيّْ
                SYRIAN|clothing.tshirt|تِيشِيرْت|تِيشيرْتْ
                SYRIAN|clothing.shirt|قَمِيص|قَميصْ
                SYRIAN|clothing.jacket|جَاكِيت|جَاكيتْ
                SYRIAN|clothing.shorts|شُورْت|شُورْتْ
                SYRIAN|clothing.blouse|بْلُوزَة|بْلوزَة
                SYRIAN|clothing.trousers|بَنْطَلُون|بَنْطَلونْ
                SYRIAN|clothing.dress|فُسْتَان|فُسْتانْ
                SYRIAN|clothing.robe|رُوب|رُوبْ
                SYRIAN|clothing.abaya|عَبَايَة|عَبايَة
                SYRIAN|clothing.hijab|حِجَاب|حِجابْ
                SYRIAN|clothing.scarf|وِشَاح|وِشاحْ
                SYRIAN|clothing.socks|جْرَابَات|جْرَابَاتْ
                SYRIAN|clothing.coat|مِعْطَف|مِعْطَفْ
                SYRIAN|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                SYRIAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                SYRIAN|clothing.high_heels|كَنْدَرَة كَعِب عَالِي|كَنْدْرِة كَعِبْ عَالِي
                SYRIAN|clothing.pajamas|بِيجَامَة|بِيجامَة
                SYRIAN|clothing.belt|حْزَام|حْزَامْ
                SYRIAN|clothing.ring|خَاتَم|خَاتَمْ
                SYRIAN|body.eye|عِين|عِينْ
                SYRIAN|body.nose|مَنْخَار|مَنْخارْ
                SYRIAN|body.mouth|تِمّ|تِمّْ
                SYRIAN|body.tongue|لِسَان|لِسانْ
                SYRIAN|body.ear|إِذِن|إِذِنْ
                SYRIAN|body.hand|يَد|يَدْ
                SYRIAN|body.leg|رِجْل|رِجْلْ
                SYRIAN|body.head|رَاس|رَاسْ
                SYRIAN|body.hair|شَعْر|شَعْرْ
                SYRIAN|body.back|ظَهْر|ظَهْرْ
                SYRIAN|body.abdomen|بَطْن|بَطْنْ
                SYRIAN|body.finger|إِصْبَع|إِصْبَعْ
                SYRIAN|body.chest|صَدْر|صَدْرْ
                SYRIAN|body.face|وِشّ|وِشّْ
                SYRIAN|body.tooth|سِنّ|سِنّْ
                SYRIAN|body.heart|قَلْب|قَلْبْ
                SYRIAN|body.shoulder|كِتْف|كِتْفْ
                SYRIAN|action.eat|آكُل|آكُلْ
                SYRIAN|action.drink|أَشْرَب|أَشْرَبْ
                SYRIAN|action.sleep|أَنَام|أَنامْ
                SYRIAN|action.sit|أَقْعُد|أَقْعُدْ
                SYRIAN|action.walk|أَمْشِي|أَمْشي
                SYRIAN|action.stand|أَقُوم|أَقومْ
                SYRIAN|action.wash|أَغْسِل|أَغْسِلْ
                SYRIAN|action.speak|أَحْكِي|أَحْكي
                SYRIAN|action.hear|أَسْمَع|أَسْمَعْ
                SYRIAN|action.open|أَفْتَح|أَفْتَحْ
                SYRIAN|action.close|أَسَكِّر|أَسَكِّرْ
                SYRIAN|action.read|أَقْرَا|أَقْرا
                SYRIAN|action.laugh|أَضْحَك|أَضْحَكْ
                SYRIAN|action.see|أَشُوف|أَشوفْ
                SYRIAN|action.dress|أَلْبَس|أَلْبَسْ
                SYRIAN|animal.dog|كَلْب|كَلْبْ
                SYRIAN|animal.sheep|خَرُوف|خَروفْ
                SYRIAN|animal.horse|حِصَان|حِصانْ
                SYRIAN|animal.sparrow|عُصْفُور|عُصْفورْ
                SYRIAN|animal.pigeon|حَمَامَة|حَمامَة
                SYRIAN|animal.camel|جَمَل|جَمَلْ
                SYRIAN|animal.rooster|دِيك|دِيكْ
                SYRIAN|animal.rabbit|أَرْنَب|أَرْنَبْ
                SYRIAN|transport.taxi|تَكْسِي|تَكْسي
                SYRIAN|transport.bus|بَاص|بَاصْ
                SYRIAN|transport.minibus|سِرْفِيس|سِرْفيسْ
                SYRIAN|transport.bicycle|بِسْكْلِيت|بِسْكْليتْ
                SYRIAN|transport.motorcycle|مُوتُور|مُوتورْ
                SYRIAN|transport.train|قْطَار|قْطَارْ
                SYRIAN|transport.helicopter|هِلِيكُوبْتَر|هِليكوبْتَرْ
                SYRIAN|transport.boat|قَارِب|قَارِبْ
                SYRIAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلامُ عَلَيْكُمْ
                SYRIAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلامْ
                SYRIAN|greeting.hello|مَرْحَبَا|مَرْحَبا
                SYRIAN|greeting.welcome|أَهْلِين وَسَهْلِين|أَهْلينْ وَسَهْلينْ
                SYRIAN|greeting.how_are_you|كِيفَك؟|كِيفَكْ؟
                SYRIAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                SYRIAN|greeting.good_morning|صَبَاح الخِير|صَباحْ الْخيرْ
                SYRIAN|greeting.good_evening|مَسَا الخِير|مَسا الْخيرْ
                SYRIAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                SYRIAN|greeting.good_health|يَعْطِيك العَافْيَة|يَعْطيكْ الْعافْيَة
                SYRIAN|greeting.goodbye|مَعَ السَّلَامَة|مَعَ السَّلامَة
                SYRIAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمانِ اللَّهْ
                SYRIAN|greeting.thanks|يِسْلَمُو|يِسْلَمو
                SYRIAN|greeting.bless|اللَّهُ يبَارِك فِيك|اللَّهْ يْبَارِكْ فِيكْ
            """.trimIndent(),
            """
                IRAQI|family.father|أَبُويَه|أَبُويَهْ
                IRAQI|family.brother|أَخُوي|أَخُويْ
                IRAQI|family.paternal_aunt|عَمَّتِي|عَمْتِي
                IRAQI|family.maternal_aunt|خَالَتِي|خَالْتِي
                IRAQI|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                IRAQI|family.paternal_uncle_son|وَلَد عَمِّي|وَلَدْ عَمِّي
                IRAQI|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                IRAQI|family.paternal_aunt_son|وَلَد عَمْتِي|وَلَدْ عَمْتِي
                IRAQI|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتِي
                IRAQI|family.maternal_uncle_son|وَلَد خَالِي|وَلَدْ خَالِي
                IRAQI|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                IRAQI|family.maternal_aunt_son|وَلَد خَالْتِي|وَلَدْ خَالْتِي
                IRAQI|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتِي
                IRAQI|family.brother_son|وَلَد أَخُوي|وَلَدْ أَخُويْ
                IRAQI|family.brother_daughter|بِنْت أَخُوي|بِنْتْ أَخُويْ
                IRAQI|family.sister_son|وَلَد أُخْتِي|وَلَدْ أُخْتِي
                IRAQI|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                IRAQI|family.husband_mother|أُم زُوجِي|أُمْ زُوجِي
                IRAQI|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتِي
                IRAQI|family.husband_sister|أُخْت زُوجِي|أُخْتْ زُوجِي
                IRAQI|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتِي
                IRAQI|family.sister_husband|زُوج أُخْتِي|زُوجْ أُخْتِي
                IRAQI|family.brother_wife|مَرْت أَخُوي|مَرْتْ أَخُويْ
                IRAQI|family.daughter_husband|زُوج بِنْتِي|زُوجْ بِنْتِي
                IRAQI|family.son_wife|مَرْت اِبْنِي|مَرْتْ اِبْنِي
                IRAQI|family.female_in_law|نَسِيبَتِي|نَسِيبْتِي
                IRAQI|family.male_relative|قَرِيبِي|گَرِيبِي
                IRAQI|family.female_relative|قَرِيبَتِي|گَرِيبْتِي
                IRAQI|request.water|أَرِيد مَاي|أَرِيدْ مَايْ
                IRAQI|request.tea|أَرِيد تشَاي|أَرِيدْ چَايْ
                IRAQI|request.rest|أَرِيد أَرْتَاح|أَرِيدْ أَرْتَاحْ
                IRAQI|request.medicine|أَرِيد دَوَاي|أَرِيدْ دَوَايْ
                IRAQI|request.toilet|أَرِيد أَرُوح لِلحَمَّام|أَرِيدْ أَرُوحْ لِلْحَمَّامْ
                IRAQI|request.adjust_pillow|عَدِّل لِي المْخَدَّة|عَدِّلْ لِي الْمْخَدَّة
                IRAQI|request.cold|أَحِس بِبَرْد|أَحِسْ بِبَرْدْ
                IRAQI|dish.iraqi.masgouf|سِمَتش مَسْكُوف|سِمَچْ مَسْگُوفْ
                IRAQI|dish.iraqi.quzi|قُوزِي عَالْتِمَّن|قُوزِي عَالْتِمَّنْ
                IRAQI|dish.iraqi.tashreeb|تَشْرِيب لَحَم|تَشْرِيبْ لَحَمْ
                IRAQI|dish.iraqi.pacha|بَاجَة|بَاچَة
                IRAQI|dish.iraqi.rice_kibbeh|كُبَّة تِمَّن|كُبِّة تِمَّنْ
                IRAQI|dish.iraqi.fish_mutabbaq|مُطَبَّق سِمَتش|مْطَبَّگْ سِمَچْ
                IRAQI|dish.iraqi.okra|بَامْيَا عَالْتِمَّن|بَامْيَا عَالْتِمَّنْ
                IRAQI|dish.iraqi.turnip|شَلْغَم مَطْبُوخ|شَلْغَمْ مَطْبُوخْ
                IRAQI|dish.iraqi.kleicha|كْلِيجَة|كْلِيچَة
                IRAQI|routine.call_to_prayer|أَذَان|أَذَانْ
                IRAQI|routine.ablution|وُضُوء|وُضُوءْ
                IRAQI|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صَلَاة
                IRAQI|routine.quran|مُصْحَف|مُصْحَفْ
                IRAQI|routine.evening_coffee|قَهْوَة العَصِر|گَهْوِة الْعَصِرْ
                IRAQI|routine.sleep|نُوم|نُومْ
                IRAQI|routine.walk|مَشْي|مَشِي
                IRAQI|vegetable.onion|بَصَل|بَصَلْ
                IRAQI|vegetable.garlic|ثُوم|ثُومْ
                IRAQI|vegetable.carrot|جَزَر|جَزَرْ
                IRAQI|vegetable.cucumber|خِيَار|خِيَارْ
                IRAQI|vegetable.pepper|فِلْفِل|فِلْفِلْ
                IRAQI|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانْ
                IRAQI|vegetable.lettuce|خَسّ|خَسّْ
                IRAQI|vegetable.parsley|بَقْدُونِس|بَقْدُونِسْ
                IRAQI|vegetable.lemon|لِيمُون|لِيمُونْ
                IRAQI|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                IRAQI|vegetable.cauliflower|قَرْنَابِيط|قَرْنَابِيطْ
                IRAQI|vegetable.radish|فِجِل|فِجِلْ
                IRAQI|vegetable.pumpkin|قَرَع|قَرَعْ
                IRAQI|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                IRAQI|fruit.apple|تُفَّاح|تُفَّاحْ
                IRAQI|fruit.banana|مُوز|مُوزْ
                IRAQI|fruit.orange|بُرْتُقَال|بُرْتُقَالْ
                IRAQI|fruit.grapes|عِنَب|عِنَبْ
                IRAQI|fruit.watermelon|رَقِّي|رَگِّي
                IRAQI|fruit.melon|شَمَّام|شَمَّامْ
                IRAQI|fruit.peach|خُوخ|خُوخْ
                IRAQI|fruit.apricot|مِشْمِش|مِشْمِشْ
                IRAQI|fruit.pear|عَرْمُوط|عَرْمُوطْ
                IRAQI|fruit.pomegranate|رُمَّان|رُمَّانْ
                IRAQI|fruit.figs|تِين|تِينْ
                IRAQI|fruit.dates|تَمْر|تَمْرْ
                IRAQI|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                IRAQI|fruit.cherry|كَرَز|كَرَزْ
                IRAQI|fruit.berries|تُوت|تُوتْ
                IRAQI|fruit.mandarin|لَالَنْكِي|لَالَنْگِي
                IRAQI|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                IRAQI|food.bread|خُبُز|خُبُزْ
                IRAQI|food.milk|حَلِيب|حَلِيبْ
                IRAQI|food.eggs|بِيض|بِيضْ
                IRAQI|food.cheese|جُبْن|جُبْنْ
                IRAQI|food.yogurt|لَبَن|لَبَنْ
                IRAQI|food.rice|تِمَّن|تِمَّنْ
                IRAQI|food.meat|لَحْم|لَحْمْ
                IRAQI|food.chicken|دَجَاج|دَجَاجْ
                IRAQI|food.fish|سِمَتش|سِمَچْ
                IRAQI|food.olives|زِيتُون|زِيتُونْ
                IRAQI|food.oil|زِيت|زِيتْ
                IRAQI|food.tea|تشَاي|چَايْ
                IRAQI|food.coffee|قَهْوَة|گَهْوَة
                IRAQI|food.juice|عَصِير|عَصِيرْ
                IRAQI|food.water|مَاي|مَايْ
                IRAQI|food.honey|عَسَل|عَسَلْ
                IRAQI|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                IRAQI|food.salt|مِلْح|مِلْحْ
                IRAQI|food.sugar|سُكَّر|سُكَّرْ
                IRAQI|home.bed|سَرِير|سَرِيرْ
                IRAQI|home.door|بَاب|بَابْ
                IRAQI|home.window|شُبَّاك|شُبَّاكْ
                IRAQI|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                IRAQI|home.stove|طَبَّاخ|طَبَّاخْ
                IRAQI|home.oven|فُرْن|فُرْنْ
                IRAQI|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                IRAQI|home.wardrobe|كَنْتُور|كَنْتُورْ
                IRAQI|home.key|مِفْتَاح|مِفْتَاحْ
                IRAQI|home.kitchen|مَطْبَخ|مَطْبَخْ
                IRAQI|home.cup|كُوب|كُوبْ
                IRAQI|home.knife|سِكِّين|سِكِّينْ
                IRAQI|home.teapot|قُورِي|گُورِي
                IRAQI|home.pot|جِدِر|جِدِرْ
                IRAQI|home.water_bottle|بُطُل مَيّ|بُطُلْ مَيّْ
                IRAQI|clothing.tshirt|تِيشِيرْت|تِيشِيرْتْ
                IRAQI|clothing.shirt|قَمِيص|قَمِيصْ
                IRAQI|clothing.jacket|جَاكِيت|جَاكِيتْ
                IRAQI|clothing.shorts|شُورْت|شُورْتْ
                IRAQI|clothing.trousers|بَنْطَرُون|بَنْطَرُونْ
                IRAQI|clothing.dress|فُسْتَان|فُسْتَانْ
                IRAQI|clothing.robe|رُوب|رُوبْ
                IRAQI|clothing.hijab|حِجَاب|حِجَابْ
                IRAQI|clothing.scarf|وِشَاح|وِشَاحْ
                IRAQI|clothing.socks|جْوَارِيب|جْوَارِيبْ
                IRAQI|clothing.coat|قَبُّوط|گَبُّوطْ
                IRAQI|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                IRAQI|clothing.sandals|صَنْدَل|صَنْدَلْ
                IRAQI|clothing.high_heels|قُنْدَرَة كَعَب عَالِي|قُنْدْرِة كَعَبْ عَالِي
                IRAQI|clothing.belt|حِزَام|حِزَامْ
                IRAQI|clothing.ring|خَاتَم|خَاتَمْ
                IRAQI|body.eye|عِين|عِينْ
                IRAQI|body.nose|خَشِم|خَشِمْ
                IRAQI|body.mouth|حَلِك|حَلِگْ
                IRAQI|body.tongue|لِسَان|لِسَانْ
                IRAQI|body.ear|إِذِن|إِذِنْ
                IRAQI|body.hand|يَد|يَدْ
                IRAQI|body.leg|رِجْل|رِجْلْ
                IRAQI|body.head|رَاس|رَاسْ
                IRAQI|body.hair|شَعْر|شَعْرْ
                IRAQI|body.back|ظَهْر|ظَهْرْ
                IRAQI|body.abdomen|بَطْن|بَطْنْ
                IRAQI|body.finger|إِصْبَع|إِصْبَعْ
                IRAQI|body.neck|رَقْبَة|رُگْبَة
                IRAQI|body.chest|صَدْر|صَدْرْ
                IRAQI|body.face|وَجْه|وَجْهْ
                IRAQI|body.tooth|سِنّ|سِنّْ
                IRAQI|body.heart|قَلْب|گَلُبْ
                IRAQI|body.shoulder|كِتْف|كِتْفْ
                IRAQI|action.eat|آكُل|آكُلْ
                IRAQI|action.drink|أَشْرَب|أَشْرَبْ
                IRAQI|action.sleep|أَنَام|أَنَامْ
                IRAQI|action.sit|أَقْعُد|أَگْعُدْ
                IRAQI|action.stand|أَقُوم|أَگُومْ
                IRAQI|action.wash|أَغْسِل|أَغْسِلْ
                IRAQI|action.speak|أَحْتشِي|أَحْچِي
                IRAQI|action.hear|أَسْمَع|أَسْمَعْ
                IRAQI|action.open|أَفْتَح|أَفْتَحْ
                IRAQI|action.close|أَسِد|أَسِدّْ
                IRAQI|action.read|أَقْرَا|أَگْرَا
                IRAQI|action.laugh|أَضْحَك|أَضْحَكْ
                IRAQI|action.see|أَشُوف|أَشُوفْ
                IRAQI|action.dress|أَلْبَس|أَلْبَسْ
                IRAQI|action.pray|أُصَلِّي|أَصَلِّي
                IRAQI|animal.dog|كَلْب|كَلْبْ
                IRAQI|animal.sheep|خَرُوف|خَرُوفْ
                IRAQI|animal.horse|حِصَان|حِصَانْ
                IRAQI|animal.cow|بَقَرَة|بَگْرَة
                IRAQI|animal.sparrow|عُصْفُور|عُصْفُورْ
                IRAQI|animal.camel|جَمَل|جَمَلْ
                IRAQI|animal.rooster|دِيك|دِيكْ
                IRAQI|animal.rabbit|أَرْنَب|أَرْنَبْ
                IRAQI|transport.bus|بَاص|بَاصْ
                IRAQI|transport.minibus|بَاص صْغِير|بَاصْ صْغِيرْ
                IRAQI|transport.bicycle|بَايْسِكِل|بَايْسِكِلْ
                IRAQI|transport.train|قِطَار|قِطَارْ
                IRAQI|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                IRAQI|transport.boat|زَوْرَق|زَوْرَقْ
                IRAQI|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                IRAQI|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                IRAQI|greeting.hello|هَلَا بِيك|هَلَا بِيكْ
                IRAQI|greeting.welcome|أَهْلِين وَسَهْلِين|أَهْلِينْ وَسَهْلِينْ
                IRAQI|greeting.how_are_you|شلُونَك؟|شْلُونَكْ؟
                IRAQI|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                IRAQI|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                IRAQI|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                IRAQI|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                IRAQI|greeting.good_health|عَاشَت إِيدَك|عَاشَتْ إِيدَكْ
                IRAQI|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                IRAQI|greeting.thanks|مَشْكُور|مَشْكُورْ
                IRAQI|greeting.bless|اللَّهُ يبَارِك بِيك|اللَّهْ يْبَارِكْ بِيكْ
            """.trimIndent(),
            """
                TUNISIAN|family.paternal_aunt|عَمَّتِي|عَمْتِي
                TUNISIAN|family.maternal_aunt|خَالَتِي|خَالْتِي
                TUNISIAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                TUNISIAN|family.paternal_uncle_son|وَلْد عَمِّي|وِلْدْ عَمِّي
                TUNISIAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                TUNISIAN|family.paternal_aunt_son|وَلْد عَمْتِي|وِلْدْ عَمْتِي
                TUNISIAN|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتِي
                TUNISIAN|family.maternal_uncle_son|وَلْد خَالِي|وِلْدْ خَالِي
                TUNISIAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                TUNISIAN|family.maternal_aunt_son|وَلْد خَالْتِي|وِلْدْ خَالْتِي
                TUNISIAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتِي
                TUNISIAN|family.brother_son|وَلْد خُويَا|وِلْدْ خُويَا
                TUNISIAN|family.brother_daughter|بِنْت خُويَا|بِنْتْ خُويَا
                TUNISIAN|family.sister_son|وَلْد أُخْتِي|وِلْدْ أُخْتِي
                TUNISIAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                TUNISIAN|family.husband_mother|أُم رَاجْلِي|أُمْ رَاجْلِي
                TUNISIAN|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتِي
                TUNISIAN|family.husband_sister|أُخْت رَاجْلِي|أُخْتْ رَاجْلِي
                TUNISIAN|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتِي
                TUNISIAN|family.sister_husband|رَاجِل أُخْتِي|رَاجِلْ أُخْتِي
                TUNISIAN|family.brother_wife|مَرْت خُويَا|مَرْتْ خُويَا
                TUNISIAN|family.daughter_husband|رَاجِل بِنْتِي|رَاجِلْ بِنْتِي
                TUNISIAN|family.son_wife|مَرْت وَلْدِي|مَرْتْ وِلْدِي
                TUNISIAN|family.male_in_law|نَسِيبِي|نْسِيبِي
                TUNISIAN|family.female_in_law|نَسِيبَتِي|نْسِيبْتِي
                TUNISIAN|family.male_relative|قَرِيبِي|قْرِيبِي
                TUNISIAN|family.female_relative|قَرِيبَتِي|قْرِيبْتِي
                TUNISIAN|request.water|نْحِب شْوَيَّة مَا|نْحِبّْ شْوَيَّة مَا
                TUNISIAN|request.tea|نْحِب تَاي|نْحِبّْ تَايْ
                TUNISIAN|request.rest|نْحِب نِرْتَاح|نْحِبّْ نِرْتَاحْ
                TUNISIAN|request.medicine|نْحِب دْوَايَا|نْحِبّْ دْوَايَا
                TUNISIAN|request.toilet|نْحِب نَمْشِي لِلتْوَالِيت|نْحِبّْ نِمْشِي لِلتّْوَالِيتْ
                TUNISIAN|request.adjust_pillow|صَلَّح لِي المِخَدَّة|صَلَّحْ لِي الْمِخَدَّة
                TUNISIAN|request.headache|رَاسِي يُوجَع فِيَّا|رَاسِي يُوجَعْ فِيَّا
                TUNISIAN|request.cold|البَرْد قْوِي|الْبَرْدْ قْوِيّْ
                TUNISIAN|dish.tunisian.couscous_fish|كُسْكْسِي بِالحُوت|كُسْكْسِي بِالْحُوتْ
                TUNISIAN|dish.tunisian.brik|بْرِيك|بْرِيكْ
                TUNISIAN|dish.tunisian.tajine|طَاجِين تُونْسِي|طَاجِينْ تُونْسِي
                TUNISIAN|dish.tunisian.plate|صْحَن تُونْسِي|صْحَنْ تُونْسِي
                TUNISIAN|dish.tunisian.bird_tongue_soup|شُرْبِة لْسَان عَصْفُور|شُرْبِة لْسَانْ عَصْفُورْ
                TUNISIAN|dish.tunisian.pea_stew|مَرْقِة جِلْبَانَة|مَرْڨِة جِلْبَانَة
                TUNISIAN|dish.tunisian.pasta|مَقْرُونَة بِالصَّلْصَة|مَڨْرُونَة بِالصَّلْصَة
                TUNISIAN|dish.tunisian.merguez_omelette|عُجَّة بِالمَرْقَاز|عُجَّة بِالْمَرْڨَازْ
                TUNISIAN|dish.tunisian.maqroudh|مَقْرُوض قَيْرَوَانِي|مَڨْرُوضْ قَيْرَوَانِي
                TUNISIAN|routine.call_to_prayer|أَذَان|أَذَانْ
                TUNISIAN|routine.ablution|وُضُوء|وُضُوءْ
                TUNISIAN|routine.prayer|صَلَاة|صْلَاة
                TUNISIAN|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صْلَاة
                TUNISIAN|routine.quran|مُصْحَف|مُصْحَفْ
                TUNISIAN|routine.evening_coffee|قَهْوَة العَشِيَّة|قَهْوِة الْعَشِيَّة
                TUNISIAN|routine.sleep|رْقَاد|رْڨَادْ
                TUNISIAN|routine.walk|مَشْي|مَشِي
                TUNISIAN|vegetable.tomato|طَمَاطِم|طَمَاطِمْ
                TUNISIAN|vegetable.onion|بَصَل|بَصَلْ
                TUNISIAN|vegetable.garlic|تُوم|تُومْ
                TUNISIAN|vegetable.cucumber|خِيَار|خِيَارْ
                TUNISIAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                TUNISIAN|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانْ
                TUNISIAN|vegetable.zucchini|قْرَع أَخْضَر|ڨْرَعْ أَخْضَرْ
                TUNISIAN|vegetable.lettuce|خَسّ|خَسّْ
                TUNISIAN|vegetable.parsley|مَعْدَنُوس|مَعْدَنُوسْ
                TUNISIAN|vegetable.coriander|كُزْبَرَة|كُزْبْرَة
                TUNISIAN|vegetable.lemon|قَارِص|قَارِصْ
                TUNISIAN|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                TUNISIAN|vegetable.cauliflower|شَفْلُور|شَفْلُورْ
                TUNISIAN|vegetable.radish|فِجِل|فِجِلْ
                TUNISIAN|vegetable.pumpkin|قَرْع|ڨْرَعْ
                TUNISIAN|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                TUNISIAN|fruit.apple|تُفَّاح|تُفَّاحْ
                TUNISIAN|fruit.banana|مُوز|مُوزْ
                TUNISIAN|fruit.orange|بُرْدْقَان|بُرْدْڨَانْ
                TUNISIAN|fruit.grapes|عِنَب|عِنَبْ
                TUNISIAN|fruit.watermelon|دَلَّاع|دَلَّاعْ
                TUNISIAN|fruit.melon|بِطِّيخ|بِطِّيخْ
                TUNISIAN|fruit.peach|خُوخ|خُوخْ
                TUNISIAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                TUNISIAN|fruit.pear|إِجَّاص|إِجَّاصْ
                TUNISIAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                TUNISIAN|fruit.figs|كَرْمُوس|كَرْمُوسْ
                TUNISIAN|fruit.dates|تَمْر|تَمْرْ
                TUNISIAN|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                TUNISIAN|fruit.cherry|كَرَز|كَرَزْ
                TUNISIAN|fruit.berries|تُوت|تُوتْ
                TUNISIAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                TUNISIAN|food.bread|خُبْز|خُبْزْ
                TUNISIAN|food.milk|حَلِيب|حَلِيبْ
                TUNISIAN|food.eggs|عْظَم|عْظَمْ
                TUNISIAN|food.cheese|جُبْن|جُبْنْ
                TUNISIAN|food.yogurt|يَاغُرْت|يَاغُرْتْ
                TUNISIAN|food.rice|رُوز|رُوزْ
                TUNISIAN|food.pasta|مَقْرُونَة|مَڨْرُونَة
                TUNISIAN|food.meat|لَحْم|لَحْمْ
                TUNISIAN|food.chicken|دَجَاج|دَجَاجْ
                TUNISIAN|food.fish|حُوت|حُوتْ
                TUNISIAN|food.olives|زِيتُون|زِيتُونْ
                TUNISIAN|food.oil|زِيت|زِيتْ
                TUNISIAN|food.tea|تَاي|تَايْ
                TUNISIAN|food.juice|عَصِير|عَصِيرْ
                TUNISIAN|food.water|مَاء|مَاءْ
                TUNISIAN|food.honey|عَسَل|عَسَلْ
                TUNISIAN|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                TUNISIAN|food.salt|مِلْح|مِلْحْ
                TUNISIAN|food.sugar|سُكَّر|سُكَّرْ
                TUNISIAN|home.bed|سَرِير|سَرِيرْ
                TUNISIAN|home.pillow|مَخَدَّة|مِخَدَّة
                TUNISIAN|home.blanket|فَرَاشِيَّة|فْرَاشِيَّة
                TUNISIAN|home.sofa|فُوتَاي|فُوتَايْ
                TUNISIAN|home.door|بَاب|بَابْ
                TUNISIAN|home.window|شُبَّاك|شُبَّاكْ
                TUNISIAN|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                TUNISIAN|home.stove|غَاز|غَازْ
                TUNISIAN|home.oven|فُرْن|فُرْنْ
                TUNISIAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                TUNISIAN|home.key|مِفْتَاح|مِفْتَاحْ
                TUNISIAN|home.cup|كَاس|كَاسْ
                TUNISIAN|home.spoon|مَغْرَفَة|مَغْرْفَة
                TUNISIAN|home.teapot|بَرَّاد|بَرَّادْ
                TUNISIAN|home.pot|طَنْجَرَة|طَنْجْرَة
                TUNISIAN|home.water_bottle|دَبُّوزَة مَاء|دَبُّوزِة مَاءْ
                TUNISIAN|clothing.tshirt|تِيشِيرْت|تِيشِيرْتْ
                TUNISIAN|clothing.shirt|قَمِيص|قَمِيصْ
                TUNISIAN|clothing.shorts|شُورْت|شُورْتْ
                TUNISIAN|clothing.trousers|سِرْوَال|سِرْوَالْ
                TUNISIAN|clothing.dress|فُسْتَان|فُسْتَانْ
                TUNISIAN|clothing.robe|رُوب|رُوبْ
                TUNISIAN|clothing.hijab|حِجَاب|حِجَابْ
                TUNISIAN|clothing.scarf|وِشَاح|وِشَاحْ
                TUNISIAN|clothing.shoes|سْبَاط|سْبَاطْ
                TUNISIAN|clothing.glasses|نَظَّارَات|نَظَّارَاتْ
                TUNISIAN|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                TUNISIAN|clothing.bag|صَاك|صَاكْ
                TUNISIAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                TUNISIAN|clothing.high_heels|سْبَاط طَالُون|سْبَاطْ طَالُونْ
                TUNISIAN|clothing.ring|خَاتَم|خَاتَمْ
                TUNISIAN|body.eye|عِين|عِينْ
                TUNISIAN|body.nose|خَشِم|خَشِمْ
                TUNISIAN|body.mouth|فَم|فَمْ
                TUNISIAN|body.tongue|لِسَان|لِسَانْ
                TUNISIAN|body.ear|وِذِن|وِذِنْ
                TUNISIAN|body.hand|يَد|يَدْ
                TUNISIAN|body.leg|سَاق|سَاقْ
                TUNISIAN|body.head|رَاس|رَاسْ
                TUNISIAN|body.hair|شَعْر|شَعْرْ
                TUNISIAN|body.back|ظَهْر|ظَهْرْ
                TUNISIAN|body.abdomen|بَطْن|بَطْنْ
                TUNISIAN|body.finger|صُبُع|صُبُعْ
                TUNISIAN|body.neck|رَقْبَة|رَڨْبَة
                TUNISIAN|body.chest|صَدْر|صَدْرْ
                TUNISIAN|body.face|وَجْه|وَجْهْ
                TUNISIAN|body.heart|قَلْب|قَلْبْ
                TUNISIAN|body.shoulder|كِتْف|كِتْفْ
                TUNISIAN|action.eat|نَاكِل|نَاكِلْ
                TUNISIAN|action.drink|نِشْرِب|نِشْرِبْ
                TUNISIAN|action.sleep|نُرْقُد|نُرْڨُدْ
                TUNISIAN|action.sit|نُقْعُد|نُڨْعُدْ
                TUNISIAN|action.stand|نُوقَف|نُوقَفْ
                TUNISIAN|action.wash|نِغْسِل|نِغْسِلْ
                TUNISIAN|action.hear|نِسْمَع|نِسْمَعْ
                TUNISIAN|action.open|نْحِلّ|نْحِلّْ
                TUNISIAN|action.close|نْسَكِّر|نْسَكِّرْ
                TUNISIAN|action.laugh|نِضْحَك|نِضْحَكْ
                TUNISIAN|action.see|نْشُوف|نْشُوفْ
                TUNISIAN|action.dress|نِلْبِس|نِلْبِسْ
                TUNISIAN|animal.cat|قَطُّوسَة|ڨَطُّوسَة
                TUNISIAN|animal.dog|كَلْب|كَلْبْ
                TUNISIAN|animal.sheep|عَلُّوش|عَلُّوشْ
                TUNISIAN|animal.horse|حِصَان|حِصَانْ
                TUNISIAN|animal.cow|بَقَرَة|بَڨْرَة
                TUNISIAN|animal.sparrow|عُصْفُور|عُصْفُورْ
                TUNISIAN|animal.pigeon|حَمَامَة|حْمَامَة
                TUNISIAN|animal.chicken|دَجَاجَة|دْجَاجَة
                TUNISIAN|animal.camel|جَمَل|جَمَلْ
                TUNISIAN|animal.rooster|سَرْدُوك|سَرْدُوكْ
                TUNISIAN|animal.rabbit|أَرْنَب|أَرْنَبْ
                TUNISIAN|transport.bus|كَار|كَارْ
                TUNISIAN|transport.minibus|لُوَاج|لُوَاجْ
                TUNISIAN|transport.bicycle|بِسْكْلَات|بِسْكْلَاتْ
                TUNISIAN|transport.motorcycle|مُوتُور|مُوتُورْ
                TUNISIAN|transport.train|تْرَان|تْرَانْ
                TUNISIAN|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                TUNISIAN|transport.truck|كَامْيُون|كَامْيُونْ
                TUNISIAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                TUNISIAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                TUNISIAN|greeting.welcome|مَرْحَبَا بِيك|مَرْحَبَا بِيكْ
                TUNISIAN|greeting.how_are_you|شنُوَّة أَحْوَالَك؟|شْنُوَّة أَحْوَالِكْ؟
                TUNISIAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                TUNISIAN|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                TUNISIAN|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                TUNISIAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                TUNISIAN|greeting.good_health|يَعْطِيك الصَّحَّة|يَعْطِيكْ الصَّحَّة
                TUNISIAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                TUNISIAN|greeting.thanks|شُكْرًا لِيك|شُكْرًا لِيكْ
                TUNISIAN|greeting.bless|رَبِّي يبَارِك فِيك|رَبِّي يْبَارِكْ فِيكْ
            """.trimIndent(),
            """
                MOROCCAN|family.paternal_aunt|عَمَّتِي|عَمْتِي
                MOROCCAN|family.maternal_aunt|خَالَتِي|خَالْتِي
                MOROCCAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                MOROCCAN|family.paternal_uncle_son|وَلْد عَمِّي|وَلْدْ عَمِّي
                MOROCCAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                MOROCCAN|family.paternal_aunt_son|وَلْد عَمْتِي|وَلْدْ عَمْتِي
                MOROCCAN|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتِي
                MOROCCAN|family.maternal_uncle_son|وَلْد خَالِي|وَلْدْ خَالِي
                MOROCCAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                MOROCCAN|family.maternal_aunt_son|وَلْد خَالْتِي|وَلْدْ خَالْتِي
                MOROCCAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتِي
                MOROCCAN|family.brother_son|وَلْد خُويَا|وَلْدْ خُويَا
                MOROCCAN|family.brother_daughter|بِنْت خُويَا|بِنْتْ خُويَا
                MOROCCAN|family.sister_son|وَلْد أُخْتِي|وَلْدْ أُخْتِي
                MOROCCAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                MOROCCAN|family.husband_mother|أُم رَاجْلِي|أُمْ رَاجْلِي
                MOROCCAN|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتِي
                MOROCCAN|family.husband_sister|أُخْت رَاجْلِي|أُخْتْ رَاجْلِي
                MOROCCAN|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتِي
                MOROCCAN|family.sister_husband|رَاجِل أُخْتِي|رَاجِلْ أُخْتِي
                MOROCCAN|family.brother_wife|مَرْت خُويَا|مَرْتْ خُويَا
                MOROCCAN|family.daughter_husband|رَاجِل بِنْتِي|رَاجِلْ بِنْتِي
                MOROCCAN|family.son_wife|مَرْت وَلْدِي|مَرْتْ وَلْدِي
                MOROCCAN|family.male_in_law|نَسِيبِي|نْسِيبِي
                MOROCCAN|family.female_in_law|نَسِيبَتِي|نْسِيبْتِي
                MOROCCAN|family.male_relative|قَرِيبِي|قْرِيبِي
                MOROCCAN|family.female_relative|قَرِيبَتِي|قْرِيبْتِي
                MOROCCAN|request.water|بْغِيت المَا|بْغِيتْ الْمَا
                MOROCCAN|request.tea|بْغِيت أَتَاي|بْغِيتْ أَتَايْ
                MOROCCAN|request.rest|بْغِيت نِرْتَاح|بْغِيتْ نِرْتَاحْ
                MOROCCAN|request.medicine|بْغِيت الدْوَا دْيَالِي|بْغِيتْ الدّْوَا دْيَالِي
                MOROCCAN|request.toilet|بْغِيت نَمْشِي لِلحَمَّام|بْغِيتْ نْمْشِي لِلْحَمَّامْ
                MOROCCAN|request.adjust_pillow|صَوَّب لِيَّا المْخَدَّة|صَوَّبْ لِيَّا الْمْخَدَّة
                MOROCCAN|request.cold|حَسِّيت بِالبَرْد|حَسِّيتْ بِالْبَرْدْ
                MOROCCAN|dish.moroccan.prune_tajine|طَاجِين بِالبَرْقُوق|طَاجِينْ بِالْبَرْقُوقْ
                MOROCCAN|dish.moroccan.couscous|كْسْكْسُو سَبْع خْضَار|كْسْكْسُو سَبْعْ خْضَارْ
                MOROCCAN|dish.moroccan.pastilla|بَسْطِيلَة بِالحُوت|بَسْطِيلَة بِالْحُوتْ
                MOROCCAN|dish.moroccan.rfissa|رْفِيسَة بِالدْجَاج|رْفِيسَة بِالدّْجَاجْ
                MOROCCAN|dish.moroccan.zaalouk|زَعْلُوك|زَعْلُوكْ
                MOROCCAN|dish.moroccan.kefta_tajine|طَاجِين كُفْتَة بِالبِيض|طَاجِينْ كُفْتَة بِالْبِيضْ
                MOROCCAN|dish.moroccan.skewers|قُطْبَان|گُطْبَانْ
                MOROCCAN|dish.moroccan.msemen|مْسَمَّن|مْسَمَّنْ
                MOROCCAN|dish.moroccan.baghrir|بْغْرِير|بْغْرِيرْ
                MOROCCAN|routine.call_to_prayer|أَذَان|أَذَانْ
                MOROCCAN|routine.ablution|وُضُوء|وُضُوءْ
                MOROCCAN|routine.prayer|صَلَاة|صْلَاة
                MOROCCAN|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صْلَاة
                MOROCCAN|routine.quran|مُصْحَف|مُصْحَفْ
                MOROCCAN|routine.evening_coffee|قَهْوَة العَشِيَّة|قَهْوِة الْعَشِيَّة
                MOROCCAN|routine.sleep|نْعَاس|نْعَاسْ
                MOROCCAN|routine.walk|مَشْي|مْشِي
                MOROCCAN|vegetable.onion|بَصَل|بَصَلْ
                MOROCCAN|vegetable.garlic|تُوم|تُومْ
                MOROCCAN|vegetable.cucumber|خِيَار|خِيَارْ
                MOROCCAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                MOROCCAN|vegetable.eggplant|دَنْجَال|دَنْجَالْ
                MOROCCAN|vegetable.lettuce|خَسّ|خَسّْ
                MOROCCAN|vegetable.parsley|مَعْدَنُوس|مَعْدَنُوسْ
                MOROCCAN|vegetable.coriander|قَزْبُور|گَزْبُورْ
                MOROCCAN|vegetable.lemon|حَامْض|حَامِضْ
                MOROCCAN|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                MOROCCAN|vegetable.cauliflower|شِيفْلُور|شِيفْلُورْ
                MOROCCAN|vegetable.radish|فِجِل|فِجِلْ
                MOROCCAN|vegetable.pumpkin|قَرْعَة حَمْرَا|گَرْعَة حَمْرَا
                MOROCCAN|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                MOROCCAN|fruit.apple|تُفَّاح|تُفَّاحْ
                MOROCCAN|fruit.banana|بَنَان|بَنَانْ
                MOROCCAN|fruit.orange|بُرْتُقَال|بُرْتُقَالْ
                MOROCCAN|fruit.grapes|عِنَب|عِنَبْ
                MOROCCAN|fruit.watermelon|دَلَّاح|دَلَّاحْ
                MOROCCAN|fruit.melon|شَمَّام|شَمَّامْ
                MOROCCAN|fruit.peach|خُوخ|خُوخْ
                MOROCCAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                MOROCCAN|fruit.pear|إِجَّاص|إِجَّاصْ
                MOROCCAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                MOROCCAN|fruit.figs|كَرْمُوس|كَرْمُوسْ
                MOROCCAN|fruit.dates|تَمْر|تَمْرْ
                MOROCCAN|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                MOROCCAN|fruit.cherry|كَرَز|كَرَزْ
                MOROCCAN|fruit.berries|تُوت|تُوتْ
                MOROCCAN|fruit.mandarin|مَنْدَرِين|مَنْدَرِينْ
                MOROCCAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                MOROCCAN|food.bread|خُبْز|خُبْزْ
                MOROCCAN|food.milk|حَلِيب|حَلِيبْ
                MOROCCAN|food.eggs|بِيض|بِيضْ
                MOROCCAN|food.cheese|فُرْمَاج|فُرْمَاجْ
                MOROCCAN|food.yogurt|يَاغُورْت|يَاغُورْتْ
                MOROCCAN|food.rice|رُوز|رُوزْ
                MOROCCAN|food.pasta|مَقْرُونِيَّة|مَگْرُونِيَّة
                MOROCCAN|food.soup|حَرِيرَة|حْرِيرَة
                MOROCCAN|food.meat|لَحْم|لَحْمْ
                MOROCCAN|food.chicken|دْجَاج|دْجَاجْ
                MOROCCAN|food.fish|حُوت|حُوتْ
                MOROCCAN|food.olives|زِيتُون|زِيتُونْ
                MOROCCAN|food.oil|زِيت|زِيتْ
                MOROCCAN|food.tea|أَتَاي|أَتَايْ
                MOROCCAN|food.juice|عَصِير|عَصِيرْ
                MOROCCAN|food.water|مَاء|مَاءْ
                MOROCCAN|food.honey|عَسَل|عَسَلْ
                MOROCCAN|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                MOROCCAN|food.salt|مِلْح|مِلْحْ
                MOROCCAN|food.sugar|سُكَّر|سُكَّرْ
                MOROCCAN|home.bed|سَرِير|سَرِيرْ
                MOROCCAN|home.pillow|مَخَدَّة|مْخَدَّة
                MOROCCAN|home.door|بَاب|بَابْ
                MOROCCAN|home.window|شَرْجَم|شَرْجَمْ
                MOROCCAN|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                MOROCCAN|home.oven|فَرَّان|فَرَّانْ
                MOROCCAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                MOROCCAN|home.wardrobe|خِزَانَة|خْزَانَة
                MOROCCAN|home.key|مِفْتَاح|مِفْتَاحْ
                MOROCCAN|home.cup|كَاس|كَاسْ
                MOROCCAN|home.spoon|مَعْلَقَة|مْعِلْقَة
                MOROCCAN|home.knife|مُوس|مُوسْ
                MOROCCAN|home.teapot|بَرَّاد|بَرَّادْ
                MOROCCAN|home.pot|طَنْجَرَة|طَنْجْرَة
                MOROCCAN|home.water_bottle|قَرْعَة مَاء|گَرْعِة مَاءْ
                MOROCCAN|clothing.tshirt|تِيشِيرْت|تِيشِيرْتْ
                MOROCCAN|clothing.shirt|قَمِيص|قَمِيصْ
                MOROCCAN|clothing.shorts|شُورْت|شُورْتْ
                MOROCCAN|clothing.trousers|سِرْوَال|سِرْوَالْ
                MOROCCAN|clothing.dress|فُسْتَان|فُسْتَانْ
                MOROCCAN|clothing.robe|رُوب|رُوبْ
                MOROCCAN|clothing.hijab|حِجَاب|حِجَابْ
                MOROCCAN|clothing.scarf|وِشَاح|وِشَاحْ
                MOROCCAN|clothing.shoes|صْبَاط|صْبَاطْ
                MOROCCAN|clothing.socks|تْقَاشِر|تْگَاشِرْ
                MOROCCAN|clothing.glasses|نَضَاضِر|نْضَاضِرْ
                MOROCCAN|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                MOROCCAN|clothing.bag|صَاك|صَاكْ
                MOROCCAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                MOROCCAN|clothing.high_heels|صْبَاط طَالُون|صْبَاطْ طَالُونْ
                MOROCCAN|clothing.ring|خَاتَم|خَاتَمْ
                MOROCCAN|body.eye|عِين|عِينْ
                MOROCCAN|body.nose|نِيف|نِيفْ
                MOROCCAN|body.mouth|فَم|فَمْ
                MOROCCAN|body.tongue|لِسَان|لِسَانْ
                MOROCCAN|body.ear|وِذِن|وِذِنْ
                MOROCCAN|body.hand|يَد|يَدْ
                MOROCCAN|body.leg|رِجْل|رِجْلْ
                MOROCCAN|body.head|رَاس|رَاسْ
                MOROCCAN|body.hair|شَعْر|شَعْرْ
                MOROCCAN|body.back|ظَهْر|ظَهْرْ
                MOROCCAN|body.abdomen|بَطْن|بَطْنْ
                MOROCCAN|body.finger|صُبَع|صُبَعْ
                MOROCCAN|body.neck|رَقْبَة|رَگْبَة
                MOROCCAN|body.chest|صَدْر|صَدْرْ
                MOROCCAN|body.face|وَجْه|وَجْهْ
                MOROCCAN|body.tooth|سِنّ|سِنّْ
                MOROCCAN|body.heart|قَلْب|قَلْبْ
                MOROCCAN|body.shoulder|كِتْف|كِتْفْ
                MOROCCAN|action.eat|نَاكُل|نَاكُلْ
                MOROCCAN|action.drink|نِشْرَب|نِشْرَبْ
                MOROCCAN|action.sleep|نِنْعَس|نِنْعَسْ
                MOROCCAN|action.sit|نْجْلِس|نْجْلِسْ
                MOROCCAN|action.walk|نِمْشِي|نْمْشِي
                MOROCCAN|action.stand|نُوقَف|نُوقَفْ
                MOROCCAN|action.wash|نِغْسِل|نِغْسِلْ
                MOROCCAN|action.speak|نَهْضَر|نَهْضَرْ
                MOROCCAN|action.hear|نِسْمَع|نِسْمَعْ
                MOROCCAN|action.open|نْحِلّ|نْحِلّْ
                MOROCCAN|action.close|نْسَد|نْسَدّْ
                MOROCCAN|action.laugh|نِضْحَك|نِضْحَكْ
                MOROCCAN|action.see|نْشُوف|نْشُوفْ
                MOROCCAN|action.dress|نِلْبَس|نِلْبَسْ
                MOROCCAN|animal.cat|مُشّ|مُشّْ
                MOROCCAN|animal.dog|كَلْب|كَلْبْ
                MOROCCAN|animal.horse|حِصَان|حِصَانْ
                MOROCCAN|animal.cow|بَقَرَة|بَگْرَة
                MOROCCAN|animal.sparrow|عُصْفُور|عُصْفُورْ
                MOROCCAN|animal.pigeon|حَمَامَة|حْمَامَة
                MOROCCAN|animal.chicken|دَجَاجَة|دْجَاجَة
                MOROCCAN|animal.camel|جَمَل|جَمَلْ
                MOROCCAN|animal.rooster|دِيك|دِيكْ
                MOROCCAN|animal.rabbit|قْنِيَّة|گْنِيَّة
                MOROCCAN|transport.car|طُومُوبِيل|طُومُوبِيلْ
                MOROCCAN|transport.bus|طُوبِيس|طُوبِيسْ
                MOROCCAN|transport.minibus|طُوبِيس صْغِير|طُوبِيسْ صْغِيرْ
                MOROCCAN|transport.motorcycle|مُوطُور|مُوطُورْ
                MOROCCAN|transport.train|تْرَان|تْرَانْ
                MOROCCAN|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                MOROCCAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                MOROCCAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                MOROCCAN|greeting.welcome|مَرْحَبَا بِيك|مَرْحَبَا بِيكْ
                MOROCCAN|greeting.how_are_you|لَابَاس عَلِيك؟|لَابَاسْ عَلِيكْ؟
                MOROCCAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                MOROCCAN|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                MOROCCAN|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                MOROCCAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                MOROCCAN|greeting.good_health|يَعْطِيك الصَّحَّة|يَعْطِيكْ الصَّحَّة
                MOROCCAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                MOROCCAN|greeting.thanks|شُكْرًا بْزَّاف|شُكْرًا بْزَّافْ
                MOROCCAN|greeting.bless|اللَّهُ يبَارِك فِيك|اللَّهْ يْبَارَكْ فِيكْ
            """.trimIndent(),
            """
                ALGERIAN|family.paternal_aunt|عَمَّتِي|عَمْتِي
                ALGERIAN|family.maternal_aunt|خَالَتِي|خَالْتِي
                ALGERIAN|family.granddaughter|حَفِيدَتِي|حَفِيدْتِي
                ALGERIAN|family.paternal_uncle_son|وَلْد عَمِّي|وَلْدْ عَمِّي
                ALGERIAN|family.paternal_uncle_daughter|بِنْت عَمِّي|بِنْتْ عَمِّي
                ALGERIAN|family.paternal_aunt_son|وَلْد عَمْتِي|وَلْدْ عَمْتِي
                ALGERIAN|family.paternal_aunt_daughter|بِنْت عَمْتِي|بِنْتْ عَمْتِي
                ALGERIAN|family.maternal_uncle_son|وَلْد خَالِي|وَلْدْ خَالِي
                ALGERIAN|family.maternal_uncle_daughter|بِنْت خَالِي|بِنْتْ خَالِي
                ALGERIAN|family.maternal_aunt_son|وَلْد خَالْتِي|وَلْدْ خَالْتِي
                ALGERIAN|family.maternal_aunt_daughter|بِنْت خَالْتِي|بِنْتْ خَالْتِي
                ALGERIAN|family.brother_son|وَلْد خُويَا|وَلْدْ خُويَا
                ALGERIAN|family.brother_daughter|بِنْت خُويَا|بِنْتْ خُويَا
                ALGERIAN|family.sister_son|وَلْد أُخْتِي|وَلْدْ أُخْتِي
                ALGERIAN|family.sister_daughter|بِنْت أُخْتِي|بِنْتْ أُخْتِي
                ALGERIAN|family.husband_mother|أُم رَاجْلِي|أُمْ رَاجْلِي
                ALGERIAN|family.wife_mother|أُم مَرْتِي|أُمْ مَرْتِي
                ALGERIAN|family.husband_sister|أُخْت رَاجْلِي|أُخْتْ رَاجْلِي
                ALGERIAN|family.wife_sister|أُخْت مَرْتِي|أُخْتْ مَرْتِي
                ALGERIAN|family.sister_husband|رَاجِل أُخْتِي|رَاجِلْ أُخْتِي
                ALGERIAN|family.brother_wife|مَرْت خُويَا|مَرْتْ خُويَا
                ALGERIAN|family.daughter_husband|رَاجِل بِنْتِي|رَاجِلْ بِنْتِي
                ALGERIAN|family.son_wife|مَرْت وَلْدِي|مَرْتْ وَلْدِي
                ALGERIAN|family.male_in_law|نَسِيبِي|نْسِيبِي
                ALGERIAN|family.female_in_law|نَسِيبَتِي|نْسِيبْتِي
                ALGERIAN|family.male_relative|قَرِيبِي|قْرِيبِي
                ALGERIAN|family.female_relative|قَرِيبَتِي|قْرِيبْتِي
                ALGERIAN|request.water|نْحَب نَشْرَب المَا|نْحَبّْ نَشْرَبْ الْمَا
                ALGERIAN|request.tea|نْحَب أَتَاي|نْحَبّْ أَتَايْ
                ALGERIAN|request.rest|نْحَب نِرْتَاح|نْحَبّْ نِرْتَاحْ
                ALGERIAN|request.medicine|نْحَب دْوَايَا|نْحَبّْ دْوَايَا
                ALGERIAN|request.toilet|بْغِيت نْرُوح لِلتْوَالِيت|بْغِيتْ نْرُوحْ لِلتّْوَالِيتْ
                ALGERIAN|request.adjust_pillow|صَلَّحْلِي المْخَدَّة|صَلَّحْلِي الْمْخَدَّة
                ALGERIAN|request.cold|رَانِي بَرْدَان|رَانِي بَرْدَانْ
                ALGERIAN|dish.algerian.couscous|طْعَام|طْعَامْ
                ALGERIAN|dish.algerian.olive_tajine|طَاجِين الزِّيتُون|طَاجِينْ الزِّيتُونْ
                ALGERIAN|dish.algerian.frik_soup|شُرْبِة فْرِيك|شُرْبِة فْرِيكْ
                ALGERIAN|dish.algerian.bourek|بُورَاك|بُورَاكْ
                ALGERIAN|dish.algerian.sweet_tajine|طَاجِين الحْلُو|طَاجِينْ الْحْلُو
                ALGERIAN|dish.algerian.berkoukes|بَرْكُوكَس|بَرْكُوكَسْ
                ALGERIAN|dish.algerian.potato_stew|جْوَاز بَطَاطَا|جْوَازْ بَطَاطَا
                ALGERIAN|dish.algerian.mahajeb|مْحَاجِب|مْحَاجِبْ
                ALGERIAN|dish.algerian.stuffed_artichoke|قَرْنُون مْعَمَّر|ڨَرْنُونْ مْعَمَّرْ
                ALGERIAN|dish.algerian.kalb_el_louz|قَلْب اللُّوز|قَلْبْ اللُّوزْ
                ALGERIAN|dish.algerian.makrout|مَقْرُوط العَسَل|مَقْرُوطْ الْعَسَلْ
                ALGERIAN|routine.call_to_prayer|أَذَان|أَذَانْ
                ALGERIAN|routine.ablution|وُضُوء|وُضُوءْ
                ALGERIAN|routine.prayer|صَلَاة|صْلَاة
                ALGERIAN|routine.prayer_rug|سَجَّادَة صَلَاة|سَجَّادِة صْلَاة
                ALGERIAN|routine.quran|مُصْحَف|مُصْحَفْ
                ALGERIAN|routine.evening_coffee|قَهْوَة العَشِيَّة|قَهْوِة الْعَشِيَّة
                ALGERIAN|routine.sleep|رْقَاد|رْڨَادْ
                ALGERIAN|routine.walk|مَشْي|مْشِي
                ALGERIAN|vegetable.tomato|طْمَاطَم|طْمَاطَمْ
                ALGERIAN|vegetable.onion|بَصَل|بَصَلْ
                ALGERIAN|vegetable.garlic|تُوم|تُومْ
                ALGERIAN|vegetable.cucumber|خِيَار|خِيَارْ
                ALGERIAN|vegetable.pepper|فِلْفِل|فِلْفِلْ
                ALGERIAN|vegetable.eggplant|بَاذِنْجَان|بَاذِنْجَانْ
                ALGERIAN|vegetable.lettuce|خَسّ|خَسّْ
                ALGERIAN|vegetable.parsley|مَعْدَنُوس|مَعْدَنُوسْ
                ALGERIAN|vegetable.coriander|قُزْبُر|ڨُزْبُرْ
                ALGERIAN|vegetable.lemon|قَارَص|قَارَصْ
                ALGERIAN|vegetable.spinach|سَبَانِخ|سَبَانِخْ
                ALGERIAN|vegetable.cauliflower|شِيفْلُور|شِيفْلُورْ
                ALGERIAN|vegetable.radish|فِجِل|فِجِلْ
                ALGERIAN|vegetable.pumpkin|قَرْعَة حَمْرَا|ڨَرْعَة حَمْرَا
                ALGERIAN|vegetable.mint|نَعْنَاع|نَعْنَاعْ
                ALGERIAN|fruit.apple|تُفَّاح|تُفَّاحْ
                ALGERIAN|fruit.banana|بَنَان|بَنَانْ
                ALGERIAN|fruit.orange|تشِينَة|تْشِينَة
                ALGERIAN|fruit.grapes|عِنَب|عِنَبْ
                ALGERIAN|fruit.watermelon|دَلَّاع|دَلَّاعْ
                ALGERIAN|fruit.melon|شَمَّام|شَمَّامْ
                ALGERIAN|fruit.peach|خُوخ|خُوخْ
                ALGERIAN|fruit.apricot|مِشْمِش|مِشْمِشْ
                ALGERIAN|fruit.pomegranate|رُمَّان|رُمَّانْ
                ALGERIAN|fruit.figs|كَرْمُوس|كَرْمُوسْ
                ALGERIAN|fruit.dates|تَمْر|تَمْرْ
                ALGERIAN|fruit.pineapple|أَنَانَاس|أَنَانَاسْ
                ALGERIAN|fruit.cherry|كَرَز|كَرَزْ
                ALGERIAN|fruit.berries|تُوت|تُوتْ
                ALGERIAN|fruit.mandarin|مَنْدَارِين|مَنْدَارِينْ
                ALGERIAN|fruit.coconut|جُوز الهِنْد|جُوزْ الْهِنْدْ
                ALGERIAN|food.bread|خُبْز|خُبْزْ
                ALGERIAN|food.milk|حَلِيب|حَلِيبْ
                ALGERIAN|food.eggs|بِيض|بِيضْ
                ALGERIAN|food.cheese|فُرْمَاج|فُرْمَاجْ
                ALGERIAN|food.yogurt|يَاغُرْت|يَاغُرْتْ
                ALGERIAN|food.rice|رُوز|رُوزْ
                ALGERIAN|food.pasta|مَقْرُون|مَڨْرُونْ
                ALGERIAN|food.meat|لَحْم|لَحْمْ
                ALGERIAN|food.chicken|دَجَاج|دْجَاجْ
                ALGERIAN|food.fish|حُوت|حُوتْ
                ALGERIAN|food.olives|زِيتُون|زِيتُونْ
                ALGERIAN|food.oil|زِيت|زِيتْ
                ALGERIAN|food.tea|أَتَاي|أَتَايْ
                ALGERIAN|food.juice|عَصِير|عَصِيرْ
                ALGERIAN|food.water|مَاء|مَاءْ
                ALGERIAN|food.honey|عَسَل|عَسَلْ
                ALGERIAN|food.biscuit|بِسْكُوِيت|بِسْكُوِيتْ
                ALGERIAN|food.salt|مِلْح|مِلْحْ
                ALGERIAN|food.sugar|سُكَّر|سُكَّرْ
                ALGERIAN|home.bed|سَرِير|سَرِيرْ
                ALGERIAN|home.pillow|مَخَدَّة|مْخَدَّة
                ALGERIAN|home.sofa|فُوتَاي|فُوتَايْ
                ALGERIAN|home.door|بَاب|بَابْ
                ALGERIAN|home.television|تِلِفِزْيُون|تِلِفِزْيُونْ
                ALGERIAN|home.stove|كُويزِينْيِير|كُويزِينْيِيرْ
                ALGERIAN|home.oven|فُرْن|فُرْنْ
                ALGERIAN|home.air_conditioner|مُكَيِّف|مُكَيِّفْ
                ALGERIAN|home.key|مِفْتَاح|مِفْتَاحْ
                ALGERIAN|home.cup|كَاس|كَاسْ
                ALGERIAN|home.spoon|مُغْرُف|مْغْرُفْ
                ALGERIAN|home.knife|مُوس|مُوسْ
                ALGERIAN|home.teapot|بَرَّاد|بَرَّادْ
                ALGERIAN|home.pot|طَنْجَرَة|طَنْجْرَة
                ALGERIAN|home.water_bottle|قَرْعَة مَاء|ڨَرْعِة مَاءْ
                ALGERIAN|clothing.tshirt|تِيشِيرْت|تِيشِيرْتْ
                ALGERIAN|clothing.shirt|قَمِيص|قَمِيصْ
                ALGERIAN|clothing.shorts|شُورْت|شُورْتْ
                ALGERIAN|clothing.trousers|سِرْوَال|سِرْوَالْ
                ALGERIAN|clothing.dress|فُسْتَان|فُسْتَانْ
                ALGERIAN|clothing.robe|رُوب|رُوبْ
                ALGERIAN|clothing.hijab|حِجَاب|حِجَابْ
                ALGERIAN|clothing.scarf|وِشَاح|وِشَاحْ
                ALGERIAN|clothing.shoes|سْبَاط|سْبَاطْ
                ALGERIAN|clothing.socks|تْقَاشِر|تْڨَاشِرْ
                ALGERIAN|clothing.watch|سَاعَة يَد|سَاعِة يَدّْ
                ALGERIAN|clothing.bag|صَاك|صَاكْ
                ALGERIAN|clothing.sandals|صَنْدَل|صَنْدَلْ
                ALGERIAN|clothing.high_heels|سْبَاط طَالُون|سْبَاطْ طَالُونْ
                ALGERIAN|clothing.ring|خَاتَم|خَاتَمْ
                ALGERIAN|body.eye|عِين|عِينْ
                ALGERIAN|body.nose|نِيف|نِيفْ
                ALGERIAN|body.mouth|فَم|فَمْ
                ALGERIAN|body.tongue|لِسَان|لِسَانْ
                ALGERIAN|body.ear|وِذِن|وِذِنْ
                ALGERIAN|body.hand|يَد|يَدْ
                ALGERIAN|body.leg|رِجْل|رِجْلْ
                ALGERIAN|body.head|رَاس|رَاسْ
                ALGERIAN|body.hair|شَعْر|شَعْرْ
                ALGERIAN|body.back|ظَهْر|ظَهْرْ
                ALGERIAN|body.abdomen|بَطْن|بَطْنْ
                ALGERIAN|body.finger|صُبُع|صُبُعْ
                ALGERIAN|body.neck|رَقْبَة|رَڨْبَة
                ALGERIAN|body.chest|صَدْر|صَدْرْ
                ALGERIAN|body.face|وَجْه|وَجْهْ
                ALGERIAN|body.heart|قَلْب|قَلْبْ
                ALGERIAN|body.shoulder|كِتْف|كِتْفْ
                ALGERIAN|action.eat|نَاكُل|نَاكُلْ
                ALGERIAN|action.drink|نِشْرُب|نِشْرُبْ
                ALGERIAN|action.sleep|نُرْقُد|نُرْڨُدْ
                ALGERIAN|action.sit|نُقْعُد|نُڨْعُدْ
                ALGERIAN|action.walk|نِمْشِي|نْمْشِي
                ALGERIAN|action.stand|نُوقَف|نُوقَفْ
                ALGERIAN|action.wash|نِغْسِل|نِغْسِلْ
                ALGERIAN|action.speak|نَهْدَر|نَهْدَرْ
                ALGERIAN|action.hear|نِسْمَع|نِسْمَعْ
                ALGERIAN|action.open|نْحِلّ|نْحِلّْ
                ALGERIAN|action.close|نْسَكِّر|نْسَكِّرْ
                ALGERIAN|action.laugh|نِضْحَك|نِضْحَكْ
                ALGERIAN|action.see|نْشُوف|نْشُوفْ
                ALGERIAN|action.dress|نِلْبِس|نِلْبِسْ
                ALGERIAN|animal.cat|قِطَّة|ڨَطَّة
                ALGERIAN|animal.dog|كَلْب|كَلْبْ
                ALGERIAN|animal.sheep|خَرُوف|خَرُوفْ
                ALGERIAN|animal.horse|حِصَان|حِصَانْ
                ALGERIAN|animal.cow|بَقَرَة|بَڨْرَة
                ALGERIAN|animal.sparrow|زَاوْش|زَاوْشْ
                ALGERIAN|animal.pigeon|حَمَامَة|حْمَامَة
                ALGERIAN|animal.chicken|دَجَاجَة|دْجَاجَة
                ALGERIAN|animal.camel|جَمَل|جَمَلْ
                ALGERIAN|animal.rooster|دِيك|دِيكْ
                ALGERIAN|animal.rabbit|أَرْنَب|أَرْنَبْ
                ALGERIAN|transport.car|طُومُوبِيل|طُومُوبِيلْ
                ALGERIAN|transport.bus|طُوبِيس|طُوبِيسْ
                ALGERIAN|transport.train|تْرَان|تْرَانْ
                ALGERIAN|transport.helicopter|هِلِيكُوبْتَر|هِلِيكُوبْتَرْ
                ALGERIAN|transport.boat|قَارِب|قَارِبْ
                ALGERIAN|transport.truck|كَامْيُون|كَامْيُونْ
                ALGERIAN|greeting.salam|السَّلَامُ عَلَيْكُم|السَّلَامُ عَلَيْكُمْ
                ALGERIAN|greeting.reply|وَعَلَيْكُمُ السَّلَام|وَعَلَيْكُمُ السَّلَامْ
                ALGERIAN|greeting.how_are_you|لَابَاس عَلِيك؟|لَابَاسْ عَلِيكْ؟
                ALGERIAN|greeting.fine|الحَمْدُ لِلَّه|الْحَمْدُ لِلَّهْ
                ALGERIAN|greeting.good_morning|صَبَاح الخِير|صَبَاحْ الْخِيرْ
                ALGERIAN|greeting.good_evening|مَسَا الخِير|مَسَا الْخِيرْ
                ALGERIAN|greeting.brighten|نَوَّرْت|نَوَّرْتْ
                ALGERIAN|greeting.good_health|يَعْطِيك الصَّحَّة|يَعْطِيكْ الصَّحَّة
                ALGERIAN|greeting.safe|فِي أَمَانِ اللَّه|فِي أَمَانِ اللَّهْ
                ALGERIAN|greeting.bless|رَبِّي يبَارِك فِيك|رَبِّي يْبَارِكْ فِيكْ
            """.trimIndent()
        )
        buildMap {
            for (block in data) for (line in block.lineSequence()) {
                if (line.isBlank()) continue
                val fields = line.split('|')
                require(fields.size == 4) { "Invalid Gemini review candidate" }
                val dialect = ArabicDialect.valueOf(fields[0])
                val key = dialect to fields[1]
                require(put(key, Candidate(fields[2], fields[3])) == null) {
                    "Duplicate Gemini candidate for $"+ "{key}"
                }
            }
        }
    }

    // Written suggestions are review candidates, not certified playback forms.
    // Do not reintroduce the pausal-sukun / unsupported-glyph bugs removed in v1.6.1.
    private val diacritics = Regex("[\u064B-\u065F\u0670\u0640]")
    private val unsupportedTtsLetters = Regex("[چپگڤ]")
    private val wordFinalSukun = Regex("ْ(?=\\s|[؟!،.؛:]|$)")
    private val inflectedPausalTa = Regex("ة[ًٌٍَُِ]")
    private val independentlyReviewedNamespaces = setOf("dish", "greeting", "request")
    // Explicit 1.6.1 Libyan pronunciation regression targets. The independent
    // localization audit must not be silently invalidated by an unlistened proposal.
    private val preservedLibyanPronunciations = setOf("home.pillow", "animal.chicken")

    fun apply(dialect: ArabicDialect, semanticKey: String?, current: String): String {
        val key = semanticKey ?: return current
        val candidate = entries[dialect to key] ?: return current
        // A changed built-in, caregiver override, or later revision must never
        // be overwritten by a stale Gemini review row.
        if (candidate.original != current) return current
        if (dialect == ArabicDialect.LIBYAN && key in preservedLibyanPronunciations) return current
        // Classical endings and previously verified/patch-protected dialect expressions
        // require individual listening evidence before changing production speech.
        if (dialect == ArabicDialect.STANDARD ||
            key.substringBefore('.') in independentlyReviewedNamespaces ||
            NeuralLexicon161.forms[key]?.containsKey(dialect) == true ||
            DialectSpeechRevisions.hasPatch(dialect, key)
        ) return current
        val proposed = candidate.proposed
        // Edge Neural voices are not verified to pronounce Persian/extended
        // graphemes as intended. Keep them in the CSV for native review.
        if (unsupportedTtsLetters.containsMatchIn(proposed) ||
            wordFinalSukun.containsMatchIn(proposed) ||
            inflectedPausalTa.containsMatchIn(proposed)
        ) return current
        // Any change in lexical letters or spaces is a translation/wording
        // change, not a verified diacritic refinement.
        if (diacritics.replace(proposed, "") != diacritics.replace(current, "")) return current
        return proposed
    }

    internal fun candidateCount(): Int = entries.size
}
