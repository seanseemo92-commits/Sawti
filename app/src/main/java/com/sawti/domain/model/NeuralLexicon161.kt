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
                require(fields.size == 2) { "Malformed v1.6.1 line: $line" }
                val key = "$namespace.${fields[0]}"
                val form = DialectForm(fields[1], fields[1])
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
            husband_father|بو راجلي
            husband_mother|أم راجلي
            wife_father|بو مرتي
            wife_mother|أم مرتي
            husband_brother|خو راجلي
            husband_sister|أخت راجلي
            wife_brother|خو مرتي
            wife_sister|أخت مرتي
            sister_husband|راجل أختي
            daughter_husband|راجل بنتي
            son_wife|مرت ولدي
        """)
        regional(ArabicDialect.LIBYAN, "family", """
            brother_wife|مرت خوي
            paternal_uncle_son|ولد عمي
            paternal_aunt_son|ولد عمتي
            maternal_uncle_son|ولد خالي
            maternal_aunt_son|ولد خالتي
            brother_son|ولد خوي
            sister_son|ولد أختي
            brother_daughter|بنت خوي
            sister_daughter|بنت أختي
        """)
        register(west, "family", """
            brother_wife|مرت خويا
            paternal_uncle_son|ولد عمي
            paternal_aunt_son|ولد عمتي
            maternal_uncle_son|ولد خالي
            maternal_aunt_son|ولد خالتي
            brother_son|ولد خويا
            sister_son|ولد أختي
            brother_daughter|بنت خويا
            sister_daughter|بنت أختي
        """)
        regional(ArabicDialect.EGYPTIAN, "family", """
            husband_father|أبو جوزي
            husband_mother|أم جوزي
            wife_father|أبو مراتي
            wife_mother|أم مراتي
            husband_brother|أخو جوزي
            husband_sister|أخت جوزي
            wife_brother|أخو مراتي
            wife_sister|أخت مراتي
            sister_husband|جوز أختي
            daughter_husband|جوز بنتي
            brother_wife|مرات أخويا
            son_wife|مرات ابني
            paternal_uncle_son|ابن عمي
            paternal_aunt_son|ابن عمتي
            maternal_uncle_son|ابن خالي
            maternal_aunt_son|ابن خالتي
            brother_son|ابن أخويا
            sister_son|ابن أختي
            brother_daughter|بنت أخويا
            sister_daughter|بنت أختي
        """)
        regional(ArabicDialect.SYRIAN, "family", """
            father|أبوي
            brother|أخوي
            husband_father|أبو جوزي
            husband_mother|أم جوزي
            wife_father|أبو مرتي
            wife_mother|أم مرتي
            husband_brother|أخو جوزي
            husband_sister|أخت جوزي
            wife_brother|أخو مرتي
            wife_sister|أخت مرتي
            sister_husband|جوز أختي
            daughter_husband|جوز بنتي
            brother_wife|مرت أخوي
            son_wife|مرت ابني
            paternal_uncle_son|ابن عمي
            paternal_aunt_son|ابن عمتي
            maternal_uncle_son|ابن خالي
            maternal_aunt_son|ابن خالتي
            brother_son|ابن أخوي
            sister_son|ابن أختي
            brother_daughter|بنت أخوي
            sister_daughter|بنت أختي
        """)
        regional(ArabicDialect.IRAQI, "family", """
            brother|أخوي
            husband_father|أبو زوجي
            husband_mother|أم زوجي
            wife_father|أبو مرتي
            wife_mother|أم مرتي
            husband_brother|أخو زوجي
            husband_sister|أخت زوجي
            wife_brother|أخو مرتي
            wife_sister|أخت مرتي
            sister_husband|زوج أختي
            daughter_husband|زوج بنتي
            brother_wife|مرت أخوي
            son_wife|مرت ابني
            paternal_uncle_son|ولد عمي
            paternal_aunt_son|ولد عمتي
            maternal_uncle_son|ولد خالي
            maternal_aunt_son|ولد خالتي
            brother_son|ولد أخوي
            sister_son|ولد أختي
            brother_daughter|بنت أخوي
            sister_daughter|بنت أختي
        """)
        regional(ArabicDialect.SAUDI, "family", """
            husband_father|أبو زوجي
            husband_mother|أم زوجي
            wife_father|أبو زوجتي
            wife_mother|أم زوجتي
            husband_brother|أخو زوجي
            husband_sister|أخت زوجي
            wife_brother|أخو زوجتي
            wife_sister|أخت زوجتي
            sister_husband|زوج أختي
            daughter_husband|زوج بنتي
            brother_wife|زوجة أخوي
            son_wife|زوجة ولدي
            paternal_uncle_son|ولد عمي
            paternal_aunt_son|ولد عمتي
            maternal_uncle_son|ولد خالي
            maternal_aunt_son|ولد خالتي
            brother_son|ولد أخوي
            sister_son|ولد أختي
            brother_daughter|بنت أخوي
            sister_daughter|بنت أختي
        """)
        register(all, "family", """
            paternal_uncle_daughter|بنت عمي
            maternal_uncle_daughter|بنت خالي
        """)
        register(maghreb + ArabicDialect.IRAQI + ArabicDialect.SYRIAN, "family", """
            paternal_aunt_daughter|بنت عمتي
            maternal_aunt_daughter|بنت خالتي
        """)
        regional(ArabicDialect.EGYPTIAN, "family", """
            paternal_aunt_daughter|بنت عمتي
            maternal_aunt_daughter|بنت خالتي
        """)
        regional(ArabicDialect.SAUDI, "family", """
            paternal_aunt_daughter|بنت عمتي
            maternal_aunt_daughter|بنت خالتي
        """)

        register(all, "food", """
            olives|زيتون
            oil|زيت
        """)
        register(all.filter { it != ArabicDialect.TUNISIAN }, "food", "eggs|بيض|بيض")
        regional(ArabicDialect.TUNISIAN, "food", "eggs|عظم|عظم")
        register(all, "fruit", """
            peach|خوخ
            coconut|جوز الهند
        """)
        register(all.filter { it !in listOf(ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN) },
            "fruit", "banana|موز|موز")
        register(listOf(ArabicDialect.ALGERIAN, ArabicDialect.MOROCCAN),
            "fruit", "banana|بنان|بنان")
        register(all, "vegetable", "pepper|فلفل|فلفل")
        register(all.filter { it !in listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN,
            ArabicDialect.SAUDI, ArabicDialect.IRAQI) }, "vegetable", "garlic|ثوم|توم")
        register(listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN),
            "vegetable", "garlic|توم|توم")
        register(listOf(ArabicDialect.SAUDI, ArabicDialect.IRAQI),
            "vegetable", "garlic|ثوم|ثوم")

        register(all.filter { it !in listOf(ArabicDialect.TUNISIAN, ArabicDialect.ALGERIAN,
            ArabicDialect.MOROCCAN) }, "routine", "sleep|نوم|نوم")
        register(listOf(ArabicDialect.TUNISIAN, ArabicDialect.ALGERIAN),
            "routine", "sleep|رقاد|رقاد")
        regional(ArabicDialect.MOROCCAN, "routine", "sleep|نعاس|نعاس")
        register(maghreb, "routine", "evening_coffee|قهوة العشية|قهوة العشية")
        register(listOf(ArabicDialect.EGYPTIAN, ArabicDialect.SYRIAN),
            "routine", "evening_coffee|قهوة المسا|قهوة المسا")
        regional(ArabicDialect.IRAQI, "routine", "evening_coffee|قهوة العصر|قهوة العصر")
        regional(ArabicDialect.SAUDI, "routine", "evening_coffee|قهوة العصر|قهوة العصر")

        register(all.filter { it !in west }, "home", "fork|شوكة|شوكة")
        register(west, "home", "fork|فرشيطة|فرشيطة")
        regional(ArabicDialect.LIBYAN, "home", """
            water_bottle|شيشة اميه
            sofa|صالون
            stove|غاز
        """)
        regional(ArabicDialect.TUNISIAN, "home", """
            water_bottle|دبوزة ماء
            sofa|فوتاي
            stove|غاز
        """)
        regional(ArabicDialect.ALGERIAN, "home", """
            water_bottle|قرعة ماء
            sofa|فوتاي
            stove|كويزينيير
        """)
        regional(ArabicDialect.MOROCCAN, "home", """
            water_bottle|قرعة ماء
            sofa|سداري
            stove|بوطة
        """)
        regional(ArabicDialect.EGYPTIAN, "home", """
            water_bottle|إزازة مية
            sofa|كنبة
            stove|بوتاجاز
        """)
        regional(ArabicDialect.SYRIAN, "home", """
            water_bottle|قنينة مي
            sofa|كنبة
            stove|بوتاجاز
        """)
        regional(ArabicDialect.IRAQI, "home", """
            water_bottle|بطل مي
            sofa|قنفة
            stove|طباخ
        """)
        regional(ArabicDialect.SAUDI, "home", """
            water_bottle|قارورة موية
            sofa|كنب
            stove|فرن غاز
        """)
        register(all, "home", "fan|مروحة|مروحة")
        register(maghreb + ArabicDialect.SYRIAN, "home", "pot|طنجرة|طنجرة")
        regional(ArabicDialect.EGYPTIAN, "home", "pot|حلة|حلة")
        regional(ArabicDialect.IRAQI, "home", "pot|جدر|جدر")
        regional(ArabicDialect.SAUDI, "home", "pot|قدر|قدر")

        register(all, "clothing", """
            robe|روب
            abaya|عباية
        """)
        register(all, "body", """
            eye|عين
            shoulder|كتف
            neck|رقبة
        """)
        register(maghreb, "body", "ear|وذن|وذن")
        regional(ArabicDialect.EGYPTIAN, "body", "ear|ودن|ودن")
        register(listOf(ArabicDialect.SYRIAN, ArabicDialect.IRAQI),
            "body", "ear|إذن|إذن")
        regional(ArabicDialect.SAUDI, "body", "ear|إذن|إذن")

        regional(ArabicDialect.SYRIAN, "animal", "chicken|جاجة|جاجة")
        regional(ArabicDialect.SYRIAN, "action", """
            close|أسكر
            see|أشوف
            speak|أحكي
        """)
        regional(ArabicDialect.IRAQI, "action", """
            sit|أقعد
            read|أقرا
        """)
        regional(ArabicDialect.SAUDI, "action", "read|أقرا|أقرا")
        regional(ArabicDialect.MOROCCAN, "action", "sit|نجلس|نجلس")
        regional(ArabicDialect.MOROCCAN, "request", "headache|راسي كيوجعني|راسي كيوجعني")
        regional(ArabicDialect.ALGERIAN, "request",
            "toilet|بغيت نروح للتواليت|بغيت نروح للتواليت")
        entries
    }
}
