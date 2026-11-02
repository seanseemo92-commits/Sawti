package com.sawti.domain.model

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
    val relationshipConcepts: Map<String, String> = linkedMapOf(
        "family.father" to "أبي",
        "family.mother" to "أمي",
        "family.husband" to "زوجي",
        "family.wife" to "زوجتي",
        "family.son" to "ابني",
        "family.daughter" to "ابنتي",
        "family.brother" to "أخي",
        "family.sister" to "أختي",
        "family.paternal_uncle" to "عمي",
        "family.paternal_aunt" to "عمتي",
        "family.maternal_uncle" to "خالي",
        "family.maternal_aunt" to "خالتي",
        "family.grandfather" to "جدي",
        "family.grandmother" to "جدتي",
        "family.grandson" to "حفيدي",
        "family.granddaughter" to "حفيدتي",
        "family.paternal_uncle_son" to "ابن عمي",
        "family.paternal_uncle_daughter" to "بنت عمي",
        "family.paternal_aunt_son" to "ابن عمتي",
        "family.paternal_aunt_daughter" to "بنت عمتي",
        "family.maternal_uncle_son" to "ابن خالي",
        "family.maternal_uncle_daughter" to "بنت خالي",
        "family.maternal_aunt_son" to "ابن خالتي",
        "family.maternal_aunt_daughter" to "بنت خالتي",
        "family.brother_son" to "ابن أخي",
        "family.brother_daughter" to "بنت أخي",
        "family.sister_son" to "ابن أختي",
        "family.sister_daughter" to "بنت أختي",
        "family.husband_father" to "أبو زوجي",
        "family.husband_mother" to "أم زوجي",
        "family.wife_father" to "أبو زوجتي",
        "family.wife_mother" to "أم زوجتي",
        "family.husband_brother" to "أخو زوجي",
        "family.husband_sister" to "أخت زوجي",
        "family.wife_brother" to "أخو زوجتي",
        "family.wife_sister" to "أخت زوجتي",
        "family.sister_husband" to "زوج أختي",
        "family.brother_wife" to "زوجة أخي",
        "family.daughter_husband" to "زوج ابنتي",
        "family.son_wife" to "زوجة ابني",
        "family.male_in_law" to "نسيبي",
        "family.female_in_law" to "نسيبتي",
        "family.male_relative" to "قريبي",
        "family.female_relative" to "قريبتي",
    )
    val relationshipLabels: List<String> = relationshipConcepts.values.toList()

    fun isBuiltIn(item: WordItem): Boolean =
        item.categoryId == CategoryIds.FAMILY && item.subgroup == RELATIVES &&
            item.semanticKey in relationshipConcepts

    fun validPeopleGroup(group: String): Boolean = group in peopleGroups
}
