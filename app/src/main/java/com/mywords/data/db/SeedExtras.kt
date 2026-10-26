package com.mywords.data.db

/**
 * Functional cloze prompts ("نَأْكُلُ الـ...") and the default quick-access favourites.
 * Keys are "categoryId/displayText" exactly as in WordSeed. A word with no entry falls back to
 * its category default; words in [noPrompt] have no 💬 button.
 */
object SeedExtras {

    /** Favourites are defaulted once, by the top-up that introduces them (see RoomWordRepository). */
    const val FAVORITES_SINCE_VERSION = 4

    private val categoryDefault = mapOf(
        "vegetables" to "نَطْبُخُ الـ...",
        "fruits" to "نَأْكُلُ الـ...",
        "food" to "نَأْكُلُ الـ...",
        "clothes" to "نَلْبَسُ الـ...",
        "actions" to "أُرِيدُ الـ...",
        "family" to "أُرِيدُ أَنْ أَتَكَلَّمَ مَعَ ...",
        "libyan" to "نَأْكُلُ الـ...",
    )

    private val noPrompt = setOf("actions/فتح", "actions/قفل", "actions/سمع", "actions/كنس")

    private fun group(prompt: String, category: String, vararg names: String) =
        names.associate { "$category/$it" to prompt }

    private val overrides: Map<String, String> = buildMap {
        // daily requests
        put("daily/نبي مية", "نَشْرَبُ الـ...")
        put("daily/نبي شاي", "نَشْرَبُ الـ...")
        put("daily/نبي نرتاح", "عِنْدَمَا أَتْعَبُ أُرِيدُ الـ...")
        put("daily/نبي دوايا", "فِي المَوْعِدِ آخُذُ الـ...")
        put("daily/نبي الحمام", "أَذْهَبُ إِلَى الـ...")
        put("daily/عدّل لي المخدة", "أُرِيدُ تَعْدِيلَ الـ...")

        // vegetables
        putAll(group("نَأْكُلُ الـ...", "vegetables", "خيار", "خس", "فجل"))
        putAll(group("نَضَعُ الـ... فِي الطَّبْخِ", "vegetables", "معدنوس / بقدونس", "كزبرة"))
        put("vegetables/نعناع", "نَشْرَبُ الشَّايَ بِالـ...")
        put("vegetables/ليمون", "نَعْصِرُ الـ...")

        // food & drinks
        putAll(group("نَشْرَبُ الـ...", "food", "حليب", "شاي", "قهوة", "عصير", "ماء"))
        put("food/زيت", "نَطْبُخُ بِالـ...")
        put("food/ملح", "نَضَعُ فِي الطَّعَامِ الـ...")
        put("food/سكر", "نَضَعُ فِي الشَّايِ الـ...")
        putAll(group("نَضَعُ عَلَى الخُبْزِ الـ...", "food", "زبدة", "عسل"))
        put("food/شوربة (حساء)", "نَتَنَاوَلُ الـ...")

        // home & furniture
        put("home/سرير", "نَنَامُ عَلَى الـ...")
        put("home/مخدة", "نَضَعُ رَأْسَنَا عَلَى الـ...")
        put("home/بطانية", "نَتَغَطَّى بِالـ...")
        put("home/طاولة", "نَضَعُ الطَّعَامَ عَلَى الـ...")
        put("home/كرسي", "نَجْلِسُ عَلَى الـ...")
        put("home/باب", "نَفْتَحُ الـ...")
        put("home/تلفزيون", "نُشَاهِدُ الـ...")
        put("home/ثلاجة", "نَحْفَظُ الطَّعَامَ فِي الـ...")
        put("home/غسالة", "نَغْسِلُ الثِّيَابَ فِي الـ...")
        put("home/فرن (غاز)", "نَطْبُخُ عَلَى الـ...")
        put("home/مكيف", "نُشَغِّلُ الـ...")
        put("home/مروحة", "نُشَغِّلُ الـ...")
        put("home/دولاب", "نَضَعُ الثِّيَابَ فِي الـ...")
        put("home/مفتاح", "نَفْتَحُ البَابَ بِالـ...")
        put("home/ساعة", "نَنْظُرُ إِلَى الـ... لِنَعْرِفَ الوَقْتَ")
        put("home/شوكة", "نَأْكُلُ بِالـ...")
        put("home/سكين", "نَقْطَعُ بِالـ...")
        put("home/صالون", "نَجْلِسُ عَلَى الـ...")
        put("home/روشن", "نَنْظُرُ مِنَ الـ...")
        put("home/بساط / حواشي", "نَمْشِي عَلَى الـ...")
        put("home/كوجينة", "نَطْبُخُ فِي الـ...")
        put("home/طاسة", "نَشْرَبُ المَاءَ فِي الـ...")
        put("home/صونية", "نَضَعُ الأَكْلَ عَلَى الـ...")
        put("home/كاشيك", "نَأْكُلُ الشُّورْبَةَ بِالـ...")
        put("home/براد", "نَصُبُّ الشَّايَ مِنَ الـ...")
        put("home/طنجرة", "نَطْبُخُ الأَكْلَ فِي الـ...")
        put("home/شيشة", "نَحْفَظُ المَاءَ فِي الـ...")

        // clothes
        put("clothes/نظارة", "نَرَى جَيِّدًا بِالـ...")
        put("clothes/ساعة يد", "نَعْرِفُ الوَقْتَ مِنَ الـ...")
        put("clothes/حقيبة", "نَحْمِلُ الأَغْرَاضَ فِي الـ...")

        // body parts
        put("body/عين", "نَرَى بِالـ...")
        put("body/أنف", "نَشُمُّ بِالـ...")
        put("body/فم", "نَأْكُلُ بِالـ...")
        put("body/لسان", "نَتَذَوَّقُ الطَّعْمَ بِالـ...")
        put("body/أذن", "نَسْمَعُ بِالـ...")
        put("body/يد", "نُمْسِكُ الكُوبَ بِالـ...")
        put("body/قدم (رجل)", "نَمْشِي عَلَى الـ...")
        put("body/رأس", "نَضَعُ القُبَّعَةَ عَلَى الـ...")
        put("body/شعر", "نُمَشِّطُ الـ...")
        put("body/ظهر", "نَسْتَرِيحُ عَلَى الـ...")
        put("body/بطن", "الطَّعَامُ يَذْهَبُ إِلَى الـ...")
        put("body/ركبة", "نَثْنِي الـ...")
        put("body/أصبع", "نُشِيرُ بِالـ...")
        put("body/رقبة", "نَلْبَسُ القِلَادَةَ حَوْلَ الـ...")
        put("body/صدر", "نَضَعُ يَدَنَا عَلَى الـ...")
        put("body/وجه", "نَغْسِلُ الـ...")
        put("body/ذراع", "نَرْفَعُ الـ...")
        put("body/سن", "نُنَظِّفُ الـ...")
        put("body/قلب", "نُحِبُّ بِالـ...")
        put("body/ظفر", "نَقُصُّ الـ...")
        put("body/كتف", "نَحْمِلُ عَلَى الـ...")
        put("body/حاجب", "فَوْقَ العَيْنِ يُوجَدُ الـ...")

        // daily actions
        putAll(group("أُحِبُّ الـ...", "actions", "قراءة", "كتابة", "ضحك", "طبخ"))
        put("actions/تصفيق", "نُشَجِّعُ بِالـ...")


        // Libyan foods
        putAll(group("نَشْرَبُ الـ...", "libyan", "شاي بالنعناع", "قهوة عربية"))

        // routines & faith
        put("routine/أذان", "نَسْمَعُ الـ...")
        put("routine/وضوء", "قَبْلَ الصَّلَاةِ نَقُومُ بِالـ...")
        put("routine/صلاة", "نَقِفُ لِلـ...")
        put("routine/سجادة", "نُصَلِّي عَلَى الـ...")
        put("routine/سبحة", "نُسَبِّحُ بِالـ...")
        put("routine/مصحف", "نَقْرَأُ القُرْآنَ مِنَ الـ...")
        put("routine/قهوة العشية", "بَعْدَ العَصْرِ نَشْرَبُ الـ...")
        put("routine/نوم", "فِي اللَّيْلِ نَذْهَبُ إِلَى الـ...")
        put("routine/راحة", "عِنْدَمَا نَتْعَبُ نَأْخُذُ الـ...")
        put("routine/مشي", "فِي الصَّبَاحِ نَخْرُجُ لِلـ...")

        // animals
        putAll(group("فِي البَيْتِ نَرَى الـ...", "animals", "قطة", "كلب", "أرنب", "ببغاء"))
        putAll(group("فِي المَزْرَعَةِ نَرَى الـ...", "animals", "خروف", "حصان", "بقرة", "دجاجة", "بطة", "جمل", "ديك", "ماعز"))
        putAll(group("فِي حَدِيقَةِ الحَيَوَانِ نَرَى الـ...", "animals", "أسد", "فيل", "زرافة", "قرد", "سلحفاة"))
        putAll(group("فِي السَّمَاءِ يَطِيرُ الـ...", "animals", "عصفور", "نسر"))
        putAll(group("فِي السَّمَاءِ تَطِيرُ الـ...", "animals", "حمامة", "فراشة", "نحلة"))
    }

    private val favorites = setOf(
        "daily/نبي مية", "daily/نبي شاي", "daily/نبي دوايا", "daily/نبي الحمام", "daily/نبي نرتاح",
        "clothes/نظارة",
    )

    fun sentencePrompt(categoryId: String, display: String): String? {
        val key = "$categoryId/$display"
        if (key in noPrompt) return null
        return overrides[key] ?: categoryDefault[categoryId]
    }

    fun isFavorite(categoryId: String, display: String): Boolean = "$categoryId/$display" in favorites
}
