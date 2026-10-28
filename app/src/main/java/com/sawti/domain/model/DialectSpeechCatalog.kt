package com.sawti.domain.model

/** Spoken phonetics separate from user-visible labels and persisted semantic image keys.
 * High-risk colloquial terms are manually specified. Remaining words are fully marked
 * by a deterministic approximation, which needs a native-speaker/engine listening pass.
 */
object DialectSpeechCatalog {
    private val curated: Map<ArabicDialect, Map<String, String>> = mapOf(
        ArabicDialect.LIBYAN to mapOf(
            "بيض" to "دَحْيْ",
            "حوت (سمك)" to "حُوتْ",
            "خبزة" to "خُبْزَهْ",
            "مية" to "إِمِّيَهْ",
            "نبي مية" to "نَبِّي إِمِّيَهْ",
            "شاهي" to "شَاهِيْ",
            "مكرونة" to "مَكَرُونَهْ",
            "زبادي" to "زْبَادِيْ",
            "شوربة" to "شُورْبَهْ",
            "عصير" to "عَصِيرْ",
            "كرموس" to "كَرْمُوسْ",
            "إجاص" to "إِجَّاصْ",
            "مندرين" to "مَنْدَرِينْ",
            "بازيليه" to "بَازِيلْيَا",
            "قرعة" to "قَرْعَهْ",
            "قرنبيط" to "قَرْنَبِيطْ",
            "قطوسة" to "قَطُّوسَهْ",
            "سردوك" to "سَرْدُوكْ",
            "قنية" to "قْنِيَّهْ",
            "كلب" to "كَلْبْ",
            "حصان" to "حْصَانْ",
            "شخاشير" to "شْخَاشِيرْ",
            "سبتة" to "سَبْتَهْ",
            "كندرة" to "كُنْدْرَهْ",
            "كندرة كعب" to "كُنْدْرَهْ كَعْبْ",
            "طاقية" to "طَاقِيَّهْ",
            "فم" to "فَمْ",
            "خشم" to "خَشْمْ",
            "ودن" to "وِدْنْ",
            "لسان" to "لْسَانْ",
            "سنة" to "سْنُونْ",
            "صبع" to "صُبُعْ",
            "ركبة" to "رُكْبَهْ",
            "رقبة" to "رَقَبَهْ",
            "نبي شاهي" to "نَبِّي شَاهِيْ",
            "نبي دوايا" to "نَبِّي دَوَايَا",
            "نبي نرتاح" to "نَبِّي نِرْتَاحْ",
            "نبي الحمام" to "نَبِّي الْحَمَّامْ",
            "عدّل لي المخدة" to "عَدِّلْ لِي الْمَخَدَّهْ",
            "راسي يوجع" to "رَاسِي يُوجَعْ",
            "سقعانة" to "سَقْعَانَهْ",
            "نقعمز" to "نْقَعْمَزْ",
            "نوقف" to "نُوقَفْ",
            "ناكل" to "نَاكُلْ",
            "نشرب" to "نِشْرَبْ",
            "نغسل" to "نْغْسِلْ",
            "ندوي" to "نْدْوِيْ",
            "نسكر" to "نْسَكِّرْ",
            "نفتح" to "نِفْتَحْ",
            "نصلي" to "نْصَلِّيْ",
            "نلبس" to "نِلْبَسْ",
            "نشوف" to "نْشُوفْ",
        ),
        ArabicDialect.TUNISIAN to mapOf(
            "بيض" to "عْظَامْ",
            "حوت (سمك)" to "حُوتْ",
            "شاهي" to "تَايْ",
            "كرموس" to "كَرْمُوسْ",
            "إجاص" to "أَنْجَاصْ",
            "بازيليه" to "جِلْبَانَهْ",
            "قرنبيط" to "شَفْلُورْ",
            "قطوسة" to "قَطُّوسَهْ",
            "سردوك" to "سَرْدُوكْ",
            "قنية" to "قْنِيَّهْ",
            "شخاشير" to "كَلْسِيطَهْ",
            "كندرة" to "سْبَادْرِيْ",
            "طاقية" to "كَاسْكِيطَهْ",
            "نبي مية" to "نْحِبْ مَاءْ",
            "نبي شاهي" to "نْحِبْ تَايْ",
            "نبي دوايا" to "حَاجْتِي بْدْوَايَا",
            "نقعمز" to "نُقْعُدْ",
            "ندوي" to "نِحْكِيْ",
            "نشوف" to "نْغْزُرْ",
            "نسكر" to "نْسَكِّرْ",
        ),
        ArabicDialect.ALGERIAN to mapOf(
            "بيض" to "وْلَادْ الْجَاجْ",
            "حوت (سمك)" to "حُوتْ",
            "شاهي" to "تَايْ",
            "قرعة" to "كَابُويَا",
            "طماطم" to "طْمَاطِيشْ",
            "بازيليه" to "جِلْبَانَهْ",
            "مندرين" to "مَنْدَارِينْ",
            "عصفور" to "زَاوْشْ",
            "شخاشير" to "تْقَاشِرْ",
            "كندرة" to "بَسْكِيطْ",
            "طاقية" to "كَاسْكِيطَهْ",
            "نبي مية" to "رَانِي حَابْ مَاءْ",
            "نبي دوايا" to "بَاغِي دْوَايَا",
            "نقعمز" to "نُقْعُدْ",
            "ندوي" to "نْهْدَرْ",
            "نسكر" to "نْبَلَّعْ",
            "نشوف" to "نْشُوفْ",
        ),
        ArabicDialect.MOROCCAN to mapOf(
            "بيض" to "بَيْضْ",
            "حوت (سمك)" to "حُوتْ",
            "شاهي" to "أَتَايْ",
            "طماطم" to "مَطِيشَهْ",
            "إجاص" to "بُوعْوَيْدْ",
            "زبادي" to "دَانُونْ",
            "دجاج" to "فَرُّوجْ",
            "شخاشير" to "تْقَاشِرْ",
            "سبتة" to "سَمْطَهْ",
            "كندرة" to "سْبَرْدِيلَهْ",
            "نظارة" to "نْضَاضْرْ",
            "نبي شاهي" to "بْغِيتْ أَتَايْ",
            "نبي دوايا" to "بْغِيتْ دْوَايَا",
            "نقعمز" to "نْجْلْسْ",
            "ندوي" to "نْهْضَرْ",
            "نسكر" to "نْسَدْ",
            "نشوف" to "نْشُوفْ",
        ),
        ArabicDialect.EGYPTIAN to mapOf(
            "بيض" to "بَيْضْ",
            "حوت (سمك)" to "سَمَكْ",
            "خبزة" to "عِيشْ",
            "شاهي" to "شَايْ",
            "مية" to "مَيَّهْ",
            "طماطم" to "قُوطَهْ",
            "بازيليه" to "بَسِلَّهْ",
            "مندرين" to "يُوسْفَنْدِيْ",
            "إجاص" to "كُمِّتْرَى",
            "قطوسة" to "قِطَّهْ",
            "سردوك" to "دِيكْ",
            "شخاشير" to "شَرَبَاتْ",
            "كندرة" to "كُوتْشِيْ",
            "طاقية" to "طَاقِيَّهْ",
            "نبي مية" to "عَايِزْ مَيَّهْ",
            "نبي دوايا" to "عَايِزْ دَوَا",
            "نقعمز" to "أَقْعُدْ",
            "ندوي" to "أَتْكَلِّمْ",
            "نشوف" to "أَبُصْ",
            "نسكر" to "أَقْفِلْ",
            "نلبس" to "أَلْبِسْ",
        ),
        ArabicDialect.SYRIAN to mapOf(
            "بيض" to "بَيْضْ",
            "حوت (سمك)" to "سَمَكْ",
            "خبزة" to "خُبِزْ",
            "شاهي" to "شَايْ",
            "مية" to "مَيّْ",
            "طماطم" to "بَنْدُورَهْ",
            "بازيليه" to "بَازِيلَّا",
            "مندرين" to "كِلْمَنْتِينَا",
            "إجاص" to "نْجَاصْ",
            "قطوسة" to "بِسَّهْ",
            "شخاشير" to "كِلْسَاتْ",
            "كندرة" to "بَوْطْ",
            "سبتة" to "قَشَاطْ",
            "نبي مية" to "بَدِّي مَيّْ",
            "نبي دوايا" to "بَدِّي دَوَايْ",
            "نقعمز" to "أَقْعُدْ",
            "ندوي" to "أَحْكِيْ",
            "نشوف" to "أَتْطَلَّعْ",
            "نسكر" to "سَكِّرْ",
        ),
        ArabicDialect.IRAQI to mapOf(
            "بيض" to "بَيْضْ",
            "حوت (سمك)" to "سِمَتْشْ",
            "خبزة" to "خُبُزْ",
            "شاهي" to "تْشَايْ",
            "مية" to "مَايْ",
            "طماطم" to "طَمَاطَهْ",
            "بازيليه" to "بْزَالْيَا",
            "مندرين" to "لَالَنْكِيْ",
            "إجاص" to "عَرْمُوطْ",
            "قطوسة" to "بْزُونَهْ",
            "شخاشير" to "جْوَارِيبْ",
            "كندرة" to "قُنْدَرَهْ",
            "طاقية" to "شَفَقَهْ",
            "نظارة" to "مَنَاظِرْ",
            "نبي مية" to "أَرِيدْ مَايْ",
            "نبي دوايا" to "أَرِيدْ دَوَا",
            "نقعمز" to "أَكْعُدْ",
            "ندوي" to "أَحْتْشِيْ",
            "نسكر" to "أَسِدْ",
            "نشوف" to "أَبَاوِعْ",
        ),
        ArabicDialect.SAUDI to mapOf(
            "بيض" to "بَيْضْ",
            "حوت (سمك)" to "سِمَكْ",
            "خبزة" to "خُبْزْ",
            "شاهي" to "شَاهِيْ",
            "مية" to "مَايْ",
            "طماطم" to "طَمَاطْ",
            "قلعاوي" to "سَنْطَرَهْ",
            "قطوسة" to "قَطْوَهْ",
            "شخاشير" to "دِلَاغَاتْ",
            "كندرة" to "جُوتِيْ",
            "طاقية" to "كَبُّوسْ",
            "نبي مية" to "أَبِي مَايْ",
            "نبي دوايا" to "أَبِي دَوَايْ",
            "نقعمز" to "أَقْعُدْ",
            "ندوي" to "أَسُولِفْ",
            "نشوف" to "أَطَالِعْ",
            "نسكر" to "أَصُكْ",
        ),
    )
    private const val SUKUN = 'ْ'
    private val shortVowels = setOf('َ','ُ','ِ')
    private val tanween = mapOf('ً' to 'َ', 'ٌ' to 'ُ', 'ٍ' to 'ِ')
    private val longVowels = setOf('ا','ى','آ')
    private fun isMark(c: Char) = c in 'ً'..'ٟ' || c == 'ٰ'
    private fun isLetter(c: Char) = c in 'ء'..'ي' || c in "چگڤپ"

    fun spoken(original: String, localized: String, originalPrompt: String, dialect: ArabicDialect): String {
        val source = curated[dialect]?.get(original)
            ?: if (localized == original) originalPrompt else localized
        return fullyMark(source)
    }
    private fun clusters(raw: String): List<Pair<Char, String>> {
        val result = mutableListOf<Pair<Char, String>>()
        for (c in raw) {
            if (isMark(c) && result.isNotEmpty()) {
                val pair = result.removeAt(result.lastIndex)
                result += pair.first to (pair.second + c)
            } else if (isLetter(c)) result += c to ""
        }
        return result
    }
    /** Checks all Arabic consonant letters and pause endings; no assertions about natural prosody. */
    fun isFullyMarked(speech: String): Boolean {
        if (speech.isBlank()) return false
        for (part in speech.split(Regex("\\s+"))) {
            if (part.isBlank()) continue
            val units = clusters(part)
            if (units.isEmpty()) return false
            if (units.any { (letter,marks) ->
                    letter !in longVowels && marks.none { it in shortVowels || it == SUKUN }
                }) return false
            val ending = units.last()
            if (ending.first !in longVowels && SUKUN !in ending.second) return false
        }
        return true
    }
    /** Long alif/aleph vowel carriers remain unmarked; final ة is spoken هْ. */
    fun fullyMark(input: String): String {
        val raw = input.substringBefore(" / ").replace(Regex("\\([^)]*\\)"), "")
            .replace("ـ", "").replace("چ","تْش").replace("گ","ج")
            .trim().replace(Regex("\\s+"), " ")
        if (raw.isEmpty()) return ""
        return raw.split(' ').joinToString(" ") { token ->
            buildString {
                val parts = clusters(token)
                parts.forEachIndexed { index, (original, existing) ->
                    val final = index == parts.lastIndex
                    val c = if (final && original == 'ة') 'ه' else original
                    val marks = existing.map { tanween[it] ?: it }.toMutableList()
                    if (final && c !in longVowels) {
                        marks.removeAll { it in shortVowels || it == SUKUN }
                        marks += SUKUN
                    } else if (c !in longVowels && marks.none { it in shortVowels || it == SUKUN }) {
                        marks += 'َ'
                    }
                    append(c)
                    marks.forEach { append(it) }
                }
            }
        }
    }
}
