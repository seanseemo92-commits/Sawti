package com.sawti.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomCategoryRulesTest {
    @Test fun builtInsCannotBeEditedOrDeleted() {
        listOf("family", "daily", "fruits", "actions", "cuisine").forEach {
            assertFalse(CustomCategoryRules.canManage(it))
            assertFalse(CustomCategoryRules.canDelete(it, 0))
        }
    }

    @Test fun populatedCustomCategoryCannotBeDeleted() {
        val id = "custom_550e8400-e29b-41d4-a716-446655440000"
        assertTrue(CustomCategoryRules.canManage(id))
        assertFalse(CustomCategoryRules.canDelete(id, 1))
        assertTrue(CustomCategoryRules.canDelete(id, 0))
    }

    @Test fun validatesPaletteAndOrder() {
        assertTrue(CustomCategoryRules.validPalette(11))
        assertFalse(CustomCategoryRules.validPalette(12))
        assertTrue(CustomCategoryRules.validSortOrder(0))
        assertFalse(CustomCategoryRules.validSortOrder(-1))
    }
}
