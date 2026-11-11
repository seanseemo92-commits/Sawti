package com.sawti.presentation.quran

/** Per-ayah reciters. The edition ID is also used for separate private audio storage. */
enum class QuranReciter(
    val editionId: String,
    val displayName: String,
) {
    ALAFASY("ar.alafasy", "مشاري راشد العفاسي"),
    SALAH_BUKHATIR("salah.bukhatir", "صلاح أبو خاطر"),
    HUSARY_MUALLIM("husary.muallim", "محمود خليل الحصري (المعلّم)");

    companion object {
        // Old saved selections of the removed Sudais edition safely fall back to Alafasy.
        fun fromEdition(id: String?): QuranReciter =
            entries.firstOrNull { it.editionId == id } ?: ALAFASY
    }
}
