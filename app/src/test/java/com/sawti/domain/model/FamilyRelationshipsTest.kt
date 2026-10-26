package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyRelationshipsTest {
    @Test fun relativesAreFirstAndGroupsStaySelectableWhenEmpty() {
        assertEquals(FamilyRelationships.RELATIVES, FamilyRelationships.tabs.first())
        assertTrue(FamilyRelationships.tabs.containsAll(listOf("الزوج", "الأبناء", "الإخوة", "الأخوات", "الوالدة")))
        assertEquals(FamilyRelationships.tabs.size, FamilyRelationships.tabs.distinct().size)
        assertFalse(FamilyRelationships.validPeopleGroup(FamilyRelationships.RELATIVES))
        assertTrue(FamilyRelationships.validPeopleGroup("الأبناء"))
    }

    @Test fun relationshipWordsAreBuiltInWithoutPersonalNames() {
        val cat = WordSeed.categories.single { it.id == CategoryIds.FAMILY }
        assertTrue(cat.previewOnTap)
        assertEquals(FamilyRelationships.relationshipLabels, cat.words.map { it.display })
        assertTrue(cat.words.all { it.subgroup == FamilyRelationships.RELATIVES })
        assertTrue(FamilyRelationships.relationshipLabels.containsAll(
            listOf("أبي", "أمي", "زوجي", "زوجتي", "ابني", "ابنتي", "أخي", "أختي",
                   "عمي", "عمتي", "خالي", "خالتي", "ابن خالي", "بنت خالتي")
        ))
        assertEquals(FamilyRelationships.relationshipLabels.size,
            FamilyRelationships.relationshipLabels.distinct().size)

        assertTrue(cat.words.all { it.subgroup == FamilyRelationships.RELATIVES })
    }
}
