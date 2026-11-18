package com.sawti.domain.model

/** Stable transport concepts with explicit written speech candidates in all nine variants.
 * Native-speaker review and device listening remain independent, pending review gates.
 * Images and saved rows use semanticKey, never the translated caption.
 */
object TransportCatalog {
    data class Vehicle(val semanticKey: String, val emoji: String, val forms: Map<ArabicDialect, DialectForm>)

    val vehicles: List<Vehicle> = listOf(
        Vehicle("transport.car", "🚗", mapOf(
            ArabicDialect.STANDARD to DialectForm("سيارة", "سيارة"),
            ArabicDialect.LIBYAN to DialectForm("سيارة", "سيارة"),
            ArabicDialect.SAUDI to DialectForm("سيارة", "سيارة"),
            ArabicDialect.EGYPTIAN to DialectForm("عربية", "عربية"),
            ArabicDialect.SYRIAN to DialectForm("سيارة", "سيارة"),
            ArabicDialect.IRAQI to DialectForm("سيارة", "سيارة"),
            ArabicDialect.TUNISIAN to DialectForm("كرهبة", "كرهبة"),
            ArabicDialect.MOROCCAN to DialectForm("طوموبيل", "طوموبيل"),
            ArabicDialect.ALGERIAN to DialectForm("طوموبيل", "طوموبيل"),
        )),
        Vehicle("transport.taxi", "🚕", mapOf(
            ArabicDialect.STANDARD to DialectForm("سيارة أجرة", "سيارة أجرة"),
            ArabicDialect.LIBYAN to DialectForm("تاكسي", "تاكسي"),
            ArabicDialect.SAUDI to DialectForm("تاكسي", "تاكسي"),
            ArabicDialect.EGYPTIAN to DialectForm("تاكسي", "تاكسي"),
            ArabicDialect.SYRIAN to DialectForm("تكسي", "تكسي"),
            ArabicDialect.IRAQI to DialectForm("تاكسي", "تاكسي"),
            ArabicDialect.TUNISIAN to DialectForm("تاكسي", "تاكسي"),
            ArabicDialect.MOROCCAN to DialectForm("طاكسي", "طاكسي"),
            ArabicDialect.ALGERIAN to DialectForm("طاكسي", "طاكسي"),
        )),
        Vehicle("transport.bus", "🚌", mapOf(
            ArabicDialect.STANDARD to DialectForm("حافلة", "حافلة"),
            ArabicDialect.LIBYAN to DialectForm("باص", "باص"),
            ArabicDialect.SAUDI to DialectForm("باص", "باص"),
            ArabicDialect.EGYPTIAN to DialectForm("أتوبيس", "أتوبيس"),
            ArabicDialect.SYRIAN to DialectForm("باص", "باص"),
            ArabicDialect.IRAQI to DialectForm("باص", "باص"),
            ArabicDialect.TUNISIAN to DialectForm("كار", "كار"),
            ArabicDialect.MOROCCAN to DialectForm("طوبيس", "طوبيس"),
            ArabicDialect.ALGERIAN to DialectForm("طوبيس", "طوبيس"),
        )),
        Vehicle("transport.minibus", "🚐", mapOf(
            ArabicDialect.STANDARD to DialectForm("حافلة صغيرة", "حافلة صغيرة"),
            ArabicDialect.LIBYAN to DialectForm("ميكروباص", "ميكروباص"),
            ArabicDialect.SAUDI to DialectForm("باص صغير", "باص صغير"),
            ArabicDialect.EGYPTIAN to DialectForm("ميكروباص", "ميكروباص"),
            ArabicDialect.SYRIAN to DialectForm("سرفيس", "سرفيس"),
            ArabicDialect.IRAQI to DialectForm("باص صغير", "باص صغير"),
            ArabicDialect.TUNISIAN to DialectForm("لواج", "لواج"),
            ArabicDialect.MOROCCAN to DialectForm("طوبيس صغير", "طوبيس صغير"),
            ArabicDialect.ALGERIAN to DialectForm("حافلة صغيرة", "حافلة صغيرة"),
        )),
        Vehicle("transport.bicycle", "🚲", mapOf(
            ArabicDialect.STANDARD to DialectForm("دراجة هوائية", "دراجة هوائية"),
            ArabicDialect.LIBYAN to DialectForm("دراجة", "دراجة"),
            ArabicDialect.SAUDI to DialectForm("سيكل", "سيكل"),
            ArabicDialect.EGYPTIAN to DialectForm("عجلة", "عجلة"),
            ArabicDialect.SYRIAN to DialectForm("بسكليت", "بسكليت"),
            ArabicDialect.IRAQI to DialectForm("بايسكل", "بايسكل"),
            ArabicDialect.TUNISIAN to DialectForm("بسكلات", "بسكلات"),
            ArabicDialect.MOROCCAN to DialectForm("بيكالة", "بيكالة"),
            ArabicDialect.ALGERIAN to DialectForm("فيلو", "فيلو"),
        )),
        Vehicle("transport.motorcycle", "🏍️", mapOf(
            ArabicDialect.STANDARD to DialectForm("دراجة نارية", "دراجة نارية"),
            ArabicDialect.LIBYAN to DialectForm("دراجة نارية", "دراجة نارية"),
            ArabicDialect.SAUDI to DialectForm("دباب", "دباب"),
            ArabicDialect.EGYPTIAN to DialectForm("موتوسيكل", "موتوسيكل"),
            ArabicDialect.SYRIAN to DialectForm("موتور", "موتور"),
            ArabicDialect.IRAQI to DialectForm("دراجة نارية", "دراجة نارية"),
            ArabicDialect.TUNISIAN to DialectForm("موتور", "موتور"),
            ArabicDialect.MOROCCAN to DialectForm("موطور", "موطور"),
            ArabicDialect.ALGERIAN to DialectForm("موتو", "موتو"),
        )),
        Vehicle("transport.train", "🚆", mapOf(
            ArabicDialect.STANDARD to DialectForm("قطار", "قطار"),
            ArabicDialect.LIBYAN to DialectForm("قطار", "قطار"),
            ArabicDialect.SAUDI to DialectForm("قطار", "قطار"),
            ArabicDialect.EGYPTIAN to DialectForm("قطر", "قطر"),
            ArabicDialect.SYRIAN to DialectForm("قطار", "قطار"),
            ArabicDialect.IRAQI to DialectForm("قطار", "قطار"),
            ArabicDialect.TUNISIAN to DialectForm("تران", "تران"),
            ArabicDialect.MOROCCAN to DialectForm("تران", "تران"),
            ArabicDialect.ALGERIAN to DialectForm("تران", "تران"),
        )),
        Vehicle("transport.airplane", "✈️", mapOf(
            ArabicDialect.STANDARD to DialectForm("طائرة", "طائرة"),
            ArabicDialect.LIBYAN to DialectForm("طيارة", "طيارة"),
            ArabicDialect.SAUDI to DialectForm("طيارة", "طيارة"),
            ArabicDialect.EGYPTIAN to DialectForm("طيارة", "طيارة"),
            ArabicDialect.SYRIAN to DialectForm("طيارة", "طيارة"),
            ArabicDialect.IRAQI to DialectForm("طيارة", "طيارة"),
            ArabicDialect.TUNISIAN to DialectForm("طيارة", "طيارة"),
            ArabicDialect.MOROCCAN to DialectForm("طيارة", "طيارة"),
            ArabicDialect.ALGERIAN to DialectForm("طيارة", "طيارة"),
        )),
        Vehicle("transport.helicopter", "🚁", mapOf(
            ArabicDialect.STANDARD to DialectForm("مروحية", "مروحية"),
            ArabicDialect.LIBYAN to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.SAUDI to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.EGYPTIAN to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.SYRIAN to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.IRAQI to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.TUNISIAN to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.MOROCCAN to DialectForm("هليكوبتر", "هليكوبتر"),
            ArabicDialect.ALGERIAN to DialectForm("هليكوبتر", "هليكوبتر"),
        )),
        Vehicle("transport.ship", "🚢", mapOf(
            ArabicDialect.STANDARD to DialectForm("سفينة", "سفينة"),
            ArabicDialect.LIBYAN to DialectForm("باخرة", "باخرة"),
            ArabicDialect.SAUDI to DialectForm("سفينة", "سفينة"),
            ArabicDialect.EGYPTIAN to DialectForm("مركب كبير", "مركب كبير"),
            ArabicDialect.SYRIAN to DialectForm("باخرة", "باخرة"),
            ArabicDialect.IRAQI to DialectForm("باخرة", "باخرة"),
            ArabicDialect.TUNISIAN to DialectForm("باطو", "باطو"),
            ArabicDialect.MOROCCAN to DialectForm("باخرة", "باخرة"),
            ArabicDialect.ALGERIAN to DialectForm("باخرة", "باخرة"),
        )),
        Vehicle("transport.boat", "🛶", mapOf(
            ArabicDialect.STANDARD to DialectForm("قارب", "قارب"),
            ArabicDialect.LIBYAN to DialectForm("قارب", "قارب"),
            ArabicDialect.SAUDI to DialectForm("قارب", "قارب"),
            ArabicDialect.EGYPTIAN to DialectForm("مركب", "مركب"),
            ArabicDialect.SYRIAN to DialectForm("قارب", "قارب"),
            ArabicDialect.IRAQI to DialectForm("زورق", "زورق"),
            ArabicDialect.TUNISIAN to DialectForm("فلوكة", "فلوكة"),
            ArabicDialect.MOROCCAN to DialectForm("فلوكة", "فلوكة"),
            ArabicDialect.ALGERIAN to DialectForm("قارب", "قارب"),
        )),
        Vehicle("transport.truck", "🚚", mapOf(
            ArabicDialect.STANDARD to DialectForm("شاحنة", "شاحنة"),
            ArabicDialect.LIBYAN to DialectForm("شاحنة", "شاحنة"),
            ArabicDialect.SAUDI to DialectForm("شاحنة", "شاحنة"),
            ArabicDialect.EGYPTIAN to DialectForm("عربية نقل", "عربية نقل"),
            ArabicDialect.SYRIAN to DialectForm("شاحنة", "شاحنة"),
            ArabicDialect.IRAQI to DialectForm("لوري", "لوري"),
            ArabicDialect.TUNISIAN to DialectForm("كاميون", "كاميون"),
            ArabicDialect.MOROCCAN to DialectForm("كاميو", "كاميو"),
            ArabicDialect.ALGERIAN to DialectForm("كاميون", "كاميون"),
        )),
    )

    internal val dialectForms: Map<String, Map<ArabicDialect, DialectForm>> = vehicles.associate {
        it.semanticKey to it.forms.filterKeys { dialect -> dialect != ArabicDialect.STANDARD }
    }

    fun sentencePrompt(semanticKey: String, dialect: ArabicDialect): String? {
        if (vehicles.none { it.semanticKey == semanticKey }) return null
        return when (dialect) {
            ArabicDialect.STANDARD -> "أستخدم ال..."
            ArabicDialect.LIBYAN, ArabicDialect.TUNISIAN -> "نركب في ال..."
            ArabicDialect.SAUDI, ArabicDialect.IRAQI -> "أركب ال..."
            ArabicDialect.EGYPTIAN -> "بركب ال..."
            ArabicDialect.SYRIAN -> "بركب ال..."
            ArabicDialect.MOROCCAN -> "كنركب في ال..."
            ArabicDialect.ALGERIAN -> "نركب في ال..."
        }
    }
}
