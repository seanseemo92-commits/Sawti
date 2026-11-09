package com.sawti.presentation.quran

/** Quran audio editions supported by the same verse-by-verse CDN. */
enum class QuranReciter(
    val editionId: String,
    val displayName: String,
    val bitrateKbps: Int,
) {
    ALAFASY("ar.alafasy", "مشاري راشد العفاسي", 64),
    SUDAIS("ar.sudais", "عبدالرحمن السديس", 192);

    companion object {
        fun fromEdition(id: String?): QuranReciter =
            entries.firstOrNull { it.editionId == id } ?: ALAFASY
    }
}
