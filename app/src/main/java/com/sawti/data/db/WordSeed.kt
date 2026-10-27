package com.sawti.data.db

import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.RegionalDishesCatalog
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.TtsEndings

/**
 * Built-in vocabulary with refined clinical phonetics for Libyan Arabic TTS.
 */
object WordSeed {

    data class Word(
        val display: String,
        val tts: String,
        val emoji: String = "",
        val subgroup: String = "",
        val cue: String? = null,
        val sort: Int? = null,
    )

    data class Cat(
        val id: String,
        val title: String,
        val palette: Int,
        val words: List<Word>,
        val previewOnTap: Boolean = false,
    )

    val categories: List<Cat> = listOf(
        // ---- 1. عائلتي ----
        // The first tab is ready-made relationship vocabulary; all person groups start empty.
        // No actual names or fictional relatives are shipped in a fresh install.
        Cat(
            id = CategoryIds.FAMILY, title = "عائلتي", palette = 0,
            previewOnTap = true,
            words = com.sawti.domain.model.FamilyRelationships.relationshipLabels.map { title ->
                Word(display = title, tts = title,
                    subgroup = com.sawti.domain.model.FamilyRelationships.RELATIVES)
            },
        ),

        // ---- 2. طلباتي اليومية ----
        Cat(
            id = CategoryIds.DAILY, title = "طلباتي اليومية", palette = 1,
            words = listOf(
                Word("نبي مية", "نَبِّيْ إِمِّيَّهْ", "💧", cue = "إِمْـ..."),
                Word("نبي شاهي", "نَبِّيْ شَاهِي", "🍵", cue = "شَا..."),
                Word("نبي نرتاح", "نَبِّيْ نَرْتَاحْ", "🛋️", cue = "نَرْ..."),
                Word("نبي دوايا", "نَبِّيْ إِدْوَايَا", "💊", cue = "دْوَا..."),
                Word("نبي الحمام", "نَبِّيْ الحَمَّامْ", "🚿", cue = "حَمْـ..."),
                Word("عدّل لي المخدة", "عَدِّلْ لِي المِخَدَّهْ", "🛏️", cue = "عَدْ..."),
                Word("راسي يوجع", "رَاسِي يُوجِعْ فِيا", "🤕", cue = "رَا..."),
                Word("سقعانة", "سَقْعَانَهْ", "🥶", cue = "سَقْـ..."),
            ),
        ),

        // ---- 3. أكلات ليبية ----
        Cat(
            id = "libyan", title = "أكلات ليبية", palette = 10,
            words = listOf(
                Word("بازين", "بَازِينْ", "🥣", cue = "بَا..."),
                Word("رشدة كسكاس", "رِشْدِةْ كِسْكَاسْ", "🍜", cue = "رِشْـ..."),
                Word("رشدة برمة", "رِشْدِةْ بُرْمَهْ", "🥘", cue = "رِشْـ..."),
                Word("مكرونة مبكبكة", "مَكَرُونَهْ إِمْبَكْبَكَهْ", "🍝", cue = "مْبَكْـ..."),
                Word("مكرونة مسقية", "مَكَرُونَهْ مَسْقِيَّهْ", "🍛", cue = "مَسْـ..."),
                Word("كسكسي بالحوت", "كُسْكُسِي بِالحُوتْ", "🐠", cue = "كُسْـ..."),
                Word("رز بلاو", "رُزْ بِلَاوْ", "🍚", cue = "رُزْ..."),
                Word("شربة ليبية", "شُرْبَهْ لِيبِيَّهْ", "🍲", cue = "شُرْ..."),
                Word("طبيخة بطاطا", "إِطْبِيخِةْ بَطَاطَا", "🥔", cue = "طْبِيـ..."),
                Word("عصبان", "عُصْبَانْ", "🥙", cue = "عُصْـ..."),
                Word("بوريك", "بُورِيكْ", "🥟", cue = "بُو..."),
                Word("خبزة طاجين", "خُبْزِةْ طَاجِينْ", "🫓", cue = "طَا..."),
                Word("مقروض", "مَقْرُوضْ", "🍪", cue = "مَقْـ..."),
                Word("شاهي بالنعناع", "شَاهِي بِالنَّعْنَاعْ", "🍵", cue = "شَا..."),
                Word("قهوة عربية", "قَهْوَهْ عَرَبِيَّهْ", "☕", cue = "قَهْـ..."),
            ) + RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN).dishes.map { dish ->
                Word(dish.displayText, dish.ttsPrompt, dish.emoji, cue = dish.phonemicCue)
            },
        ),

        // ---- 4. روتين ويوميات ----
        Cat(
            id = "routine", title = "روتين ويوميات", palette = 11,
            words = listOf(
                Word("أذان", "أَذَانْ", "🕌", cue = "أَذَا..."),
                Word("وضوء", "وُضُوءْ", "🚰", cue = "وُضُو..."),
                Word("صلاة", "صَلَاةْ", "🤲", cue = "صَلا..."),
                Word("سجادة", "سَجَّادَهْ", "🧎", cue = "سَجْـ..."),
                Word("سبحة", "سُبْحَهْ", "📿", cue = "سُبْـ..."),
                Word("مصحف", "مُصْحَفْ", "📗", cue = "مُصْـ..."),
                Word("قهوة العشية", "قَهْوِةْ العَشِيَّهْ", "☕", cue = "عَشِيـ..."),
                Word("نوم", "نَوْمْ", "😴", cue = "نَوْ..."),
                Word("راحة", "رَاحَهْ", "🛋️", cue = "رَا..."),
                Word("مشي", "مَشْيْ", "🚶", cue = "مَشْـ..."),
            ),
        ),

        // ---- 5. الخضروات ----
        Cat(
            id = "vegetables", title = "الخضروات", palette = 3,
            words = listOf(
                Word("طماطم", "طَمَاطِمْ", "🍅", cue = "طَمَا..."),
                Word("بطاطا", "بَطَاطَا", "🥔", cue = "بَطَا..."),
                Word("بصل", "بَصَلْ", "🧅", cue = "بَصَـ..."),
                Word("ثوم", "ثُومْ", "🧄", cue = "ثُو..."),
                Word("جزر", "جَزَرْ", "🥕", cue = "جَزَ..."),
                Word("خيار", "خِيَارْ", "🥒", cue = "خِيَا..."),
                Word("فلفل", "فُلْفُلْ", "🌶️", cue = "فُلْـ..."),
                Word("باذنجان", "بَاذِنْجَانْ", "🍆", cue = "بَاذِنْـ..."),
                Word("كوسة", "كُوسَهْ", "🥒", cue = "كُو..."),
                Word("خس", "خَسّ", "🥗", cue = "خَسْ..."),
                Word("معدنوس", "مَعْدَنُوسْ", "🌿", cue = "مَعْدَ..."),
                Word("كزبرة", "كُزْبُرَهْ", "🌱", cue = "كُزْ..."),
                Word("ليمون", "لِيمُونْ", "🍋", cue = "لِيـ..."),
                Word("بازيليه", "بَازِيلْيَهْ", "🟢", cue = "بَا..."),
                Word("فاصوليا", "فَاصُولِيَا", "🫘", cue = "فَاصُو..."),
                Word("سبانخ", "سَبَانِخْ", "🥬", cue = "سَبَا..."),
                Word("قرنبيط", "قُرْنَبِيطْ", "🌼", cue = "قُرْنَـ..."),
                Word("بروكلي", "بُرُوكْلِي", "🥦", cue = "بُرُو..."),
                Word("فجل", "فِجِلْ", "🌸", cue = "فِجْـ..."),
                Word("قرعة", "قَرْعَهْ", "🎃", cue = "قَرْ..."),
                Word("نعناع", "نَعْنَاعْ", "🍃", cue = "نَعْـ..."),
                Word("ذرة", "ذُرَهْ", "🌽", cue = "ذُ..."),
            ),
        ),

        // ---- 6. الفواكه ----
        Cat(
            id = "fruits", title = "الفواكه", palette = 2,
            words = listOf(
                Word("تفاح", "تُفَّاحْ", "🍎", cue = "تُفَّا..."),
                Word("موز", "مَوْزْ", "🍌", cue = "مَوْ..."),
                Word("برتقال", "بُرْتُقَالْ", "🍊", cue = "بُرْتُـ..."),
                Word("عنب", "عِنَبْ", "🍇", cue = "عِـ..."),
                Word("فراولة", "فَرَاوْلَهْ", "🍓", cue = "فَرَا..."),
                Word("مانجو", "مَانْجُو", "🥭", cue = "مَانْـ..."),
                Word("دلاع", "دَلَّاعْ", "🍉", cue = "دَلَّا..."),
                Word("قلعاوي", "قَلْعَاوِي", "🍈", cue = "قَلْـ..."),
                Word("خوخ", "خَوْخْ", "🍑", cue = "خَوْ..."),
                Word("مشمش", "مِشْمِشْ", "🟠", cue = "مِشْـ..."),
                Word("إجاص", "إِجَّاصْ", "🍐", cue = "إِجَّا..."),
                Word("رمان", "رُمَّانْ", "🔴", cue = "رُمَّا..."),
                Word("كرموس", "كَرْمُوسْ", "🟣", cue = "كَرْ..."),
                Word("تمر", "تَمْرْ", "🌴", cue = "تَمْـ..."),
                Word("أناناس", "أَنَانَاسْ", "🍍", cue = "أَنَا..."),
                Word("كيوي", "كِيوِي", "🥝", cue = "كِيـ..."),
                Word("كرز", "كَرَزْ", "🍒", cue = "كَـ..."),
                Word("توت", "تُوتْ", "🫐", cue = "تُو..."),
                Word("مندرين", "مَنْدَرِينْ", "🧡", cue = "مَنْـ..."),
                Word("أفوكادو", "أَفُوكَادُو", "🥑", cue = "أَفُو..."),
                Word("جوز الهند", "جَوْزُ الهِنْدْ", "🥥", cue = "جَوْزْ..."),
            ),
        ),

        // ---- 7. الطعام والشراب ----
        Cat(
            id = "food", title = "الطعام والشراب", palette = 4,
            words = listOf(
                Word("خبزة", "خُبْزَهْ", "🍞", cue = "خُبْـ..."),
                Word("حليب", "حَلِيبْ", "🥛", cue = "حَلِيـ..."),
                Word("بيض", "بَيْضْ", "🥚", cue = "بَيْـ..."),
                Word("جبنة", "جُبْنَهْ", "🧀", cue = "جُبْـ..."),
                Word("زبادي", "زَبَادِي", "🥛", cue = "زَبَا..."),
                Word("زبدة", "زُبْدَهْ", "🧈", cue = "زُبْـ..."),
                Word("أرز", "رُزّ", "🍚", cue = "رُزْ..."),
                Word("مكرونة", "مَكَرُونَهْ", "🍝", cue = "مَكَـ..."),
                Word("شوربة", "شُرْبَهْ", "🍲", cue = "شُرْ..."),
                Word("لحم", "لَحْمْ", "🥩", cue = "لَحْـ..."),
                Word("دجاج", "دَجَاجْ", "🍗", cue = "دَجَا..."),
                Word("حوت (سمك)", "حُوتْ", "🐟", cue = "حُو..."),
                Word("زيتون", "زَيْتُونْ", "🫒", cue = "زَيْـ..."),
                Word("زيت", "زَيْتْ", "🫗", cue = "زَيْـ..."),
                Word("شاهي", "شَاهِي", "🍵", cue = "شَا..."),
                Word("قهوة", "قَهْوَهْ", "☕", cue = "قَهْـ..."),
                Word("عصير", "عَصِيرْ", "🧃", cue = "عَصِيـ..."),
                Word("مية", "إِمِّيَّهْ", "💧", cue = "إِمْـ..."),
                Word("عسل", "عَسَلْ", "🍯", cue = "عَسَـ..."),
                Word("بسكويت", "بِسْكُوِيتْ", "🍪", cue = "بِسْـ..."),
                Word("ملح", "مِلْحْ", "🧂", cue = "مِلْـ..."),
                Word("سكر", "سُكَّرْ", "🍬", cue = "سُكْـ..."),
            ),
        ),

        // ---- 8. المنزل والأثاث ----
        Cat(
            id = "home", title = "المنزل والأثاث", palette = 6,
            words = listOf(
                Word("سرير", "سَرِيرْ", "🛏️", cue = "سَرِيـ..."),
                Word("مخدة", "مِخَدَّهْ", "🛌", cue = "مِخَـ..."),
                Word("ورغان / بطانية", "وِرْغَانْ", "🧶", cue = "وِرْ..."),
                Word("طاولة", "طَاوْلَهْ", "🟫", cue = "طَاوْ..."),
                Word("كرسي", "كُرْسِي", "🪑", cue = "كُرْ..."),
                Word("صالون", "صَالُونْ", "🛋️", cue = "صَا..."),
                Word("باب", "بَابْ", "🚪", cue = "بَا..."),
                Word("روشن", "رَوْشَنْ", "🪟", cue = "رَوْ..."),
                Word("تلفزيون", "تِلِفِزْيُونْ", "📺", cue = "تِلِـ..."),
                Word("ثلاجة", "ثَلَّاجَهْ", "🧊", cue = "ثَلَّا..."),
                Word("غسالة", "غَسَّالَهْ", "🧺", cue = "غَسَّا..."),
                Word("غاز", "غَازْ", "🔥", cue = "غَا..."),
                Word("كوشة", "كُوشَهْ", "🥘", cue = "كُو..."),
                Word("مكيف", "مُكَيِّفْ", "❄️", cue = "مُكَيْـ..."),
                Word("مروحة", "مِرْوَحَهْ", "🌀", cue = "مِرْ..."),
                Word("دولاب", "دُولَابْ", "🗄️", cue = "دُو..."),
                Word("مفتاح", "مَفْتَاحْ", "🔑", cue = "مَفْـ..."),
                Word("ساعة", "سَاعَهْ", "⏰", cue = "سَا..."),
                Word("بساط / حواشي", "إِبْسَاطْ", "🟥", cue = "إِبْـ..."),
                Word("كوجينة", "كُوجِينَهْ", "🍳", cue = "كُو..."),
                Word("طاسة", "طَاسَهْ", "🥃", cue = "طَا..."),
                Word("صونية", "صُونِيَّهْ", "🍽️", cue = "صُو..."),
                Word("كاشيك", "كَاشِيكْ", "🥄", cue = "كَا..."),
                Word("فرشيطة", "فَرْشِيطَهْ", "🍴", cue = "فَرْ..."),
                Word("موس (سكين)", "مُوسْ", "🔪", cue = "مُو..."),
                Word("براد", "بَرَّادْ", "🫖", cue = "بَرَّا..."),
                Word("طنجرة", "طَنْجْرَهْ", "🍲", cue = "طَنْـ..."),
                Word("شيشة", "شِيشَهْ", "🍼", cue = "شِيـ..."),
            ),
        ),

        // ---- 9. الملابس ----
        Cat(
            id = "clothes", title = "الملابس", palette = 5,
            words = listOf(
                Word("تيشرت", "تِيشِرْتْ", "👕", cue = "تِي..."),
                Word("قميص", "قَمِيصْ", "👔", cue = "قَمِ..."),
                Word("جاكة", "جَاكَهْ", "🧥", cue = "جَا..."),
                Word("شورت", "شُورْتْ", "🩳", cue = "شُو..."),
                Word("سورية", "سُورِيَّهْ", "👕", cue = "سُو..."),
                Word("بنطلون", "بَنْطَلُونْ", "👖", cue = "بَنْـ..."),
                Word("فستان", "فُسْتَانْ", "👗", cue = "فُسْـ..."),
                Word("روب / قفطان", "رُوبْ", "👘", cue = "رُو..."),
                Word("عباية", "عَبَايَهْ", "🧕", cue = "عَبَا..."),
                Word("حجاب", "حِجَابْ", "🧕", cue = "حِجَا..."),
                Word("وشاح", "وِشَاحْ", "🧣", cue = "وِشَا..."),
                Word("كندرة", "كَنْدْرَهْ", "👟", cue = "كَنْـ..."),
                Word("شخاشير", "إِشْخَاشِيرْ", "🧦", cue = "إِشْـ..."),
                Word("كبوط", "كَبُّوطْ", "🧥", cue = "كَبُّو..."),
                Word("نظارة", "نَظَّارَهْ", "👓", cue = "نَظَّا..."),
                Word("طاقية", "طَاقِيَّهْ", "👒", cue = "طَا..."),
                Word("ساعة يد", "سَاعِةْ يَدْ", "⌚", cue = "سَا..."),
                Word("شنطة", "شَنْطَهْ", "👜", cue = "شَنْـ..."),
                Word("صندل", "صَنْدَلْ", "🩴", cue = "صَنْـ..."),
                Word("كندرة كعب", "كَنْدْرِةْ كَعْبْ", "👠", cue = "كَعْبْ..."),
                Word("بيجامة", "بِيجَامَهْ", "🩳", cue = "بِيـ..."),
                Word("سبتة", "سَبْتَهْ", "🪢", cue = "سَبْـ..."),
                Word("خاتم", "خَاتَمْ", "💍", cue = "خَا..."),
            ),
        ),

        // ---- 10. أجزاء الجسم ----
        Cat(
            id = "body", title = "أجزاء الجسم", palette = 9,
            words = listOf(
                Word("عين", "عَيْنْ", "👁️", cue = "عَيْـ..."),
                Word("خشم", "خَشْمْ", "👃", cue = "خَشْـ..."),
                Word("فم", "فَمْ", "👄", cue = "فَـ..."),
                Word("لسان", "لِسَانْ", "👅", cue = "لِسَا..."),
                Word("ودن", "وِدْنْ", "👂", cue = "وِدْ..."),
                Word("يد", "يَدْ", "✋", cue = "يَـ..."),
                Word("رجل / كراع", "إِكْرَاعْ", "🦶", cue = "إِكْـ..."),
                Word("راس", "رَاسْ", "🙂", cue = "رَا..."),
                Word("شعر", "شَعْرْ", "🦱", cue = "شَعْـ..."),
                Word("ظهر", "ظَهْرْ", "🧍", cue = "ظَهْـ..."),
                Word("بطن", "بَطْنْ", "🤰", cue = "بَطْـ..."),
                Word("ركبة", "رُكْبَهْ", "🦵", cue = "رُكْـ..."),
                Word("صبع", "صُبْعْ", "☝️", cue = "صُبْـ..."),
                Word("رقبة", "رَقَبَهْ", "🧣", cue = "رَقَـ..."),
                Word("صدر", "صَدْرْ", "🫁", cue = "صَدْ..."),
                Word("وجه", "وَجِهْ", "😊", cue = "وَجْـ..."),
                Word("سنة", "سِنَّهْ", "🦷", cue = "سِـ..."),
                Word("قلب", "قَلْبْ", "❤️", cue = "قَلْـ..."),
                Word("كتف", "كَتِفْ", "💪", cue = "كَـ..."),
            ),
        ),

        // ---- 11. الأفعال اليومية ----
        Cat(
            id = "actions", title = "الأفعال اليومية", palette = 7,
            words = listOf(
                Word("ناكل", "نَاكُلْ", "😋", cue = "نَا..."),
                Word("نشرب", "نَشْرَبْ", "🥤", cue = "نَشْـ..."),
                Word("نرقد", "نُرْقُدْ", "😴", cue = "نُرْ..."),
                Word("نقعمز", "إِنْقَعِّمِزْ", "🪑", cue = "إِنْـ..."),
                Word("نمشي", "نَمْشِي", "🚶", cue = "نَمْـ..."),
                Word("نوقف", "نُوقَفْ", "🧍", cue = "نُو..."),
                Word("نغسل", "نَغْسِلْ", "🧼", cue = "نَغْـ..."),
                Word("ندوي", "نَدْوِي", "🗣️", cue = "نَدْ..."),
                Word("نسمع", "نَسْمَعْ", "🎧", cue = "نَسْـ..."),
                Word("نفتح", "نَفْتَحْ", "🔓", cue = "نَفْـ..."),
                Word("نسكر", "إِنْسَكِّرْ", "🔒", cue = "إِنْـ..."),
                Word("نقرا", "نَقْرَا", "📖", cue = "نَقْـ..."),
                Word("نضحك", "نَضْحَكْ", "😄", cue = "نَضْـ..."),
                Word("نشوف", "إِنْشُوفْ", "👀", cue = "إِنْـ..."),
                Word("نلبس", "نَلْبِسْ", "👚", cue = "نَلْـ..."),
                Word("نصلي", "إِنْصَلِّي", "🤲", cue = "إِنْـ..."),
            ),
        ),

        // ---- 12. الحيوانات ----
        Cat(
            id = "animals", title = "الحيوانات والطيور", palette = 8,
            words = listOf(
                Word("قطوسة", "قَطُّوسَهْ", "🐱", cue = "قَطْـ..."),
                Word("كلب", "كَلْبْ", "🐶", cue = "كَلْـ..."),
                Word("خروف", "خَرُوفْ", "🐑", cue = "خَرُو..."),
                Word("حصان", "حِصَانْ", "🐴", cue = "حِصَا..."),
                Word("بقرة", "بَقَرَهْ", "🐄", cue = "بَقَـ..."),
                Word("عصفور", "عُصْفُورْ", "🐦", cue = "عُصْـ..."),
                Word("حمامة", "حَمَامَهْ", "🕊️", cue = "حَمَا..."),
                Word("دجاجة", "دَجَاجَهْ", "🐔", cue = "دَجَا..."),
                Word("بطة", "بَطَّهْ", "🦆", cue = "بَطْـ..."),
                Word("جمل", "جَمَلْ", "🐪", cue = "جَمَـ..."),
                Word("سردوك", "سَرْدُوكْ", "🐓", cue = "سَرْ..."),
                Word("قنية", "قُنَّيَّهْ", "🐰", cue = "قُنْـ..."),
            ),
        ),
    )

    const val VERSION = 17

    // جدول التحويلات الشامل لمنع تكرار الكلمات والحفاظ على بيانات وتفضيلات الوالدة
    val renames: List<Triple<String, String, String>> = listOf(
        // المنزل
        Triple("home", "ملعقة", "كاشيك"),
        Triple("home", "صحن", "صونية"),
        Triple("home", "شوكة", "فرشيطة"),
        Triple("home", "سكين", "موس (سكين)"),
        Triple("home", "كنبة (صالون)", "صالون"),
        Triple("home", "نافذة", "روشن"),
        Triple("home", "سجادة", "بساط / حواشي"),
        Triple("home", "طاسة (كأس)", "طاسة"),
        Triple("home", "فرن (غاز)", "غاز"),
        Triple("home", "بطانية", "ورغان / بطانية"),

        // الملابس
        Triple("clothes", "قميص", "سورية"),
        Triple("clothes", "حذاء", "كندرة"),
        Triple("clothes", "جوارب", "شخاشير"),
        Triple("clothes", "تقاشر", "شخاشير"),
        Triple("clothes", "جاكيت (كوت)", "كبوط"),
        Triple("clothes", "كبوط / جاكيت", "كبوط"),
        Triple("clothes", "زبون / عباية", "روب / قفطان"),

        // الخضروات
        Triple("vegetables", "بازلاء", "بازيليه"),
        Triple("vegetables", "بقطين", "قرعة"),
        Triple("vegetables", "بقطين / قرع", "قرعة"),
        Triple("vegetables", "يقطين", "قرعة"),
        Triple("vegetables", "معدنوس / بقدونس", "معدنوس"),

        // الفواكه
        Triple("fruits", "دلاع (بطيخ)", "دلاع"),
        Triple("fruits", "قلعاوي (شمام)", "قلعاوي"),
        Triple("fruits", "إجاص (كمثرى)", "إجاص"),
        Triple("fruits", "كرموس (تين)", "كرموس"),
        Triple("fruits", "يوسفي (مندرين)", "مندرين"),

        // الطعام والشراب
        Triple("food", "خبز", "خبزة"),
        Triple("food", "جبن", "جبنة"),
        Triple("food", "سمك", "حوت (سمك)"),
        Triple("food", "ماء", "مية"),
        Triple("food", "شوربة (حساء)", "شوربة"),
        Triple("food", "بسكويت (كعك)", "بسكويت"),

        // الجسم
        Triple("body", "أنف", "خشم"),
        Triple("body", "أذن", "ودن"),
        Triple("body", "رأس", "راس"),
        Triple("body", "أصبع", "صبع"),
        Triple("body", "قدم (رجل)", "رجل / كراع"),

        // الأفعال اليومية (حاسمة لمنع تكرار الأفعال في القائمة)
        Triple("actions", "أكل", "ناكل"),
        Triple("actions", "شرب", "نشرب"),
        Triple("actions", "نوم", "نرقد"),
        Triple("actions", "جلوس", "نقعمز"),
        Triple("actions", "مشي", "نمشي"),
        Triple("actions", "وقوف", "نوقف"),
        Triple("actions", "غسل", "نغسل"),
        Triple("actions", "كلام", "ندوي"),
        Triple("actions", "سمع", "نسمع"),
        Triple("actions", "فتح", "نفتح"),
        Triple("actions", "قفل", "نسكر"),
        Triple("actions", "قراءة", "نقرا"),
        Triple("actions", "ضحك", "نضحك"),
        Triple("actions", "نظر", "نشوف"),
        Triple("actions", "لبس", "نلبس"),
        Triple("actions", "صلاة", "نصلي"),

        // الحيوانات
        Triple("animals", "قطة", "قطوسة"),
        Triple("animals", "أرنب", "قنية"),
        Triple("animals", "ديك", "سردوك"),
    )

    fun categoryEntities(): List<CategoryEntity> =
        categories.mapIndexed { i, c -> CategoryEntity(c.id, c.title, c.palette, c.previewOnTap, i) }

    fun itemEntities(): List<WordItemEntity> {
        // The requested new Libyan pronunciations are hand-curated exactly as written;
        // do not append an extra final sukun to them automatically.
        val curatedLibyan = RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN)
            .dishes.map { it.displayText }.toSet()
        return categories.flatMap { c ->
            c.words.mapIndexed { i, w ->
                val spoken = if (c.id == "libyan" && w.display in curatedLibyan)
                    w.tts else TtsEndings.normalize(w.tts)
                WordItemEntity(
                    categoryId = c.id,
                    subgroup = w.subgroup,
                    displayText = w.display,
                    ttsPrompt = spoken,
                    phonemicCue = w.cue ?: PhonemicCue.derive(spoken),
                    sentencePrompt = SeedExtras.sentencePrompt(c.id, w.display),
                    isFavorite = SeedExtras.isFavorite(c.id, w.display),
                    emoji = w.emoji,
                    sortOrder = w.sort ?: i,
                )
            }
        }
    }
}
