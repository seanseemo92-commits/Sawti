package com.sawti.domain.model

/**
 * Compact, adult-facing greetings. Stable concept keys are independent of labels and dialects.
 *
 * The canonical form is the Fusha baseline. All eight regional varieties provide explicit
 * display AND speech candidates; correct native pronunciation still requires device listening
 * and native-speaker review, as does the rest of Sawti's dialect catalog.
 */
object GreetingsCatalog {
    const val CATEGORY_ID = "greetings"

    data class Greeting(
        val key: String,
        val standardText: String,
        val standardSpeech: String,
        val emoji: String,
    )

    val greetings = listOf(
        Greeting("greeting.salam", "السلام عليكم", "السَّلَامُ عَلَيْكُمْ", "🤝"),
        Greeting("greeting.reply", "وعليكم السلام", "وَعَلَيْكُمُ السَّلَامُ", "🤝"),
        Greeting("greeting.hello", "مرحبًا", "مَرْحَبًا", "👋"),
        Greeting("greeting.welcome", "أهلًا وسهلًا", "أَهْلًا وَسَهْلًا", "😊"),
        Greeting("greeting.how_are_you", "كيف حالك؟", "كَيْفَ حَالُكَ؟", "🙂"),
        Greeting("greeting.fine", "الحمد لله", "الْحَمْدُ لِلَّهِ", "🙏"),
        Greeting("greeting.good_morning", "صباح الخير", "صَبَاحُ الْخَيْرِ", "🌅"),
        Greeting("greeting.good_evening", "مساء الخير", "مَسَاءُ الْخَيْرِ", "🌇"),
        Greeting("greeting.brighten", "نورت", "نَوَّرْتَ", "✨"),
        Greeting("greeting.good_health", "يعطيك العافية", "يُعْطِيكَ الْعَافِيَةَ", "🌷"),
        Greeting("greeting.goodbye", "مع السلامة", "مَعَ السَّلَامَةِ", "👋"),
        Greeting("greeting.safe", "في أمان الله", "فِي أَمَانِ اللَّهِ", "🌿"),
        Greeting("greeting.thanks", "شكرًا لك", "شُكْرًا لَكَ", "❤️"),
        Greeting("greeting.bless", "بارك الله فيك", "بَارَكَ اللَّهُ فِيكَ", "🌼"),
    )

    /**
     * Each list has the same index as greetings above.
     * Do not invent native/TTS approval from compilation; speechText stays editable per card.
     */
    private val localized: Map<ArabicDialect, List<String>> = mapOf(
        ArabicDialect.LIBYAN to listOf(
            "السلام عليكم", "وعليكم السلام", "هلا بيك", "أهلا وسهلا", "شن حالك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك الصحة",
            "مع السلامة", "في أمان الله", "شكراً ليك", "بارك الله فيك",
        ),
        ArabicDialect.SAUDI to listOf(
            "السلام عليكم", "وعليكم السلام", "هلا", "حيّاك الله", "كيف حالك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك العافية",
            "مع السلامة", "في أمان الله", "شكراً لك", "الله يبارك فيك",
        ),
        ArabicDialect.EGYPTIAN to listOf(
            "السلام عليكم", "وعليكم السلام", "أهلاً", "أهلاً وسهلاً", "أخبارك إيه؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "تسلم",
            "مع السلامة", "في أمان الله", "شكراً ليك", "ربنا يبارك فيك",
        ),
        ArabicDialect.SYRIAN to listOf(
            "السلام عليكم", "وعليكم السلام", "مرحبا", "أهلين وسهلين", "كيفك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك العافية",
            "مع السلامة", "في أمان الله", "يسلمو", "الله يبارك فيك",
        ),
        ArabicDialect.IRAQI to listOf(
            "السلام عليكم", "وعليكم السلام", "هلا بيك", "أهلين وسهلين", "شلونك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "عاشت إيدك",
            "مع السلامة", "في أمان الله", "مشكور", "الله يبارك بيك",
        ),
        ArabicDialect.TUNISIAN to listOf(
            "السلام عليكم", "وعليكم السلام", "عسلامة", "مرحبا بيك", "شنوة أحوالك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك الصحة",
            "بالسلامة", "في أمان الله", "شكراً ليك", "ربي يبارك فيك",
        ),
        ArabicDialect.MOROCCAN to listOf(
            "السلام عليكم", "وعليكم السلام", "مرحبا", "مرحبا بيك", "لاباس عليك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك الصحة",
            "بالسلامة", "في أمان الله", "شكراً بزاف", "الله يبارك فيك",
        ),
        ArabicDialect.ALGERIAN to listOf(
            "السلام عليكم", "وعليكم السلام", "مرحبا", "أهلا وسهلا", "لاباس عليك؟",
            "الحمد لله", "صباح الخير", "مسا الخير", "نورت", "يعطيك الصحة",
            "بالسلامة", "في أمان الله", "صحا", "ربي يبارك فيك",
        ),
    )

    val dialectForms: Map<String, Map<ArabicDialect, DialectForm>> =
        greetings.indices.associate { index ->
            greetings[index].key to localized.mapValues { (_, expressions) ->
                check(expressions.size == greetings.size) { "Incomplete greetings dialect catalog" }
                val text = expressions[index]
                // Explicit regional pronunciation candidate: no accidental Fusha substitution.
                DialectForm(displayText = text, speechText = text)
            }
        }

    init {
        check(greetings.map { it.key }.distinct().size == greetings.size)
        check(localized.keys == ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }.toSet())
        check(localized.values.all { it.size == greetings.size && it.all(String::isNotBlank) })
    }
}
