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
        Greeting("greeting.salam", "السلام عليكم", "السلام عليكم", "🤝"),
        Greeting("greeting.reply", "وعليكم السلام", "وعليكم السلام", "🤝"),
        Greeting("greeting.hello", "مرحبا", "مرحبا", "👋"),
        Greeting("greeting.welcome", "أهلا وسهلا", "أهلا وسهلا", "😊"),
        Greeting("greeting.how_are_you", "كيف حالك؟", "كيف حالك؟", "🙂"),
        Greeting("greeting.fine", "الحمد لله", "الحمد لله", "🙏"),
        Greeting("greeting.good_morning", "صباح الخير", "صباح الخير", "🌅"),
        Greeting("greeting.good_evening", "مساء الخير", "مساء الخير", "🌇"),
        Greeting("greeting.brighten", "نورت", "نورت", "✨"),
        Greeting("greeting.good_health", "يعطيك العافية", "يعطيك العافية", "🌷"),
        Greeting("greeting.goodbye", "مع السلامة", "مع السلامة", "👋"),
        Greeting("greeting.safe", "في أمان الله", "في أمان الله", "🌿"),
        Greeting("greeting.thanks", "شكرا لك", "شكرا لك", "❤️"),
        Greeting("greeting.bless", "بارك الله فيك", "بارك الله فيك", "🌼"),
    )

    /**
     * Each list has the same index as greetings above.
     * Do not invent native/TTS approval from compilation; speechText stays editable per card.
     */
    /** Display and phonetic form are deliberately independent for all 112 regional greetings. */
    private val localized: Map<ArabicDialect, List<Pair<String, String>>> = mapOf(
        ArabicDialect.LIBYAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "هلا بيك" to "هلا بيك",
            "أهلا وسهلا" to "أهلا وسهلا",
            "شن حالك؟" to "شن حالك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك الصحة" to "يعطيك الصحة",
            "مع السلامة" to "مع السلامة",
            "في أمان الله" to "في أمان الله",
            "شكرا ليك" to "شكرا ليك",
            "بارك الله فيك" to "بارك الله فيك",
        ),
        ArabicDialect.SAUDI to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "هلا" to "هلا",
            "حياك الله" to "حياك الله",
            "كيف حالك؟" to "كيف حالك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك العافية" to "يعطيك العافية",
            "مع السلامة" to "مع السلامة",
            "في أمان الله" to "في أمان الله",
            "شكرا لك" to "شكرا لك",
            "الله يبارك فيك" to "الله يبارك فيك",
        ),
        ArabicDialect.EGYPTIAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "أهلا" to "أهلا",
            "أهلا وسهلا" to "أهلا وسهلا",
            "أخبارك إيه؟" to "أخبارك إيه؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "تسلم" to "تسلم",
            "مع السلامة" to "مع السلامة",
            "في أمان الله" to "في أمان الله",
            "شكرا ليك" to "شكرا ليك",
            "ربنا يبارك فيك" to "ربنا يبارك فيك",
        ),
        ArabicDialect.SYRIAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "مرحبا" to "مرحبا",
            "أهلين وسهلين" to "أهلين وسهلين",
            "كيفك؟" to "كيفك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك العافية" to "يعطيك العافية",
            "مع السلامة" to "مع السلامة",
            "في أمان الله" to "في أمان الله",
            "يسلمو" to "يسلمو",
            "الله يبارك فيك" to "الله يبارك فيك",
        ),
        ArabicDialect.IRAQI to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "هلا بيك" to "هلا بيك",
            "أهلين وسهلين" to "أهلين وسهلين",
            "شلونك؟" to "شلونك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "عاشت إيدك" to "عاشت إيدك",
            "مع السلامة" to "مع السلامة",
            "في أمان الله" to "في أمان الله",
            "مشكور" to "مشكور",
            "الله يبارك بيك" to "الله يبارك بيك",
        ),
        ArabicDialect.TUNISIAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "عسلامة" to "عسلامة",
            "مرحبا بيك" to "مرحبا بيك",
            "شنوة أحوالك؟" to "شنوة أحوالك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك الصحة" to "يعطيك الصحة",
            "بالسلامة" to "بالسلامة",
            "في أمان الله" to "في أمان الله",
            "شكرا ليك" to "شكرا ليك",
            "ربي يبارك فيك" to "ربي يبارك فيك",
        ),
        ArabicDialect.MOROCCAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "مرحبا" to "مرحبا",
            "مرحبا بيك" to "مرحبا بيك",
            "لاباس عليك؟" to "لاباس عليك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك الصحة" to "يعطيك الصحة",
            "بالسلامة" to "بالسلامة",
            "في أمان الله" to "في أمان الله",
            "شكرا بزاف" to "شكرا بزاف",
            "الله يبارك فيك" to "الله يبارك فيك",
        ),
        ArabicDialect.ALGERIAN to listOf(
            "السلام عليكم" to "السلام عليكم",
            "وعليكم السلام" to "وعليكم السلام",
            "مرحبا" to "مرحبا",
            "أهلا وسهلا" to "أهلا وسهلا",
            "لاباس عليك؟" to "لاباس عليك؟",
            "الحمد لله" to "الحمد لله",
            "صباح الخير" to "صباح الخير",
            "مسا الخير" to "مسا الخير",
            "نورت" to "نورت",
            "يعطيك الصحة" to "يعطيك الصحة",
            "بالسلامة" to "بالسلامة",
            "في أمان الله" to "في أمان الله",
            "صحا" to "صحا",
            "ربي يبارك فيك" to "ربي يبارك فيك",
        ),
    )

    val dialectForms: Map<String, Map<ArabicDialect, DialectForm>> =
        greetings.indices.associate { index ->
            greetings[index].key to localized.mapValues { (_, expressions) ->
                check(expressions.size == greetings.size) { "Incomplete greetings dialect catalog" }
                val (display, _) = expressions[index]
                DialectForm(displayText = display, speechText = display)
            }
        }

    init {
        check(greetings.map { it.key }.distinct().size == greetings.size)
        check(localized.keys == ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }.toSet())
        check(localized.values.all { it.size == greetings.size && it.all { (display, vocalized) -> display.isNotBlank() && vocalized.isNotBlank() } })
    }
}
