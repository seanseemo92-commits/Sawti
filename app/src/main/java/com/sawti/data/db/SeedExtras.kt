package com.sawti.data.db

/**
 * Functional cloze prompts ("نَأْكُلُ الـ...") for sentence hints.
 * Keys are "categoryId/displayText" exactly as in WordSeed. A word with no entry falls back to
 * its category default; words in [noPrompt] have no 💬 button.
 */
object SeedExtras {

    private val categoryDefault = mapOf(
        "vegetables" to "نَطْبُخُ الـ...",
        "fruits" to "نَأْكُلُ الـ...",
        "food" to "نَأْكُلُ الـ...",
        "clothes" to "نَلْبَسُ الـ...",

        "family" to "أُرِيدُ أَنْ أَتَكَلَّمَ مَعَ ...",
        "libyan" to "نَأْكُلُ الـ...")

    private val noPrompt = setOf("actions/نفتح", "actions/نسكر", "actions/نسمع")

    private fun group(prompt: String, category: String, vararg names: String) =
        names.associate { "$category/$it" to prompt }

    private val overrides: Map<String, String> = buildMap {
        // daily requests
        put("daily/نبي مية", "نَشْرَبُ الـ...")
        put("daily/نبي شاهي", "نَشْرَبُ الـ...")
        put("daily/نبي نرتاح", "عِنْدَمَا أَتْعَبُ أُرِيدُ الـ...")
        put("daily/نبي دوايا", "فِي المَوْعِدِ آخُذُ الـ...")
        put("daily/نبي الحمام", "أَذْهَبُ إِلَى الـ...")
        put("daily/عدّل لي المخدة", "أُرِيدُ تَعْدِيلَ الـ...")
        // vegetables
        putAll(group("نَأْكُلُ الـ...", "vegetables", "خيار", "خس", "فجل"))
        putAll(group("نَضَعُ الـ... فِي الطَّبْخِ", "vegetables", "معدنوس", "كزبرة"))
        put("vegetables/نعناع", "نَشْرَبُ الشَّايَ بِالـ...")
        put("vegetables/ليمون", "نَعْصِرُ الـ...")
        // food & drinks
        putAll(group("نَشْرَبُ الـ...", "food", "حليب", "شاهي", "قهوة", "عصير", "مية"))
        put("food/زيت", "نَطْبُخُ بِالـ...")
        put("food/ملح", "نَضَعُ فِي الطَّعَامِ الـ...")
        put("food/سكر", "نَضَعُ فِي الشَّايِ الـ...")
        putAll(group("نَضَعُ عَلَى الخُبْزِ الـ...", "food", "زبدة", "عسل"))
        put("food/شوربة", "نَتَنَاوَلُ الـ...")
        // home & furniture
        put("home/سرير", "نَنَامُ عَلَى الـ...")
        put("home/مخدة", "نَضَعُ رَأْسَنَا عَلَى الـ...")
        put("home/ورغان / بطانية", "نَتَغَطَّى بِالـ...")
        put("home/طاولة", "نَضَعُ الطَّعَامَ عَلَى الـ...")
        put("home/كرسي", "نَجْلِسُ عَلَى الـ...")
        put("home/باب", "نَفْتَحُ الـ...")
        put("home/تلفزيون", "نُشَاهِدُ الـ...")
        put("home/ثلاجة", "نَحْفَظُ الطَّعَامَ فِي الـ...")
        put("home/غسالة", "نَغْسِلُ الثِّيَابَ فِي الـ...")
        put("home/غاز", "نَطْبُخُ عَلَى الـ...")
        put("home/مكيف", "نُشَغِّلُ الـ...")
        put("home/مروحة", "نُشَغِّلُ الـ...")
        put("home/دولاب", "نَضَعُ الثِّيَابَ فِي الـ...")
        put("home/مفتاح", "نَفْتَحُ البَابَ بِالـ...")
        put("home/ساعة", "نَنْظُرُ إِلَى الـ... لِنَعْرِفَ الوَقْتَ")
        put("home/فرشيطة", "نَأْكُلُ بِالـ...")
        put("home/موس (سكين)", "نَقْطَعُ بِالـ...")
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
        put("clothes/شنطة", "نَحْمِلُ الأَغْرَاضَ فِي الـ...")
        // body parts
        put("body/عين", "نَرَى بِالـ...")
        put("body/خشم", "نَشُمُّ بِالـ...")
        put("body/فم", "نَأْكُلُ بِالـ...")
        put("body/لسان", "نَتَذَوَّقُ الطَّعْمَ بِالـ...")
        put("body/ودن", "نَسْمَعُ بِالـ...")
        put("body/يد", "نُمْسِكُ الكُوبَ بِالـ...")
        put("body/رجل / كراع", "نَمْشِي عَلَى الـ...")
        put("body/راس", "نَضَعُ القُبَّعَةَ عَلَى الـ...")
        put("body/شعر", "نُمَشِّطُ الـ...")
        put("body/ظهر", "نَسْتَرِيحُ عَلَى الـ...")
        put("body/بطن", "الطَّعَامُ يَذْهَبُ إِلَى الـ...")
        put("body/ركبة", "نَثْنِي الـ...")
        put("body/صبع", "نُشِيرُ بِالـ...")
        put("body/رقبة", "نَلْبَسُ القِلَادَةَ حَوْلَ الـ...")
        put("body/صدر", "نَضَعُ يَدَنَا عَلَى الـ...")
        put("body/وجه", "نَغْسِلُ الـ...")

        put("body/سنة", "نُنَظِّفُ الـ...")
        put("body/قلب", "نُحِبُّ بِالـ...")

        put("body/كتف", "نَحْمِلُ عَلَى الـ...")

        // Conjugated verbs are complete utterances: a noun-style cloze prompt
        // ("أُرِيدُ الـ...") is not grammatical for them.
        // Libyan foods
        putAll(group("نَشْرَبُ الـ...", "libyan", "شاهي بالنعناع", "قهوة عربية"))
        putAll(group("نَبِّي نَاكُلْ الـ...", "libyan", "مبطن", "حرايمي", "سفنز", "زميطة", "عيش باللبن"))
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
        putAll(group("فِي البَيْتِ نَرَى الـ...", "animals", "قطوسة", "كلب", "قنية"))
        putAll(group("فِي المَزْرَعَةِ نَرَى الـ...", "animals", "خروف", "حصان", "بقرة", "دجاجة", "بطة", "جمل", "سردوك"))

        putAll(group("فِي السَّمَاءِ يَطِيرُ الـ...", "animals", "عصفور"))
        putAll(group("فِي السَّمَاءِ تَطِيرُ الـ...", "animals", "حمامة"))
    }


    /** Test-only contract: every explicitly configured key must match current seed vocabulary. */
    fun sentencePrompt(categoryId: String, display: String): String? {
        val key = "$categoryId/$display"
        if (categoryId == "actions" || key in noPrompt) return null
        return overrides[key] ?: categoryDefault[categoryId]
    }

    internal fun configuredKeys(): Set<String> = noPrompt + overrides.keys
}
