package com.mywords.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Display vocabulary and preferred regional TTS locale; availability depends on device voices. */
enum class ArabicDialect(val label: String, val localeTag: String, val sample: String) {
    STANDARD("العربية الفصحى", "ar", "أريد ماءً من فضلك"),
    LIBYAN("الليبية", "ar-LY", "نبي مية لو سمحت"),
    SAUDI("السعودية", "ar-SA", "أبغى موية لو سمحت"),
    EGYPTIAN("المصرية", "ar-EG", "ممكن مية لو سمحت"),
    SYRIAN("السورية", "ar-SY", "بدي مي لو سمحت"),
    IRAQI("العراقية", "ar-IQ", "أريد مي، الله يخليك"),
    TUNISIAN("التونسية", "ar-TN", "نحب شوية ماء، يعيشك"),
    MOROCCAN("المغربية", "ar-MA", "بغيت الما عافاك"),
    ALGERIAN("الجزائرية", "ar-DZ", "نحب نشرب الما، من فضلك"),
}

/** Card text scaling: Normal / Large / Extra Large. */
enum class TextSize(val factor: Float) { NORMAL(1f), LARGE(1.2f), EXTRA_LARGE(1.4f) }

/** TTS speaking speed. */
enum class SpeechRate(val factor: Float) { SLOW(0.75f), NORMAL(0.85f), FAST(1.0f) }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSize: TextSize = TextSize.NORMAL,
    val speechRate: SpeechRate = SpeechRate.NORMAL,
    val dialect: ArabicDialect = ArabicDialect.LIBYAN,
    val preferredVoice: String? = null,
    val hapticsEnabled: Boolean = true,
    /** "وضع الأداء السلس": no spring/alpha animations or shadows, smaller decoded images. */
    val performanceMode: Boolean = false,
)
