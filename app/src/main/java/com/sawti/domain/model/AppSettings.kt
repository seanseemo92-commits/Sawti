package com.sawti.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Display vocabulary and regional metadata; the engine uses a separate general-Arabic policy. */
enum class ArabicDialect(val label: String, val localeTag: String) {
    STANDARD("العربية الفصحى", "ar"),
    LIBYAN("الليبية", "ar-LY"),
    SAUDI("السعودية", "ar-SA"),
    EGYPTIAN("المصرية", "ar-EG"),
    SYRIAN("السورية", "ar-SY"),
    IRAQI("العراقية", "ar-IQ"),
    TUNISIAN("التونسية", "ar-TN"),
    MOROCCAN("المغربية", "ar-MA"),
    ALGERIAN("الجزائرية", "ar-DZ");

    /** Null for Fusha: it belongs to no single country. */
    val countryCode: String? get() = localeTag.substringAfter('-', "").ifBlank { null }
}

/** Card text scaling: Normal / Large / Extra Large. */
enum class TextSize(val factor: Float) { NORMAL(1f), LARGE(1.2f), EXTRA_LARGE(1.4f) }

/** TTS speaking speed. */
enum class SpeechRate(val factor: Float) { SLOW(0.75f), NORMAL(0.85f), FAST(1.0f) }

/** How long a card must remain pressed before its tap is accepted. */
enum class TouchDwell(val millis: Long) {
    OFF(0L), NORMAL(300L), LONG(500L)
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSize: TextSize = TextSize.NORMAL,
    val speechRate: SpeechRate = SpeechRate.NORMAL,
    val dialect: ArabicDialect = ArabicDialect.STANDARD,
    val preferredVoice: String? = null,
    val hapticsEnabled: Boolean = true,
    /** "وضع الأداء السلس": no spring/alpha animations or shadows, smaller decoded images. */
    val performanceMode: Boolean = false,
    val touchDwell: TouchDwell = TouchDwell.OFF,
    val sentenceStripEnabled: Boolean = false,
    /** Stored in SharedPreferences; never in Room. Defaults false for first-launch guidance. */
    val isOnboardingCompleted: Boolean = true,
)
