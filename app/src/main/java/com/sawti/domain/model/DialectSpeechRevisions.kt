package com.sawti.domain.model

/**
 * Runtime-preserved visual and sentence revisions originally reviewed against the supplied
 * Argon audit of 2026-11-02 and the 245-entry written update of 2026-11-15.
 * Native-speaker review and device TTS listening remain separate pending review gates.
 * Review-only alternatives and explanatory parentheses are intentionally excluded.
 * This is a non-destructive projection: the Room seed and user edits stay unchanged.
 */
internal object DialectSpeechRevisions {
    private data class Field(val expected: String, val proposed: String)
    private data class Patch(
        val display: Field? = null,
        val sentence: Field? = null,
    )
    private fun standard(): Map<String, Patch> = mapOf(
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أريد ماء")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أريد شايا")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: أريد أن أرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: أريد دوائي")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: أريد الذهاب إلى الحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: عدل لي الوسادة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),





        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),


        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "حذاء بكعب عال")),
        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ..."))
    )

    private fun libyan(): Map<String, Patch> = mapOf(
        "family.father" to Patch(display = Field("أبي", "بوي")),
        "family.husband" to Patch(display = Field("زوجي", "راجلي")),
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.brother" to Patch(display = Field("أخي", "خوي")),
        "family.grandmother" to Patch(display = Field("جدتي", "حناي")),
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "خو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "خو راجلي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "بو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "بو راجلي")),
        "family.son" to Patch(display = Field("ابني", "ولدي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),



        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد خوي")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت خوي")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم راجلي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت راجلي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "راجل أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت خوي")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "راجل بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ولدي")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نبي مية")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نبي شاهي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: نبي نرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: نبي دوايا")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: نبي الحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: عدل لي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),

        "vegetable.coriander" to Patch(display = Field("كزبرة", "قصبر")),


        "fruit.pear" to Patch(display = Field("إجاص", "كمثرى")),

        "food.rice" to Patch(display = Field("أرز", "رز")),
        "food.fish" to Patch(display = Field("حوت (سمك)", "حوت")),
        "home.blanket" to Patch(display = Field("ورغان / بطانية", "ورغان")),
        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.carpet" to Patch(display = Field("بساط / حواشي", "بساط")),
        "home.knife" to Patch(display = Field("موس (سكين)", "موس")),
        "clothing.robe" to Patch(display = Field("روب / قفطان", "روب")),

        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ..."))
    )

    private fun saudi(): Map<String, Patch> = mapOf(
        "family.son" to Patch(display = Field("ابني", "ولدي")),
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "أخو زوجتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "أخو زوجي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "أبو زوجتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "أبو زوجي")),
        "family.father" to Patch(display = Field("أبي", "أبوي")),

        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "أخوي")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد أخوي")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت أخوي")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم زوجي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم زوجتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت زوجي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت زوجتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "زوج أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت أخوي")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "زوج بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ولدي")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أبغى ماي")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أبغى شاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: أبغى أرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: أبغى دواي")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: أبغى أروح الحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: عدل لي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.saudi.kabsa" to Patch(display = Field("كبسة لحم", "كبسة لحم")),
        "dish.saudi.saleeg" to Patch(display = Field("سليق طائفي", "سليق طائفي")),
        "dish.saudi.madhbi" to Patch(display = Field("مظبي دجاج", "مظبي دجاج")),
        "dish.saudi.liver" to Patch(display = Field("كبدة حاشي", "كبدة حاشي")),












        "home.sofa" to Patch(display = Field("أريكة", "كنبة")),

        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.wardrobe" to Patch(display = Field("خزانة", "دولاب")),
        "home.pot" to Patch(display = Field("طنجرة", "قدر")),

        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),




        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "جزمة كعب")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),



        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ..."))
    )

    private fun egyptian(): Map<String, Patch> = mapOf(
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "أخو مراتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "أخو جوزي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "أبو مراتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "أبو جوزي")),
        "family.father" to Patch(display = Field("أبي", "أبويا")),
        "family.husband" to Patch(display = Field("زوجي", "جوزي")),
        "family.wife" to Patch(display = Field("زوجتي", "مراتي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "أخويا")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ابن عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ابن عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ابن خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ابن خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ابن أخي")),

        "family.sister_son" to Patch(display = Field("ابن أختي", "ابن أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم جوزي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مراتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت جوزي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مراتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "جوز أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرات أخويا")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "جوز بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرات ابني")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: عايز مية")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: ممكن شاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: ممكن أرتاح شوية")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: ممكن دوايا")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: ممكن أدخل الحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: ظبطلي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.egyptian.molokhia" to Patch(display = Field("ملوخية بالتقلية", "ملوخية بالتقلية")),
        "dish.egyptian.mahshi" to Patch(display = Field("محشي مشكل", "محشي مشكل")),
        "dish.egyptian.moussaka" to Patch(display = Field("مسقعة بلدي", "مسقعة بلدي")),
        "dish.egyptian.trotters_fattah" to Patch(display = Field("كوارع وفتة", "كوارع وفتة")),
        "dish.egyptian.bird_tongue_soup" to Patch(display = Field("شربة لسان عصفور", "شربة لسان عصفور")),



        "vegetable.lemon" to Patch(display = Field("ليمون", "لمون")),



        "fruit.orange" to Patch(display = Field("برتقال", "برتقان")),







        "home.sofa" to Patch(display = Field("أريكة", "كنبة")),

        "home.stove" to Patch(display = Field("موقد", "بوتاجاز")),
        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.wardrobe" to Patch(display = Field("خزانة", "دولاب")),
        "home.carpet" to Patch(display = Field("بساط", "سجادة")),




        "home.pot" to Patch(display = Field("طنجرة", "حلة")),

        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),

        "clothing.socks" to Patch(display = Field("شرابات", "شرابات")),




        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "جزمة بكعب")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),

        "body.mouth" to Patch(display = Field("فم", "بق")),




        "body.face" to Patch(display = Field("وجه", "وش")),

        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ...")),
        "animal.chicken" to Patch(display = Field("دجاجة", "فرخة"))
    )

    private fun syrian(): Map<String, Patch> = mapOf(
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "أخو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "أخو جوزي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "أبو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "أبو جوزي")),
        "family.husband" to Patch(display = Field("زوجي", "جوزي")),
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ابن عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ابن عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ابن خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ابن خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ابن أخي")),

        "family.sister_son" to Patch(display = Field("ابن أختي", "ابن أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم جوزي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت جوزي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "جوز أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت أخي")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "جوز بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ابني")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: بدي مي")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: بدي شاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: بدي ارتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: بدي دواي")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: بدي روح عالحمام")),
        "request.adjust_pillow" to Patch(display = Field("زبطلي المخدة", "زبط لي المخدة"), sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: زبطلي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.syrian.shakriya" to Patch(display = Field("شاكرية برز", "شاكرية برز")),
        "dish.syrian.maqluba" to Patch(display = Field("مقلوبة باذنجان", "مقلوبة باذنجان")),
        "dish.syrian.kebab" to Patch(display = Field("كباب حلبي", "كباب حلبي")),
        "dish.syrian.mutabbal" to Patch(display = Field("متبل باذنجان", "متبل باذنجان")),
        "dish.syrian.haraq_osbao" to Patch(display = Field("حراق بأصبعه", "حراق إصبعه")),
        "dish.syrian.kunafa" to Patch(display = Field("كنافة نابلسية", "كنافة نابلسية")),


        "vegetable.zucchini" to Patch(display = Field("كوسة", "كوسا")),



        "fruit.orange" to Patch(display = Field("برتقال", "بردقان")),








        "home.sofa" to Patch(display = Field("أريكة", "كنباية")),

        "home.stove" to Patch(display = Field("موقد", "غاز")),
        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),


        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),





        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "كندرة كعب عالي")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),

        "body.mouth" to Patch(display = Field("فم", "تم")),


        "body.face" to Patch(display = Field("وجه", "وش")),
        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ..."))
    )

    private fun iraqi(): Map<String, Patch> = mapOf(
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "أخو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "أخو زوجي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "أبو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "أبو زوجي")),
        "family.father" to Patch(display = Field("أبي", "أبويه")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "أخوي")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد أخوي")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت أخوي")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم زوجي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت زوجي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "زوج أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت أخوي")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "زوج بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ابني")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أريد ماي")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: أريد جاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: أريد أرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: أريد دواي")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: أريد أروح للحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: عدل لي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.iraqi.dolma" to Patch(display = Field("دولمة عراقية", "دولمة عراقية")),
        "dish.iraqi.tashreeb" to Patch(display = Field("تشريب لحم", "تشريب لحم")),


        "dish.iraqi.biryani" to Patch(display = Field("برياني عراقي", "برياني عراقي")),
        "dish.iraqi.makhlama" to Patch(display = Field("فافير / مخلمة", "مخلمة")),


        "vegetable.peas" to Patch(display = Field("بازلاء", "بزاليا")),









        "home.sofa" to Patch(display = Field("أريكة", "قنفة")),

        "home.stove" to Patch(display = Field("موقد", "طباخ")),
        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.wardrobe" to Patch(display = Field("خزانة", "كنتور")),
        "home.carpet" to Patch(display = Field("بساط", "زولية")),


        "home.pot" to Patch(display = Field("طنجرة", "جدر")),

        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),




        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "قندرة كعب عالي")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),

        "body.mouth" to Patch(display = Field("فم", "حلك")),



        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ..."))
    )

    private fun tunisian(): Map<String, Patch> = mapOf(
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "خو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "خو راجلي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "بو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "بو راجلي")),
        "family.father" to Patch(display = Field("أبي", "بابا")),
        "family.husband" to Patch(display = Field("زوجي", "راجلي")),
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.son" to Patch(display = Field("ابني", "ولدي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "خويا")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد خويا")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت خويا")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم راجلي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت راجلي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "راجل أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت خويا")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "راجل بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ولدي")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نحب شوية ما")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نحب تاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: نحب نرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: نحب دوايا")),
        "request.toilet" to Patch(display = Field("نحب نمشي للتوالات", "نحب نمشي للتواليت"), sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: نحب نمشي للتوالات")),
        "request.adjust_pillow" to Patch(display = Field("صلحلي المخدة", "صلح لي المخدة"), sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: صلحلي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.tunisian.molokhia" to Patch(display = Field("ملوخية تونسية", "ملوخية تونسية")),
        "dish.tunisian.pasta" to Patch(display = Field("مقرونة بالصلصة", "مقرونة بالصلصة")),
        "dish.tunisian.maqroudh" to Patch(display = Field("مقروض قيرواني", "مقروض قيرواني")),


        "vegetable.carrot" to Patch(display = Field("جزر", "سفنارية")),
        "vegetable.zucchini" to Patch(display = Field("كوسة", "قرع أخضر")),

        "vegetable.lemon" to Patch(display = Field("ليمون", "قارص")),



        "fruit.banana" to Patch(display = Field("موز", "بنان")),
        "fruit.orange" to Patch(display = Field("برتقال", "بردقان")),











        "home.stove" to Patch(display = Field("موقد", "غاز")),
        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.wardrobe" to Patch(display = Field("خزانة", "مريو")),
        "home.carpet" to Patch(display = Field("بساط", "زربية")),







        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),



        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "سباط طالون")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),






        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ...")),

    )

    private fun moroccan(): Map<String, Patch> = mapOf(
        "family.father" to Patch(display = Field("أبي", "با")),
        "family.mother" to Patch(display = Field("أمي", "مي")),
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "خو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "خو راجلي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "بو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "بو راجلي")),
        "family.husband" to Patch(display = Field("زوجي", "راجلي")),
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.son" to Patch(display = Field("ابني", "ولدي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "خويا")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد خويا")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت خويا")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم راجلي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت راجلي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "راجل أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت خويا")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "راجل بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ولدي")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: بغيت الما")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: بغيت أتاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: بغيت نرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: بغيت الدوا ديالي")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: بغيت نمشي للحمام")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: صوب ليا المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),

        "dish.moroccan.pastilla" to Patch(display = Field("بسطيلة بالحوت", "بسطيلة بالحوت")),
        "dish.moroccan.harira" to Patch(display = Field("حريرة مغربية", "حريرة مغربية")),
        "dish.moroccan.tangia" to Patch(display = Field("طنجية مراكشية", "طنجية مراكشية")),
        "dish.moroccan.rfissa" to Patch(display = Field("رفيسة بالدجاج", "رفيسة بالدجاج")),
        "dish.moroccan.kefta_tajine" to Patch(display = Field("طاجين كفتة بالبيض", "طاجين كفتة بالبيض")),




        "vegetable.carrot" to Patch(display = Field("جزر", "خيزو")),



        "vegetable.lemon" to Patch(display = Field("ليمون", "حامض")),





        "fruit.banana" to Patch(display = Field("موز", "بنان")),











        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),
        "home.carpet" to Patch(display = Field("بساط", "زربية")),






        "home.water_bottle" to Patch(display = Field("قرعة ماء", "قرعة ما")),
        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),





        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "صباط طالون")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),





        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ...")),



    )

    private fun algerian(): Map<String, Patch> = mapOf(
        "family.wife_brother" to Patch(display = Field("أخو زوجتي", "خو مرتي")),
        "family.husband_brother" to Patch(display = Field("أخو زوجي", "خو راجلي")),
        "family.wife_father" to Patch(display = Field("أبو زوجتي", "بو مرتي")),
        "family.husband_father" to Patch(display = Field("أبو زوجي", "بو راجلي")),
        "family.father" to Patch(display = Field("أبي", "بابا")),
        "family.mother" to Patch(display = Field("أمي", "يما")),
        "family.husband" to Patch(display = Field("زوجي", "راجلي")),
        "family.wife" to Patch(display = Field("زوجتي", "مرتي")),
        "family.son" to Patch(display = Field("ابني", "ولدي")),
        "family.daughter" to Patch(display = Field("ابنتي", "بنتي")),
        "family.brother" to Patch(display = Field("أخي", "خويا")),
        "family.paternal_uncle_son" to Patch(display = Field("ابن عمي", "ولد عمي")),

        "family.paternal_aunt_son" to Patch(display = Field("ابن عمتي", "ولد عمتي")),

        "family.maternal_uncle_son" to Patch(display = Field("ابن خالي", "ولد خالي")),

        "family.maternal_aunt_son" to Patch(display = Field("ابن خالتي", "ولد خالتي")),

        "family.brother_son" to Patch(display = Field("ابن أخي", "ولد خويا")),
        "family.brother_daughter" to Patch(display = Field("بنت أخي", "بنت خويا")),
        "family.sister_son" to Patch(display = Field("ابن أختي", "ولد أختي")),

        "family.husband_mother" to Patch(display = Field("أم زوجي", "أم راجلي")),
        "family.wife_mother" to Patch(display = Field("أم زوجتي", "أم مرتي")),
        "family.husband_sister" to Patch(display = Field("أخت زوجي", "أخت راجلي")),
        "family.wife_sister" to Patch(display = Field("أخت زوجتي", "أخت مرتي")),
        "family.sister_husband" to Patch(display = Field("زوج أختي", "راجل أختي")),
        "family.brother_wife" to Patch(display = Field("زوجة أخي", "مرت خويا")),
        "family.daughter_husband" to Patch(display = Field("زوج ابنتي", "راجل بنتي")),
        "family.son_wife" to Patch(display = Field("زوجة ابني", "مرت ولدي")),
        "request.water" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نحب نشرب الما")),
        "request.tea" to Patch(sentence = Field("نشرب الـ...", "عند الطلب أقول: نحب أتاي")),
        "request.rest" to Patch(sentence = Field("عندما أتعب أريد الـ...", "عند الطلب أقول: نحب نرتاح")),
        "request.medicine" to Patch(sentence = Field("في الموعد آخذ الـ...", "عند الطلب أقول: نحب دوايا")),
        "request.toilet" to Patch(sentence = Field("أذهب إلى الـ...", "عند الطلب أقول: نحب نروح للتواليت")),
        "request.adjust_pillow" to Patch(sentence = Field("أريد تعديل الـ...", "عند الطلب أقول: صلحلي المخدة")),
        "request.headache" to Patch(sentence = Field("", "عندما أتألم أقول: ...")),
        "request.cold" to Patch(sentence = Field("", "في الشتاء أقول: ...")),
        "dish.algerian.chakhchoukha" to Patch(display = Field("شخشوخة بسكرية", "شخشوخة بسكرية")),
        "dish.algerian.garantita" to Patch(display = Field("كرانتيكا", "كرانتيكا")),


        "vegetable.carrot" to Patch(display = Field("جزر", "زرودية")),


        "vegetable.lemon" to Patch(display = Field("ليمون", "قارص")),



        "fruit.banana" to Patch(display = Field("موز", "بنان")),
        "fruit.orange" to Patch(display = Field("برتقال", "تشينة")),












        "home.oven" to Patch(sentence = Field("", "نخبز الخبز في الـ...")),

        "home.carpet" to Patch(display = Field("بساط", "زربية")),






        "home.water_bottle" to Patch(display = Field("قرعة ماء", "قرعة ما")),
        "clothing.tshirt" to Patch(display = Field("قميص قصير الأكمام", "تيشيرت")),

        "clothing.shorts" to Patch(display = Field("سروال قصير", "شورت")),




        "clothing.high_heels" to Patch(display = Field("حذاء بكعب عال", "سباط طالون")),
        "clothing.pajamas" to Patch(display = Field("ملابس النوم", "بيجامة")),







        "action.eat" to Patch(sentence = Field("", "عندما أجوع أنا ...")),
        "action.drink" to Patch(sentence = Field("", "عندما أعطش أنا ...")),
        "action.sleep" to Patch(sentence = Field("", "في الليل أنا ...")),
        "action.sit" to Patch(sentence = Field("", "على الكرسي أنا ...")),
        "action.walk" to Patch(sentence = Field("", "في الطريق أنا ...")),
        "action.stand" to Patch(sentence = Field("", "من مكاني أنا ...")),
        "action.wash" to Patch(sentence = Field("", "بالماء والصابون أنا ...")),
        "action.speak" to Patch(sentence = Field("", "مع أسرتي أنا ...")),
        "action.hear" to Patch(sentence = Field("", "بأذني أنا ...")),
        "action.open" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.close" to Patch(sentence = Field("", "الباب أنا ...")),
        "action.read" to Patch(sentence = Field("", "في الكتاب أنا ...")),
        "action.laugh" to Patch(sentence = Field("", "عندما أفرح أنا ...")),
        "action.see" to Patch(sentence = Field("", "بعيني أنا ...")),
        "action.dress" to Patch(sentence = Field("", "ثيابي أنا ...")),
        "action.pray" to Patch(sentence = Field("", "في المسجد أنا ...")),


    )


    /** Exact written display and TTS candidates supplied for the 245 outstanding entries.
     * These are runtime projections, not Room migrations or native/TTS listening approvals.
     */
    private data class Completed(val display: String)
    private val completed: Map<ArabicDialect, Map<String, Completed>> by lazy {
        mapOf(
            ArabicDialect.SAUDI to mapOf(
                "action.stand" to Completed("أقوم"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("خشم"),
                "clothing.bag" to Completed("شنطة"),
                "clothing.glasses" to Completed("نظارة"),
                "clothing.jacket" to Completed("جاكيت"),
                "clothing.trousers" to Completed("بنطلون"),
                "food.pasta" to Completed("مكرونة"),
                "food.rice" to Completed("رز"),
                "food.soup" to Completed("شوربة"),
                "fruit.watermelon" to Completed("حبحب"),
                "home.pillow" to Completed("مخدة"),
                "home.water_bottle" to Completed("قارورة موية"),
                "home.window" to Completed("شباك"),
                "routine.evening_coffee" to Completed("قهوة المسا"),
                "vegetable.cauliflower" to Completed("زهرة"),
                "vegetable.green_beans" to Completed("فاصوليا خضرا"),
                "vegetable.peas" to Completed("بازلا"),
                "vegetable.pumpkin" to Completed("قرع"),
            ),
            ArabicDialect.IRAQI to mapOf(
                "action.stand" to Completed("أقوم"),
                "body.finger" to Completed("إصبع"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("خشم"),
                "clothing.bag" to Completed("جنطة"),
                "clothing.coat" to Completed("قبوط"),
                "clothing.jacket" to Completed("جاكيت"),
                "clothing.trousers" to Completed("بنطرون"),
                "food.rice" to Completed("تمن"),
                "food.soup" to Completed("شوربة"),
                "food.yogurt" to Completed("لبن"),
                "fruit.watermelon" to Completed("رقي"),
                "home.cup" to Completed("كوب"),
                "home.pillow" to Completed("مخدة"),
                "home.teapot" to Completed("قوري"),
                "home.water_bottle" to Completed("بطل مي"),
                "home.window" to Completed("شباك"),
                "routine.evening_coffee" to Completed("قهوة المسا"),
                "vegetable.cauliflower" to Completed("قرنابيط"),
                "vegetable.green_beans" to Completed("فاصوليا خضرا"),
                "vegetable.potato" to Completed("بتيتة"),
                "vegetable.pumpkin" to Completed("قرع"),
            ),
            ArabicDialect.SYRIAN to mapOf(
                "action.pray" to Completed("أصلي"),
                "action.read" to Completed("أقرا"),
                "action.sleep" to Completed("أنام"),
                "action.stand" to Completed("أقوم"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("منخار"),
                "clothing.bag" to Completed("شنطة"),
                "clothing.cap" to Completed("طاقية"),
                "clothing.glasses" to Completed("نظارة"),
                "clothing.jacket" to Completed("جاكيت"),
                "clothing.trousers" to Completed("بنطلون"),
                "food.cheese" to Completed("جبنة"),
                "food.chicken" to Completed("جاج"),
                "food.rice" to Completed("رز"),
                "food.soup" to Completed("شوربة"),
                "food.yogurt" to Completed("لبن"),
                "home.blanket" to Completed("حرام"),
                "home.cup" to Completed("كاسة"),
                "home.pillow" to Completed("مخدة"),
                "home.water_bottle" to Completed("قنينة مي"),
                "home.window" to Completed("شباك"),
                "routine.evening_coffee" to Completed("قهوة المسا"),
                "vegetable.cauliflower" to Completed("زهرة"),
                "vegetable.green_beans" to Completed("فاصوليا خضرا"),
                "vegetable.potato" to Completed("بطاطا"),
                "vegetable.pumpkin" to Completed("قرع"),
            ),
            ArabicDialect.EGYPTIAN to mapOf(
                "action.read" to Completed("أقرا"),
                "body.ear" to Completed("ودن"),
                "body.finger" to Completed("صباع"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("مناخير"),
                "body.tooth" to Completed("سنة"),
                "clothing.bag" to Completed("شنطة"),
                "clothing.coat" to Completed("بالطو"),
                "clothing.glasses" to Completed("نضارة"),
                "clothing.jacket" to Completed("جاكيت"),
                "clothing.trousers" to Completed("بنطلون"),
                "food.cheese" to Completed("جبنة"),
                "food.chicken" to Completed("فراخ"),
                "food.milk" to Completed("لبن"),
                "food.pasta" to Completed("مكرونة"),
                "food.rice" to Completed("رز"),
                "food.soup" to Completed("شوربة"),
                "home.cup" to Completed("كوباية"),
                "home.knife" to Completed("سكينة"),
                "home.pillow" to Completed("مخدة"),
                "home.spoon" to Completed("معلقة"),
                "home.teapot" to Completed("براد"),
                "home.water_bottle" to Completed("إزازة مية"),
                "home.window" to Completed("شباك"),
                "routine.evening_coffee" to Completed("قهوة المسا"),
                "vegetable.eggplant" to Completed("بتنجان"),
                "vegetable.green_beans" to Completed("فاصوليا خضرا"),
                "vegetable.pumpkin" to Completed("قرع"),
            ),
            ArabicDialect.TUNISIAN to mapOf(
                "action.dress" to Completed("نلبس"),
                "action.drink" to Completed("نشرب"),
                "action.eat" to Completed("ناكل"),
                "action.hear" to Completed("نسمع"),
                "action.laugh" to Completed("نضحك"),
                "action.open" to Completed("نحل"),
                "action.pray" to Completed("نصلي"),
                "action.read" to Completed("نقرا"),
                "action.sleep" to Completed("نرقد"),
                "action.stand" to Completed("نوقف"),
                "action.walk" to Completed("نمشي"),
                "action.wash" to Completed("نغسل"),
                "animal.sheep" to Completed("علوش"),
                "body.ear" to Completed("وذن"),
                "body.finger" to Completed("صبع"),
                "body.head" to Completed("راس"),
                "body.nose" to Completed("خشم"),
                "body.tooth" to Completed("سنة"),
                "clothing.bag" to Completed("صاك"),
                "clothing.belt" to Completed("سبتة"),
                "clothing.coat" to Completed("مانتو"),
                "clothing.jacket" to Completed("فيستة"),
                "food.pasta" to Completed("مقرونة"),
                "food.rice" to Completed("روز"),
                "food.soup" to Completed("شربة"),
                "food.water" to Completed("ماء"),
                "food.yogurt" to Completed("ياغرت"),
                "fruit.mandarin" to Completed("مندرينة"),
                "fruit.melon" to Completed("بطيخ"),
                "fruit.watermelon" to Completed("دلاع"),
                "home.blanket" to Completed("فراشية"),
                "home.cup" to Completed("كاس"),
                "home.fork" to Completed("فرشيطة"),
                "home.kitchen" to Completed("كوجينة"),
                "home.knife" to Completed("سكينة"),
                "home.pillow" to Completed("مخدة"),
                "home.spoon" to Completed("مغرفة"),
                "home.teapot" to Completed("براد"),
                "home.water_bottle" to Completed("دبوزة ماء"),
                "home.window" to Completed("شباك"),
                "routine.evening_coffee" to Completed("قهوة العشية"),
                "vegetable.green_beans" to Completed("لوبيا خضرا"),
                "vegetable.parsley" to Completed("معدنوس"),
                "vegetable.potato" to Completed("بطاطا"),
                "vegetable.pumpkin" to Completed("قرع"),
            ),
            ArabicDialect.ALGERIAN to mapOf(
                "action.dress" to Completed("نلبس"),
                "action.drink" to Completed("نشرب"),
                "action.eat" to Completed("ناكل"),
                "action.hear" to Completed("نسمع"),
                "action.laugh" to Completed("نضحك"),
                "action.open" to Completed("نحل"),
                "action.pray" to Completed("نصلي"),
                "action.read" to Completed("نقرا"),
                "action.sleep" to Completed("نرقد"),
                "action.stand" to Completed("نوقف"),
                "action.walk" to Completed("نمشي"),
                "action.wash" to Completed("نغسل"),
                "animal.rabbit" to Completed("أرنب"),
                "body.ear" to Completed("وذن"),
                "body.finger" to Completed("صبع"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("نيف"),
                "body.tooth" to Completed("سنة"),
                "clothing.bag" to Completed("صاك"),
                "clothing.belt" to Completed("سبتة"),
                "clothing.coat" to Completed("مانتو"),
                "clothing.glasses" to Completed("نظارة"),
                "clothing.jacket" to Completed("فيستة"),
                "food.cheese" to Completed("فرماج"),
                "food.pasta" to Completed("مقرون"),
                "food.rice" to Completed("روز"),
                "food.soup" to Completed("شربة"),
                "food.water" to Completed("ماء"),
                "food.yogurt" to Completed("ياغرت"),
                "fruit.figs" to Completed("كرموس"),
                "fruit.pear" to Completed("بوعويدة"),
                "fruit.watermelon" to Completed("دلاع"),
                "home.blanket" to Completed("كوفيرتة"),
                "home.cup" to Completed("كاس"),
                "home.fork" to Completed("فرشيطة"),
                "home.kitchen" to Completed("كوزينة"),
                "home.knife" to Completed("موس"),
                "home.pillow" to Completed("مخدة"),
                "home.spoon" to Completed("مغرف"),
                "home.teapot" to Completed("براد"),
                "home.water_bottle" to Completed("قرعة ماء"),
                "home.window" to Completed("طاقة"),
                "routine.evening_coffee" to Completed("قهوة العشية"),
                "vegetable.cauliflower" to Completed("شيفلور"),
                "vegetable.coriander" to Completed("قزبر"),
                "vegetable.green_beans" to Completed("لوبيا خضرا"),
                "vegetable.parsley" to Completed("معدنوس"),
                "vegetable.potato" to Completed("بطاطا"),
            ),
            ArabicDialect.MOROCCAN to mapOf(
                "action.dress" to Completed("نلبس"),
                "action.drink" to Completed("نشرب"),
                "action.eat" to Completed("ناكل"),
                "action.hear" to Completed("نسمع"),
                "action.laugh" to Completed("نضحك"),
                "action.open" to Completed("نحل"),
                "action.pray" to Completed("نصلي"),
                "action.read" to Completed("نقرا"),
                "action.sleep" to Completed("ننعس"),
                "action.stand" to Completed("نوقف"),
                "action.walk" to Completed("نمشي"),
                "action.wash" to Completed("نغسل"),
                "animal.cat" to Completed("مش"),
                "animal.rabbit" to Completed("قنية"),
                "animal.sheep" to Completed("حولي"),
                "body.ear" to Completed("وذن"),
                "body.finger" to Completed("صبع"),
                "body.head" to Completed("راس"),
                "body.leg" to Completed("رجل"),
                "body.nose" to Completed("نيف"),
                "clothing.bag" to Completed("صاك"),
                "clothing.cap" to Completed("طاقية"),
                "clothing.coat" to Completed("مانتو"),
                "clothing.jacket" to Completed("جاكيطة"),
                "food.cheese" to Completed("فرماج"),
                "food.pasta" to Completed("مقرونية"),
                "food.rice" to Completed("روز"),
                "food.soup" to Completed("حريرة"),
                "food.water" to Completed("ماء"),
                "fruit.figs" to Completed("كرموس"),
                "fruit.mandarin" to Completed("مندرين"),
                "fruit.watermelon" to Completed("دلاح"),
                "home.blanket" to Completed("مانطة"),
                "home.cup" to Completed("كاس"),
                "home.fork" to Completed("فرشيطة"),
                "home.kitchen" to Completed("كوزينة"),
                "home.knife" to Completed("موس"),
                "home.oven" to Completed("فران"),
                "home.pillow" to Completed("مخدة"),
                "home.spoon" to Completed("معلقة"),
                "home.teapot" to Completed("براد"),
                "home.water_bottle" to Completed("قرعة ماء"),
                "home.window" to Completed("شرجم"),
                "routine.evening_coffee" to Completed("قهوة العشية"),
                "vegetable.cauliflower" to Completed("شيفلور"),
                "vegetable.coriander" to Completed("قزبور"),
                "vegetable.eggplant" to Completed("دنجال"),
                "vegetable.green_beans" to Completed("لوبيا خضرا"),
                "vegetable.parsley" to Completed("معدنوس"),
                "vegetable.peas" to Completed("جلبانة"),
                "vegetable.potato" to Completed("بطاطا"),
                "vegetable.pumpkin" to Completed("قرعة حمرا"),
            ),
        )
    }

    private val all: Map<ArabicDialect, Map<String, Patch>> by lazy {
        mapOf(
            ArabicDialect.STANDARD to standard(),
            ArabicDialect.LIBYAN to libyan(),
            ArabicDialect.SAUDI to saudi(),
            ArabicDialect.EGYPTIAN to egyptian(),
            ArabicDialect.SYRIAN to syrian(),
            ArabicDialect.IRAQI to iraqi(),
            ArabicDialect.TUNISIAN to tunisian(),
            ArabicDialect.MOROCCAN to moroccan(),
            ArabicDialect.ALGERIAN to algerian(),
        )
    }

    fun hasPatch(dialect: ArabicDialect, key: String) =
        all[dialect]?.containsKey(key) == true || hasCompletedCandidate(dialect, key)

    fun hasCompletedCandidate(dialect: ArabicDialect, key: String): Boolean =
        completed[dialect]?.containsKey(key) == true

    fun overrideLabel(dialect: ArabicDialect, key: String, text: String): String {
        val choice = all[dialect]?.get(key)?.display
        return if (choice != null && choice.expected == text) choice.proposed else text
    }

    private fun select(current: String, field: Field?, protected: Boolean): String =
        if (!protected && field != null && field.expected == current) field.proposed else current

    /**
     * Textual vocabulary corrections only. All old Microsoft-specific
     * pronunciation and vowel override rules were deleted.
     */
    fun applySpeech(item: WordItem, dialect: ArabicDialect, form: SpeechForm): SpeechForm {
        if (item.categoryId.startsWith("custom_")) return form
        val key = item.semanticKey ?: return form
        val labelCustomized = item.customTextFields and CardDetails.LABEL != 0
        val speechCustomized = item.customTextFields and CardDetails.SPEECH != 0
        val cueCustomized = item.customTextFields and CardDetails.CUE != 0
        val newest = NeuralLexicon161.forms[key]?.get(dialect)
        val patch = all[dialect]?.get(key)
        val approved = completed[dialect]?.get(key)
        val correctedDisplay = when {
            labelCustomized -> form.displayText
            newest != null -> newest.displayText
            approved != null -> approved.display
            else -> select(form.displayText, patch?.display, false)
        }
        // Built-in voice input is normal Arabic orthography, not a second
        // manually vowelled phonetic language.
        val correctedSpeech = if (labelCustomized || speechCustomized)
            form.speechText else correctedDisplay
        val correctedCue = if (labelCustomized || speechCustomized || cueCustomized)
            form.phonemicCue else PhonemicCue.derive(correctedSpeech)
        return form.copy(displayText = correctedDisplay, speechText = correctedSpeech,
            phonemicCue = correctedCue,
            source = if (!labelCustomized && !speechCustomized && correctedDisplay != form.displayText)
                SpeechSource.EXPLICIT_DIALECT else form.source)
    }

    fun applySentence(original: WordItem, dialect: ArabicDialect, projected: WordItem): WordItem {
        if (original.categoryId.startsWith("custom_")) return projected
        if (original.customTextFields and CardDetails.SENTENCE != 0) return projected
        val key = original.semanticKey ?: return projected
        val patch = all[dialect]?.get(key)?.sentence ?: return projected
        val current = projected.sentencePrompt.orEmpty()
        return if (current == patch.expected) projected.copy(sentencePrompt = patch.proposed) else projected
    }
}
