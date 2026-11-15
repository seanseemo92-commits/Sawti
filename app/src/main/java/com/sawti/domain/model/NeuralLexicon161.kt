package com.sawti.domain.model

/**
 * v1.6.1 explicit regional card forms. Stable semantic keys, not displayed captions,
 * select the pronunciation. Written forms require native-speaker/device listening review.
 *
 * This lexicon supersedes historical patches and completed suggestions; it never edits
 * Room rows, caregiver-provided labels, recordings, or custom speech.
 */
internal object NeuralLexicon161 {
    private val all = ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }
    private val maghreb = listOf(ArabicDialect.LIBYAN, ArabicDialect.TUNISIAN,
        ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN)
    private val west = maghreb.filter { it != ArabicDialect.LIBYAN }

    val forms: Map<String, Map<ArabicDialect, DialectForm>> by lazy {
        val entries = linkedMapOf<String, MutableMap<ArabicDialect, DialectForm>>()

        fun register(dialects: List<ArabicDialect>, namespace: String, lines: String) {
            for (line in lines.trimIndent().lineSequence().filter { it.isNotBlank() }) {
                val fields = line.split('|')
                require(fields.size == 3) { "Malformed v1.6.1 line: $line" }
                val key = "$namespace.${fields[0]}"
                val form = DialectForm(fields[1], fields[2])
                for (dialect in dialects) {
                    require(dialect != ArabicDialect.STANDARD)
                    val perDialect = entries.getOrPut(key) { linkedMapOf() }
                    check(perDialect.put(dialect, form) == null) { "Duplicate v1.6.1 form: $dialect/$key" }
                }
            }
        }
        fun regional(dialect: ArabicDialect, namespace: String, lines: String) =
            register(listOf(dialect), namespace, lines)

        register(maghreb, "family", """
            husband_father|بو راجلي|بُو رَاجْلِي
            husband_mother|أم راجلي|أُم رَاجْلِي
            wife_father|بو مرتي|بُو مَرْتِي
            wife_mother|أم مرتي|أُم مَرْتِي
            husband_brother|خو راجلي|خُو رَاجْلِي
            husband_sister|أخت راجلي|أُخْت رَاجْلِي
            wife_brother|خو مرتي|خُو مَرْتِي
            wife_sister|أخت مرتي|أُخْت مَرْتِي
            sister_husband|راجل أختي|رَاجِل أُخْتِي
            daughter_husband|راجل بنتي|رَاجِل بِنْتِي
            son_wife|مرت ولدي|مَرْت وَلْدِي
        """)
        regional(ArabicDialect.LIBYAN, "family", """
            brother_wife|مرت خوي|مَرْت خُوي
            paternal_uncle_son|ولد عمي|وِلْد عَمِّي
            paternal_aunt_son|ولد عمتي|وِلْد عَمْتِي
            maternal_uncle_son|ولد خالي|وِلْد خَالِي
            maternal_aunt_son|ولد خالتي|وِلْد خَالْتِي
            brother_son|ولد خوي|وِلْد خُوي
            sister_son|ولد أختي|وِلْد أُخْتِي
            brother_daughter|بنت خوي|بِنْت خُوي
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        register(west, "family", """
            brother_wife|مرت خويا|مَرْت خُويَا
            paternal_uncle_son|ولد عمي|وَلْد عَمِّي
            paternal_aunt_son|ولد عمتي|وَلْد عَمْتِي
            maternal_uncle_son|ولد خالي|وَلْد خَالِي
            maternal_aunt_son|ولد خالتي|وَلْد خَالْتِي
            brother_son|ولد خويا|وَلْد خُويَا
            sister_son|ولد أختي|وَلْد أُخْتِي
            brother_daughter|بنت خويا|بِنْت خُويَا
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        regional(ArabicDialect.EGYPTIAN, "family", """
            husband_father|أبو جوزي|أَبُو جُوزِي
            husband_mother|أم جوزي|أُم جُوزِي
            wife_father|أبو مراتي|أَبُو مِرَاتِي
            wife_mother|أم مراتي|أُم مِرَاتِي
            husband_brother|أخو جوزي|أَخُو جُوزِي
            husband_sister|أخت جوزي|أُخْت جُوزِي
            wife_brother|أخو مراتي|أَخُو مِرَاتِي
            wife_sister|أخت مراتي|أُخْت مِرَاتِي
            sister_husband|جوز أختي|جُوز أُخْتِي
            daughter_husband|جوز بنتي|جُوز بِنْتِي
            brother_wife|مرات أخويا|مِرَات أَخُويَا
            son_wife|مرات ابني|مِرَات اِبْنِي
            paternal_uncle_son|ابن عمي|اِبْن عَمِّي
            paternal_aunt_son|ابن عمتي|اِبْن عَمِّتِي
            maternal_uncle_son|ابن خالي|اِبْن خَالِي
            maternal_aunt_son|ابن خالتي|اِبْن خَالْتِي
            brother_son|ابن أخويا|اِبْن أَخُويَا
            sister_son|ابن أختي|اِبْن أُخْتِي
            brother_daughter|بنت أخويا|بِنْت أَخُويَا
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        regional(ArabicDialect.SYRIAN, "family", """
            father|أبوي|أَبُوي
            brother|أخوي|أَخُوي
            husband_father|أبو جوزي|أَبُو جُوزِي
            husband_mother|أم جوزي|أُم جُوزِي
            wife_father|أبو مرتي|أَبُو مَرْتِي
            wife_mother|أم مرتي|أُم مَرْتِي
            husband_brother|أخو جوزي|أَخُو جُوزِي
            husband_sister|أخت جوزي|أُخْت جُوزِي
            wife_brother|أخو مرتي|أَخُو مَرْتِي
            wife_sister|أخت مرتي|أُخْت مَرْتِي
            sister_husband|جوز أختي|جُوز أُخْتِي
            daughter_husband|جوز بنتي|جُوز بِنْتِي
            brother_wife|مرت أخوي|مَرْت أَخُوي
            son_wife|مرت ابني|مَرْت اِبْنِي
            paternal_uncle_son|ابن عمي|اِبْن عَمِّي
            paternal_aunt_son|ابن عمتي|اِبْن عَمْتِي
            maternal_uncle_son|ابن خالي|اِبْن خَالِي
            maternal_aunt_son|ابن خالتي|اِبْن خَالْتِي
            brother_son|ابن أخوي|اِبْن أَخُوي
            sister_son|ابن أختي|اِبْن أُخْتِي
            brother_daughter|بنت أخوي|بِنْت أَخُوي
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        regional(ArabicDialect.IRAQI, "family", """
            brother|أخوي|أَخُوي
            husband_father|أبو زوجي|أَبُو زُوجِي
            husband_mother|أم زوجي|أُم زُوجِي
            wife_father|أبو مرتي|أَبُو مَرْتِي
            wife_mother|أم مرتي|أُم مَرْتِي
            husband_brother|أخو زوجي|أَخُو زُوجِي
            husband_sister|أخت زوجي|أُخْت زُوجِي
            wife_brother|أخو مرتي|أَخُو مَرْتِي
            wife_sister|أخت مرتي|أُخْت مَرْتِي
            sister_husband|زوج أختي|زُوج أُخْتِي
            daughter_husband|زوج بنتي|زُوج بِنْتِي
            brother_wife|مرت أخوي|مَرْت أَخُوي
            son_wife|مرت ابني|مَرْت اِبْنِي
            paternal_uncle_son|ولد عمي|وَلَد عَمِّي
            paternal_aunt_son|ولد عمتي|وَلَد عَمْتِي
            maternal_uncle_son|ولد خالي|وَلَد خَالِي
            maternal_aunt_son|ولد خالتي|وَلَد خَالْتِي
            brother_son|ولد أخوي|وَلَد أَخُوي
            sister_son|ولد أختي|وَلَد أُخْتِي
            brother_daughter|بنت أخوي|بِنْت أَخُوي
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        regional(ArabicDialect.SAUDI, "family", """
            husband_father|أبو زوجي|أَبُو زَوْجِي
            husband_mother|أم زوجي|أُم زَوْجِي
            wife_father|أبو زوجتي|أَبُو زَوْجَتِي
            wife_mother|أم زوجتي|أُم زَوْجَتِي
            husband_brother|أخو زوجي|أَخُو زَوْجِي
            husband_sister|أخت زوجي|أُخْت زَوْجِي
            wife_brother|أخو زوجتي|أَخُو زَوْجَتِي
            wife_sister|أخت زوجتي|أُخْت زَوْجَتِي
            sister_husband|زوج أختي|زَوْج أُخْتِي
            daughter_husband|زوج بنتي|زَوْج بِنْتِي
            brother_wife|زوجة أخوي|زَوْجَة أَخُوي
            son_wife|زوجة ولدي|زَوْجَة وَلَدِي
            paternal_uncle_son|ولد عمي|وَلَد عَمِّي
            paternal_aunt_son|ولد عمتي|وَلَد عَمَّتِي
            maternal_uncle_son|ولد خالي|وَلَد خَالِي
            maternal_aunt_son|ولد خالتي|وَلَد خَالَتِي
            brother_son|ولد أخوي|وَلَد أَخُوي
            sister_son|ولد أختي|وَلَد أُخْتِي
            brother_daughter|بنت أخوي|بِنْت أَخُوي
            sister_daughter|بنت أختي|بِنْت أُخْتِي
        """)
        register(all, "family", """
            paternal_uncle_daughter|بنت عمي|بِنْت عَمِّي
            maternal_uncle_daughter|بنت خالي|بِنْت خَالِي
        """)
        register(maghreb + ArabicDialect.IRAQI + ArabicDialect.SYRIAN, "family", """
            paternal_aunt_daughter|بنت عمتي|بِنْت عَمْتِي
            maternal_aunt_daughter|بنت خالتي|بِنْت خَالْتِي
        """)
        regional(ArabicDialect.EGYPTIAN, "family", """
            paternal_aunt_daughter|بنت عمتي|بِنْت عَمِّتِي
            maternal_aunt_daughter|بنت خالتي|بِنْت خَالْتِي
        """)
        regional(ArabicDialect.SAUDI, "family", """
            paternal_aunt_daughter|بنت عمتي|بِنْت عَمَّتِي
            maternal_aunt_daughter|بنت خالتي|بِنْت خَالَتِي
        """)

        register(all, "food", """
            olives|زيتون|زِيتُون
            oil|زيت|زِيت
        """)
        register(all.filter { it != ArabicDialect.TUNISIAN }, "food", "eggs|بيض|بِيض")
        regional(ArabicDialect.TUNISIAN, "food", "eggs|عظم|عْظَم")
        register(all, "fruit", """
            peach|خوخ|خُوخ
            coconut|جوز الهند|جُوز الهِنْد
        """)
        register(all.filter { it !in listOf(ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN) },
            "fruit", "banana|موز|مُوز")
        register(listOf(ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN),
            "fruit", "banana|بنان|بَنَان")
        register(all, "vegetable", "pepper|فلفل|فِلْفِل")
        register(all.filter { it !in listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN,
            ArabicDialect.SAUDI, ArabicDialect.IRAQI) }, "vegetable", "garlic|ثوم|تُوم")
        register(listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN),
            "vegetable", "garlic|توم|تُوم")
        register(listOf(ArabicDialect.SAUDI, ArabicDialect.IRAQI),
            "vegetable", "garlic|ثوم|ثُوم")

        register(all.filter { it !in listOf(ArabicDialect.TUNISIAN, ArabicDialect.ALGERIAN,
            ArabicDialect.MOROCCAN) }, "routine", "sleep|نوم|نُوم")
        register(listOf(ArabicDialect.TUNISIAN, ArabicDialect.ALGERIAN),
            "routine", "sleep|رقاد|رْقَاد")
        regional(ArabicDialect.MOROCCAN, "routine", "sleep|نعاس|نْعَاس")
        register(maghreb, "routine", "evening_coffee|قهوة العشية|قَهْوَة العَشِيَّة")
        register(listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN),
            "routine", "evening_coffee|قهوة المسا|قَهْوَة المَسَا")
        regional(ArabicDialect.IRAQI, "routine", "evening_coffee|قهوة العصر|قَهْوَة العَصِر")
        regional(ArabicDialect.SAUDI, "routine", "evening_coffee|قهوة العصر|قَهْوَة العَصْر")

        register(all.filter { it !in west }, "home", "fork|شوكة|شُوكَة")
        register(west, "home", "fork|فرشيطة|فَرْشِيطَة")
        regional(ArabicDialect.LIBYAN, "home", """
            water_bottle|شيشة اميه|شِيشَة امَيَّه
            sofa|صالون|صَالُون
            stove|غاز|غَاز
        """)
        regional(ArabicDialect.TUNISIAN, "home", """
            water_bottle|دبوزة ماء|دَبُّوزَة مَاء
            sofa|فوتاي|فُوتَاي
            stove|غاز|غَاز
        """)
        regional(ArabicDialect.ALGERIAN, "home", """
            water_bottle|قرعة ماء|قَرْعَة مَاء
            sofa|فوتاي|فُوتَاي
            stove|كويزينيير|كُويزِينْيِير
        """)
        regional(ArabicDialect.MOROCCAN, "home", """
            water_bottle|قرعة ماء|قَرْعَة مَاء
            sofa|سداري|سَدَّارِي
            stove|بوطة|بُوطَة
        """)
        regional(ArabicDialect.EGYPTIAN, "home", """
            water_bottle|إزازة مية|إِزَازَة مَيَّة
            sofa|كنبة|كَنَبَة
            stove|بوتاجاز|بُوتَاجَاز
        """)
        regional(ArabicDialect.SYRIAN, "home", """
            water_bottle|قنينة مي|قَنِّينَة مَيّ
            sofa|كنبة|كَنَبَة
            stove|بوتاجاز|بُوتَاجَاز
        """)
        regional(ArabicDialect.IRAQI, "home", """
            water_bottle|بطل مي|بُطُل مَيّ
            sofa|قنفة|قَنَفَة
            stove|طباخ|طَبَّاخ
        """)
        regional(ArabicDialect.SAUDI, "home", """
            water_bottle|قارورة موية|قَارُورَة مُويَة
            sofa|كنب|كَنَب
            stove|فرن غاز|فُرْن غَاز
        """)
        register(all, "home", "fan|مروحة|مَرْوَحَة")
        register(maghreb + ArabicDialect.SYRIAN, "home", "pot|طنجرة|طَنْجَرَة")
        regional(ArabicDialect.EGYPTIAN, "home", "pot|حلة|حَلَّة")
        regional(ArabicDialect.IRAQI, "home", "pot|جدر|جِدِر")
        regional(ArabicDialect.SAUDI, "home", "pot|قدر|قِدْر")

        register(all, "clothing", """
            robe|روب|رُوب
            abaya|عباية|عَبَايَة
        """)
        register(all, "body", """
            eye|عين|عِين
            shoulder|كتف|كِتْف
            neck|رقبة|رَقْبَة
        """)
        register(maghreb, "body", "ear|وذن|وِذِن")
        regional(ArabicDialect.EGYPTIAN, "body", "ear|ودن|وِدْن")
        register(listOf(ArabicDialect.SYRIAN, ArabicDialect.IRAQI),
            "body", "ear|إذن|إِذِن")
        regional(ArabicDialect.SAUDI, "body", "ear|إذن|إِذْن")

        regional(ArabicDialect.SYRIAN, "animal", "chicken|جاجة|جَاجَة")
        regional(ArabicDialect.SYRIAN, "action", """
            close|أسكر|أَسَكِّر
            see|أشوف|أَشُوف
            speak|أحكي|أَحْكِي
        """)
        regional(ArabicDialect.IRAQI, "action", """
            sit|أقعد|أَقْعُد
            read|أقرا|أَقْرَا
        """)
        regional(ArabicDialect.SAUDI, "action", "read|أقرا|أَقْرَا")
        regional(ArabicDialect.MOROCCAN, "action", "sit|نجلس|نْجْلِس")
        regional(ArabicDialect.MOROCCAN, "request", "headache|راسي كيوجعني|رَاسِي كَيُوجَعْنِي")
        regional(ArabicDialect.ALGERIAN, "request",
            "toilet|بغيت نروح للتواليت|بْغِيت نْرُوح لِلتْوَالِيت")
        entries
    }
}
