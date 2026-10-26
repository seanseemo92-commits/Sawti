package com.mywords.domain.model

/** Family relationship vocabulary and fixed person groups; these are not sample people's names. */
object FamilyRelationships {
    const val RELATIVES = "الأقرباء"
    val tabs: List<String> = listOf(
        RELATIVES, "الزوج", "الزوجة", "الأبناء", "البنات", "الإخوة", "الأخوات",
        "الوالد", "الوالدة", "الأعمام", "العمات", "الأخوال", "الخالات",
        "الأجداد", "الجدات", "أبناء العم", "بنات العم", "أبناء العمة",
        "بنات العمة", "أبناء الخال", "بنات الخال", "أبناء الخالة", "بنات الخالة",
        "الأحفاد", "الحفيدات", "الأقارب الآخرون",
    )
    val peopleGroups: List<String> = tabs.drop(1)

    /** Names and extended relation terms read aloud with one tap in the first tab. */
    val relationshipLabels: List<String> = listOf(
        "أبي", "أمي", "زوجي", "زوجتي", "ابني", "ابنتي", "أخي", "أختي",
        "عمي", "عمتي", "خالي", "خالتي", "جدي", "جدتي", "حفيدي", "حفيدتي",
        "ابن عمي", "بنت عمي", "ابن عمتي", "بنت عمتي",
        "ابن خالي", "بنت خالي", "ابن خالتي", "بنت خالتي",
        "ابن أخي", "بنت أخي", "ابن أختي", "بنت أختي",
        "أبو زوجي", "أم زوجي", "أبو زوجتي", "أم زوجتي",
        "أخو زوجي", "أخت زوجي", "أخو زوجتي", "أخت زوجتي",
        "زوج أختي", "زوجة أخي", "زوج ابنتي", "زوجة ابني",
        "نسيبي", "نسيبتي", "قريبي", "قريبتي",
    )

    fun isBuiltIn(item: WordItem): Boolean =
        item.categoryId == CategoryIds.FAMILY && item.subgroup == RELATIVES &&
            item.displayText in relationshipLabels

    fun validPeopleGroup(group: String): Boolean = group in peopleGroups
}
